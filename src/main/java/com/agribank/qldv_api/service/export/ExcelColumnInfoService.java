package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.gateway.ExcelColumnInfoClient;
import com.agribank.qldv_api.request.excel_column_info.ExcelColumnInfoRequest;
import com.agribank.qldv_api.response.excel_column_info.ExcelColumnInfoResponse;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelColumnInfoService {
    private final ExcelColumnInfoClient client;
    private final ModelMapper modelMapper;

    public List<ExcelColumnInfo> getColumnInfos(String code) {
        List<ExcelColumnInfo> columnInfos = client.findByCode(code).getData();
        if (columnInfos.isEmpty()){
            throw new CommonException("Không tìm thấy column mặc định");
        }

        return columnInfos;
    }

    public List<ExcelColumnInfoResponse> getColumn(String code) {
        return getColumnInfos(code).stream().map(e -> modelMapper.map(e, ExcelColumnInfoResponse.class)).collect(Collectors.toList());
    }

    public String updateShowExcelColumnInfo(List<ExcelColumnInfoRequest> requests) {
        List<String> ids = new ArrayList<>();
        Map<String, ExcelColumnInfoRequest> excelColumnInfoRequestMap = new HashMap<>();
        for (ExcelColumnInfoRequest request : requests) {
            if (Objects.isNull(request.getId()) || request.getId().isBlank()) {
                throw new CommonException("Kiểm tra lại dữ liệu id cột");
            }

            if (Objects.isNull(request.getIsShow())) {
                throw new CommonException("Kiểm tra lại dữ liệu isShow");
            }

            ids.add(request.getId());

            excelColumnInfoRequestMap.put(request.getId(), request);
        }

        List<ExcelColumnInfo> excelColumnInfos = client.findAllById(ids).getData();
        for (ExcelColumnInfo excelColumnInfo : excelColumnInfos) {
            ExcelColumnInfoRequest excelColumnInfoRequest = excelColumnInfoRequestMap.getOrDefault(
                    excelColumnInfo.getId(),
                    null);

            if (Objects.nonNull(excelColumnInfoRequest)) {
                excelColumnInfo.setIsShow(excelColumnInfoRequest.getIsShow());
            }
        }

        client.saveAll(excelColumnInfos);

        return "Cập nhật thành công!";
    }
}
