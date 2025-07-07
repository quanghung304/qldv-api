package com.agribank.qldv_api.service.export;

import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.layoutConfig.LayoutConfigItem;
import com.agribank.qldv_api.response.organization.OrganizationResponse;
import com.agribank.qldv_api.response.pdf.PDFContentResult;
import com.agribank.qldv_api.service.LayoutConfigService;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldv_api.service.organization.OrganizationService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldv_api.utils.PDFHelper;
import com.agribank.qldvutils.entity.LayoutConfig;
import com.agribank.qldvutils.exception.CommonException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExportPDFReportService {

    @Value("${export.pdf.service.url}")
    private String pdfServiceUrl;
    private final LayoutConfigService layoutConfigService;
    private final UserService userService;
    private final OrganizationService organizationService;

    public PDFContentResult exportReportPdf(List<Map<String, Object>> dataReport, String reportName, String pageType){
        RecordReportData reportData = getRecordReportData(dataReport,reportName);
        try{
            String xmlContent = handleReadFile("static/templateImport/templateExport/xml/ExportBase.xml");
            String xsltContent;
            if (Objects.equals(pageType, "A4")) {
                xsltContent = handleReadFile("static/templateImport/templateExport/xslt/ExportBase.xslt");
            } else {
                xsltContent = handleReadFile("static/templateImport/templateExport/xslt/ExportBaseHorizontal.xslt");
            }
            xsltContent = handleBindTableHeader(reportData.layoutConfigItems, reportData.columnExport.size(), xsltContent);
            xmlContent = handleBindXml(xmlContent, reportName, reportData);
            Date now = new Date();
            String fileName = String.format("%s_%s", reportName, now.getTime());
            byte[] file = PDFHelper.handleConvertPdfReturnFile(xsltContent, xmlContent, fileName, pdfServiceUrl);
            return new PDFContentResult(file, fileName);
        }
        catch (Exception exception) {
            System.out.println(exception.getMessage());
            return new PDFContentResult(null, "InternalErrorServer");
        }
    }

    private String handleBindXml(String templateXml, String reportName, RecordReportData reportData) {
        String xmlResult = templateXml;
        xmlResult = handleBindReportSummary(reportName, xmlResult);
        xmlResult = handleBindDataReport(reportData, xmlResult);
        //xmlResult = handleBindSummaryData(reportData, xmlResult);
        return xmlResult;
    }

    private @NotNull String handleBindDataReport(RecordReportData reportData, String xmlResult) {
        String templateData = """
                <DoiTuong>
                   %s
                </DoiTuong>
                """;
        String templateItem = """
                <Item style="%s">
                    <GiaTri>%s</GiaTri>
                </Item>
                """;

        StringBuilder dataObject = new StringBuilder();
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        if(reportData.mapData == null || reportData.mapData.isEmpty()){
            xmlResult = xmlResult.replace("##DanhSachDoiTuong##", "");
            return xmlResult;
        }
        for (int i = 0; i< reportData.mapData.size(); i++){
            Map<String, Object> mapData = reportData.mapData.get(i);
            StringBuilder dataItem = new StringBuilder();
            for(LayoutConfigItem layoutConfigItem : reportData.columnExport){
                Object value = null;
                if(Objects.equals(layoutConfigItem.columnField, "order")){
                    value = i+1;
                }
                if(mapData.containsKey(layoutConfigItem.columnField)){
                    value = mapData.get(layoutConfigItem.columnField);
                }

                if(value == null){
                    dataItem.append(String.format(templateItem, "", ""));
                    continue;
                }

                if(Objects.equals(layoutConfigItem.columnType, "Number")){
                    Locale spanishLocale = new Locale("vi", "VN");
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(spanishLocale);
                    String formattedNumber = numberFormat.format((int) value);
                    dataItem.append(String.format(templateItem, "", formattedNumber));
                } else if(Objects.equals(layoutConfigItem.columnType, "Date")){
                    String formattedDate = "";
                    try {
                        Date date = inputFormat.parse(value.toString());
                        formattedDate = outputFormat.format(date);
                    } catch(ParseException ignored) {}
                    dataItem.append(String.format(templateItem, "", formattedDate));
                } else{
                    dataItem.append(String.format(templateItem, "", CommonUtils.replaceSpecialCharacter(String.valueOf(value))));
                }
            }

            dataObject.append(String.format(templateData, dataItem));
        }
        xmlResult = xmlResult.replace("##DanhSachDoiTuong##", dataObject.toString());
        return xmlResult;
    }

    private @NotNull String handleBindReportSummary(String reportName, String xmlResult) {
        try {
            UserDetailsImpl userDetails = userService.getUserRequested();
            if (userDetails == null) {
                return xmlResult;
            }
            OrganizationResponse organization = organizationService.get(userDetails.getOrganizationCode());
            if (organization == null) {
                return xmlResult;
            }
            xmlResult = xmlResult.replace("##ToChucDang##", organization.getName() != null ? organization.getName().toUpperCase() : "")
                    .replace("##BaoCao##", reportName != null ? reportName.toUpperCase() : "");
            return xmlResult;
        } catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    private @NotNull RecordReportData getRecordReportData(List<Map<String, Object>> listData, String reportName) {
        LayoutConfig layoutConfig= layoutConfigService.findByDescription(reportName);
        List<LayoutConfigItem> layoutConfigItems = getLayoutConfigItem(layoutConfig);
        List<LayoutConfigItem> firstLayout = layoutConfigItems.stream().filter(e -> e.rowOrder == 0 && (e.colSpan == null || e.colSpan == 1)).toList();
        List<LayoutConfigItem> secondLayout = layoutConfigItems.stream().filter(e -> e.rowOrder == 1 && (e.colSpan == null || e.colSpan == 1)).toList();
        List<LayoutConfigItem> thirdLayout = layoutConfigItems.stream().filter(e -> e.rowOrder == 2 && (e.colSpan == null || e.colSpan == 1)).toList();
        List<LayoutConfigItem> columnExport = new ArrayList<>();
        LayoutConfigItem orderColumn = new LayoutConfigItem();
        orderColumn.columnName = "STT";
        orderColumn.columnField = "order";
        orderColumn.sortOrder = -1;
        orderColumn.columnType = "Order";
        orderColumn.width = 10;
        orderColumn.isShow = true;
        layoutConfigItems.add(orderColumn);
        layoutConfigItems.sort(Comparator.comparing(LayoutConfigItem::getSortOrder));
        for(LayoutConfigItem layoutConfigItem: layoutConfigItems){
            if(Objects.equals(layoutConfigItem.columnField, "organizationCode")){
                layoutConfigItem.width = 20;
            }
            else if (Objects.equals(layoutConfigItem.columnField, "order")){
                layoutConfigItem.width = 15;
            }
            else{
                layoutConfigItem.width = 25;
            }
            layoutConfigItem.sortOrder = layoutConfigItem.sortOrder + 1;
        }
        columnExport.add(orderColumn);
        columnExport.addAll(firstLayout);
        columnExport.addAll(secondLayout);
        columnExport.addAll(thirdLayout);
        columnExport.sort(Comparator.comparing(LayoutConfigItem::getSortOrder));

        return new RecordReportData(layoutConfigItems, columnExport, listData);
    }

    private record RecordReportData(List<LayoutConfigItem> layoutConfigItems, List<LayoutConfigItem> columnExport, List<Map<String, Object>> mapData) {
    }

    private List<LayoutConfigItem> getLayoutConfigItem(LayoutConfig config) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<LayoutConfigItem>>() {}.getType();
        return gson.fromJson(config.getContent(), listType);
    }

    private String handleBindTableHeader(List<LayoutConfigItem> layoutConfigItems, int numberColumns, String xsltTemplate) {
        StringBuilder trBuilder = new StringBuilder();
        for(int i=0; i<3; i++){
            int finalI = i;
            List<LayoutConfigItem> layout = layoutConfigItems.stream().filter(e -> e.rowOrder == finalI).toList();
            if(layout.isEmpty()){
                continue;
            }
            String trDetail = "<tr class=\"font-bold tr-header\">%s</tr>";
            StringBuilder builder = getTitleReportHeader(layout);
            trBuilder.append(String.format(trDetail, builder));
        }
        String trSubDetail = "<tr class=\"font-italic tr-header\">%s</tr>";
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<numberColumns; i++){
            String th = String.format("<th>(%s)</th>", i+1);
            builder.append(th);
        }
        trBuilder.append(String.format(trSubDetail, builder));
        xsltTemplate = xsltTemplate.replace("##TableHeader##", trBuilder.toString());
        return xsltTemplate;
    }

    private static @NotNull StringBuilder getTitleReportHeader(List<LayoutConfigItem> layout) {
        StringBuilder builder = new StringBuilder();
        for(LayoutConfigItem item : layout){
            if(item.rowSpan != null){
                String tdDetail = """
                    <th style="" rowspan="%s">
                        <div class="edit-label">%s</div>
                    </th>
                    """;
                builder.append(String.format(tdDetail, item.rowSpan, item.columnName));
            }
            else if(item.colSpan != null){
                String tdDetail = """
                    <th style="" colspan="%s">
                        <div class="edit-label">%s</div>
                    </th>
                    """;
                builder.append(String.format(tdDetail, item.colSpan, item.columnName));
            }
            else{
                String tdDetail = """
                    <th style="">
                        <div class="edit-label">%s</div>
                    </th>
                    """;
                builder.append(String.format(tdDetail, item.columnName));
            }
        }
        return builder;
    }

    private static String handleReadFile(String path) throws IOException {
        ClassPathResource xmlTemplate = new ClassPathResource(path);
        InputStream inputStream = xmlTemplate.getInputStream();
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
