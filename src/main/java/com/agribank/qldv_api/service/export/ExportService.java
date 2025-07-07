package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.export.ExportParam;
import com.agribank.qldv_api.response.export.ExportResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.ExcelColumnInfo;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.agribank.qldv_api.utils.CommonUtils.isNullOrEmpty;


@Service
@Primary
@RequiredArgsConstructor
public class ExportService {
    @Value("${app.max.rows.export}")
    private Integer MAX_ROWS_EXPORT;

    @Value("${app.max.rows.pdf.export}")
    private Integer MAX_ROWS_PDF_EXPORT;

    private final ExcelColumnInfoService excelColumnInfoService;
    private final ZipHelper zipHelper;
    private final ExportPDFReportService exportPDFReportService;

    protected record RecordColumnExport(List<ExcelColumnInfo> layoutConfigItems, List<ExcelColumnInfo> columnExport) {
    }

    public ExportResponse exportData(Object serviceParam, String title, String period, String layoutName, int numberTableHeaderRows, String titleMessage){
        ExportResponse exportResult = new ExportResponse();
        RecordColumnExport recordColumnExport = handleGetColumnExport(layoutName, numberTableHeaderRows);
        ExportParam exportParam = new ExportParam();
        exportParam.setColumnsExport(recordColumnExport.columnExport());
        exportParam.setTitle(title);
        exportParam.setPeriod(period);
        exportParam.setServiceParameter(serviceParam);
        exportParam.setNumberTableHeaderRows(numberTableHeaderRows);
        initBeforeExport();
        int totalRecords = handleGetTotalRecord(serviceParam);
        int totalPage = (int) Math.ceil((double) totalRecords / MAX_ROWS_EXPORT);
        Gson gson = new Gson();
        String fileName = CommonUtils.removeVietnameseDiacritics(title.toUpperCase());
        fileName = String.join("_", fileName.split(" "));
        if(totalPage == 1){
            int pageIndex = 0;
            List<Map<String, Object>> exportData = handleGetDataExport(serviceParam, pageIndex);
            try {
                //Tao workbook
                Workbook workbook = handleExportData(exportParam, exportData, recordColumnExport.layoutConfigItems(), false);
                long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
                exportResult.setFileName(fileName + ".xlsx");
                exportResult.setWorkbook(workbook);
                return exportResult;
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
                return exportResult;
            }
        }
        else {
            List<String> fileNames = new ArrayList<>();
            List<FileInputStream> fileInputStreams = new ArrayList<>();
            boolean success = true;
            for(int i=0; i<totalPage; i++){
                List<Map<String, Object>> exportData = handleGetDataExport(serviceParam, i);
                try {
                    //Tao workbook
                    Workbook workbook = handleExportData(exportParam, exportData, recordColumnExport.layoutConfigItems(), false);
                    long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
                    String reportTitle = fileName + "_" + gson.toJson(randomNum);
                    //Convert file
                    FileInputStream fileInputStream = CommonUtils.convertWorkbookToStream(workbook, reportTitle);
                    fileNames.add(reportTitle + ".xlsx");
                    fileInputStreams.add(fileInputStream);
                }
                catch (Exception exception){
                    System.out.println(Arrays.toString(exception.getStackTrace()));
                    success = false;
                    break;
                }
            }
            if(!success){
                return exportResult;
            }
            String zipFileUrl = zipHelper.handleZipFile(fileInputStreams, fileNames, fileName);
            exportResult.setFileZip(new File(zipFileUrl));
            exportResult.setFileName(fileName + ".zip");
            return exportResult;
        }
    }

    public ExportResponse exportDataBcsl(Object serviceParam, String title, String period, String layoutName, int numberTableHeaderRows, String titleMessage){
        ExportResponse exportResult = new ExportResponse();
        RecordColumnExport recordColumnExport = handleGetColumnExport(layoutName, numberTableHeaderRows);
        ExportParam exportParam = new ExportParam();
        exportParam.setColumnsExport(recordColumnExport.columnExport());
        exportParam.setTitle(title);
        exportParam.setPeriod(period);
        exportParam.setServiceParameter(serviceParam);
        exportParam.setNumberTableHeaderRows(numberTableHeaderRows);
        initBeforeExport();
        int totalRecords = handleGetTotalRecord(serviceParam);
        int totalPage = (int) Math.ceil((double) totalRecords / MAX_ROWS_EXPORT);
        Gson gson = new Gson();
        String fileName = CommonUtils.removeVietnameseDiacritics(title.toUpperCase());
        fileName = String.join("_", fileName.split(" "));
        if(totalPage == 1){
            int pageIndex = 0;
            List<Map<String, Object>> exportData = handleGetDataExport(serviceParam, pageIndex);
            try {
                //Tao workbook
                Workbook workbook = handleExportData(exportParam, exportData, recordColumnExport.layoutConfigItems(), true);
                long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
                exportResult.setFileName(fileName + ".xlsx");
                exportResult.setWorkbook(workbook);
                return exportResult;
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
                return exportResult;
            }
        }
        else {
            List<String> fileNames = new ArrayList<>();
            List<FileInputStream> fileInputStreams = new ArrayList<>();
            boolean success = true;
            for(int i=0; i<totalPage; i++){
                List<Map<String, Object>> exportData = handleGetDataExport(serviceParam, i);
                try {
                    //Tao workbook
                    Workbook workbook = handleExportData(exportParam, exportData, recordColumnExport.layoutConfigItems(), true);
                    long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
                    String reportTitle = fileName + "_" + gson.toJson(randomNum);
                    //Convert file
                    FileInputStream fileInputStream = CommonUtils.convertWorkbookToStream(workbook, reportTitle);
                    fileNames.add(reportTitle + ".xlsx");
                    fileInputStreams.add(fileInputStream);
                }
                catch (Exception exception){
                    System.out.println(Arrays.toString(exception.getStackTrace()));
                    success = false;
                    break;
                }
            }
            if(!success){
                return exportResult;
            }
            String zipFileUrl = zipHelper.handleZipFile(fileInputStreams, fileNames, fileName);
            exportResult.setFileZip(new File(zipFileUrl));
            exportResult.setFileName(fileName + ".zip");
            return exportResult;
        }
    }

    public PDFContentResult exportPDFData(Object serviceParam, String title, String period, String reportName, int numberTableHeaderRows, String pageType){
        ExportParam exportParam = new ExportParam();
        exportParam.setTitle(title);
        exportParam.setPeriod(period);
        exportParam.setServiceParameter(serviceParam);
        exportParam.setNumberTableHeaderRows(numberTableHeaderRows);
        initBeforeExport();
        int totalRecords = handleGetTotalRecord(serviceParam);
        int totalPage = (int) Math.ceil((double) totalRecords / MAX_ROWS_PDF_EXPORT);
        if(totalPage == 1){
            int pageIndex = 0;
            List<Map<String, Object>> exportData = handleGetDataExport(serviceParam, pageIndex);
            try {
                return exportPDFReportService.exportReportPdf(exportData, reportName, pageType);
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
                return new PDFContentResult(null, "InternalError");
            }
        }
        else if (totalPage <= 100) {
            List<byte[]> fileDatas = new ArrayList<>();
            for(int i=0; i<totalPage; i++){
                List<Map<String, Object>> exportData = handleGetDataExport(serviceParam, i);
                try {
                    PDFContentResult data = exportPDFReportService.exportReportPdf(exportData, reportName, pageType);
                    fileDatas.add(data.getPdfContent());
                }
                catch (Exception exception){
                    System.out.println(Arrays.toString(exception.getStackTrace()));
                    break;
                }
            }
            if(fileDatas.isEmpty()){
                return new PDFContentResult(null, "InternalError");
            }
            String zipFile = handleZipFilePdfMulti(fileDatas, reportName);
            return new PDFContentResult(new byte[0], zipFile);
        } else {
            return new PDFContentResult(null, "FileOutOfSize");
        }
    }

    private String handleZipFilePdfMulti(List<byte[]> fileResult, String reportName) {
        List<String> fileNames = new ArrayList<>();
        for(int i=0; i< fileResult.size(); i++){
            long randomNum = ThreadLocalRandom.current().nextLong(1, 999999999999999999L);
            String fileName = String.format(reportName + "_" + randomNum + ".pdf");
            fileNames.add(fileName);
        }
        reportName = CommonUtils.removeAccents(reportName);
        String fileZipName = String.join("_", reportName.split(" "));
        String zipPath = zipHelper.handleZipFileByte(fileResult, fileNames, fileZipName);
        if(isNullOrEmpty(zipPath)){
            return null;
        }
        else{
            return zipPath;
        }
    }

    public void initBeforeExport() {
    }

    private void handlePushMessageFail(String title) {
//        UserDto userDto = commonFunction.handleGetCurrentUser();
//        notificationService.pushMessage("Xuất file thất bại", title, "", userDto.email, null,
//                false, NotificationEnum.NEWS.getValue(), true);
    }

    private void handlePushMessageSuccess(String title, String filePath) {
//        UserDto userDto = commonFunction.handleGetCurrentUser();
//        notificationService.pushMessage("Xuất file thành công", title,"", userDto.email, filePath,
//                false, NotificationEnum.FILE.getValue(), true);
    }


    public List<Map<String, Object>> handleGetDataExport(Object serviceParam, int pageIndex) {
        return new ArrayList<>();
    }

    public int handleGetTotalRecord(Object serviceParam) {
        return 0;
    }


    protected RecordColumnExport handleGetColumnExport(String code, int numberTableHeaderRows) {
        List<ExcelColumnInfo> layoutConfigItems = excelColumnInfoService.getColumnInfos(code);
        //Danh sach cot du lieu la nhung cot khong bi merge column
        List<List<ExcelColumnInfo>> listLayoutConfigItems = new ArrayList<>();
        for(int i = 0; i< numberTableHeaderRows; i++){
            List<ExcelColumnInfo> items = layoutConfigItems.stream().filter(e -> (e.getRowOrder() == null || e.getRowOrder() == 0)
                    && (e.getColSpan() == null || e.getColSpan() == 1)).toList();
            listLayoutConfigItems.add(items);
        }
        List<ExcelColumnInfo> columnExport = new ArrayList<>();
        ExcelColumnInfo sortOrderColumn = layoutConfigItems
                .stream().filter(e -> e.getColumnField().equals("order"))
                .findAny().orElse(null);
        if(Objects.isNull(sortOrderColumn)){
            ExcelColumnInfo orderColumn = initOrderColumn(listLayoutConfigItems);
            layoutConfigItems.add(orderColumn);
            layoutConfigItems.sort(Comparator.comparing(ExcelColumnInfo::getSortOrder));

            for(ExcelColumnInfo layoutConfigItem: layoutConfigItems){
                layoutConfigItem.setSortOrder(layoutConfigItem.getSortOrder() + 1);
            }
            columnExport.add(orderColumn);
        }

        for(List<ExcelColumnInfo> items: listLayoutConfigItems){
            columnExport.addAll(items);
        }
        columnExport.sort(Comparator.comparing(ExcelColumnInfo::getSortOrder));
        return new RecordColumnExport(layoutConfigItems, columnExport);
    }


    private static ExcelColumnInfo initOrderColumn(List<List<ExcelColumnInfo>> listLayoutConfigItems) {
        return ExcelColumnInfo.builder()
                .columnName("STT")
                .columnField("order")
                .sortOrder(-1)
                .columnType("Order")
                .width(25)
                .isShow(1)
                .rowOrder(0)
                .align("center")
                .rowSpan(listLayoutConfigItems.size() > 1 ? listLayoutConfigItems.size() : null)
                .build();
    }


    public XSSFWorkbook handleExportData(ExportParam exportParam, List<Map<String, Object>> exportData, List<ExcelColumnInfo> titleTable, boolean isStatistic) {
        List<Map<String, Object>> exportDataTotal = new ArrayList<>();
        if (isStatistic){
            exportDataTotal.add(exportData.get(exportData.size() - 1));
            exportData.remove(exportData.size() - 1);
        }
        List<ExcelColumnInfo> columnsExport = exportParam.getColumnsExport();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet1");
        int numberRows = initReportHeader(workbook, sheet, exportParam.getTitle(),
                exportParam.getPeriod(), exportParam.getServiceParameter(), columnsExport.size());
        numberRows = initTableHeader(workbook, sheet, titleTable, columnsExport.size(), exportParam.getNumberTableHeaderRows(), numberRows);
        numberRows = createRowValue(workbook, sheet, columnsExport, numberRows, exportData);
        numberRows = createRowTotalValue(workbook, sheet, columnsExport, numberRows, exportDataTotal);
        numberRows = createSummaryRow(workbook, sheet, columnsExport, numberRows, exportData);
        numberRows = createFooter(workbook, sheet, numberRows, columnsExport.size());
        createTextNote(workbook, sheet, numberRows, titleTable, exportParam);
        return workbook;
    }

    public int initReportHeader(XSSFWorkbook workbook, XSSFSheet sheet, String title, String period, Object serviceParameter, int numberColumns){
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(false);
        font.setFontHeight(10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);

        CellStyle styleBold = workbook.createCellStyle();
        styleBold.setWrapText(false);
        XSSFFont fontBold = workbook.createFont();
        fontBold.setBold(true);
        fontBold.setFontHeight(10);
        fontBold.setFontName("Times New Roman");
        styleBold.setFont(fontBold);
        styleBold.setAlignment(HorizontalAlignment.CENTER);
        styleBold.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle styleBold1 = workbook.createCellStyle();
        styleBold1.setWrapText(false);
        XSSFFont fontBold1 = workbook.createFont();
        fontBold1.setBold(true);
        fontBold1.setFontHeight(11);
        fontBold1.setFontName("Times New Roman");
        styleBold1.setFont(fontBold1);
        styleBold1.setAlignment(HorizontalAlignment.CENTER);
        styleBold1.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle styleItalic = workbook.createCellStyle();
        styleItalic.setWrapText(false);
        XSSFFont fontItalic = workbook.createFont();
        fontItalic.setBold(false);
        fontItalic.setItalic(true);
        fontItalic.setFontHeight(10);
        fontItalic.setFontName("Times New Roman");
        styleItalic.setFont(fontItalic);
        styleItalic.setAlignment(HorizontalAlignment.CENTER);
        styleItalic.setVerticalAlignment(VerticalAlignment.CENTER);

        int numberRow = initNationalMotto(sheet, numberColumns, style, styleBold, styleItalic);
        numberRow++;
        // Khoi tao tieu de cho bao cao
        Row titleRow = sheet.createRow(++numberRow);

        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title.toUpperCase());
        titleCell.setCellStyle(styleBold1);
        CellRangeAddress region = new CellRangeAddress(numberRow, numberRow, 0, numberColumns - 1);
        sheet.addMergedRegion(region);
        numberRow++;
        numberRow = initReportPeriod(workbook, sheet, period, numberColumns, numberRow);
        sheet.createRow(numberRow);

        if (serviceParameter != null){
            numberRow = handleCustomInitHeader(workbook, sheet, serviceParameter, numberRow);
        }

        return numberRow;
    }

    private int initNationalMotto(XSSFSheet sheet, int numberColumns, CellStyle style, CellStyle styleBold, CellStyle styleItalic) {
        int numberRow = 0;
        Row firstRow = sheet.createRow(numberRow);

        // Các giá trị cần hiển thị ở firstRow
        String[] values = {"ĐẢNG BỘ NHNo&PTNT VIỆT NAM", "ĐẢNG CỘNG SẢN VIỆT NAM"};

        // Đảm bảo numberColumns hợp lệ
        int totalColumns = numberColumns > 0 ? numberColumns : 10; // Mặc định 10 nếu numberColumns không hợp lệ
        if (totalColumns < 2) totalColumns = 2; // Đảm bảo ít nhất 2 cột để chia đôi

        // Tính số cột cho mỗi ô gộp
        int columnsPerRegion = totalColumns / values.length; // Chia đôi
        if (columnsPerRegion < 1) columnsPerRegion = 1; // Đảm bảo ít nhất 1 cột mỗi ô

        // Tạo các ô gộp động cho firstRow
        for (int i = 0; i < values.length; i++) {
            // Tính chỉ số cột bắt đầu và kết thúc
            int startColumn = i * columnsPerRegion;
            int endColumn = startColumn + columnsPerRegion - 1;
            // Ô cuối gộp hết các cột còn lại
            if (i == values.length - 1) {
                endColumn = totalColumns - 1;
            }

            // Tạo cell tại cột bắt đầu
            Cell cell = firstRow.createCell(startColumn);
            cell.setCellValue(values[i]);

            // Gộp các cột
            if (startColumn < endColumn) {
                sheet.addMergedRegion(new CellRangeAddress(
                        firstRow.getRowNum(), // Hàng bắt đầu
                        firstRow.getRowNum(), // Hàng kết thúc
                        startColumn, // Cột bắt đầu
                        endColumn   // Cột kết thúc
                ));
            }

            // Thiết lập căn giữa
            CellStyle styleCentered = sheet.getWorkbook().createCellStyle();
            styleCentered.cloneStyleFrom(styleBold); // Sao chép styleBold
            styleCentered.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
            styleCentered.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc
            cell.setCellStyle(styleCentered);
        }

        // Tạo secondRow
        Row secondRow = sheet.createRow(++numberRow);
        Cell secondCell = secondRow.createCell(0);
        secondCell.setCellValue("ĐẢNG ỦY/CHI BỘ…..");

        // Gộp các cột cho secondRow giống ô đầu tiên của firstRow
        int endColumnForSecondRow = columnsPerRegion - 1; // Gộp giống ô "ĐẢNG BỘ NHNo&PTNT VIỆT NAM"
        if (endColumnForSecondRow > 0) {
            sheet.addMergedRegion(new CellRangeAddress(
                    secondRow.getRowNum(), // Hàng bắt đầu
                    secondRow.getRowNum(), // Hàng kết thúc
                    0, // Cột bắt đầu
                    endColumnForSecondRow // Cột kết thúc
            ));
        }

        // Thiết lập căn giữa cho secondCell
        CellStyle styleCentered = sheet.getWorkbook().createCellStyle();
        styleCentered.cloneStyleFrom(styleBold); // Sao chép styleBold
        styleCentered.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
        styleCentered.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc
        secondCell.setCellStyle(styleCentered);

        return numberRow;
    }

    private int initReportPeriod(XSSFWorkbook workbook, XSSFSheet sheet, String period, int numberColumns, int numberRow) {
        Row periodRow = sheet.createRow(numberRow);
        CellStyle periodStyle = workbook.createCellStyle();
        XSSFFont periodFont = workbook.createFont();
        periodFont.setBold(true);
        periodFont.setFontHeight(10);
        periodFont.setFontName("Times New Roman");
        periodStyle.setFont(periodFont);
        periodStyle.setAlignment(HorizontalAlignment.CENTER);
        periodStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Cell periodCell = periodRow.createCell(0);
        periodCell.setCellValue(period);
        periodCell.setCellStyle(periodStyle);

        CellRangeAddress periodRegion = new CellRangeAddress(numberRow, numberRow, 0, numberColumns - 1);
        sheet.addMergedRegion(periodRegion);
        handleSetStyleForMergeCell(sheet, periodRegion, periodStyle);
        numberRow++;
        return numberRow;
    }


    public int handleCustomInitHeader(XSSFWorkbook workbook, XSSFSheet sheet, Object customData, int numberRow) {
        return numberRow;
    }


    public int initTableHeader(XSSFWorkbook workbook, XSSFSheet sheet, List<ExcelColumnInfo> columnsExport, int numberColumns, int numberTableHeaderRows, int rowNum){
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        initBorderStyle(style);
        style.setWrapText(true);
        for(int i=0; i<numberTableHeaderRows; i++){
            rowNum = handleInitTableHeader(sheet, columnsExport, style, rowNum, i);
        }
        rowNum = handleInitSubTitleTableHeader(workbook, sheet, numberColumns, rowNum);
        return rowNum;
    }

    private int handleInitTableHeader(XSSFSheet sheet, List<ExcelColumnInfo> columnsExport, CellStyle style, int rowNum, int rowOrder){
        List<ExcelColumnInfo> layout = columnsExport.stream().filter(e -> e.getRowOrder() == rowOrder).toList();
        if(layout.isEmpty()){
            return rowNum;
        }
        Row row = sheet.createRow(++rowNum);
        if(rowOrder == 2){
            row.setHeight((short) 800);
        }
        int currentIndex;
        for(ExcelColumnInfo layoutConfigItem : layout){
            currentIndex = layoutConfigItem.getSortOrder();
            Cell cell = row.createCell(layoutConfigItem.getSortOrder());
            cell.setCellValue(layoutConfigItem.getColumnName());
            int columnWidth = 5000;
            try{
                columnWidth = layoutConfigItem.getWidth() * 100;
                if(columnWidth > 5000){
                    columnWidth = 5000;
                }
            }
            catch (Exception ignore){
            }
            sheet.setColumnWidth(currentIndex, columnWidth);
            if(layoutConfigItem.getRowSpan() != null){
                CellRangeAddress region = new CellRangeAddress(rowNum, rowNum + layoutConfigItem.getRowSpan() -1 , layoutConfigItem.getSortOrder(), layoutConfigItem.getSortOrder());
                sheet.addMergedRegion(region);
                handleSetStyleForMergeCell(sheet, region, style);
            }
            else if(layoutConfigItem.getColSpan() != null){
                CellRangeAddress region = new CellRangeAddress(rowNum, rowNum, layoutConfigItem.getSortOrder(), layoutConfigItem.getSortOrder() + layoutConfigItem.getColSpan() - 1);
                sheet.addMergedRegion(region);
                handleSetStyleForMergeCell(sheet, region, style);
            }
            else{
                cell.setCellStyle(style);
            }
        }
        return rowNum;
    }

    private int handleInitSubTitleTableHeader(XSSFWorkbook workbook, XSSFSheet sheet, int numberColumns, int rowNum) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(false);
        font.setItalic(true);
        font.setFontHeight(10);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        initBorderStyle(style);
        style.setWrapText(true);
        Row row = sheet.createRow(++rowNum);
        for (int i=0; i<numberColumns; i++){
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(String.format("(%s)", i + 1));
        }
        return rowNum;
    }


    public int createRowValue(XSSFWorkbook workbook, XSSFSheet sheet, List<ExcelColumnInfo> columnsExport, int rowNum, List<Map<String, Object>> exportData){
        for (int i=0; i<exportData.size(); i++){
            Row row = sheet.createRow(rowNum);
            XSSFFont font = workbook.createFont();
            font.setFontHeight(10);
            font.setFontName("Times New Roman");
            for (int j=0; j<columnsExport.size(); j++){
                ExcelColumnInfo columnExport = columnsExport.get(j);
                Cell cell = row.createCell(j);
                CellStyle style = handleCreateCellStyle(workbook, columnExport);
                style.setFont(font);
                cell.setCellStyle(style);
                if(columnExport.getColumnField().equals("order")){
                    cell.setCellValue(i + 1);
                }
                else{
                    Object value = handleGetCellValue(exportData.get(i), columnExport);
                    if (value != null)
                        value = handleFormatCellValue(value, columnExport.getColumnType());
                    cell.setCellValue(value != null ? value.toString() : "");
                }
            }

            rowNum++;
        }
        return rowNum;
    }

    public int createRowTotalValue(XSSFWorkbook workbook, XSSFSheet sheet, List<ExcelColumnInfo> columnsExport, int rowNum, List<Map<String, Object>> exportData){
        for (int i=0; i<exportData.size(); i++){
            Row row = sheet.createRow(rowNum);
            XSSFFont font = workbook.createFont();
            font.setFontHeight(10);
            font.setFontName("Times New Roman");
            for (int j=0; j<columnsExport.size(); j++){
                ExcelColumnInfo columnExport = columnsExport.get(j);
                Cell cell = row.createCell(j);
                CellStyle style = handleCreateCellStyle(workbook, columnExport);
                style.setFont(font);
                cell.setCellStyle(style);
                if(columnExport.getColumnField().equals("order")){
                    cell.setCellValue("Tổng");
                }
                else{
                    Object value = handleGetCellValue(exportData.get(i), columnExport);
                    if (value != null)
                        value = handleFormatCellValue(value, columnExport.getColumnType());
                    cell.setCellValue(value != null ? value.toString() : "");
                }
            }

            rowNum++;
        }
        return rowNum;
    }


    @SuppressWarnings("unchecked")
    public Object handleGetCellValue(Object data, ExcelColumnInfo columnExport) {
        Map<String, Object> mapData = (Map<String, Object>) data;
        Object value = null;
        if(mapData.containsKey(columnExport.getColumnField())){
            value = mapData.get(columnExport.getColumnField());
        }
        if(value == null){
            value = "";
        }
        return value;
    }

    public int createSummaryRow(XSSFWorkbook workbook, XSSFSheet sheet, List<ExcelColumnInfo> columnsExport, int numberRows, List<Map<String, Object>> exportData) {
        return numberRows;
    }

    public int createFooter(XSSFWorkbook workbook, XSSFSheet sheet, int numberRows, int columSize) {
        Row row = sheet.createRow(numberRows + 2);
        CellStyle style = initCellStyleNotWrapText(workbook, "CENTER", "CENTER", 10, false, false);
        CellStyle styleBold = initCellStyleNotWrapText(workbook, "CENTER", "CENTER", 10, true, false);

        // Các giá trị cần hiển thị
        String[] values = {"LẬP BIỂU", "KIỂM SOÁT", "T/M CẤP ỦY/CHI BỘ"};

        // Đảm bảo columSize hợp lệ
        int totalColumns = columSize > 0 ? columSize : 9; // Mặc định 9 nếu columSize không hợp lệ
        if (totalColumns < values.length) totalColumns = values.length; // Đảm bảo đủ cột cho 3 ô

        // Tính số cột cho mỗi ô
        int columnsPerRegion = totalColumns / values.length; // Chia đều
        if (columnsPerRegion < 1) columnsPerRegion = 1; // Đảm bảo ít nhất 1 cột mỗi ô

        // Tạo các ô gộp động
        for (int i = 0; i < values.length; i++) {
            // Tính chỉ số cột bắt đầu và kết thúc
            int startColumn = i * columnsPerRegion;
            int endColumn = startColumn + columnsPerRegion - 1;
            // Ô cuối gộp hết các cột còn lại
            if (i == values.length - 1) {
                endColumn = totalColumns - 1;
            }

            // Tạo cell tại cột bắt đầu
            Cell cell = row.createCell(startColumn);
            cell.setCellValue(values[i]);

            // Gộp các cột
            if (startColumn < endColumn) {
                sheet.addMergedRegion(new CellRangeAddress(
                        row.getRowNum(), // Hàng bắt đầu
                        row.getRowNum(), // Hàng kết thúc
                        startColumn, // Cột bắt đầu
                        endColumn   // Cột kết thúc
                ));
            }

            // Thiết lập căn giữa
            CellStyle styleCentered = workbook.createCellStyle();
            styleCentered.cloneStyleFrom(styleBold); // Sao chép styleBold
            styleCentered.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
            styleCentered.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc
            cell.setCellStyle(styleCentered);
        }

        return numberRows + 5;
    }

    public void createTextNote(XSSFWorkbook workbook, XSSFSheet sheet, int numberRows, List<ExcelColumnInfo> columnsExport, ExportParam exportParam) {

    }

    public void handleSetStyleForMergeCell(XSSFSheet sheet, CellRangeAddress region, CellStyle style) {
        for (int rowM = region.getFirstRow(); rowM <= region.getLastRow(); rowM++) {
            Row currentRow = sheet.getRow(rowM);
            if (currentRow == null) {
                currentRow = sheet.createRow(rowM);
            }

            for (int col = region.getFirstColumn(); col <= region.getLastColumn(); col++) {
                Cell cell = currentRow.getCell(col);
                if (cell == null) {
                    cell = currentRow.createCell(col);
                }
                cell.setCellStyle(style);
            }
        }
    }

    public void initBorderStyle(CellStyle style){
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
    }

    public CellStyle initCellStyleNotWrapText(XSSFWorkbook workbook, String align, String verticalAlign, int fontHeight, boolean fontBold, boolean fontItalic){
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(fontBold);
        font.setItalic(fontItalic);
        font.setFontHeight(fontHeight);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setWrapText(false);
        switch (align){
            case "RIGHT":
                style.setAlignment(HorizontalAlignment.RIGHT);
                break;
            case "CENTER":
                style.setAlignment(HorizontalAlignment.CENTER);
                break;
            default:
                style.setAlignment(HorizontalAlignment.LEFT);
                break;
        }

        if (verticalAlign.equals("CENTER")) {
            style.setVerticalAlignment(VerticalAlignment.CENTER);
        } else {
            style.setVerticalAlignment(VerticalAlignment.TOP);
        }

        return style;
    }


    public CellStyle handleCreateCellStyle(XSSFWorkbook workbook, ExcelColumnInfo layoutConfigItem){
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if(Objects.equals(layoutConfigItem.getAlign(), "center")){
            style.setAlignment(HorizontalAlignment.CENTER);
        }
        else if(Objects.equals(layoutConfigItem.getAlign(), "left")){
            style.setAlignment(HorizontalAlignment.LEFT);
        }
        else{
            style.setAlignment(HorizontalAlignment.RIGHT);
        }
        style.setWrapText(true);
        return style;
    }


    public String handleFormatCellValue(Object value, String dataType) {
        if(value == null){
            return "";
        }
        String result = value.toString();
        result = result.strip();
        if(result.isEmpty()){
            return result;
        }
        if(dataType.equals("Currency")){
            try {
                Double currency = (Double) value;
                DecimalFormat df = new DecimalFormat("###,###,###,###,###.###");
                result = df.format(currency);
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
            }
        }
        else if (dataType.equals("Number")) {
            Locale spanishLocale = new Locale("vi", "VN");
            NumberFormat numberFormat = NumberFormat.getNumberInstance(spanishLocale);
            try {
                Integer amount = Integer.parseInt(result);
                result = numberFormat.format(amount);
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
            }
        }
        else if(dataType.equals("Date")){
            if(value instanceof Date date){
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                result = dateFormat.format(date);
            }
            else if(value instanceof String){
                SimpleDateFormat strFormat = new SimpleDateFormat("yyyyMMdd");
                try{
                    Date date = strFormat.parse(value.toString());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    result = dateFormat.format(date);
                }
                catch (Exception exception){
                    System.out.println(exception.getMessage());
                }
            }
        }
        else if(dataType.equals("DateTime")){
            if(value instanceof Date date){
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                result = dateFormat.format(date);
            }
            else if(value instanceof String){
                SimpleDateFormat strFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                try{
                    Date date = strFormat.parse(value.toString());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    result = dateFormat.format(date);
                }
                catch (Exception exception){
                    System.out.println(exception.getMessage());
                }
            }
        }
//        else if(dataType.contains("Enum")){
//            EnumResource enumResource = commonFunction.getEnumResource();
//            String key = "";
//            if(value instanceof Boolean){
//                key = (boolean)value ? dataType + "_1" : dataType + "_0";
//            }
//            else{
//                key = dataType + "_" + value;
//            }
//
//            String currentSource = enumResource.resource.get(key);
//            if(currentSource != null){
//                result = currentSource;
//            }
//        }
        return result;
    }

    private UserDetailsImpl getUserRequested(){
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
