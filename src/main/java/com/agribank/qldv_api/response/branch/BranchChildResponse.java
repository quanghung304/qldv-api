package com.agribank.qldv_api.response.branch;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BranchChildResponse extends BranchResponse {
    List<BranchResponse> branchChild;
}
