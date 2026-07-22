package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.ECaseStatusCode;
import com.agribank.qldv_api.exception.FieldValidationException;
import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.exception.NotFoundException;
import com.agribank.qldv_api.gateway.AttachmentClient;
import com.agribank.qldv_api.gateway.CaseClient;
import com.agribank.qldv_api.gateway.CaseEstablishmentClient;
import com.agribank.qldv_api.gateway.CaseTypeClient;
import com.agribank.qldv_api.gateway.EmployeeInfoClient;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationTypeClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.casemgmt.EstablishmentCaseRequest;
import com.agribank.qldv_api.request.casemgmt.LeadershipInfoRequest;
import com.agribank.qldv_api.request.casemgmt.ProposedCommitteeMemberRequest;
import com.agribank.qldv_api.response.casemgmt.EstablishmentCaseResponse;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.entity.Attachment;
import com.agribank.qldvutils.entity.Case;
import com.agribank.qldvutils.entity.CaseEstablishment;
import com.agribank.qldvutils.entity.CaseEstablishmentCommittee;
import com.agribank.qldvutils.entity.CaseType;
import com.agribank.qldvutils.entity.OrganizationType;
import com.agribank.qldvutils.enums.ECommitteePosition;
import com.agribank.qldvutils.enums.EOperationStatus;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.casemgmt.EstablishmentCasePersistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service dùng chung cho API-SC02-01 (tạo mới, Bước 1 Thành lập TCĐ cấp Agribank) và API-SC02-02
 * (cập nhật một phần khi hồ sơ còn ở A-01) — thiết kế theo đúng RR-03 (prompt_S2-04_API_SC02.md
 * mục 2): 1 hàm dùng chung nhận {@code authorityLevel}/{@code allowedOrganizationTypeId} làm
 * input, KHÔNG rẽ nhánh business rule theo authorityLevel, KHÔNG hardcode ngưỡng member_count —
 * để Sprint 5 (SC-07, cấp cơ sở) chỉ cần thêm 1 lớp controller mỏng ép sẵn
 * {@code allowedOrganizationTypeId} + gọi lại nguyên vẹn service này.

 * CHỈ xử lý phần validate CẦN TRUY VẤN DB (tồn tại organization_type_id/staff_id, trùng tên tổ
 * chức, ngưỡng member_count theo danh mục...) — phần validate format/độ dài/cross-field tự thân
 * KHÔNG cần DB đã chuyển sang {@code EstablishmentCaseRequest.validate()}/{@code
 * validateForUpdate()}, gọi ở {@code CaseController} TRƯỚC khi vào service.

 * Giai đoạn đầu PMDV_STAFF CHƯA có dữ liệu đồng bộ từ GA — validate staff_id (leadership_info,
 * proposed_committee_members) phải tra trực tiếp qua {@link EmployeeInfoClient} (bảng GA thật,
 * qldv-db/EmployeeInfoService), KHÔNG dùng StaffClient/PMDV_STAFF.

 * Toàn bộ thao tác GHI (Case + CaseEstablishment + committee + liên kết attachment) gói thành 1
 * {@link EstablishmentCasePersistRequest} và gửi xuống qldv-db bằng ĐÚNG 1 lệnh
 * ({@code CaseEstablishmentClient.persist}) — chạy trong 1 transaction ở qldv-db
 * ({@code CaseEstablishmentPersistService}), lỗi ở bất kỳ bước nào cũng rollback toàn bộ. Ở đây
 * (qldv-api) chỉ làm nhiệm vụ đọc dữ liệu để validate + build request — không tự thực hiện ghi
 * DB nào cả.

 * BR-SC02-06 (tự động kích hoạt Data Mapping/API-DOC-01 khi lưu đủ field bắt buộc) CHƯA được nối
 * ở đây — cơ chế sinh văn bản tự động (S2-03, API-DOC-01..04) chưa triển khai trong repo tại thời
 * điểm code task này; khi S2-03 sẵn sàng, gọi service đó ngay sau khi tạo/cập nhật thành công tại
 * đúng vị trí đánh dấu TODO bên dưới.
 */
@Service
@RequiredArgsConstructor
public class EstablishmentCaseService {
    private static final String ESTABLISH_CASE_TYPE_CODE = "ESTABLISH";
    private static final String INITIAL_STATUS_CODE = ECaseStatusCode.A_01.getCode();

    private final CaseClient caseClient;
    private final CaseEstablishmentClient caseEstablishmentClient;
    private final CaseTypeClient caseTypeClient;
    private final OrganizationTypeClient organizationTypeClient;
    private final OrganizationClient organizationClient;
    private final EmployeeInfoClient employeeInfoClient;
    private final AttachmentClient attachmentClient;
    private final UserService userService;

    /**
     * API-SC02-01. {@code allowedOrganizationTypeId} null ở Sprint 2 (client tự chọn trong danh
     * mục) — Sprint 5/SC-07 truyền cố định mã "Chi bộ trực thuộc đảng bộ cơ sở" (BR-SC07-01).
     * {@code originFlow} do CALLER (controller) truyền — service không tự suy ra từ
     * authorityLevel (mục 2.4 prompt): Sprint 2 luôn gọi với Constants.CASE_FLOW_BTCDU ("A").
     * Request đã được {@code request.validate()} chạy TRƯỚC ở controller — ở đây chỉ còn validate
     * phần cần DB.
     */
    public EstablishmentCaseResponse createEstablishmentCase(EstablishmentCaseRequest request, Integer authorityLevel,
                                                              String originFlow, String allowedOrganizationTypeId) {
        UserDetailsImpl user = requireUser();
        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        String organizationTypeId = allowedOrganizationTypeId != null
                ? allowedOrganizationTypeId : request.getOrganizationTypeId();
        OrganizationType organizationType = validateOrganizationType(organizationTypeId, errors);

        validateOrganizationNameUnique(errors, request.getProposedOrganizationName());
        validateMemberCountThreshold(errors, request.getMemberCount(), organizationType);
        validateLeadershipInfoExists(errors, request.getLeadershipInfo());
        Set<String> foundStaffCodes = validateProposedCommitteeMembersExist(errors, request.getProposedCommitteeMembers());

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        CaseType caseType = requireEstablishCaseType();

        Case newCase = Case.builder()
                .caseCode(generateCaseCode(caseType.getCode()))
                .caseTypeId(caseType.getId())
                .authorityLevel(authorityLevel)
                .originFlow(originFlow)
                .statusId(INITIAL_STATUS_CODE)
                .createdBy(user.getId())
                .proposedOrganizationName(request.getProposedOrganizationName())
                .build();

        CaseEstablishment establishment = CaseEstablishment.builder().build();
        applyUpdates(establishment, request, organizationTypeId);

        EstablishmentCasePersistRequest persistRequest = new EstablishmentCasePersistRequest();
        persistRequest.setCaseEntity(newCase);
        persistRequest.setEstablishment(establishment);
        persistRequest.setCommitteeMembers(buildCommitteeRows(request.getProposedCommitteeMembers(), foundStaffCodes));

        Case savedCase = caseEstablishmentClient.persist(persistRequest).getData();

        return new EstablishmentCaseResponse(savedCase.getId(), savedCase.getCaseCode(), savedCase.getStatusId(),
                savedCase.getCreatedAt(), savedCase.getUpdatedAt(), warnings);
    }

    /**
     * API-SC02-02. Cập nhật MỘT PHẦN — field null trong request nghĩa là "không đổi". BR-SC02-05:
     * chỉ cho phép khi status_id = A-01, chặn ở BE bất kể FE có ẩn field hay không. Request đã
     * được {@code request.validateForUpdate()} chạy TRƯỚC ở controller.
     */
    public EstablishmentCaseResponse updateEstablishmentCase(String caseId, EstablishmentCaseRequest request,
                                                              String allowedOrganizationTypeId) {
        requireUser();
        Case existingCase = caseClient.findById(caseId).getData().orElse(null);
        if (existingCase == null) {
            throw new NotFoundException("Không tìm thấy hồ sơ nghiệp vụ");
        }
        if (!INITIAL_STATUS_CODE.equals(existingCase.getStatusId())) {
            throw new ForbiddenException("BR-SC02-05: Hồ sơ không còn ở trạng thái Đang thực hiện (A-01), không thể chỉnh sửa");
        }

        CaseEstablishment existing = caseEstablishmentClient.findByCaseId(caseId).getData().orElse(
                CaseEstablishment.builder().caseId(caseId).build());

        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, String> warnings = new LinkedHashMap<>();

        String requestedOrganizationTypeId = allowedOrganizationTypeId != null
                ? allowedOrganizationTypeId : request.getOrganizationTypeId();
        String effectiveOrganizationTypeId = requestedOrganizationTypeId != null
                ? requestedOrganizationTypeId : existing.getOrganizationTypeId();
        OrganizationType organizationType = effectiveOrganizationTypeId != null
                ? validateOrganizationType(effectiveOrganizationTypeId, errors) : null;

        if (request.getProposedOrganizationName() != null
                && !request.getProposedOrganizationName().equalsIgnoreCase(existingCase.getProposedOrganizationName())) {
            validateOrganizationNameUnique(errors, request.getProposedOrganizationName());
        }

        Integer effectiveMemberCount = request.getMemberCount() != null ? request.getMemberCount() : existing.getMemberCount();
        if (organizationType != null && effectiveMemberCount != null) {
            validateMemberCountThreshold(errors, effectiveMemberCount, organizationType);
        }

        if (request.getLeadershipInfo() != null) {
            validateLeadershipInfoExists(errors, request.getLeadershipInfo());
        }

        Set<String> foundStaffCodes = Set.of();
        boolean replaceCommitteeMembers = request.getProposedCommitteeMembers() != null;
        if (replaceCommitteeMembers) {
            foundStaffCodes = validateProposedCommitteeMembersExist(errors, request.getProposedCommitteeMembers());
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        if (request.getProposedOrganizationName() != null
                && !request.getProposedOrganizationName().equals(existingCase.getProposedOrganizationName())) {
            existingCase.setProposedOrganizationName(request.getProposedOrganizationName());
        }

        applyUpdates(existing, request, requestedOrganizationTypeId);

        EstablishmentCasePersistRequest persistRequest = new EstablishmentCasePersistRequest();
        persistRequest.setCaseEntity(existingCase);
        persistRequest.setEstablishment(existing);
        persistRequest.setCommitteeMembers(replaceCommitteeMembers
                ? buildCommitteeRows(request.getProposedCommitteeMembers(), foundStaffCodes) : null);

        Case savedCase = caseEstablishmentClient.persist(persistRequest).getData();

        return new EstablishmentCaseResponse(savedCase.getId(), savedCase.getCaseCode(), savedCase.getStatusId(),
                savedCase.getCreatedAt(), savedCase.getUpdatedAt(), warnings);
    }

    // ---------------------------------------------------------------- helpers (đều cần DB)

    private UserDetailsImpl requireUser() {
        UserDetailsImpl user = userService.getUserRequested();
        if (user == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }
        return user;
    }

    private CaseType requireEstablishCaseType() {
        return caseTypeClient.findByCode(ESTABLISH_CASE_TYPE_CODE).getData()
                .orElseThrow(() -> new CommonException(
                        "Danh mục loại nghiệp vụ 'Thành lập' (case_type_id=ESTABLISH) chưa được seed — phụ thuộc S1-02"));
    }

    private OrganizationType validateOrganizationType(String organizationTypeId, Map<String, String> errors) {
        if (organizationTypeId == null || organizationTypeId.isBlank()) {
            errors.put("organizationTypeId", "ERR-SC02-06: organization_type_id không được để trống");
            return null;
        }
        Optional<OrganizationType> organizationType = organizationTypeClient.findById(organizationTypeId).getData();
        if (organizationType.isEmpty()) {
            errors.put("organizationTypeId", "ERR-SC02-06: organization_type_id không thuộc danh mục loại hình tổ chức đảng");
            return null;
        }
        return organizationType.get();
    }

    private void validateOrganizationNameUnique(Map<String, String> errors, String proposedOrganizationName) {
        if (proposedOrganizationName == null || proposedOrganizationName.isBlank()) {
            return;
        }
        Boolean exists = organizationClient
                .existsActiveByName(proposedOrganizationName, EOperationStatus.ACTIVE.getId())
                .getData();
        if (Boolean.TRUE.equals(exists)) {
            errors.put("proposedOrganizationName", "ERR-SC02-05: proposed_organization_name đã trùng tên 1 tổ chức đảng đang Hoạt động");
        }
    }

    /** BR-SC02-01 — ngưỡng lookup từ PMDV_ORGANIZATION_TYPE.min_member_count, KHÔNG hardcode 3/30. */
    private void validateMemberCountThreshold(Map<String, String> errors, Integer memberCount, OrganizationType organizationType) {
        if (memberCount == null || organizationType == null || organizationType.getMinMemberCount() == null) {
            return;
        }
        if (memberCount < organizationType.getMinMemberCount()) {
            errors.put("memberCount", "ERR-SC02-07: member_count phải >= " + organizationType.getMinMemberCount()
                    + " theo loại hình tổ chức đảng đã chọn (BR-SC02-01)");
        }
    }

    /** staff_id (nếu tra cứu GA) phải tồn tại — giai đoạn đầu tra trực tiếp EmployeeInfoClient, chưa dùng PMDV_STAFF. */
    private void validateLeadershipInfoExists(Map<String, String> errors, LeadershipInfoRequest leadershipInfo) {
        if (leadershipInfo == null || leadershipInfo.getStaffId() == null || leadershipInfo.getStaffId().isBlank()) {
            return;
        }
        EmployeeInfoDto employee = employeeInfoClient.findByEmpno(leadershipInfo.getStaffId()).getData();
        if (employee == null) {
            errors.put("leadershipInfo", "ERR-SC02-03: staff_id trong leadership_info không tồn tại ở hệ thống GA");
        }
    }

    /**
     * Tra hàng loạt (IN) qua EmployeeInfoClient — tránh N+1 (coding-convention.md mục 8). Trả về
     * tập staff_code hợp lệ để dùng ghép cùng committee rows, tránh gọi lại lần 2.
     */
    private Set<String> validateProposedCommitteeMembersExist(Map<String, String> errors,
                                                                List<ProposedCommitteeMemberRequest> members) {
        if (members == null || members.isEmpty()) {
            return Set.of();
        }
        List<String> staffIds = members.stream()
                .map(ProposedCommitteeMemberRequest::getStaffId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<EmployeeInfoDto> found = staffIds.isEmpty() ? List.of() : safeList(employeeInfoClient.findByEmpnos(staffIds).getData());
        Set<String> foundCodes = found.stream().map(EmployeeInfoDto::getStaffCode).collect(Collectors.toSet());
        List<String> missing = staffIds.stream().filter(id -> !foundCodes.contains(id)).toList();
        if (!missing.isEmpty()) {
            errors.put("proposedCommitteeMembers", "ERR-SC02-10: staff_id không hợp lệ/không tồn tại: " + String.join(", ", missing));
        }
        return foundCodes;
    }

    private String generateCaseCode(String caseTypeCode) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String prefix = caseTypeCode + "-" + LocalDate.now().format(formatter);
        Optional<Case> latest = caseClient.findLatestByPrefix(prefix + "-").getData();
        int nextSeq = 1;
        if (latest.isPresent()) {
            String latestCode = latest.get().getCaseCode();
            nextSeq = Integer.parseInt(latestCode.substring(latestCode.lastIndexOf('-') + 1)) + 1;
        }
        return prefix + "-" + String.format("%04d", nextSeq);
    }

    private void applyUpdates(CaseEstablishment establishment, EstablishmentCaseRequest request, String organizationTypeId) {
        if (request.getBrcd() != null) {
            establishment.setBrcd(request.getBrcd());
        }
        if (request.getStaffCount() != null) {
            establishment.setStaffCount(request.getStaffCount());
        }
        if (request.getLeadershipInfo() != null) {
            establishment.setLeadershipStaffId(request.getLeadershipInfo().getStaffId());
            establishment.setLeadershipInfoText(request.getLeadershipInfo().getFreeText());
        }
        if (request.getBoardDecisionNo() != null) {
            establishment.setBoardDecisionNo(request.getBoardDecisionNo());
        }
        if (request.getBoardDecisionDate() != null) {
            establishment.setBoardDecisionDate(request.getBoardDecisionDate());
        }
        if (request.getBoardDecisionSummary() != null) {
            establishment.setBoardDecisionSummary(request.getBoardDecisionSummary());
        }
        if (organizationTypeId != null) {
            establishment.setOrganizationTypeId(organizationTypeId);
        }
        if (request.getMemberCount() != null) {
            establishment.setMemberCount(request.getMemberCount());
        }
        if (request.getCommitteeMemberCount() != null) {
            establishment.setCommitteeMemberCount(request.getCommitteeMemberCount());
        }
        if (request.getCommitteeStructure() != null) {
            establishment.setCommitteeStructure(request.getCommitteeStructure());
        }
        if (request.getPoliticalStandardConclusionNo() != null) {
            establishment.setPoliticalStandardConclusionNo(request.getPoliticalStandardConclusionNo());
        }
        if (request.getPoliticalStandardConclusionDate() != null) {
            establishment.setPoliticalStandardConclusionDate(request.getPoliticalStandardConclusionDate());
        }
    }

    /**
     * Chỉ DỰNG danh sách (không tự lưu) — CaseEstablishmentPersistService (qldv-db) sẽ gán
     * case_id cho từng dòng và ghi trong CÙNG transaction với Case/CaseEstablishment.
     */
    private List<CaseEstablishmentCommittee> buildCommitteeRows(List<ProposedCommitteeMemberRequest> members,
                                                                 Set<String> foundStaffCodes) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<CaseEstablishmentCommittee> rows = new ArrayList<>();
        for (ProposedCommitteeMemberRequest member : members) {
            if (!foundStaffCodes.contains(member.getStaffId())) {
                continue;
            }
            rows.add(CaseEstablishmentCommittee.builder()
                    .staffCode(member.getStaffId())
                    .proposedPosition(ECommitteePosition.valueOf(member.getProposedPosition()).getId())
                    .build());
        }
        return rows;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
