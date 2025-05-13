package com.agribank.qldv_api.response.branch;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BranchResponse {
    Integer brcd;
    String engbrnm;
    String engbrshrtnm;
    String lclbrnm;
    String lclbrshrtnm;
    Integer prntbrcd;
}
