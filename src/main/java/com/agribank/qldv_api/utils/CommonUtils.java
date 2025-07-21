package com.agribank.qldv_api.utils;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldvutils.exception.CommonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.Workbook;
import org.mis.encrypt.interfaces.ICreateService;
import org.mis.encrypt.interfaces.IMisEncrypt;
import org.mis.encrypt.services.CreateService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Timestamp;
import java.util.regex.Pattern;



public class CommonUtils {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
    private static Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
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
        Map<String, Object> dataMap = new LinkedHashMap<>();
        BeanWrapper wrapper = new BeanWrapperImpl(object);

        // Iterate over fieldMap keys (entity fields)
        for (String fieldName : fieldMap.keySet()) {
            if (wrapper.isReadableProperty(fieldName)) {
                Object value = wrapper.getPropertyValue(fieldName);
                // Use user-friendly name from fieldMap as key
                if (value instanceof Date) {
                    dataMap.put(fieldMap.get(fieldName), DATE_FORMATTER.format((Date) value));
                } else if (value instanceof java.sql.Date) {
                    dataMap.put(fieldMap.get(fieldName), DATE_FORMATTER.format((java.sql.Date) value));
                } else {
                    dataMap.put(fieldMap.get(fieldName), value);
                }
            }
        }

        return dataMap;
    }

    public static String removeVietnameseDiacritics(String text) {
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(normalizedText).replaceAll("");
        result = result.replaceAll("Đ", "D");
        result = result.replaceAll(",", "");
        return result;
    }

    public static FileInputStream convertWorkbookToStream(Workbook workbook, String fileName){
        try {
            // Create a temporary file
            File tempFile = File.createTempFile(fileName, ".xlsx");
            boolean result = tempFile.setExecutable(true);

            // Write the workbook to the temporary file
            FileOutputStream fos = new FileOutputStream(tempFile);
            workbook.write(fos);
            fos.close();

            // Convert the temporary file to a MultipartFile
            return new FileInputStream(tempFile);
        }
        catch (Exception ignored){
        }
        return null;
    }

    public static String getCurrentDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate today = LocalDate.now();

        return today.format(formatter);
    }


    public static LocalDate convertDateIntoLocalDate(Date date){
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date convertStringToDate(String dateStr){
        if (Objects.isNull(dateStr)){
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return formatter.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    public static String dateToString(Date date){
        if (Objects.isNull(date)){
            return null;
        }

        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("Asia/Bangkok"));
            return zonedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }catch (Exception e){
            return null;
        }

    }

    public static Boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty() || str.isBlank();
    }

    public static String removeAccents(String input) {
        if (isNullOrEmpty(input)) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("Đ", "D").replaceAll("đ", "d");
    }

    public static Date parseDateString(String dateStr) {
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.of("Asia/Bangkok"));
            return Date.from(zonedDateTime.toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    public static String replaceSpecialCharacter(String input){
        if(input == null){
            return null;
        }

        return input
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    public static Date addOneYears(Date date){
        if (Objects.isNull(date)){
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 1);

        return calendar.getTime();
    }
    public static String getOrganizationByRequestedUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (Objects.equals(userDetails.getOrganizationCode(), Constants.BTCDU_CODE)) {
            return null;
        }

        return userDetails.getOrganizationCode().substring(0, Constants.FORM_B_NAME_LENGTH);
    }

    public static String createJsonData(Object object, List<?> organizationMergeDetails, Map<String, String> detailFieldMap, Map<String, String> getCombinedFieldMap) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> draftDataMap = CommonUtils.createFilteredDataMap(object, getCombinedFieldMap);
            int i = 1;

            for (Object mergedOrganization : organizationMergeDetails) {
                BeanWrapper wrapper = new BeanWrapperImpl(mergedOrganization);

                // Iterate over fieldMap keys (entity fields)
                for (String fieldName : detailFieldMap.keySet()) {
                    if (wrapper.isReadableProperty(fieldName)) {
                        Object value = wrapper.getPropertyValue(fieldName);
                        draftDataMap.put(detailFieldMap.get(fieldName) + " " + i, value);
                    }
                }
                i++;
            }

            return objectMapper.writeValueAsString(draftDataMap);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
}
