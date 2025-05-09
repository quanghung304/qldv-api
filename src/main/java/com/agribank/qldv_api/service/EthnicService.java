package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.EthnicClient;
import com.agribank.qldv_api.response.ethnic.EthnicResponse;
import com.agribank.qldvutils.entity.Ethnic;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EthnicService {
    private final EthnicClient client;
    private final ModelMapper modelMapper;

    public List<EthnicResponse> getAll(){
        List<Ethnic> ethnics = client.findAll().getData();

        if (ethnics.isEmpty()){
            return null;
        }

        return ethnics.stream().map(e -> modelMapper.map(e, EthnicResponse.class)).toList();
    }
}
