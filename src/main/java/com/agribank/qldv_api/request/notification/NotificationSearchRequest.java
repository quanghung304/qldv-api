package com.agribank.qldv_api.request.notification;

import com.agribank.qldvutils.request.PagingRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/** Body của POST /api/v1/notifications — chỉ page/pageSize/sort, userId lấy từ SecurityContext ở service, không phải field FE gửi lên. */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationSearchRequest extends PagingRequest {
}
