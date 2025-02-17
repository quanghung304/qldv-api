package com.agribank.qldv_api.request;

import lombok.Data;

import java.util.Objects;

@Data
public class BasePagingResquest {
    private Integer page;
    private Integer limit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasePagingResquest that)) return false;
        return Objects.equals(page, that.page) && Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, limit);
    }
}
