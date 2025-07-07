package com.agribank.qldv_api.service.export;

import com.google.gson.Gson;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipHelper {
    @Value(value = "${zip.folder.path}")
    private String zipFolder;

    public String handleZipFile(List<String> filePaths, String fileExtension, String zipFileName){
        Gson gson = new Gson();
        if(!zipFolder.endsWith("/")){
            zipFolder += "/";
        }
        long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
        String nameResult = zipFileName + "_" + gson.toJson(randomNum) + ".zip";
        zipFileName = zipFolder + nameResult;
        try {
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (String filePath : filePaths) {
                File file = new File(filePath);
                List<String> fileName = List.of(filePath.split("/"));
                try (FileInputStream fis = new FileInputStream(file)) {
                    addToZip(fis, fileName.get(fileName.size() - 1), zipOut);
                } catch (Exception ex) {
                    System.err.println("File not found: " + ex.getMessage());
                }
            }
            zipOut.close();
        }
        catch (Exception exception){
            zipFileName = "";
        }

        return zipFileName;
    }

    public String handleZipFile(List<FileInputStream> fileInputStreams, List<String> fileInputNames, String zipFileName){
        Gson gson = new Gson();
        if(!zipFolder.endsWith("/")){
            zipFolder += "/";
        }
        long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
        String nameResult = zipFileName + "_" + gson.toJson(randomNum) + ".zip";
        zipFileName = zipFolder + nameResult;
        try {
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            for (int i=0; i<fileInputStreams.size(); i++){
                addToZip(fileInputStreams.get(i), fileInputNames.get(i), zipOut);
            }
            zipOut.close();
        }
        catch (Exception exception){
            zipFileName = "";
        }

        return zipFileName;
    }

    public String handleZipFileByte(List<byte[]> contents, List<String> filenames, String zipFileName) {
        Gson gson = new Gson();
        if(!zipFolder.endsWith("/")){
            zipFolder += "/";
        }
        long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
        String nameResult = zipFileName + "_" + gson.toJson(randomNum) + ".zip";
        zipFileName = zipFolder + nameResult;
        try {
            if (contents.size() != filenames.size()) {
                return "";
            }
            ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream(contents, filenames);
            byte[] zipBytes = byteArrayOutputStream.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(zipFileName)) {
                fos.write(zipBytes);
            }
        }
        catch (Exception exception){
            return "";
        }
        return zipFileName;
    }

    private static void addToZip(FileInputStream fis, String fileName, ZipOutputStream zipOut) throws IOException {
        // Create a new ZipEntry with the file name
        ZipEntry zipEntry = new ZipEntry(fileName);

        // Add the ZipEntry to the ZipOutputStream
        zipOut.putNextEntry(zipEntry);

        // Read the file and write its contents to the ZipOutputStream
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zipOut.write(buffer, 0, length);
        }

        // Complete the entry
        zipOut.closeEntry();
    }

    @NotNull
    private static ByteArrayOutputStream getByteArrayOutputStream(List<byte[]> contents, List<String> filenames) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
            for (int i = 0; i < contents.size(); i++) {
                byte[] fileData = contents.get(i);
                String filename = filenames.get(i);

                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);
                zos.write(fileData);
                zos.closeEntry();
            }
        }
        return byteArrayOutputStream;
    }
}
