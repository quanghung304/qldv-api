package com.agribank.qldv_api.service.organization;

import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.enums.EOrganizationStatus;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.RequestService;
import com.agribank.qldv_api.service.import_file.ImportFileConfigDetailService;
import com.agribank.qldv_api.service.import_file.ImportFileConfigService;
import com.agribank.qldv_api.service.import_file.ImportFileService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.OrganizationDraft;
import com.agribank.qldvutils.entity.OrganizationReference;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.import_file.ImportFileConfigDetail;
import com.agribank.qldvutils.response.BaseResponse;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("importOrganization")
public class ImportOrganizationService extends ImportFileService {
    @Lazy
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private OrganizationReferenceService organizationReferenceService;
    @Autowired
    private RequestService requestService;
    @Autowired
    private OrganizationDraftService organizationDraftService;
    private final EForm form = EForm.BIEU_01;

    private List<OrganizationReference> organizationReferenceList = new ArrayList<>();
    private List<String> codes = new ArrayList<>();

    public ImportOrganizationService(ImportFileConfigService importFileConfigService, ImportFileConfigDetailService importFileConfigDetailService) {
        super(importFileConfigService, importFileConfigDetailService);
    }

    protected void handleInitCheckData() {
        initCheckData = organizationService.findAll();
    }

    @SuppressWarnings("unchecked")
    protected RecordCheckData handleCustomValidate(boolean check, List<String> reasonError, Map<String, Object> tempData, List<Map<String, Object>> dataValid, List<ImportFileConfigDetail> importFileConfigDetails) {
        String form = (String) tempData.get("form");
        String code = (String) tempData.get("code");

        List<Organization> organizations = (List<Organization>) initCheckData;

        if(organizationReferenceList.isEmpty()){
            organizationReferenceList = organizationReferenceService.findAll();
        }

        if (codes.isEmpty() && !organizations.isEmpty()) {
            codes = organizations.stream().map(Organization::getCode).toList();
        }

        if (codes.contains(code)){
            check = false;
            reasonError.add(String.format("Giá trị <Mã tổ chức Đảng> <%s> đã tồn tại", code));
        }

        boolean isForm = false;
        if(organizationReferenceList.isEmpty()){
            return new RecordCheckData(check, reasonError);
        }

        for (OrganizationReference organizationReference : organizationReferenceList) {
            if (organizationReference.getName().equals(form.trim())){
                isForm = true;
            }
        }

        if (!isForm) {
            check = false;
            reasonError.add(String.format("Giá trị <Hình thức> <%s> không hợp lệ", form));
        }

        return new RecordCheckData(check, reasonError);
    }

    @SuppressWarnings("unchecked")
    protected BaseResponse<T> handleSaveDataUpload(BaseResponse response) {
        List<Map<String, Object>> dataImport = (List<Map<String, Object>>) response.getData();
        List<OrganizationDraft> organizations = new ArrayList<>();
        UserDetailsImpl userRequested = getUserRequested();
        List<Request> requests = new ArrayList<>();

        if(organizationReferenceList.isEmpty()){
            organizationReferenceList = organizationReferenceService.findAll();
        }

        Map<String, OrganizationReference> organizationReferenceMap = new HashMap<>();
        if (!organizationReferenceList.isEmpty()){
            for (OrganizationReference organizationReference : organizationReferenceList) {
                organizationReferenceMap.put(organizationReference.getName(), organizationReference);
            }
        }

        for(Map<String, Object> dataItem : dataImport){
            OrganizationDraft organizationDraft = new OrganizationDraft();

            String code = dataItem.get("code").toString();
            if (code.length() == 4){
                organizationDraft.setCode(code);
                organizationDraft.setParentCode(code);
            }else if (code.length() == 6){
                code = code.substring(0, 4);
                organizationDraft.setCode(code);
                organizationDraft.setParentCode(code);
            }else {
                code = code.substring(0, 6);
                organizationDraft.setCode(code);
                organizationDraft.setParentCode(code);
            }

            String form = dataItem.get("form").toString();
            OrganizationReference organizationReference = organizationReferenceMap.getOrDefault(form, null);

            if (Objects.nonNull(organizationReference)){
                organizationDraft.setForm(organizationReference.getCode());
            }

            if (Objects.nonNull(dataItem.get("resolutionNumber"))){
                organizationDraft.setResolutionNumber(dataItem.get("resolutionNumber").toString());
            }

            if (Objects.nonNull(dataItem.get("resolutionDate"))){
                organizationDraft.setResolutionDate(CommonUtils.convertStringToDate(dataItem.get("resolutionDate").toString()));
            }

            if (Objects.nonNull(dataItem.get("establishmentDecisionNumber"))){
                organizationDraft.setEstablishmentDecisionNumber(dataItem.get("establishmentDecisionNumber").toString());
            }

            if (Objects.nonNull(dataItem.get("decisionDate"))){
                organizationDraft.setDecisionDate(CommonUtils.convertStringToDate(dataItem.get("decisionDate").toString()));
            }

            if (Objects.nonNull(dataItem.get("effectiveDate"))){
                organizationDraft.setEffectiveDate(CommonUtils.convertStringToDate(dataItem.get("effectiveDate").toString()));
            }

            if (Objects.nonNull(dataItem.get("name"))){
                organizationDraft.setName(dataItem.get("name").toString());
            }

            organizationDraft.setApprove(EApprovalStatus.PENDING.getId());
            organizationDraft.setCreatedBy(userRequested.getId());
            organizationDraft.setStatus(EOrganizationStatus.YES.getStatus());
            organizations.add(organizationDraft);
        }

        if (!organizations.isEmpty()){
            organizations = organizationDraftService.saveAll(organizations);
        }

        for (OrganizationDraft organizationDraft : organizations) {
            Request request = requestService.initializeRequest(organizationDraft, null, form, OrganizationDraft.FIELD_MAP);
            request.setCreatedBy(userRequested.getId());

            request.setOrganizationCode(organizationDraft.getCode());
            request.setReferenceId(organizationDraft.getId());
            requests.add(request);
        }

        if (!requests.isEmpty()){
            requestService.saveAll(requests);
        }

        response.setData(null);
        response.setSuccess(Boolean.TRUE);
        response.setMessage("Import danh sách chi Đảng bộ thành công");
        return response;
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
