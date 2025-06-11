package com.agribank.qldv_api.request;

import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.sql.Date;

@Data
public class BaseFormRequest {
    //("cap quyet dinh")
    String decisionCommittee;
    //("so ket luan/nghi quyet")
    String conclusionNumber;
    //("ngay ket luan/nghi quyet")
    Date conclusionDate;
    //("so quyet dinh")
    String decisionNumber;
    //("ngay quyet dinh")
    Date decisionDate;
    //("ngay hieu luc")
    Date effectiveDate;
}
