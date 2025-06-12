package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.service.import_file.ImportFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/import-file")
public class ImportFileController {
    private final ImportFileService importFileService;

    @GetMapping("/download/template")
    public ResponseEntity<Resource> getFileTemplate(@RequestParam("type") String type) throws IOException {
        Resource fileResource = importFileService.getFileTemplate(type);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileResource.getFilename());
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileResource);
    }
}
