package com.agribank.qldv_api.service.development_plan;

import com.agribank.qldv_api.service.import_file.ImportFileConfigDetailService;
import com.agribank.qldv_api.service.import_file.ImportFileConfigService;
import com.agribank.qldv_api.service.import_file.ImportFileService;
import com.agribank.qldvutils.response.BaseResponse;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;



@Service("importDevelopPlan")
public class ImportDevelopPlanService extends ImportFileService {

    public ImportDevelopPlanService(ImportFileConfigService importFileConfigService, ImportFileConfigDetailService importFileConfigDetailService) {
        super(importFileConfigService, importFileConfigDetailService);
    }

    protected void handleInitCheckData() {
        initCheckData = null;
    }

    @SuppressWarnings("unchecked")
    protected BaseResponse<T> handleSaveDataUpload(BaseResponse response) {
        return response;
    }
}
