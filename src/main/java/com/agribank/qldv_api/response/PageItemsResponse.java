package com.agribank.qldv_api.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageItemsResponse<T> {
    Integer totalPages;
    Integer currentPage;
    Long totalItems;
    List<T> items;
}
