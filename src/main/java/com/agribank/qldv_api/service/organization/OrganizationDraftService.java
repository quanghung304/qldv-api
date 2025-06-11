package com.agribank.qldv_api.service.organization;

import com.agribank.qldv_api.gateway.OrganizationDraftClient;
import com.agribank.qldvutils.entity.OrganizationDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrganizationDraftService {
    private final OrganizationDraftClient client;

    public OrganizationDraft save(OrganizationDraft organizationDraft) {
        return client.save(organizationDraft).getData();
    }

    public OrganizationDraft findById(String id) {
        return client.findById(id).getData().orElse(null);
    }

    public List<OrganizationDraft> saveAll(List<OrganizationDraft> organizationDrafts) {
        return client.saveAll(organizationDrafts).getData();
    }
}
