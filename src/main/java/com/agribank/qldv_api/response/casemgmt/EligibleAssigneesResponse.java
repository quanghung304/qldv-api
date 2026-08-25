package com.agribank.qldv_api.response.casemgmt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/** GET /cases/{caseId}/eligible-assignees — message chỉ có giá trị khi assignees rỗng (bước không cần gán tiếp theo). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EligibleAssigneesResponse {
    List<EligibleAssigneeResponse> assignees;
    String message;
}
