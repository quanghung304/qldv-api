package com.agribank.qldv_api.request.user;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchUserIAMRequest extends PagingRequest {
    String brcd;
    String name;
    String prntbrcd;

    @Override
    public void validate() {
        super.validate();

        if(Objects.isNull(brcd)){
            this.brcd = "";
        }

        if(Objects.isNull(name)){
            this.name = "";
        }

        if(Objects.isNull(prntbrcd)){
            this.prntbrcd = "";
        }
    }
}

