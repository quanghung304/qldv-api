package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.response.user.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final IAMClient client;

    public List<DepartmentResponse> get(Integer brcd){
        return client.getDepartment(brcd).getData();
    }
}
