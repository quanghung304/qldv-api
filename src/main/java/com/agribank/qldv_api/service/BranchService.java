package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.response.branch.BranchChildResponse;
import com.agribank.qldv_api.response.branch.BranchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final IAMClient client;

    public List<BranchResponse> getAll(){
        return client.getAllBranch().getData();
    }

    public BranchChildResponse getBranchChild(Integer brcd){
        List<Integer> brcds = new ArrayList<>();
        brcds.add(brcd);
        try {
            List<BranchChildResponse> branchChildResponses = client.getBranchChildInfo(brcds).getData();
            if (Objects.nonNull(branchChildResponses) || !branchChildResponses.isEmpty()) {
                return branchChildResponses.get(0);
            }
            return null;
        }catch (Exception e){
            System.out.println("getBranchChild error");
            return null;
        }
    }

    /** Tên chi nhánh (tiếng Việt, lclbrnm) theo brcd — dùng cho BranchNameByBrcdResolver (S2-03, placeholder EXTERNAL_LOOKUP). */
    public String getBranchName(Integer brcd) {
        List<BranchResponse> branchResponses = client.getBranchInfo(List.of(brcd)).getData();
        if (branchResponses == null || branchResponses.isEmpty()) {
            return null;
        }
        return branchResponses.get(0).getLclbrnm();
    }
}
