package com.agribank.qldv_api.response.ethnic;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EthnicResponse {
    String id;
    String name;
}
