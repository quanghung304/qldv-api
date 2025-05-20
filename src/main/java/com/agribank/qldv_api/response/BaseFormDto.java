package com.agribank.qldv_api.response;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@Data
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseFormDto {
    String decisionCommittee;
    String conclusionNumber;
    Date conclusionDate;
    String decisionNumber;
    Date decisionDate;
    Date effectiveDate;
}
