package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.gateway.ExcelColumnInfoClient;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelColumnInfoService {
    private final ExcelColumnInfoClient client;

    public List<ExcelColumnInfo> getColumnInfos(String code) {
        List<ExcelColumnInfo> columnInfos = client.findByCode(code).getData();
        if (columnInfos.isEmpty()){
            throw new CommonException("Không tìm thấy column mặc định");
        }

        return columnInfos;
    }
}
