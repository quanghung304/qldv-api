package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EDVStatus;
import com.agribank.qldv_api.gateway.DvHistoryClient;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.DvHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DvHistoryService {
    private final DvHistoryClient client;
    private final DVService dvService;

    private void save(DvHistory history) {
        client.save(history);
    }

    public void saveDV(String staffCode, String dvStatus, String dvStatusName){
        try {
            DV dv = dvService.findByStaffCode(staffCode);
            dv.setDvStatus(dvStatus);

            DvHistory history = DvHistory.builder()
                    .action(dvStatusName)
                    .staffCode(staffCode)
                    .build();

            dvService.save(dv);
            save(history);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
