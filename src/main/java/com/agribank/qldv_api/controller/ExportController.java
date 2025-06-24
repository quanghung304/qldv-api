package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.service.export.ExportHandlerService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/export")
public class ExportController {
    private final ExportHandlerService exportHandlerService;

    @PostMapping("/excel")
    public void exportExcel(HttpServletResponse response, @RequestBody Object param, @RequestParam String type) throws IOException {
        try {
            ExportResponse exportResult = exportHandlerService.handleExport(param, type);
            if(Objects.isNull(exportResult)){
                throw new IOException();
            }
            if(Objects.nonNull(exportResult.getWorkbook()) ){
                Workbook workbook = exportResult.getWorkbook();
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", exportResult.getFileName()));

                workbook.write(response.getOutputStream());
                workbook.close();
                response.getOutputStream().flush();
            }
            else{
                File file = exportResult.getFileZip();
                if (!file.exists() || file.isDirectory()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("application/zip");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", file.getName()));
                response.setContentLengthLong(file.length());

                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                     BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
                response.getOutputStream().flush();
            }
        }catch (Exception exception){
            throw new IOException();
        }
    }
}
