package com.agribank.qldv_api.response.report26;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeceasedResponse {
    String id;
    Date dateOfDeath;
    String organizationCode;
    String staffCode;
    String decisionNumber;
    Date decisionDate;
    String usernameCreated;
    String usernameAccepted;
    String refId;
    Integer status;
}
