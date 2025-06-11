package com.agribank.qldv_api.service.import_file;

import com.agribank.qldv_api.gateway.import_file.ImportFileConfigClient;
import com.agribank.qldvutils.entity.import_file.ImportFileConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportFileConfigService {
    private final ImportFileConfigClient client;

    ImportFileConfig findByCode(String code) {
        return client.findByCode(code).getData();
    }
}
