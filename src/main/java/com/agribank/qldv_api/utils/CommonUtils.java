package com.agribank.qldv_api.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.mis.encrypt.interfaces.ICreateService;
import org.mis.encrypt.interfaces.IMisEncrypt;
import org.mis.encrypt.services.CreateService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    public static final Integer PAGE_SIZE_DEFAULT = 10;

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

}
