package com.agribank.qldv_api.response;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseFormDto {
    String decisionCommittee;
    @NotNull(message = "không được để trống số kết luận")
    String conclusionNumber;
    @NotNull(message = "không được để trống ngày kết luận")
    Date conclusionDate;
    @NotNull(message = "không được để trống số quyêt định")
    String decisionNumber;
    @NotNull(message = "không được để trống ngày quyêt định")
    Date decisionDate;
    @NotNull
    @NotNull(message = "không được để trống ngày hiệu định")
    Date effectiveDate;
}
