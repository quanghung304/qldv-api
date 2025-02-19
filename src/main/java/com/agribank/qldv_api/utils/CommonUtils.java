package com.agribank.qldv_api.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
}
