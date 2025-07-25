package com.agribank.qldv_api.request.form02;

import com.agribank.qldv_api.request.BaseFormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationRenameRequest extends BaseFormRequest {
    private String id;
    @NotNull(message = "Không để trống trường mã tổ chức đảng")
    private String organizationCode;

    @NotNull(message = "không được để trống trường tên tổ chức đảng")
    private String organizationName;

    private String oldOrganizationName;

}
