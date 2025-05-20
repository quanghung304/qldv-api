package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EAction;
import com.agribank.qldv_api.enums.EApprovalStatus;
import com.agribank.qldv_api.enums.EStatus;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.handler.EntityHandlerRegistry;
import com.agribank.qldvutils.entity.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.sql.Delete;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestService {
    final ObjectMapper objectMapper;
    final RequestClient requestClient;
    private final EntityHandlerRegistry handlerRegistry;

    public Request initializeRequest(Object newObject, Object oldObject, Class<?> objectClass, Set<String> propertyIgnore) {
        Request request = new Request();

        // Configure Jackson to ignore specified properties
        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept(propertyIgnore);
        SimpleFilterProvider filters = new SimpleFilterProvider().addFilter("dynamicFilter", filter);
        ObjectMapper mapper = objectMapper.copy().setFilterProvider(filters);
        Integer action = EAction.UPDATE.getId();

        try {
            // Serialize newObject to newData (null for DELETE)
            if (newObject != null) {
                request.setNewData(mapper.writeValueAsString(newObject));
            } else {
                action = EAction.DELETE.getId();
            }

            // Serialize oldObject to oldData (null for CREATE)
            if (oldObject != null) {
                request.setOldData(mapper.writeValueAsString(oldObject));
            } else {
                action = EAction.INSERT.getId();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request data", e);
        }

        // Set default fields
        request.setAction(action);
        request.setStatus(EApprovalStatus.PENDING.getId());

        return request;
    }

    public List<Request> getList() {
        return requestClient.findAll().getData();
    }

    public Request getById(String id) {
        return requestClient.findById(id).getData().orElse(null);
    }


}
