package com.agribank.qldv_api.request.casemgmt;

import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CaseSearchRequest extends PagingRequest {
    String keyword;
    List<String> caseTypeId;
    List<String> statusId;
    Integer authorityLevel;
    LocalDate createdFrom;
    LocalDate createdTo;
    LocalDate completedFrom;
    LocalDate completedTo;

    @Override
    public void validate() {
        if (createdFrom != null && createdTo != null && createdTo.isBefore(createdFrom)) {
            throw new ValidationException("created_to phải >= created_from");
        }
        if (completedFrom != null && completedTo != null && completedTo.isBefore(completedFrom)) {
            throw new ValidationException("completed_to phải >= completed_from");
        }
    }
}
