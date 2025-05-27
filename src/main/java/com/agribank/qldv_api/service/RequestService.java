package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EAction;
import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EForm;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.ApproveRequest;
import com.agribank.qldv_api.response.request.FormResponse;
import com.agribank.qldv_api.response.request.RequestDetailResponse;
import com.agribank.qldv_api.response.request.RequestResponse;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.handler.EntityHandlerRegistry;
import com.agribank.qldvutils.dto.RequestDto;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.FilterRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestService {
    @Autowired
     ObjectMapper objectMapper;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
     RequestClient requestClient;
    @Autowired
    @Lazy
    EntityHandlerRegistry handlerRegistry;

    public Request initializeRequest(Object newObject, Object oldObject, EForm form, Map<String, String> fieldMap) {
        Request request = new Request();
        Integer action = EAction.UPDATE.getId();

        try {
            // Serialize newObject to newData (null for DELETE)
            if (newObject != null) {
                Map<String, Object> newDataMap = createFilteredDataMap(newObject, fieldMap);
                request.setNewData(objectMapper.writeValueAsString(newDataMap));
            } else {
                action = EAction.DELETE.getId();
            }

            // Serialize oldObject to oldData (null for CREATE)
            if (oldObject != null) {
                Map<String, Object> oldDataMap = createFilteredDataMap(oldObject, fieldMap);
                request.setOldData(objectMapper.writeValueAsString(oldDataMap));
            } else {
                action = EAction.INSERT.getId();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request data", e);
        }

        // Set default fields
        request.setFormCode(form.getCode());
        request.setFormName(form.getName());
        request.setType(form.getType());
        request.setAction(action);
        request.setStatus(EApprovalStatus.PENDING.getId());

        return request;
    }

    private Map<String, Object> createFilteredDataMap(Object object, Map<String, String> fieldMap) {
        Map<String, Object> dataMap = new HashMap<>();
        BeanWrapper wrapper = new BeanWrapperImpl(object);

        // Iterate over fieldMap keys (entity fields)
        for (String fieldName : fieldMap.keySet()) {
            if (wrapper.isReadableProperty(fieldName)) {
                Object value = wrapper.getPropertyValue(fieldName);
                // Use user-friendly name from fieldMap as key
                dataMap.put(fieldMap.get(fieldName), value);
            }
        }

        return dataMap;
    }

    public PageResponse<RequestResponse> getList(FilterRequest filterRequest) {
        try {
            filterRequest = Objects.nonNull(filterRequest) ? filterRequest : new FilterRequest();
            PageResponse<RequestDto> requestDtoPageResponse = requestClient.getRequestList(filterRequest).getData();
            List<RequestDto> requestDtos = requestDtoPageResponse.getData();

            List<RequestResponse> responseList = new ArrayList<>();
            for (RequestDto dto : requestDtos) {
                Map<String, Object> oldDataMap = getDataObjectFromJson(dto.getOldData());
                Map<String, Object> newDataMap = getDataObjectFromJson(dto.getNewData());

                RequestResponse response = modelMapper.map(dto, RequestResponse.class);
                response.setOldData(oldDataMap);
                response.setNewData(newDataMap);

                responseList.add(response);
            }

            PageResponse<RequestResponse> response = new PageResponse<>();
            response.setTotalPages(requestDtoPageResponse.getTotalPages());
            response.setCurrentPage(requestDtoPageResponse.getCurrentPage());
            response.setTotalItems(requestDtoPageResponse.getTotalItems());
            response.setData(responseList);

            return response;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private String getOrganizationCode(String jsonData) throws Exception {
        Map<String, Object> oldDataMap = objectMapper.readValue(
                jsonData,
                new TypeReference<Map<String, Object>>() {}
        );

        return (String) oldDataMap.get("organizationCode");
    }

    public RequestDetailResponse getById(String id) {
        try {
            RequestDto request = requestClient.getDetail(id).getData();

            if (Objects.isNull(request)) {
                throw new CommonException("Không tìm thấy nội dung yêu cầu");
            }

            RequestDetailResponse response = modelMapper.map(request, RequestDetailResponse.class);

            Map<String, Object> oldDataMap = getDataObjectFromJson(request.getOldData());
            Map<String, Object> newDataMap = getDataObjectFromJson(request.getNewData());
            response.setOldData(oldDataMap);
            response.setNewData(newDataMap);

            // Handle null or empty oldData/newData
            if (request.getOldData() == null || request.getNewData() == null) {
                return response;
            }

            List<RequestDetailResponse.ChangedData> changedData = new ArrayList<>();

            // Get all keys from both maps
            Set<String> allKeys = new HashSet<>(oldDataMap.keySet());
            allKeys.addAll(newDataMap.keySet());

            // Compare fields and collect changes
            for (String key : allKeys) {
                Object oldValue = oldDataMap.get(key);
                Object newValue = newDataMap.get(key);

                if (!Objects.equals(oldValue, newValue)) {
                    changedData.add(new RequestDetailResponse.ChangedData(
                            key,
                            oldValue != null ? oldValue.toString() : null,
                            newValue != null ? newValue.toString() : null
                    ));
                }
            }

            response.setChangedData(changedData);
            return response;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object>  getDataObjectFromJson(String json) throws JsonProcessingException {
        return Objects.nonNull(json) ? objectMapper.readValue(
                json,
                new TypeReference<Map<String, Object>>() {}
        ) : null;
    }

    public List<FormResponse> getFormList() {
    List<FormResponse> responses = new ArrayList<>();

    for (EForm form: EForm.values()) {
        FormResponse formResponse = FormResponse.builder()
                .code(form.getCode())
                .name(form.getName())
                .build();
        responses.add(formResponse);
    }

    return responses;
    }

    public String approveRequest(List<ApproveRequest> approvalRequests) {
        List<String> requestIdList = new ArrayList<>();
        Map<String, ApproveRequest> requestStatusMap = new HashMap<>();
        for (ApproveRequest request: approvalRequests) {
            requestIdList.add(request.getId());
            requestStatusMap.put(request.getId(), request);
        }

        List<Request> requestList = requestClient.findAllById(requestIdList).getData();

        if (requestList.isEmpty()) {
            throw new CommonException("Không tìm thấy yêu cầu hợp lệ");
        }

        String formCode = requestList.get(0).getFormCode();
        for (Request request: requestList) {
            if (!Objects.equals(request.getFormCode(), formCode)) {
                throw new CommonException("Chỉ được duyệt các yêu cầu cùng loại");
            }
        }

        // Get handler based on formId
        EntityHandler handler = handlerRegistry.getHandler(formCode)
                .orElseThrow(() -> new IllegalArgumentException("No handler for formId"));

        Integer success = 0;

        for (Request request: requestList) {
            if (request.getStatus() != EApprovalStatus.PENDING.getId()) {
                continue;
            }

            UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            request.setApprovedBy(user.getId());
            request.setApprovedAt(new Date(System.currentTimeMillis()));

            ApproveRequest approveRequest = requestStatusMap.get(request.getId());
            if (Objects.equals(approveRequest.getStatus(), EApprovalStatus.DENIED.getId())) {
                request.setStatus(approveRequest.getStatus());
                request.setDeniedReason(approveRequest.getDeniedReason());

                handler.setDenied(request.getReferenceId());
                requestClient.save(request);
                success++;
                continue;
            }

            boolean result = false;
            try {
                // Apply changes based on action
                if (request.getOldData() == null) {
                    result = handler.applyCreate(request.getReferenceId(), user);
                } else if (request.getNewData() == null) {
                    result = handler.applyDelete(request.getReferenceId());
                } else {
                    result = handler.applyUpdate(request.getReferenceId());
                }
            } catch (Exception e) {
                continue;
            }

            if (!result) continue;

            // Update request
            request.setStatus(EApprovalStatus.APPROVED.getId());
            requestClient.save(request);
            success++;
        }

        return "Duyệt thành công " + success + " yêu cầu";
    }
}
