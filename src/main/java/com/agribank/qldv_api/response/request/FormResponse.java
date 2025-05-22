package com.agribank.qldv_api.response.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormResponse {
    private String code;
    private String name;
}
