package com.agribank.qldv_api.service.development_plan;

import com.agribank.qldv_api.service.import_file.ImportFileConfigDetailService;
import com.agribank.qldv_api.service.import_file.ImportFileConfigService;
import com.agribank.qldv_api.service.import_file.ImportFileService;
import com.agribank.qldvutils.entity.development_plan.DevelopmentPlan;
import com.agribank.qldvutils.entity.import_file.ImportFileConfigDetail;
import com.agribank.qldvutils.response.BaseResponse;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service("importDevelopPlan")
public class ImportDevelopPlanService extends ImportFileService {
    @Autowired
    private DevelopPlanService developPlanService;


    public ImportDevelopPlanService(ImportFileConfigService importFileConfigService, ImportFileConfigDetailService importFileConfigDetailService) {
        super(importFileConfigService, importFileConfigDetailService);
    }

    protected void handleInitCheckData() {
        initCheckData = null;
    }

    @SuppressWarnings("unchecked")
    protected RecordCheckData handleCustomValidate(boolean check, List<String> reasonError, Map<String, Object> tempData, List<Map<String, Object>> dataValid, List<ImportFileConfigDetail> importFileConfigDetails) {
        String code = (String) tempData.get("code");

        DevelopmentPlan developmentPlan = developPlanService.findByOrganizationCode(code);
        if (Objects.nonNull(developmentPlan)) {
            check = false;
            reasonError.add(String.format("Giá trị <Mã tổ chức Đảng> <%s> đã được tạo kế hoạch", code));
        }
        Integer min = (Integer) tempData.get("min");
        Integer target = (Integer) tempData.get("target");
        Integer strive = (Integer) tempData.get("strive");
        Integer year = (Integer) tempData.get("year");
        Integer currentYear = LocalDate.now().getYear();
        if (min > target) {
            check = false;
            reasonError.add(String.format("Giá trị <Chỉ tiêu> <%s> phải lớn hơn giá trị tối thiểu <%s>", target, min));
        }
        if (min < 0) {
            check = false;
            reasonError.add(String.format("Giá trị <Tối thiểu> <%s> phải lớn hơn 0", min));
        }
        if (min > strive) {
            check = false;
            reasonError.add(String.format("Giá trị <Phấn đấu> <%s> phải lớn hơn giá trị tối thiểu <%s>", strive, min));
        }

        if (Objects.nonNull(year) && currentYear > year) {
            check = false;
            reasonError.add(String.format("Giá trị <Năm> <%s> phải lớn hơn giá trị năm hiện tại <%s>", year, currentYear));

        }

        return new RecordCheckData(check, reasonError);
    }

    @SuppressWarnings("unchecked")
    protected BaseResponse<T> handleSaveDataUpload(BaseResponse response) {
        return response;
    }
}
