package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.ReligionClient;
import com.agribank.qldv_api.response.religion.ReligionResponse;
import com.agribank.qldvutils.entity.Religion;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReligionService {
    private final ReligionClient client;
    private final ModelMapper modelMapper;

    public List<ReligionResponse> findAll() {
        List<Religion> religionList = client.findAll().getData();

        return religionList.stream()
                .map(x -> modelMapper.map(x, ReligionResponse.class))
                .toList();
    }
}
