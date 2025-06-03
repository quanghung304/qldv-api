package com.agribank.qldv_api.utils;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.mis.encrypt.interfaces.ICreateService;
import org.mis.encrypt.interfaces.IMisEncrypt;
import org.mis.encrypt.services.CreateService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.sql.Timestamp;

public class CommonUtils {
    public static final Integer PAGE_SIZE_DEFAULT = 10;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    private static Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }

    public static Pageable getPageable(Integer page, Integer size, String sort){
        Pageable pagingSort;

        List<Sort.Order> orders = new ArrayList<>();

        int sizePage = CommonUtils.PAGE_SIZE_DEFAULT;
        if (Objects.nonNull(size)){
            sizePage = size;
        }

        if (Objects.isNull(sort)){
            orders.add(new Sort.Order(getSortDirection("desc"), "id"));
        }else {
            String[] _sort = sort.split(",");
            orders.add(new Sort.Order(getSortDirection(_sort[1].toLowerCase()), _sort[0]));
        }
        pagingSort = PageRequest.of(page, sizePage, Sort.by(orders));
        return pagingSort;
    }

    public static String splitUsername(String email){
        if (Objects.isNull(email) || email.contains(" ")) {
            return "";        }
        String[] parts = email.split("@");
        return parts[0];
    }

    public static String getAccessToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        String token = header.split(" ")[1];
        return token;
    }

    public static String handleEncryptPassword(String password, String publicKeyPath) {
        try {
            String publicKey = "";
            if(publicKeyPath.contains("public")){
                ClassPathResource classPathResource = new ClassPathResource(publicKeyPath);
                InputStream inputStream = classPathResource.getInputStream();
                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                publicKey = s.hasNext() ? s.next() : "";
            }
            else{
                publicKey = handleReadFile(publicKeyPath);
            }

            ICreateService createService = new CreateService();
            IMisEncrypt misEncrypt = createService.createMisEncrypt();
            return misEncrypt.handleEncryptPassword(publicKey, password);
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }

//    public String handleValidPassword(String passwordEncrypt){
//        String password = handleDecryptPassword(passwordEncrypt);
//        if(password == null){
//            return null;
//        }
//
//        if(password.length() > 15 || password.length() < 6){
//            return "InvalidSize";
//        }
//
//        String regexPass = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&{}])[A-Za-z\\d@$!%*?&{}]{6,}$";
//        Pattern pattern = Pattern.compile(regexPass);
//
//        Matcher matcher = pattern.matcher(password);
//        if(!matcher.matches()){
//            return "InvalidStruct";
//        }
//
//        return password;
//    }

    public static String handleReadFile(String filePath) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return content.toString();
    }

    public static Object handleCloneObject(Object sourceObject){
        Gson gson = new Gson();
        String objStr = gson.toJson(sourceObject);
        return gson.fromJson(objStr, sourceObject.getClass());
    }

    public static Timestamp timestampConvert(String dateStr){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = sdf.parse(dateStr);
            return new Timestamp(date.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean validateDatesAfter(Date fromDate, Date toDate){
        LocalDate fromLocalDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toLocalDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (toLocalDate.isBefore(fromLocalDate)) {
            return false;
        }
        return true;
    }

    public static Map<String, Object> createFilteredDataMap(Object object, Map<String, String> fieldMap) {
        Map<String, Object> dataMap = new HashMap<>();
        BeanWrapper wrapper = new BeanWrapperImpl(object);

        // Iterate over fieldMap keys (entity fields)
        for (String fieldName : fieldMap.keySet()) {
            if (wrapper.isReadableProperty(fieldName)) {
                Object value = wrapper.getPropertyValue(fieldName);
                // Use user-friendly name from fieldMap as key
                if (value instanceof java.sql.Date) {
                    dataMap.put(fieldMap.get(fieldName), DATE_FORMATTER.format((java.sql.Date) value));
                } else {
                    dataMap.put(fieldMap.get(fieldName), value);
                }
            }
        }

        return dataMap;
    }
}
