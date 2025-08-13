package com.agribank.qldv_api.service.organization;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.gateway.OrganizationDraftClient;
import com.agribank.qldv_api.gateway.OrganizationReferenceClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.form02.OrganizationHistoryClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.organization.OrganizationCreateRequest;
import com.agribank.qldv_api.request.organization.OrganizationRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.organization.OrganizationHierarchyResponse;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.OrganizationDto;
import com.agribank.qldvutils.entity.*;
import com.agribank.qldvutils.entity.form02.OrganizationHistory;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.tcd.SearchRp01Request;
import com.agribank.qldvutils.request.organization.ApproveOrganizationRequest;
import com.agribank.qldvutils.request.organization.OrganizationSearchRequest;
import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.request.organization.OrganizationRpSearchRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.tcd.BcslTcdRp01Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;
import static com.agribank.qldv_api.enums.Constants.BTCDU_CODE;

@Service
@RequiredArgsConstructor
public class OrganizationService implements EntityHandler {
    private final OrganizationClient client;
    private final RequestClient requestClient;
    private final OrganizationHistoryClient historyClient;
    private final OrganizationReferenceClient organizationReferenceClient;
    private final OrganizationDraftClient organizationDraftClient;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final OrganizationReferenceService organizationReferenceService;
    private final RequestService requestService;
    private final CheckAuthorityService checkAuthorityService;
    private final OrganizationDraftService organizationDraftService;
    @Qualifier("importOrganization")
    private final ImportOrganizationService importOrganizationService;
    private final EForm form = EForm.BIEU_01;

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

        Organization organization = findByCode(organizationRequest.getCode());
        if (Objects.nonNull(organizationRequest.getCode()) && Objects.nonNull(organization)) {
            throw new CommonException("Mã TCD đẫ tồn tại vui lòng kiểm tra lại");
        }

        //đối với cấp B, cha là chính nó
        organizationRequest.setParentCode(organizationRequest.getCode());
        return save(organizationRequest);
    }

    private OrganizationResponse save(OrganizationCreateRequest organizationRequest) {
        Request request = null;
        OrganizationDraft organizationDraft = new  OrganizationDraft();
        if (Objects.nonNull(organizationRequest.getId())) {
            request = requestClient.findByReferenceId(organizationRequest.getId()).getData();

        }

        if (Objects.nonNull(request)) {
            organizationDraft = organizationDraftService.findById(request.getReferenceId());
        }

        UserDetailsImpl userRequested = getUserRequested();
        organizationDraft = fromModel(organizationDraft, organizationRequest);
        organizationDraft.setApprove(EApprovalStatus.PENDING.getId());
        organizationDraft.setStatus(EOrganizationStatus.YES.getStatus());
        organizationDraft.setOrganizationCode(userRequested.getOrganizationCode());
        organizationDraft.setCreatedBy(userRequested.getId());

        saveRequest(request, organizationDraft, userRequested.getId(), null);
        return modelMapper.map(organizationDraft, OrganizationResponse.class);
    }

    private OrganizationDraft fromModel(OrganizationDraft organizationDraft, OrganizationCreateRequest organizationRequest){
        organizationDraft.setCode(organizationRequest.getCode());
        organizationDraft.setName(organizationRequest.getName());
        organizationDraft.setBrcd(organizationRequest.getBrcd());
        organizationDraft.setForm(organizationRequest.getForm());
        organizationDraft.setParentCode(organizationRequest.getParentCode());
        organizationDraft.setAuthorized(organizationRequest.getAuthorized());
        organizationDraft.setResolutionNumber(organizationRequest.getResolutionNumber());
        organizationDraft.setResolutionDate(organizationRequest.getResolutionDate());
        organizationDraft.setEstablishmentDecisionNumber(organizationRequest.getEstablishmentDecisionNumber());
        organizationDraft.setDecisionDate(organizationRequest.getDecisionDate());
        organizationDraft.setEffectiveDate(organizationRequest.getEffectiveDate());

        return organizationDraft;
    }

    //Thuộc nhóm khác thì tìm chi nhánh cha của chi nhánh con rồi tăng giá trị lên 1
    private void checkDBBPAndCBTTDBBP(OrganizationCreateRequest organizationRequest){
        Organization organization = findByCode(organizationRequest.getParentCode());
        if (Objects.isNull(organization)) {
            throw new CommonException("Không tồn tại Tổ chức đảng có mã code:" + organizationRequest.getParentCode());
        }

        if (!Constants.FORM_B1_NAME.equals(organization.getForm()) && !Constants.FORM_C1_NAME.equals(organization.getForm())) {
            throw new CommonException(String.format("Tổ chức Đảng: %s không có TCĐ con trực thuộc. Vui lòng chọn TCD khác", organization.getName()));
        }

        //Tìm tcd để thực hiện việc tăng mã tcd
        Organization organizationDb = getByParentCodeMax(organizationRequest.getParentCode());

        int ascii = organizationRequest.getForm().charAt(0) - organization.getForm().charAt(0);
        if (ascii != 1){
            throw new CommonException("Chưa thể tạo hình thức Tổ chức Đảng này. Vui lòng chọn Hình thức khác!");
        }
        //set giá trị mã tcd nếu nó chưa có thằng con thì + 01
        if (
                Objects.isNull(organizationDb) ||
                (Objects.nonNull(organizationDb.getCode()) && Constants.FORM_B_NAME_LENGTH.equals(organizationDb.getCode().length()))
        ){
            organizationRequest.setCode(organizationRequest.getParentCode()+"01");
            return;
        }

        Integer code = Integer.parseInt(organizationDb.getCode()) + 1;
        organizationRequest.setCode(code+"");
    }

    public OrganizationResponse update(OrganizationRequest organizationRequest){
        checkAuthorityService.hasAuthorityOverOrganization(organizationRequest.getCode());
        Organization organization = findByCode(organizationRequest.getCode());
        if (Objects.isNull(organization)) {
            throw new CommonException("Kiểm tra lại Mã tổ chức Đảng");
        }

        if (Objects.isNull(organizationRequest.getStatus())) {
            throw new CommonException("Kiểm tra lại giá trị trạng thái hoạt động");
        }
        UserDetailsImpl userRequested = getUserRequested();

        Organization organizationOld = (Organization) CommonUtils.handleCloneObject(organization);

        OrganizationDraft organizationDraft = modelMapper.map(organization, OrganizationDraft.class);
        organizationDraft.setAuthorized(organizationRequest.getAuthorized());
        organizationDraft.setName(organizationRequest.getName());
        organizationDraft.setResolutionNumber(organizationRequest.getResolutionNumber());
        organizationDraft.setResolutionDate(organizationRequest.getResolutionDate());
        organizationDraft.setEstablishmentDecisionNumber(organizationRequest.getEstablishmentDecisionNumber());
        organizationDraft.setDecisionDate(organizationRequest.getDecisionDate());
        organizationDraft.setEffectiveDate(organizationRequest.getEffectiveDate());
        organizationDraft.setStatus(organizationRequest.getStatus());
        organizationDraft.setApprove(EApprovalStatus.PENDING.getId());
        organizationDraft.setOrganizationCode(userRequested.getOrganizationCode());
        organizationDraft.setCreatedBy(userRequested.getId());

        String form = getForm(organizationOld);
        if (form.contains(organizationRequest.getForm())) {
            organizationDraft.setForm(organizationRequest.getForm());
        }else {
            throw new CommonException("Bạn chỉ có thể sửa hình thức tổ chức cùng cấp với tổ chức Đảng hiện tại");
        }

        saveRequest(null, organizationDraft, userRequested.getId(), organizationOld);
        return modelMapper.map(organizationDraft, OrganizationResponse.class);
    }

    private String getForm(Organization organizationOld) {
        String form = EOrganizationReference.GROUP_D.getCode();
        if (EOrganizationReference.GROUP_A.getCode().contains(organizationOld.getForm())) {
            form = EOrganizationReference.GROUP_A.getCode();
        }else if (EOrganizationReference.GROUP_B.getCode().contains(organizationOld.getForm())) {
            form = EOrganizationReference.GROUP_B.getCode();
        }else if (EOrganizationReference.GROUP_C.getCode().contains(organizationOld.getForm())) {
            form = EOrganizationReference.GROUP_C.getCode();
        }
        return form;
    }

    private Map<String, String> getCombinedFieldMap() {
        return new LinkedHashMap<>(OrganizationDraft.FIELD_MAP);
    }

    @SneakyThrows
    private void saveRequest(Request request, OrganizationDraft organizationDraft, String userId, Organization organizationDraftOld){
        if (Objects.isNull(request)) {
            request = requestService.initializeRequest(organizationDraft, organizationDraftOld, form, getCombinedFieldMap());
        }else {
            Map<String, Object> newDataMap = CommonUtils.createFilteredDataMap(organizationDraft, getCombinedFieldMap());
            request.setNewData(objectMapper.writeValueAsString(newDataMap));
        }

        if (Objects.nonNull(organizationDraftOld) && Objects.nonNull(request)) {
            Map<String, Object> oldDataMap = CommonUtils.createFilteredDataMap(organizationDraftOld, getCombinedFieldMap());
            request.setOldData(objectMapper.writeValueAsString(oldDataMap));
        }

        request.setCreatedBy(userId);
        organizationDraft = organizationDraftService.save(organizationDraft);

        request.setOrganizationCode(organizationDraft.getCode());
        request.setReferenceId(organizationDraft.getId());
        requestClient.save(request);
    }

    public Organization getByParentCodeMax(String parentCode){
        return  client.getByParentCodeMax(parentCode).getData();
    }

    public Organization findByCode(String organizationId) {
        DefaultResponse<Organization> organizationDefaultResponse = client.findByCode(organizationId);
        return organizationDefaultResponse.getData();
    }

    public OrganizationResponse save(Organization organization) {
        client.save(organization);
        return modelMapper.map(organization, OrganizationResponse.class);
    }

    public void saveAll(List<Organization> organizations) {
        client.saveAll(organizations);
    }

    public PageResponse<OrganizationResponse> search(OrganizationSearchRequest request){
        PageResponse<OrganizationResponse> response = new PageResponse<>();
        UserDetailsImpl userRequested = getUserRequested();

        request.setCode(getOrganizationCode(request.getCode(), userRequested));
        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("code");
        }

        PageResponse<Organization> organizationPageResponse;

        if (Objects.nonNull(request.getCode()) && request.getCode().length() > 4){
            organizationPageResponse = client.searchChild(request).getData();
        }else {
            organizationPageResponse = client.search(request).getData();
        }

        if (Objects.isNull(organizationPageResponse)) {
            return response;
        }

        response.setTotalPages(organizationPageResponse.getTotalPages());
        response.setCurrentPage(organizationPageResponse.getCurrentPage());
        response.setTotalItems(organizationPageResponse.getTotalItems());

        if (Objects.nonNull(organizationPageResponse.getData())) {
            response.setData(
                    createOrganizationListResponse(organizationPageResponse.getData())
            );
        }

        return response;
    }

    private List<OrganizationResponse> createOrganizationListResponse(List<Organization> organizationList) {
        List<OrganizationReference> formList = organizationReferenceClient.findAll().getData();

        Map<String, String> formMap = new HashMap<>();

        for (OrganizationReference reference: formList) {
            formMap.put(reference.getCode(), reference.getName());
        }

        return organizationList.stream()
                .map(organization -> {
                    OrganizationResponse organizationResponse = modelMapper.map(organization, OrganizationResponse.class);
                    organizationResponse.setForm(formMap.get(organization.getForm()));
                    return organizationResponse;
                })
                .toList();
    }

    public String getOrganizationCode(String organizationCode, UserDetailsImpl userRequested) {
        List<String> codeChild = getChildCode(userRequested.getOrganizationCode());
        if (Objects.nonNull(organizationCode) && !codeChild.contains(organizationCode)
                && !userRequested.getOrganizationCode().contains(String.valueOf(BRANCH_CODE_HEAD_QUARTER))
                && !userRequested.getOrganizationCode().contains(organizationCode)
        ){
            throw new CommonException("Bạn không có quyền tìm kiếm TCD khác");
        }

        if (!userRequested.getOrganizationCode().contains(String.valueOf(BRANCH_CODE_HEAD_QUARTER)) && Objects.isNull(organizationCode)){
            return userRequested.getOrganizationCode();
        }

        return organizationCode;
    }

    public OrganizationResponse get(String code){
        Organization organization = findByCode(code);
        return modelMapper.map(organization, OrganizationResponse.class);
    }

    public List<String> getChildCode(String code){
        List<Organization> organizations = findByParent(code);
        List<String> codeChild = new ArrayList<>();
        if (!organizations.isEmpty()){
            codeChild = organizations.stream().map(Organization::getCode).toList();
        }

        return codeChild;
    }

    public List<OrganizationResponse> findByParentCode(String code){
        List<Organization> organizations = client.findByParentCode(code).getData();
        if (Objects.isNull(organizations) || organizations.isEmpty()){
            return new ArrayList<>();
        }

        return createOrganizationListResponse(organizations);
    }

    public OrganizationResponse findByUserId(String userId) {
        if (Objects.isNull(userId)){
            UserDetailsImpl userRequested = getUserRequested();
            userId = userRequested.getId();
        }
        Organization organization = client.findByUserId(userId).getData();
        if (Objects.isNull(organization)){
            return null;
        }
        return modelMapper.map(organization, OrganizationResponse.class);
    }

    public String createRequestDelete(String code){
        checkAuthorityService.hasAuthorityOverOrganization(code);
        Organization organization = client.findByCode(code).getData();
        if (Objects.isNull(organization)){
            throw new CommonException("Sai code, vui lòng kiểm tra lại");
        }
        UserDetailsImpl userRequested = getUserRequested();
        Request partActivityRequest = requestService.initializeRequest(null, organization, form, getCombinedFieldMap());
        partActivityRequest.setFormCode(form.getCode());
        partActivityRequest.setFormName(form.getName());
        partActivityRequest.setCreatedBy(userRequested.getId());
        partActivityRequest.setReferenceId(organization.getCode());
        partActivityRequest.setOrganizationCode(organization.getCode());
        requestClient.save(partActivityRequest);

        return "Tạo yêu cầu thành công";
    }

    @Override
    public boolean applyCreate(String referenceId, UserDetailsImpl userDetails) {
        OrganizationDraft organizationDraft = organizationDraftService.findById(referenceId);
        if (Objects.isNull(organizationDraft)){
            throw new CommonException("Lỗi id draft không chính xác!");
        }
        Organization organization = modelMapper.map(organizationDraft, Organization.class);

        organizationDraft.setApprove(EApprovalStatus.APPROVED.getId());
        organizationDraft.setApprovedBy(userDetails.getId());

        OrganizationHistory history = OrganizationHistory.builder()
                .name(organization.getName())
                .type(EReport01Type.ESTABLISH.getId())
                .refId(organization.getCode())
                .effectiveDate(organization.getEffectiveDate())
                .build();
        history.setCode(organization.getCode());

        ApproveOrganizationRequest organizationRequest = ApproveOrganizationRequest.builder()
                .organization(organization)
                .draft(organizationDraft)
                .history(history)
                .build();

        client.saveEntities(organizationRequest);

        return true;
    }

    @Override
    public boolean applyUpdate(String referenceId) {
        OrganizationDraft organizationDraft = organizationDraftService.findById(referenceId);
        if (Objects.isNull(organizationDraft)){
            throw new CommonException("Lỗi id draft không chính xác!");
        }
        Organization organization = modelMapper.map(organizationDraft, Organization.class);

        UserDetailsImpl userRequested = getUserRequested();
        organizationDraft.setApprove(EApprovalStatus.APPROVED.getId());
        organizationDraft.setApprovedBy(userRequested.getId());

        OrganizationHistory history = historyClient.findByRefId(EReport01Type.ESTABLISH.getId(), organization.getCode()).getData();

        if (Objects.nonNull(history)) {
            history.setCode(organization.getCode());
            history.setName(organization.getName());
            history.setEffectiveDate(organization.getEffectiveDate());
        } else {
            history = OrganizationHistory.builder()
                    .name(organization.getName())
                    .type(EReport01Type.ESTABLISH.getId())
                    .refId(organization.getCode())
                    .effectiveDate(organization.getEffectiveDate())
                    .build();
            history.setCode(organization.getCode());
        }

        ApproveOrganizationRequest organizationRequest = ApproveOrganizationRequest.builder()
                .organization(organization)
                .draft(organizationDraft)
                .history(history)
                .build();

        client.saveEntities(organizationRequest);

        return true;
    }

    @Override
    public boolean applyDelete(String referenceId) {
        Organization organization = findByCode(referenceId);
        if (Objects.isNull(organization)){
            throw new CommonException("Lỗi referenceId không tìm thấy TCD");
        }
        organization.setStatus(EOrganizationStatus.NO.getStatus());
        client.save(organization);
        return true;
    }

    @Override
    public void setDenied(String referenceId) {
        OrganizationDraft organizationDraft = organizationDraftService.findById(referenceId);
        if (Objects.isNull(organizationDraft)){
            throw new CommonException("Không tìm thấy bản ghi có id: " + referenceId);
        }

        UserDetailsImpl userRequested = getUserRequested();
        organizationDraft.setApprovedBy(userRequested.getId());
        organizationDraft.setApprove(EApprovalStatus.DENIED.getId());
        organizationDraftService.save(organizationDraft);
    }

    public List<OrganizationResponse> getAdvisoryAgency(){
        List<Organization> organizations = client.advisoryAgency().getData();
        if (organizations.isEmpty()){
            return new ArrayList<>();
        }

        return organizations.stream().map(organization -> modelMapper.map(organization, OrganizationResponse.class)).toList();
    }

    public List<OrganizationResponse> partyBranch(){
        List<Organization> organizations = client.partyBranch().getData();
        if (organizations.isEmpty()){
            return new ArrayList<>();
        }

        return organizations.stream().map(organization -> modelMapper.map(organization, OrganizationResponse.class)).toList();
    }

    public List<OrganizationDto> getListOrganizationCodeName(){
        UserDetailsImpl userRequested = getUserRequested();
        List<Organization> organizations = findByParent(userRequested.getOrganizationCode());
        List<OrganizationDto> organizationDTOs = organizations.stream()
                .map(organization -> new OrganizationDto(organization.getCode(), organization.getName(), organization.getForm()))
                .collect(Collectors.toList());
        if (organizations.isEmpty()){
            return new ArrayList<>();
        }
        return organizationDTOs;
    }

    public PageResponse<Organization> searchRp(OrganizationRpSearchRequest request){
        UserDetailsImpl userRequested = getUserRequested();
        request.setCode(getOrganizationCode(request.getCode(), userRequested));
        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("code");
        }

        return client.searchRp(request).getData();
    }

    public List<OrganizationHierarchyResponse> getAll(){
        UserDetailsImpl userRequested = getUserRequested();
        String code = getOrganizationCode(null, userRequested);
        List<Organization> organizations = client.getOrganizationAllParent(code).getData();
        if (Objects.isNull(organizations) || organizations.isEmpty()){
            return new ArrayList<>();
        }

        List<OrganizationHierarchyResponse> response = organizations.stream().map(
                o -> modelMapper.map(o, OrganizationHierarchyResponse.class)
        ).toList();

        Map<String, List<OrganizationHierarchyResponse>> groupedByParent = response.stream()
                .filter(o -> (!Objects.equals(o.getParentCode(), o.getCode()) && !Objects.equals(o.getParentCode(), Constants.DANG_UY_AGRIBANK_CODE)))
                .collect(Collectors.groupingBy(OrganizationHierarchyResponse::getParentCode));

        for (OrganizationHierarchyResponse org : response) {
            List<OrganizationHierarchyResponse> children = groupedByParent.get(org.getCode());
            if (children != null) {
                org.setChilds(children);
            }
        }

        return response.stream()
                .filter(o -> Objects.equals(o.getParentCode(), o.getCode()))
                .toList();
    }

    public List<Organization> findAll(){
        return client.findAll().getData();
    }

    @SneakyThrows
    public BaseResponse importExcel(MultipartFile file) {
        return importOrganizationService.handleReadFileUpload(file, EExcelImport.BIEU_1.name());
    }

    public List<Organization> findAllByCode(List<String> codeList){
        return client.findAllByCode(codeList).getData();
    }

    private UserDetailsImpl getUserRequested() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<Organization> findByParent(String parentCode){
        return  client.findByParent(parentCode).getData();
    }

    public List<OrganizationResponse> getOrganizationFormB(){
        List<Organization> organizations = client.getOrganizationFormB().getData();
        if (Objects.isNull(organizations) || organizations.isEmpty()){
            return new ArrayList<>();
        }

        return organizations.stream().map(organization -> modelMapper.map(organization, OrganizationResponse.class)).toList();
    }

    public List<OrganizationResponse> getChildOrganizationByUser() {
        String organizationCode = CommonUtils.getOrganizationByRequestedUser();
        List<Organization> organizationList = new ArrayList<>();

        if (Objects.nonNull(organizationCode)) {
            organizationList = client.findByParentCode(organizationCode).getData();
        } else {
            organizationCode = BTCDU_CODE;
            organizationList = client.findByParentCode(organizationCode).getData();
            List<Organization> formBOrganization = client.getOrganizationFormB().getData();
            organizationList.addAll(formBOrganization);
        }

        return organizationList.stream()
                .map(organization -> modelMapper.map(organization, OrganizationResponse.class))
                .toList();
    }

    public List<Organization> getActiveOrganizations(){
        return client.getActiveOrganizations().getData();
    }

    public PageResponse<BcslTcdRp01Response> searchRp01(SearchRp01Request request){
        return client.searchRp01(request).getData();
    }

    public Organization getOrganizationChildMax(String code, String form){
        return client.getChildrenMax(code, form).getData();
    }

    public OrganizationResponse updateDraft(OrganizationCreateRequest request){
        Request organizationRequestDraft = requestClient.findByReferenceId(request.getId()).getData();
        if (Objects.isNull(organizationRequestDraft)){
            throw new CommonException("Không tìm thấy dữ liệu");
        }

        if (EApprovalStatus.PENDING.getId() != organizationRequestDraft.getStatus()){
            throw new CommonException("Yêu cầu này đã được cập nhật, vui lòng tạo request khác");
        }

        organizationDraftClient.findById(request.getId()).getData().orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));
        return create(request);
    }

    public OrganizationResponse getDraftDetail(String id){
        OrganizationDraft organizationDraft = organizationDraftClient.findById(id)
                .getData().orElseThrow(() -> new CommonException("Không tìm thấy dữ liệu"));

        return modelMapper.map(organizationDraft, OrganizationResponse.class);
    }
}
