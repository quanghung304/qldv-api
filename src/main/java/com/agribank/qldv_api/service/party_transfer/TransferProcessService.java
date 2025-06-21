package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.EProcessStatus;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankClient;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.party_transfer.TransferProcessRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransferProcessService {
    private final TransferProcessClient transferProcessClient;
    private final TransferToAgribankClient transferToAgribankClient;

    public PageResponse<TransferProcess> getList(TransferProcessRequest request) {
        return transferProcessClient.getList(request).getData();
    }

    public Object getDetail(String id) {
        TransferProcess transferProcess = transferProcessClient.findById(id).getData().orElse(null);

        if (Objects.isNull(transferProcess) || !Objects.equals(transferProcess.getStatus(), EProcessStatus.PROCESSING.getId())) {
            throw new CommonException("Không tìm thấy dữ liệu hoặc hồ sơ đã xử lý");
        }

        switch (transferProcess.getTransferType()) {
            case 1:
                return transferToAgribankClient.findByProcessId(transferProcess.getId());
            default:
                return null;
        }
    }

    public Integer countByOrganizationCode(String organizationCode) {
        return transferProcessClient.countByOrganizationCode(organizationCode).getData();
    }

    public TransferProcess save(TransferProcess transferProcess) {
        return transferProcessClient.save(transferProcess).getData();
    }

    public List<TransferProcess> findProcessingTransfer(String staffCode, Integer type){
        return transferProcessClient.findProcessingTransfer(staffCode, type).getData();
    }
}
