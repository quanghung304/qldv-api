package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EOrganizationReference;
import com.agribank.qldv_api.enums.EStatus;
import com.agribank.qldv_api.gateway.OrganizationDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.organization.OrganizationCreateRequest;
import com.agribank.qldv_api.request.organization.OrganizationRequest;
import com.agribank.qldv_api.request.DraftRequest;
import com.agribank.qldv_api.request.organizationDraft.OrganizationDraftSearchRequest;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.response.DraftResponse;
import com.agribank.qldv_api.response.organizationDraft.OrganizationDraftResponse;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.OrganizationDraft;
import com.agribank.qldvutils.entity.OrganizationReference;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;

@Service
@RequiredArgsConstructor
public class OrganizationDraftService {
    private final ModelMapper modelMapper;
    private final OrganizationService organizationService;
    private final OrganizationReferenceService organizationReferenceService;
    private final OrganizationDraftClient client;
    private final UserService userService;
    private final CheckAuthorityService checkAuthorityService;

    //mã đang bộ sẽ được nhập khi nó là Đảng bộ cơ sở
    //Nhóm B mã Chi Đảng bộ sẽ lấy là mã CN
    //Nhóm C sẽ lấy cấu trúc từ mã Đảng của nhóm B + giá trị 01-99(aaaabb: bb = 01-99)
    //nhóm D cũng tương tự lấy mã từ nhóm C + giá trị 01-99(aaaabbcc: cc = 01-99)
    //Cách truyềm tham số: Với nhóm A cần truyền cả code parentCode null
    //Với nhóm B thì yêu cầu truyền cả
    public OrganizationResponse create(OrganizationCreateRequest organizationRequest) {
        OrganizationReference organizationReference = organizationReferenceService.findById(organizationRequest.getForm());
        if (Objects.isNull(organizationReference)) {
            throw new CommonException("Kiểm tra lại thông tin hình thức Đảng.");
        }

        if (!EOrganizationReference.GROUP_A.getCode().equals(organizationRequest.getForm())
                && !EOrganizationReference.GROUP_B.getCode().contains(organizationRequest.getForm())) {
            checkDBBPAndCBTTDBBP(organizationRequest);
            return save(organizationRequest);
        }

        if (Objects.isNull(organizationRequest.getCode())){
            throw new CommonException("Chưa nhập mã TCD");
        }

        Organization organization = organizationService.findByCode(organizationRequest.getCode());
        if (Objects.nonNull(organizationRequest.getCode()) && Objects.nonNull(organization)) {
            throw new CommonException("Mã TCD đẫ tồn tại vui lòng kiểm tra lại");
        }

        return save(organizationRequest);
    }

    private OrganizationResponse save(OrganizationCreateRequest organizationRequest) {
        UserDetailsImpl userRequested = getUserRequested();
        OrganizationDraft organizationDraft = modelMapper.map(organizationRequest, OrganizationDraft.class);
        organizationDraft.setApprove(EApprovalStatus.PENDING.getId());
        organizationDraft.setId(UUID.randomUUID().toString());
        organizationDraft.setStatus(EStatus.YES.getStatus());
        organizationDraft.setOrganizationCode(userRequested.getOrganizationCode());
        organizationDraft.setUserBrcdCreated(userRequested.getBrcd());
        organizationDraft.setUsernameCreated(userRequested.getUsername());
        client.save(organizationDraft);
        return modelMapper.map(organizationDraft, OrganizationResponse.class);
    }

    //Thuộc nhóm khác thì tìm chi nhánh cha của chi nhánh con rồi tăng giá trị lên 1
    private void checkDBBPAndCBTTDBBP(OrganizationCreateRequest organizationRequest){
        Organization organization = organizationService.findByCode(organizationRequest.getParentCode());
        if (Objects.isNull(organization)) {
            throw new CommonException("Không tồn tại Tổ chức đảng có mã code:" + organizationRequest.getParentCode());
        }
        //Tìm tcd để thực hiện việc tăng mã tcd
        Organization organizationDb = organizationService.getByParentCodeMax(organizationRequest.getParentCode());

        //set giá trị mã tcd nếu nó chưa có thằng con thì + 01
        if (Objects.isNull(organizationDb)){
            organizationRequest.setCode(organizationRequest.getParentCode()+"01");
            return;
        }

        Integer code = Integer.parseInt(organizationDb.getCode()) + 1;
        organizationRequest.setCode(code+"");
    }

    public OrganizationResponse update(OrganizationRequest organizationRequest){
        Organization organization = organizationService.findByCode(organizationRequest.getCode());
        if (Objects.isNull(organization)) {
            throw new CommonException("Kiểm tra lại Mã tổ chức Đảng");
        }

        if (Objects.isNull(organizationRequest.getStatus())) {
            throw new CommonException("Kiểm tra lại giá trị trạng thái hoạt động");
        }
        UserDetailsImpl userRequested = getUserRequested();

        OrganizationDraft organizationDraft = modelMapper.map(organization, OrganizationDraft.class);
        organizationDraft.setAuthorized(organizationRequest.getAuthorized());
        organizationDraft.setName(organizationRequest.getName());
        organizationDraft.setResolutionNumber(organizationRequest.getResolutionNumber());
        organizationDraft.setResolutionDate(organizationRequest.getResolutionDate());
        organizationDraft.setEstablishmentDecisionNumber(organizationRequest.getEstablishmentDecisionNumber());
        organizationDraft.setDecisionDate(organizationRequest.getDecisionDate());
        organizationDraft.setEffectiveDate(organizationRequest.getEffectiveDate());
        organizationDraft.setStatus(organizationRequest.getStatus());
        organizationDraft.setId(UUID.randomUUID().toString());
        organizationDraft.setApprove(EApprovalStatus.PENDING.getId());
        organizationDraft.setOrganizationCode(userRequested.getOrganizationCode());
        organizationDraft.setUserBrcdCreated(userRequested.getBrcd());
        organizationDraft.setUsernameCreated(userRequested.getUsername());

        client.save(organizationDraft);
        return modelMapper.map(organizationDraft, OrganizationResponse.class);
    }

    public List<DraftResponse> approve(List<DraftRequest> requests) {
        List<String> ids = requests.stream().map(DraftRequest::getId).distinct().toList();

        //Lấy những Bản ghi cần được duyệt
        List<OrganizationDraft> organizationDrafts = client.findAllById(ids).getData();
        Map<String, OrganizationDraft> organizationDraftRequestMap = new HashMap<>();

        for (OrganizationDraft organizationDraft : organizationDrafts) {
            organizationDraftRequestMap.put(organizationDraft.getId(), organizationDraft);
        }

        List<DraftResponse> responses = new ArrayList<>();
        List<OrganizationDraft> organizationDraftsSave = new ArrayList<>();
        List<Organization> organizationSaves = new ArrayList<>();

        UserDetailsImpl userRequested = getUserRequested();

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

            OrganizationDraft organizationDraftSave = organizationDraftRequestMap.getOrDefault(draftRequest.getId(), null);

            if (Objects.isNull(organizationDraftSave)){
                responses.add(fromModel(draftRequest.getId(),
                        draftRequest.getStatus(),
                        "Không tồn tại bản ghi id: "
                                + draftRequest.getId()));
                continue;
            }

            //nguoi tao va người duyệt phải khác nhau
//            if (userRequested.getUsername().equals(organizationDraftSave.getUsernameCreated())) {
//                responses.add(fromModel(organizationDraftRequest.getId(),
//                        organizationDraftRequest.getStatus(),
//                        "Bạn không thể tự duyệt yêu cầu của chính mình"));
//                continue;
//            }

            //chặn duyệt lại những approve đã tùng được thao tác duyệt hoặc từ chối rồi
            if (!organizationDraftSave.getApprove().equals(EApprovalStatus.PENDING.getId())){
                responses.add(
                        fromModel(organizationDraftSave.getId(),
                                draftRequest.getStatus(),
                                "Bản ghi id: "
                                        + organizationDraftSave.getId()
                                        + ", " + organizationDraftSave.getName()
                                        + ", đã được duyệt"));
                continue;
            }

            organizationDraftSave.setUsernameAccepted(userRequested.getUsername());
            organizationDraftSave.setUserBrcdAccepted(userRequested.getBrcd());
            //trường hợp đông ý
            if (draftRequest.getStatus().equals(EApprovalStatus.APPROVED.getId())) {
                organizationDraftSave.setApprove(EApprovalStatus.APPROVED.getId());
                organizationDraftsSave.add(organizationDraftSave);
                responses.add(fromModel(organizationDraftSave.getId(),
                                draftRequest.getStatus(),
                                "Yêu cầu đã được duyệt thành công!"));

                organizationSaves.add(modelMapper.map(organizationDraftSave, Organization.class));
                continue;
            }

            //TH từ chối
            if (draftRequest.getStatus().equals(EApprovalStatus.DENIED.getId())) {
                organizationDraftSave.setApprove(EApprovalStatus.DENIED.getId());
                organizationDraftsSave.add(organizationDraftSave);
                responses.add(fromModel(organizationDraftSave.getId(),
                        draftRequest.getStatus(),
                        "Yêu cầu từ chối đã được duyệt thành công!"));
            }
        }

        if (organizationDraftsSave.isEmpty()){
            return responses;
        }

        organizationService.saveAll(organizationSaves);
        client.saveAll(organizationDraftsSave);

        return responses;
    }

    private DraftResponse fromModel(String id, Integer approve, String mess) {
        return DraftResponse.builder()
                .id(id)
                .approve(approve+"")
                .message(mess)
                .build();
    }

    public PageResponse<OrganizationDraftResponse> search(OrganizationDraftSearchRequest request){
        PageResponse<OrganizationDraftResponse> response = new PageResponse<>();
        UserDetailsImpl userRequested = getUserRequested();
        List<String> codeChild = organizationService.getChildCode(userRequested.getOrganizationCode());
        if (Objects.nonNull(request.getCode()) && !codeChild.contains(request.getCode()) && BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd()){
            return response;
        }

        if (BRANCH_CODE_HEAD_QUARTER < userRequested.getBrcd() && Objects.isNull(request.getCode())){
            request.setCode(userRequested.getOrganizationCode());
        }

        PageResponse<OrganizationDraft> draftPageResponse = client.search(request).getData();
        if (Objects.isNull(draftPageResponse)) {
            return response;
        }

        response.setTotalPages(draftPageResponse.getTotalPages());
        response.setCurrentPage(draftPageResponse.getCurrentPage());
        response.setTotalItems(draftPageResponse.getTotalItems());

        if (Objects.nonNull(draftPageResponse.getData())) {
            response.setData(draftPageResponse.getData().stream()
                    .map(organizationDraft -> {
                        OrganizationDraftResponse o = OrganizationDraftResponse.builder()
                                .id(organizationDraft.getId())
                                .approve(organizationDraft.getApprove())
                                .build();

                        o.setCode(organizationDraft.getCode());
                        o.setName(organizationDraft.getName());
                        o.setBrcd(organizationDraft.getBrcd());
                        o.setForm(organizationDraft.getForm());
                        o.setParentCode(organizationDraft.getParentCode());
                        o.setAuthorized(organizationDraft.getAuthorized());
                        o.setResolutionNumber(organizationDraft.getResolutionNumber());
                        o.setResolutionDate(organizationDraft.getResolutionDate());
                        o.setEstablishmentDecisionNumber(organizationDraft.getEstablishmentDecisionNumber());
                        o.setDecisionDate(organizationDraft.getDecisionDate());
                        o.setEffectiveDate(organizationDraft.getEffectiveDate());
                        o.setStatus(organizationDraft.getStatus());
                        o.setCreatedAt(organizationDraft.getCreatedAt());
                        o.setUpdatedAt(organizationDraft.getUpdatedAt());
                        o.setOrganizationCode(organizationDraft.getOrganizationCode());
                        o.setUsernameCreated(organizationDraft.getUsernameCreated());
                        o.setUsernameAccepted(organizationDraft.getUsernameAccepted());
                        o.setUserBrcdAccepted(organizationDraft.getUserBrcdAccepted());
                        o.setUserBrcdCreated(organizationDraft.getUserBrcdCreated());
                        return o;
                            }
                    ).toList()
            );
        }

        return response;
    }

    private UserDetailsImpl getUserRequested(){
        return userService.getUserRequested();
    }

    public String delete(String id){
        OrganizationDraft organizationDraft = client.findById(id).getData();
        if (Objects.isNull(organizationDraft)) {
            throw new CommonException("Không xóa được yêu cầu. Vui lòng kiểm tra lại sau!");
        }

        if (EApprovalStatus.PENDING.getId() != organizationDraft.getApprove()){
            throw new CommonException("Yêu cầu đã được duyệt, nên bạn không thể xóa yêu cầu này!");
        }
        client.delete(id);
        return "Xóa yêu cầu thành công";
    }
}
