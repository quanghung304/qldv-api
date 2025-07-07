package com.agribank.qldv_api.utils;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class PDFHelper {

    private static final Logger logger = LoggerFactory.getLogger("PDFHelper");

    private record RecordInitRequest(File xmlFile, File xsltFile, RestTemplate restTemplate, MultiValueMap<String, Object> body, HttpHeaders headers) {
    }

    @NotNull
    private static RecordInitRequest getRecordInitRequest(String xsltTemplate, String xmlTemplate, String fileName) throws IOException {
        File xmlFile = File.createTempFile(fileName, ".xml");
        boolean result = xmlFile.setExecutable(true);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile))) {
            writer.write(xmlTemplate);
        }

        File xsltFile = File.createTempFile(fileName, ".xslt");
        boolean result1 = xsltFile.setExecutable(true);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xsltFile))) {
            writer.write(xsltTemplate);
        }

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("xmlFile", new FileSystemResource(xmlFile));
        body.add("xsltFile", new FileSystemResource(xsltFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new RecordInitRequest(xmlFile, xsltFile, restTemplate, body, headers);
    }

    public static byte[] handleConvertPdfReturnFile(String xsltTemplate, String xmlTemplate, String fileName, String pdfServiceUrl) {
        try {
            RecordInitRequest recordInitRequest = getRecordInitRequest(xsltTemplate, xmlTemplate, fileName);
            ResponseEntity<byte[]> response = recordInitRequest.restTemplate().postForEntity(
                    pdfServiceUrl + "/transform/pdf/file?fileName=" + fileName,
                    new HttpEntity<>(recordInitRequest.body(), recordInitRequest.headers()),
                    byte[].class
            );
            //Xóa temporary file sau khi sử dụng
            recordInitRequest.xmlFile().deleteOnExit();
            recordInitRequest.xsltFile().deleteOnExit();
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return null;
        }
        catch (Exception exception){
            System.out.println(Arrays.toString(exception.getStackTrace()));
            logger.error(exception.getMessage());
        }
        return null;
    }
}
