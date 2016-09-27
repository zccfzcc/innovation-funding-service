package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.commons.security.NotSecured;
import com.worth.ifs.commons.security.SecuredBySpring;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.finance.domain.FinanceCheck;
import com.worth.ifs.project.finance.resource.FinanceCheckResource;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * A service for finance check functionality
 */
public interface FinanceCheckService {

    @PreAuthorize("hasAuthority('project_finance')")
    @SecuredBySpring(value = "VIEW", securedType = FinanceCheck.class, description = "Project finance user should be able to view any finance check")
    ServiceResult<FinanceCheckResource> getById(Long id);

    @PreAuthorize("hasAuthority('project_finance')")
    @SecuredBySpring(value = "EDIT", securedType = FinanceCheck.class, description = "Project finance user should be able to edit any finance check")
    ServiceResult<FinanceCheckResource> save(FinanceCheckResource toUpdate);

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    @SecuredBySpring(value = "GENERATE", securedType = FinanceCheck.class, description = "Comp admins and project finance users can generate finance checks" )
    ServiceResult<FinanceCheckResource> generate(Long projectId);
}
