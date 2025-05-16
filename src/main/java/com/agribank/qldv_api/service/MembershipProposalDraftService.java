package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.gateway.MembershipProposalDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.membershipProposalDraft.MPSearchDraftRequest;
import com.agribank.qldv_api.request.membershipProposalDraft.MembershipProposalRequest;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.membershipProposal.MembershipProposalDtoResponse;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MembershipProposalDraftService {
    private final UserService userService;
    private final MembershipProposalDraftClient client;
    private final MembershipProposalService membershipProposalService;
    private final DVService dvService;
    private final ModelMapper modelMapper;
    private final CheckAuthorityService checkAuthorityService;

    public String create(MembershipProposalRequest request){
        DV dv = dvService.findById(request.getStaffCode());

        if (Objects.isNull(dv)){
            throw new CommonException("Không tồn tại Đảng viên có mã Nhân viên: " + request.getStaffCode() + " Vui lòng kiểm tra lại.");
        }
        checkAuthorityService.hasAuthorityOverOrganization(dv.getOrganizationCode());

        MembershipProposalDraft membershipProposalDraft = modelMapper.map(request, MembershipProposalDraft.class);
        membershipProposalDraft.setId(UUID.randomUUID().toString());
        membershipProposalDraft.setOrganizationCode(dv.getOrganizationCode());
        membershipProposalDraft.setStatus(EApprovalStatus.PENDING.getId());

        UserDetailsImpl userRequested = userService.getUserRequested();
        membershipProposalDraft.setUsernameCreated(userRequested.getUsername());
        membershipProposalDraft.setUserBrcdCreated(userRequested.getBrcd());
        client.save(membershipProposalDraft);

        return "Tạo yêu cầu thành công";
    }

    public PageResponse<MembershipProposalDtoResponse> search(MPSearchDraftRequest request){
        return client.search(request).getData();
    }

    public String delete(String id){
        MembershipProposalDraft membershipProposalDraft = client.findById(id).getData();
        if (Objects.isNull(membershipProposalDraft)){
            throw new CommonException("Không xóa được yêu cầu! Vui lòng kiểm tra lại sau");
        }

        if (EApprovalStatus.PENDING.getId() != membershipProposalDraft.getStatus()){
            throw new CommonException("Yêu cầu đã được duyệt, nên bạn không thể xóa yêu cầu này!");
        }

        client.delete(id);
        return "Xóa yêu cầu thành công!";
    }

    public List<DraftResponse> approve(List<DraftRequest> requests){
        List<String> ids = requests.stream().map(DraftRequest::getId).distinct().toList();

        List<MembershipProposalDraft> dissolveDraftList = client.findAllById(ids).getData();
        Map<String, MembershipProposalDraft> dissolveDraftMap = new HashMap<>();
        if (Objects.isNull(dissolveDraftList) || dissolveDraftList.isEmpty()) {
            throw new CommonException("Kiểm tra lại id request");
        }

        for (MembershipProposalDraft d : dissolveDraftList) {
            dissolveDraftMap.put(d.getId(), d);
        }

        List<DraftResponse> responses = new ArrayList<>();
        List<MembershipProposal> membershipProposalSaves = new ArrayList<>();
        List<MembershipProposalDraft> proposalDrafts = new ArrayList<>();

        UserDetailsImpl userRequested = userService.getUserRequested();
        for (DraftRequest draftRequest : requests) {
            //kiểm tra giá trị approve
            if (Objects.isNull(draftRequest.getStatus())
                    || EApprovalStatus.getValue(draftRequest.getStatus()) == -1
                    || EApprovalStatus.PENDING.getId() == draftRequest.getStatus()
            ){
                responses.add(fromModel(draftRequest.getId(),
                        draftRequest.getStatus(),
                        "Sai status vui lòng kiểm tra lại!"));
                continue;
            }

            MembershipProposalDraft  membershipProposalDraft = dissolveDraftMap.getOrDefault(draftRequest.getId(), null);

            if (Objects.isNull(membershipProposalDraft)){
                responses.add(fromModel(draftRequest.getId(),
                        draftRequest.getStatus(),
                        "Không tồn tại bản ghi id: "
                                + draftRequest.getId()));
                continue;
            }


            //chặn duyệt lại những approve đã tùng được thao tác duyệt hoặc từ chối rồi
            if (!membershipProposalDraft.getStatus().equals(EApprovalStatus.PENDING.getId())){
                responses.add(
                        fromModel(membershipProposalDraft.getId(),
                                draftRequest.getStatus(),
                                "Bản ghi id: "
                                        + membershipProposalDraft.getId()
                                        + ", " + membershipProposalDraft.getStaffCode()
                                        + ", đã được duyệt"));
                continue;
            }

            membershipProposalDraft.setUsernameAccepted(userRequested.getUsername());
            membershipProposalDraft.setUserBrcdAccepted(userRequested.getBrcd());
            //trường hợp đông ý
            if (draftRequest.getStatus().equals(EApprovalStatus.APPROVED.getId())) {
                membershipProposalDraft.setStatus(EApprovalStatus.APPROVED.getId());
                proposalDrafts.add(membershipProposalDraft);
                responses.add(fromModel(membershipProposalDraft.getId(),
                        draftRequest.getStatus(),
                        "Yêu cầu đã được duyệt thành công!"));

                membershipProposalSaves.add(modelMapper.map(membershipProposalDraft, MembershipProposal.class));
                continue;
            }

            //TH từ chối
            if (draftRequest.getStatus().equals(EApprovalStatus.DENIED.getId())) {
                membershipProposalDraft.setStatus(EApprovalStatus.DENIED.getId());
                proposalDrafts.add(membershipProposalDraft);
                responses.add(fromModel(membershipProposalDraft.getId(),
                        draftRequest.getStatus(),
                        "Yêu cầu từ chối đã được duyệt thành công!"));
            }
        }

        if (proposalDrafts.isEmpty()){
            return responses;
        }

        membershipProposalService.saveAll(membershipProposalSaves);
        client.saveAll(proposalDrafts);

        return responses;
    }

    private DraftResponse fromModel(String id, Integer approve, String mess) {
        return DraftResponse.builder()
                .id(id)
                .approve(approve+"")
                .message(mess)
                .build();
    }
}
