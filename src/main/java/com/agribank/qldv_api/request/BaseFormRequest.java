package com.agribank.qldv_api.request;

import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;

@Data
public class BaseFormRequest {
    //("cap quyet dinh")
    @NotNull(message = "Cấp ủy quyết định không được để trống")
    @NotBlank(message = "Cấp ủy quyết định không được để trống")
    String decisionCommittee;
    //("so ket luan/nghi quyet")
    @NotNull(message = "Số kết luận/nghị quyết không được để trống")
    @NotBlank(message = "Số kết luận/nghị quyết không được để trống")
    String conclusionNumber;
    //("ngay ket luan/nghi quyet")
    @NotNull(message = "Ngày kết luận/nghị quyết không được để trống")
    Date conclusionDate;
    @NotNull(message = "Số quyết định không được để trống")
    @NotBlank(message = "Số quyết định không được để trống")
    //("so quyet dinh")
    String decisionNumber;
    @NotNull(message = "Ngày quyết định không được để trống")
    //("ngay quyet dinh")
    Date decisionDate;
    @NotNull(message = "Ngày hiệu lực không được để trống")
    //("ngay hieu luc")
    Date effectiveDate;

    public void validate() {
        if (!CommonUtils.validateDatesAfter(decisionDate, effectiveDate)){
            throw new CommonException("Ngày hiệu lực phải sau ngày quyết đinh");
        }
    }
}
