package com.agribank.qldv_api.request.remove_name_party;

import com.agribank.qldv_api.request.leave_party.LeavePartyRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;


@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RemoveNamePartyRequest extends LeavePartyRequest {

    @Override
    public void validate() {
    }
}
