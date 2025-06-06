package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.EmployeeInfoClient;
import com.agribank.qldv_api.gateway.MembershipProposalClient;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.entity.MembershipProposal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployeeInfoService {
    private final EmployeeInfoClient employeeInfoClient;
    private final MembershipProposalClient membershipProposalClient;

    public EmployeeInfoDto findByEmpno(String empno) {
        EmployeeInfoDto employeeInfo = employeeInfoClient.findByEmpno(empno).getData();
        MembershipProposal membershipProposal = membershipProposalClient.findByStaffCode(empno).getData();

        if (Objects.nonNull(membershipProposal)) {

            if (Objects.isNull(employeeInfo.getFullName())) {
                employeeInfo.setFullName(membershipProposal.getFullName());
            }

            if (Objects.nonNull(membershipProposal.getDecisionDate())) {
                employeeInfo.setAdmissionDate(membershipProposal.getDecisionDate());
            }
        }

        return employeeInfo;
    }
}
