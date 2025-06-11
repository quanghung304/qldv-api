package com.agribank.qldv_api.service.import_file;

import com.agribank.qldv_api.gateway.import_file.ImportFileConfigDetailClient;
import com.agribank.qldvutils.entity.import_file.ImportFileConfigDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportFileConfigDetailService {
    private final ImportFileConfigDetailClient client;

    public List<ImportFileConfigDetail> findByRefId(String refId) {
        return client.findByRefId(refId).getData();
    }
}
