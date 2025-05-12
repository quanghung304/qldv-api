package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.DVClient;
import com.agribank.qldv_api.request.dv.DVRequest;
import com.agribank.qldv_api.request.dv.SearchDVRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.dv.DVResponse;
import com.agribank.qldv_api.service.log.DVLogService;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DVService {
    private final DVClient dvClient;
    private final DVLogService dvLogService;
    private final ModelMapper modelMapper;

    public PageResponse<DVResponse> search(SearchDVRequest request){
        PageResponse<DV> dvPageResponse = dvClient.search(request).getData();
        PageResponse<DVResponse> response = new PageResponse<>();
        if (Objects.isNull(dvPageResponse)) {
            return response;
        }

        response.setTotalPages(dvPageResponse.getTotalPages());
        response.setCurrentPage(dvPageResponse.getCurrentPage());
        response.setTotalItems(dvPageResponse.getTotalItems());

        if (Objects.nonNull(dvPageResponse.getData())) {
            response.setData(dvPageResponse.getData().stream()
                    .map(dv -> modelMapper.map(dv, DVResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public String create(List<DVRequest> requests) {
        List<DV> dvs = requests.stream().map(dv -> modelMapper.map(dv, DV.class)).toList();
        DefaultResponse<List<DV>> response = dvClient.saveAll(dvs);

        dvLogService.writeLogRegister(dvs);
        return response.getMessage();
    }

    public DV findById(String id) {
        return dvClient.findById(id).getData();
    }
}
