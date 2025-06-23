package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.EProcessStatus;
import com.agribank.qldv_api.enums.ETransferType;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_out.TransferOutAgribankClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankClient;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.party_transfer.TransferProcessRequest;
import com.agribank.qldvutils.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferProcessService {
    TransferProcessClient transferProcessClient;
    TransferToAgribankClient transferToAgribankClient;
    TransferOutAgribankClient transferOutAgribankClient;

    public PageResponse<TransferProcess> getList(TransferProcessRequest request) {
        return transferProcessClient.getList(request).getData();
    }

    public Object getDetail(String id) {
        TransferProcess transferProcess = transferProcessClient.findById(id).getData().orElse(null);

        if (Objects.isNull(transferProcess) || !Objects.equals(transferProcess.getStatus(), EProcessStatus.PROCESSING.getId())) {
            throw new CommonException("Không tìm thấy dữ liệu hoặc hồ sơ đã xử lý");
        }

        ETransferType transferType = ETransferType.getTransferType(transferProcess.getTransferType());

        if (Objects.isNull(transferType)) {
            throw new CommonException("Kiểu hồ sơ chuyển sinh hoạt đảng không hợp lệ");
        }

        return switch (transferType) {
            case TRANSFER_TO_AGRIBANK -> transferToAgribankClient.findByProcessId(transferProcess.getId());
            case TRANSFER_OUT_AGRIBANK -> transferOutAgribankClient.findByProcess(transferProcess.getId());
            default -> null;
        };
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
