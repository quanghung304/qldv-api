package com.agribank.qldv_api.controller;

import com.agribank.qldv_api.response.committee_decision.CommitteeDecisionResponse;
import com.agribank.qldv_api.service.CommitteeDecisionService;
import com.agribank.qldvutils.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/committee-decision")
public class CommitteeDecisionController {
    private final CommitteeDecisionService service;

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<CommitteeDecisionResponse>>> getAll() {
        return BaseResponse.success(service.getAll());
    }
}
