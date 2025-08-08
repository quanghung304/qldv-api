package com.agribank.qldv_api.request.layoutConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LayoutConfigItem {
    public String columnName;
    public String columnField;
    public String columnTitle;
    public String columnType;
    @Getter
    public int sortOrder;
    public String align;
    public Integer colSpan;
    public int width;
    public boolean isShow;
    public Integer rowSpan;
    public int rowOrder;
}
