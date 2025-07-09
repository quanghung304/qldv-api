package com.agribank.qldv_api.service.import_file;

import com.agribank.qldv_api.enums.EExcelImport;
import com.agribank.qldv_api.request.ColumnValidate;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.import_file.ImportFileConfig;
import com.agribank.qldvutils.entity.import_file.ImportFileConfigDetail;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.BaseResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;


@Service
@RequiredArgsConstructor
public class ImportFileService {
    private final ImportFileConfigService importFileConfigService;
    private final ImportFileConfigDetailService importFileConfigDetailService;

    protected Object initCheckData;

    private boolean handleCheckFileSize(MultipartFile file){
        if (file.isEmpty()) {
            return true;
        }

        long MAX_FILE_SIZE = 4 * 1024 * 1024;
        return file.getSize() > MAX_FILE_SIZE;
    }

    public record RecordUploadData(List<Map<String, Object>> dataImport, List<Map<String, Object>> dataError) {
    }

    public record RecordCheckData(boolean valid, List<String> reason) {
    }

    public BaseResponse handleReadFileUpload(MultipartFile file, String code) throws IOException {
        if(handleCheckFileSize(file)){
            throw new CommonException("OutOfSize");
        }

        ImportFileConfig importFileConfig = importFileConfigService.findByCode(code);
        if(Objects.isNull(importFileConfig)){
            throw new CommonException("TemplateNotFound");
        }

        List<ImportFileConfigDetail> importFileConfigDetails = importFileConfigDetailService.findByRefId(importFileConfig.getId());
        if(importFileConfigDetails == null || importFileConfigDetails.isEmpty()){
            throw new CommonException("TemplateNotFound");
        }

        ZipSecureFile.setMinInflateRatio(0.00007);
        InputStream is = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        Map<Integer, ImportFileConfigDetail> headers = handleValidateHeader(sheet, importFileConfig, importFileConfigDetails);
        if(headers == null){
            throw new CommonException("InvalidHeader");
        }
        handleInitCheckData();
        RecordUploadData uploadData = handleReadData(sheet, importFileConfig, importFileConfigDetails, headers);
        initCheckData = null;
        if(Objects.nonNull(uploadData.dataError) && !uploadData.dataError.isEmpty()){
            throw new CommonException(uploadData.dataError.toString());
        }

        BaseResponse response = new BaseResponse();
        response.setData(uploadData.dataImport);

        return handleSaveDataUpload(response);
    }

    protected BaseResponse handleSaveDataUpload(BaseResponse response) {
        return response;
    }

    protected void handleInitCheckData() {
        initCheckData = null;
    }

    protected Map<Integer, ImportFileConfigDetail> handleValidateHeader(Sheet sheet, ImportFileConfig importFileConfig, List<ImportFileConfigDetail> uploadFileConfigDetails) {
        Map<Integer, ImportFileConfigDetail> headers = null;
        int header = importFileConfig.getHeaderIndex();
        Row row = sheet.getRow(header);
        short lastIndex = row.getLastCellNum();
        for(short i=0; i<lastIndex; i++){
            Cell cell = row.getCell(i);
            try{
                String text = cell.getStringCellValue();
                if(isNullOrEmpty(text)){
                    continue;
                }
                //Loai bo dau * danh dau bat buoc nhap di
                text = text.replace("(*)", "").replace("*", "").strip();
                String finalText = text;
                //Lay cot giong voi file nhap vao
                ImportFileConfigDetail headerItem = uploadFileConfigDetails.stream().filter(e ->
                                Arrays.stream((e.getMapping().split(","))).toList().contains(finalText))
                        .findFirst().orElse(null);
                if(headerItem == null){
                    continue;
                }
                if(headers == null){
                    headers = new HashMap<>();
                }
                headers.put((int) i, headerItem);
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
                break;
            }

        }
        return headers;
    }

    /**
     * Doc du lieu tu file mapping ra object
     * tqha(10/3/2025)
     */
    protected RecordUploadData handleReadData(Sheet sheet, ImportFileConfig uploadFileConfig, List<ImportFileConfigDetail> uploadFileConfigDetails, Map<Integer, ImportFileConfigDetail> headers) {
        int startIndex = uploadFileConfig.getSubHeaderIndex() != null ? uploadFileConfig.getSubHeaderIndex() + 1 : uploadFileConfig.getHeaderIndex() + 1;
        List<Map<String, Object>> dataValid = new ArrayList<>();
        List<Map<String, Object>> dataInvalid = new ArrayList<>();

        int lastIndexRow = sheet.getLastRowNum();
        int totalColumn = sheet.getRow(uploadFileConfig.getHeaderIndex()).getLastCellNum();
        boolean endLoop = false;
        for(int i=startIndex; i<lastIndexRow; i++){
            Row row = sheet.getRow(i);
            Map<String, Object> tempData = new HashMap<>();
            if (Objects.isNull(row)){
                continue;
            }
            for(int j=0; j<totalColumn; j++){
                if(!headers.containsKey(j)){
                    continue;
                }
                //Doc du lieu tu cell
                Cell cell = row.getCell(j);
                ImportFileConfigDetail headerConfig = headers.get(j);
                if(Objects.equals(headerConfig.getField(), "order") && (Objects.isNull(cell) || CellType.BLANK == cell.getCellType())){
                    endLoop = true;
                    break;
                }
                if(Objects.isNull(cell)){
                    continue;
                }
                String dataType = headerConfig.getDataType();
                Object cellData = handleGetCellData(cell, dataType);
                tempData.put(headerConfig.getField(), cellData);
            }
            if(endLoop){
                break;
            }
            //Validate du lieu
            RecordCheckData check = handleValidateData(tempData, dataValid, uploadFileConfigDetails);
            if(check.valid()){
                dataValid.add(tempData);
            }
            else{
                Map<String, Object> tempInvalid = new HashMap<>();
                tempInvalid.put("order", i);
                tempInvalid.put("reason", String.join("; ", check.reason()));
                dataInvalid.add(tempInvalid);
            }
        }
        return new RecordUploadData(dataValid, dataInvalid);
    }

    protected RecordCheckData handleValidateData(Map<String, Object> tempData, List<Map<String, Object>> dataValid, List<ImportFileConfigDetail> importFileConfigDetails) {
        //validate require du lieu
        List<String> reasonError = new ArrayList<>();
        boolean check = true;
        Gson gson = new Gson();
        for(ImportFileConfigDetail headerItem: importFileConfigDetails){
            if(Objects.nonNull(headerItem.getRequired()) && headerItem.getRequired() == 1 && !tempData.containsKey(headerItem.getField())){
                check = false;
                reasonError.add(String.format("<%s> không được để trống", headerItem.getName()));
                continue;
            }
            if(tempData.get(headerItem.getField()) == null){
                continue;
            }
            if(!isNullOrEmpty(headerItem.getRegex())){
                String dataCheck = (String) tempData.get(headerItem.getField());
                Pattern pattern = Pattern.compile(headerItem.getRegex());
                Matcher matcher = pattern.matcher(dataCheck);
                if (!matcher.matches()) {
                    check = false;
                    reasonError.add(String.format("<%s> không đúng định dạng", headerItem.getName()));
                }
            }
            ColumnValidate columnValidate = gson.fromJson(headerItem.getValidate(), ColumnValidate.class);
            if(columnValidate == null){
                continue;
            }
            if(Objects.equals(headerItem.getDataType(), "String") && tempData.get(headerItem.getField()).toString().length() > columnValidate.getMaxLength()){
                check = false;
                reasonError.add(String.format("<%s> vượt quá <%s> ký tự", headerItem.getName(), columnValidate.getMaxLength()));
            }
            else if(Objects.equals(headerItem.getDataType(), "Number")){
                Integer dataNumber = (Integer) tempData.get(headerItem.getField());
                if(Objects.nonNull(columnValidate.getMinValue()) && dataNumber < columnValidate.getMinValue()){
                    check = false;
                    reasonError.add(String.format("<%s> không được nhỏ hơn <%s>", headerItem.getName(), columnValidate.getMinValue()));
                }
                else if(columnValidate.getMaxValue() != null && dataNumber > columnValidate.getMaxValue()){
                    check = false;
                    reasonError.add(String.format("<%s> không được lớn hơn <%s>", headerItem.getName(), columnValidate.getMaxValue()));
                }
            }
            else if(Objects.equals(headerItem.getDataType(), "Date")){
                check = handleValidateDate(tempData, headerItem, columnValidate, check, reasonError);
            }
        }
        return handleCustomValidate(check, reasonError, tempData, dataValid, importFileConfigDetails);
    }

    private boolean handleValidateDate(Map<String, Object> tempData, ImportFileConfigDetail headerItem, ColumnValidate columnValidate, boolean check, List<String> reasonError) {
        Date dataDate = (Date) tempData.get(headerItem.getField());
        LocalDate dataDateLocal = CommonUtils.convertDateIntoLocalDate(dataDate);
        SimpleDateFormat strFormat = new SimpleDateFormat("yyyyMMdd");
        if(Objects.nonNull(columnValidate.getFromDate())){
            try {
                Date date = strFormat.parse(columnValidate.getFromDate());
                LocalDate dateLocal = CommonUtils.convertDateIntoLocalDate(date);
                if(dataDateLocal.isBefore(dateLocal)){
                    check = false;
                    reasonError.add(String.format("<%s> không được nhỏ hơn <%s>", headerItem.getName(), columnValidate.getFromDate()));
                }
            }
            catch (Exception ignored){
            }
        }
        if(Objects.nonNull(columnValidate.getToDate())){
            try {
                Date date = strFormat.parse(columnValidate.getToDate());
                LocalDate dateLocal = CommonUtils.convertDateIntoLocalDate(date);
                if(dataDateLocal.isAfter(dateLocal)){
                    check = false;
                    reasonError.add(String.format("<%s> không được lớn hơn <%s>", headerItem.getName(), columnValidate.getToDate()));
                }
            }
            catch (Exception ignored){
            }
        }
        return check;
    }

    protected RecordCheckData handleCustomValidate(boolean check, List<String> reasonError, Map<String, Object> tempData, List<Map<String, Object>> dataValid, List<ImportFileConfigDetail> uploadFileConfigDetails) {
        return new RecordCheckData(check, reasonError);
    }

    protected Object handleGetCellData(Cell cell, String dataType) {
        return switch (dataType) {
            case "String" -> {
                String value;
                try {
                    value = cell.getStringCellValue();
                }
                catch (Exception exception){
                    value = ((XSSFCell) cell).getRawValue();
                }
                yield value;
            }
            case "Number" -> {
                String rawData = ((XSSFCell) cell).getRawValue();
                yield Integer.parseInt(rawData);
            }
            case "Date" -> cell.getDateCellValue();
            default -> "";
        };
    }



    public Resource getFileTemplate(String type) throws IOException {
        String fileName;
        if(type.equals(EExcelImport.USERS.name())) {
            fileName = "Template_Upload_User";
        } else if(type.equals(EExcelImport.BIEU_1.name())) {
            fileName = "Template_Upload_Organization";
        } else if(type.equals(EExcelImport.BIEU_12.name())) {
            fileName = "Template_Upload_Develop_Plan";
        } else if (type.equals(EExcelImport.BIEU_15.name())) {
            fileName = "Template_Upload_DV";
        } else {
            return null;
        }
        ClassPathResource classPathResource = new ClassPathResource("static/templateImport/" + fileName + ".xlsx");
        InputStream inputStream = classPathResource.getInputStream();
        File certFile = File.createTempFile(fileName, ".xlsx");
        boolean result = certFile.setExecutable(true);
        FileUtils.copyInputStreamToFile(inputStream, certFile);
        certFile.deleteOnExit();
        return new FileSystemResource(certFile);
    }
}
