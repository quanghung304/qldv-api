package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.DvOrgHistoryClient;
import com.agribank.qldvutils.entity.DvOrgHistory;
import com.agribank.qldvutils.request.dv_org.DvOrgHisRequest;
import com.agribank.qldvutils.request.dv_org_history.DvOrgHisListRequest;
import com.agribank.qldvutils.request.dv_org_history.DvOrganizationHisRequest;
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

    public List<DvOrgHistory> findByRefId(String referenceId) {
        return client.findByRefId(referenceId).getData();
    }

    public List<DvOrgHistory> findByOldOrgCodeIn(DvOrgHisRequest request) {
        return client.findByOldOrgCodeIn(request).getData();
    }

    public List<DvOrgHistory> getOrgHis(DvOrganizationHisRequest request) {
        return client.getOrgHis(request).getData();
    }

    public List<DvOrgHistory> findByStaffCodesAndRefId(DvOrgHisListRequest request) {
        return client.findByStaffCodesAndRefId(request).getData();
    }

    public List<DvOrgHistory> findByStaffCodes(List<String> staffCodes) {
        return client.findByStaffCodes(staffCodes).getData();
    }

}
