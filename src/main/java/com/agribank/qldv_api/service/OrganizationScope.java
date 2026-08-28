package com.agribank.qldv_api.service;

import java.util.Collections;
import java.util.Set;

public class OrganizationScope {
    private final boolean full;
    private final Set<String> allowedIds;

    private OrganizationScope(boolean full, Set<String> allowedIds) {
        this.full = full;
        this.allowedIds = allowedIds;
    }

    static OrganizationScope full() {
        return new OrganizationScope(true, Collections.emptySet());
    }

    static OrganizationScope restricted(Set<String> allowedIds) {
        return new OrganizationScope(false, allowedIds);
    }

    public boolean isFull() {
        return full;
    }

    Set<String> getAllowedIds() {
        return allowedIds;
    }

    public boolean isAllowed(String organizationId) {
        return full || allowedIds.contains(organizationId);
    }
}
