package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.DVRecognitionClient;
import com.agribank.qldv_api.gateway.DVRecognitionDraftClient;
import com.agribank.qldv_api.gateway.RequestClient;
import com.agribank.qldv_api.gateway.report26.DeceasedClient;
import com.agribank.qldv_api.gateway.report26.DeceasedDraftClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.deceased.DeceasedRequest;
import com.agribank.qldv_api.request.dvRecognition.DVRecognitionRequest;
import com.agribank.qldv_api.service.handler.EntityHandler;
import com.agribank.qldv_api.service.report26.DeceasedDraftService;
import com.agribank.qldv_api.service.report26.Report26Service;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.DVRecognition;
import com.agribank.qldvutils.entity.DVRecognitionDraft;
import com.agribank.qldvutils.entity.Request;
import com.agribank.qldvutils.entity.TransformationHistoryDraft;
import com.agribank.qldvutils.entity.report26.Deceased;
import com.agribank.qldvutils.entity.report26.DeceasedDraft;
import com.agribank.qldvutils.entity.report26.PartyActivityExemptionDraft;
import com.agribank.qldvutils.entity.report26.Report26;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DVRecognitionDraftService {
    private final DVRecognitionDraftClient client;

    public DVRecognitionDraft save(DVRecognitionDraft deceasedDraft) {
        return client.save(deceasedDraft).getData();
    }

    public DVRecognitionDraft findById(String id) {
        return client.findById(id).getData().orElseThrow(() -> new CommonException("DeceasedDraft not found"));
    }
}
