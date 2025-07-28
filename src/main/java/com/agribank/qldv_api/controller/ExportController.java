package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.service.export.ExportHandlerService;
import com.agribank.qldv_api.service.import_file.ImportFileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/export")
public class ExportController {
    private final ExportHandlerService exportHandlerService;

    private final ImportFileService importFileService;

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
            } else {
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

                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
                response.getOutputStream().flush();
            }
        } catch (Exception exception) {
            throw new IOException();
        }
    }

    @PostMapping("/pdf")
    public void exportPDF(HttpServletResponse response, @RequestBody Object param, @RequestParam String type) throws IOException{
        PDFContentResult serviceResult = exportHandlerService.handleExportPDF(param, type);
        if(serviceResult.getPdfContent() != null){
            if (serviceResult.getPdfContent().length == 0) {
                File file = new File(String.valueOf(serviceResult.getFileName()));
                if (!file.exists() || file.isDirectory()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                response.setContentLengthLong(file.length());

                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                     BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                    Thread thread = new Thread(() -> {
                        try {
                            if (file.delete()) {
                                System.out.println("File deleted successfully.");
                            } else {
                                System.out.println("Failed to delete the file.");
                            }
                        }
                        catch (Exception exception){
                            System.out.println("Delete error." + file.getAbsolutePath());
                        }
                    });
                    thread.start();
                }
            } else {
                response.setContentType(String.valueOf(MediaType.APPLICATION_PDF));
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", serviceResult.getFileName()));
                response.setContentLengthLong(serviceResult.getPdfContent().length);

                try (BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(serviceResult.getPdfContent()));
                     BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
            }
        } else {
            throw new IOException();
        }
    }

    @GetMapping("/download/template")
    public ResponseEntity<Resource> getFileTemplate(@RequestParam("fileStruct") @NotNull String fileStruct) throws IOException {
        Resource fileResource = importFileService.getFileTemplate(fileStruct);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileResource.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }
}
