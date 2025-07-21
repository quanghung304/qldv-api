package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartyOrgTransferRequest extends BaseFormRequest {
    String id;
    @NotNull(message = "Chi Đảng bộ tiếp nhận không được để trống")
    @NotBlank(message = "Chi Đảng bộ tiếp nhận không được để trống")
    String receivingOrgCode;
    @NotNull(message = "Cấp ủy quyết định không được để trống")
    @NotBlank(message = "Cấp ủy quyết định không được để trống")
    String decisionCommittee;
    List<String> partyOrgTransfers;

    public void validate() {
        if (Objects.isNull(partyOrgTransfers) || partyOrgTransfers.isEmpty()) {
            throw new CommonException("Chưa chọn chi, đảng bộ được chuyển giao");
        }
    }
}
