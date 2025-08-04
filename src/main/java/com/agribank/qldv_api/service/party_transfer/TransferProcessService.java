package com.agribank.qldv_api.service.party_transfer;

import com.agribank.qldv_api.enums.EProcessStatus;
import com.agribank.qldv_api.enums.ETransferType;
import com.agribank.qldv_api.gateway.party_transfer.TransferProcessClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_out.TransferOutAgribankClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_temporary.TransferTemporaryClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_to.TransferToAgribankClient;
import com.agribank.qldv_api.gateway.party_transfer.transfer_within_agribank.TransferWithinAgribankClient;
import com.agribank.qldv_api.service.CheckAuthorityService;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldvutils.entity.party_transfer.TransferProcess;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.bcsl_report.dv.SearchRp10DataRequest;
import com.agribank.qldvutils.request.party_transfer.TransferProcessRequest;
import com.agribank.qldvutils.request.report_dv.SearchRp32Request;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.bcsl_report.dv.BcslDvRp10Response;
import com.agribank.qldvutils.response.Report32Response;
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
    private final TransferTemporaryClient transferTemporaryClient;
    TransferWithinAgribankClient transferWithinAgribankClient;
    private final CheckAuthorityService checkAuthorityService;

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
            case TEMPORARY_TRANSFER -> transferTemporaryClient.findByProcessId(transferProcess.getId());
            case TRANSFER_WITHIN_AGRIBANK ->  transferWithinAgribankClient.findByProcessId(transferProcess.getId());
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

    public PageResponse<Report32Response> searchRp32(SearchRp32Request request){
        checkAuthorityService.hasAuthorityOverOrganization(request.getOrganizationCode());
        return transferProcessClient.searchRp32(request).getData();
    }

    public List<BcslDvRp10Response> searchRp10(SearchRp10DataRequest request){
        return transferProcessClient.searchRp10(request).getData();
    }
}
