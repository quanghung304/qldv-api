package com.agribank.qldv_api.response.apiLog;

import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldvutils.response.PageResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSearchIamResponse extends PageResponse {
    List<UserIamResponse> results;
}
