package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.request.excel_column_info.ExcelColumnInfoRequest;
import com.agribank.qldv_api.response.excel_column_info.ExcelColumnInfoResponse;
import com.agribank.qldv_api.service.export.ExcelColumnInfoService;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/excel-column-info")
public class ExcelColumnInfoController {
    private final ExcelColumnInfoService service;

    @GetMapping("/get-column")
    public ResponseEntity<BaseResponse<List<ExcelColumnInfoResponse>>> getColumn(@RequestParam(name = "code") String code) {
        return BaseResponse.success(service.getColumn(code));
    }

    @PutMapping("/status-column")
    public ResponseEntity<BaseResponse<String>> getColumn(@RequestBody List<ExcelColumnInfoRequest> request) {
        return BaseResponse.success(service.updateShowExcelColumnInfo(request));
    }
}
