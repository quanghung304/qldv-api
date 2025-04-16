package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.ApiLogClient;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.ApiLog;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApiLogBaseService {
    private final ApiLogClient apiLogClient;
    private final UserService userService;

    protected ApiLog initApiLog(){
        UserDto userRequested = userService.getUserRequested();
        return ApiLog.builder()
                .username(userRequested.getUsername())
                .email(userRequested.getEmail())
                .brcd(userRequested.getBrcd())
                .build();
    }

    public void save(ApiLog apiLog){
        apiLogClient.save(apiLog);
    }

    public List<Field> handleGetPropertyChange(Object newObject, Object oldObject, Class<?> objectClass, List<String> propertyIgnore) {
        if(Objects.isNull(newObject) || Objects.isNull(oldObject)){
            return List.of();
        }

        List<Field> propertyChange = new ArrayList<>();
        Field[] fields = objectClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // Enable access to private fields
            String fieldName = field.getName();

            if(propertyIgnore.contains(fieldName)){
                continue;
            }

            try {
                Gson gson = new Gson();
                String fieldNewValue = gson.toJson(field.get(newObject));
                String fieldOldValue = gson.toJson(field.get(oldObject));

                if(!fieldNewValue.equals(fieldOldValue)){
                    propertyChange.add(field);
                }
            } catch (IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        }
        return propertyChange;
    }

    public String handleBuildContent(Object newObject, Object oldObject, Class<?> objectClass, List<Field> propertyChange, List<String> propertyCustom){
        StringBuilder content = new StringBuilder();
        for (Field field : propertyChange) {
            field.setAccessible(true); // Enable access to private fields
            String fieldName = field.getName();
            //Neu la truong dac biet thi xu ly rieng
            if(propertyCustom.contains(fieldName)){
                content.append("\n");
                continue;
            }

            //Con lai thi lay 2 gia tri trc va sau de generate noi dung
            String newValueStr = "";
            String oldValueStr = "";
            try{
                Object newValue = field.get(newObject);
                Object oldValue = field.get(oldObject);
                if(newValue != null){
                    newValueStr = newValue.toString();
                }
                if(oldValue != null){
                    oldValueStr = oldValue.toString();
                }
            }
            catch (IllegalAccessException e){
                newValueStr = "";
                oldValueStr = "";
            }
            content.append(handleGenerateChangeContent(fieldName, newValueStr, oldValueStr));
            content.append("\n");
        }
        return content.toString();
    }

    public String handleGenerateChangeContent(String fieldName, String newValue, String oldValue) {
        String content = "%s: Từ <%s> thành <%s>";
        return String.format(content, fieldName, newValue, oldValue);
    }
}
