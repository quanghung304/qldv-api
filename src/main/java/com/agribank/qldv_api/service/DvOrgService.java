package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldvutils.entity.DvOrgHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DvOrgService {
    private final DvOrgHistoryClient client;

    public DvOrgHistory save(DvOrgHistory history) {
        return client.save(history).getData();
    }

    public List<DvOrgHistory> saveAll(List<DvOrgHistory> histories) {
        return client.saveAll(histories).getData();
    }

    public List<DvOrgHistory> findByStaffCodes(List<String> staffCodes) {
        return client.findByStaffCodes(staffCodes).getData();
    }

    public List<DvOrgHistory> findByOldOrgCodeIn(List<String> oldOrgCodes) {
        return client.findByOldOrgCodeIn(oldOrgCodes).getData();
    }
}
