package com.worth.ifs.project.status.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.project.bankdetails.domain.BankDetails;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.project.builder.PartnerOrganisationBuilder;
import com.worth.ifs.project.constant.ProjectActivityStates;
import com.worth.ifs.project.domain.MonitoringOfficer;
import com.worth.ifs.project.domain.PartnerOrganisation;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.domain.ProjectUser;
import com.worth.ifs.project.finance.domain.SpendProfile;
import com.worth.ifs.project.resource.ApprovalType;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.status.resource.CompetitionProjectsStatusResource;
import com.worth.ifs.project.status.resource.ProjectStatusResource;
import com.worth.ifs.project.transactional.ProjectStatusService;
import com.worth.ifs.project.transactional.ProjectStatusServiceImpl;
import com.worth.ifs.user.domain.*;
import com.worth.ifs.user.resource.OrganisationTypeEnum;
import com.worth.ifs.user.resource.UserRoleType;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.project.bankdetails.builder.BankDetailsBuilder.newBankDetails;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.finance.builder.ApplicationFinanceBuilder.newApplicationFinance;
import static com.worth.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static com.worth.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static com.worth.ifs.project.builder.MonitoringOfficerBuilder.newMonitoringOfficer;
import static com.worth.ifs.project.builder.ProjectBuilder.newProject;
import static com.worth.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static com.worth.ifs.project.builder.SpendProfileBuilder.newSpendProfile;
import static com.worth.ifs.project.constant.ProjectActivityStates.*;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static com.worth.ifs.user.builder.OrganisationTypeBuilder.newOrganisationType;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.builder.UserBuilder.newUser;
import static com.worth.ifs.user.resource.UserRoleType.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

public class ProjectStatusServiceImplTest extends BaseServiceUnitTest<ProjectStatusService> {

    @Override
    protected ProjectStatusService supplyServiceUnderTest() {
        return new ProjectStatusServiceImpl();
    }

    @Test
    public void testGetCompetitionStatus(){
        Long competitionId = 123L;
        Competition competition = newCompetition().withId(competitionId).build();

        /**
         * Create partner and lead applicant role
         */
        Role leadApplicantRole = newRole().withType(LEADAPPLICANT).build();
        Role applicantRole = newRole().withType(APPLICANT).build();
        Role partnerRole = newRole().withType(PARTNER).build();

        /**
         * Create 3 organisations:
         * 2 Business, 1 Academic
         ***/
        OrganisationType businessOrganisationType = newOrganisationType().withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        OrganisationType academicOrganisationType = newOrganisationType().withOrganisationType(OrganisationTypeEnum.ACADEMIC).build();
        List<Organisation> organisations = newOrganisation().withOrganisationType(businessOrganisationType).build(2);
        organisations.add(newOrganisation().withOrganisationType(academicOrganisationType).build());


        /**
         * Create 3 users, one for each organisation
         */
        List<User> users = newUser().build(3);

        /**
         * Create 3 applications, one for each org, with process roles
         */
        List<ProcessRole> applicantProcessRoles = newProcessRole().withUser(users.get(0), users.get(1), users.get(2)).withRole(leadApplicantRole, applicantRole, applicantRole).withOrganisation(organisations.get(0), organisations.get(1), organisations.get(2)).build(3);
        List<Application> applications = newApplication().withCompetition(competition).withProcessRoles(applicantProcessRoles.get(0), applicantProcessRoles.get(1), applicantProcessRoles.get(2)).build(3);

        /**
         * Create 3 project with 3 Project Users from 3 different organisations with associated applications
         */
        List<ProjectUser> projectUsers = newProjectUser().withRole(PROJECT_PARTNER).withUser(users.get(0), users.get(1), users.get(2)).withOrganisation(organisations.get(0), organisations.get(1), organisations.get(2)).build(3);
        List<Project> projects = newProject().withApplication(applications.get(0), applications.get(1), applications.get(2)).withProjectUsers(projectUsers).build(3);

        /**
         * Create 3 bank detail records, one for each organisation
         */
        List<BankDetails> bankDetails = newBankDetails().withOrganisation(organisations.get(0), organisations.get(1), organisations.get(2)).build(3);

        /**
         * Build spend profile object for use with one of the partners
         */
        SpendProfile spendProfile = newSpendProfile().build();

        when(competitionRepositoryMock.findOne(competitionId)).thenReturn(competition);

        when(projectRepositoryMock.findOne(projects.get(0).getId())).thenReturn(projects.get(0));
        when(projectRepositoryMock.findOne(projects.get(1).getId())).thenReturn(projects.get(1));
        when(projectRepositoryMock.findOne(projects.get(2).getId())).thenReturn(projects.get(2));

        when(projectRepositoryMock.findByApplicationCompetitionId(competitionId)).thenReturn(projects);

        when(projectUserRepositoryMock.findByProjectId(projects.get(0).getId())).thenReturn(projectUsers);
        when(projectUserRepositoryMock.findByProjectId(projects.get(1).getId())).thenReturn(projectUsers);
        when(projectUserRepositoryMock.findByProjectId(projects.get(2).getId())).thenReturn(projectUsers);

        when(bankDetailsRepositoryMock.findByProjectIdAndOrganisationId(projects.get(0).getId(), organisations.get(0).getId())).thenReturn(bankDetails.get(0));
        when(bankDetailsRepositoryMock.findByProjectIdAndOrganisationId(projects.get(1).getId(), organisations.get(1).getId())).thenReturn(bankDetails.get(1));
        when(bankDetailsRepositoryMock.findByProjectIdAndOrganisationId(projects.get(2).getId(), organisations.get(2).getId())).thenReturn(bankDetails.get(2));

        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projects.get(0).getId(), organisations.get(0).getId())).thenReturn(Optional.of(spendProfile));
        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projects.get(1).getId(), organisations.get(1).getId())).thenReturn(Optional.of(spendProfile));
        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projects.get(2).getId(), organisations.get(2).getId())).thenReturn(Optional.of(spendProfile));

        MonitoringOfficer monitoringOfficerInDB = newMonitoringOfficer().build();
        when(monitoringOfficerRepositoryMock.findOneByProjectId(projects.get(0).getId())).thenReturn(monitoringOfficerInDB);
        when(monitoringOfficerRepositoryMock.findOneByProjectId(projects.get(1).getId())).thenReturn(monitoringOfficerInDB);
        when(monitoringOfficerRepositoryMock.findOneByProjectId(projects.get(2).getId())).thenReturn(monitoringOfficerInDB);

        when(organisationRepositoryMock.findOne(organisations.get(0).getId())).thenReturn(organisations.get(0));
        when(organisationRepositoryMock.findOne(organisations.get(1).getId())).thenReturn(organisations.get(1));
        when(organisationRepositoryMock.findOne(organisations.get(2).getId())).thenReturn(organisations.get(2));

        List<ApplicationFinance> applicationFinances = newApplicationFinance().build(3);
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(0).getApplication().getId(), organisations.get(0).getId())).thenReturn(applicationFinances.get(0));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(0).getApplication().getId(), organisations.get(1).getId())).thenReturn(applicationFinances.get(1));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(0).getApplication().getId(), organisations.get(2).getId())).thenReturn(applicationFinances.get(2));

        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(1).getApplication().getId(), organisations.get(0).getId())).thenReturn(applicationFinances.get(0));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(1).getApplication().getId(), organisations.get(1).getId())).thenReturn(applicationFinances.get(1));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(1).getApplication().getId(), organisations.get(2).getId())).thenReturn(applicationFinances.get(2));

        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(2).getApplication().getId(), organisations.get(0).getId())).thenReturn(applicationFinances.get(0));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(2).getApplication().getId(), organisations.get(1).getId())).thenReturn(applicationFinances.get(1));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(projects.get(2).getApplication().getId(), organisations.get(2).getId())).thenReturn(applicationFinances.get(2));

        ApplicationFinanceResource applicationFinanceResource0 = newApplicationFinanceResource().withGrantClaimPercentage(20).withOrganisation(organisations.get(0).getId()).build();
        when(applicationFinanceMapperMock.mapToResource(applicationFinances.get(0))).thenReturn(applicationFinanceResource0);

        ApplicationFinanceResource applicationFinanceResource1 = newApplicationFinanceResource().withGrantClaimPercentage(20).withOrganisation(organisations.get(1).getId()).build();
        when(applicationFinanceMapperMock.mapToResource(applicationFinances.get(1))).thenReturn(applicationFinanceResource1);

        ApplicationFinanceResource applicationFinanceResource2 = newApplicationFinanceResource().withGrantClaimPercentage(20).withOrganisation(organisations.get(2).getId()).build();
        when(applicationFinanceMapperMock.mapToResource(applicationFinances.get(2))).thenReturn(applicationFinanceResource2);

        List<ProjectUserResource> puResource = newProjectUserResource().withProject(projects.get(0).getId()).withOrganisation(organisations.get(0).getId(), organisations.get(1).getId(), organisations.get(2).getId()).withRole(partnerRole.getId()).withRoleName(PROJECT_PARTNER.getName()).build(3);

        when(projectUserMapperMock.mapToResource(projectUsers.get(0))).thenReturn(puResource.get(0));
        when(projectUserMapperMock.mapToResource(projectUsers.get(1))).thenReturn(puResource.get(1));
        when(projectUserMapperMock.mapToResource(projectUsers.get(2))).thenReturn(puResource.get(2));

        when(financeRowServiceMock.organisationSeeksFunding(projects.get(0).getId(), projects.get(0).getApplication().getId(), organisations.get(0).getId())).thenReturn(serviceSuccess(true));
        when(financeRowServiceMock.organisationSeeksFunding(projects.get(0).getId(), projects.get(0).getApplication().getId(), organisations.get(1).getId())).thenReturn(serviceSuccess(false));
        when(financeRowServiceMock.organisationSeeksFunding(projects.get(0).getId(), projects.get(0).getApplication().getId(), organisations.get(2).getId())).thenReturn(serviceSuccess(false));

        when(financeRowServiceMock.organisationSeeksFunding(projects.get(1).getId(), projects.get(1).getApplication().getId(), organisations.get(0).getId())).thenReturn(serviceSuccess(true));
        when(financeRowServiceMock.organisationSeeksFunding(projects.get(1).getId(), projects.get(1).getApplication().getId(), organisations.get(1).getId())).thenReturn(serviceSuccess(false));
        when(financeRowServiceMock.organisationSeeksFunding(projects.get(1).getId(), projects.get(1).getApplication().getId(), organisations.get(2).getId())).thenReturn(serviceSuccess(false));

        when(financeRowServiceMock.organisationSeeksFunding(projects.get(2).getId(), projects.get(2).getApplication().getId(), organisations.get(0).getId())).thenReturn(serviceSuccess(true));
        when(financeRowServiceMock.organisationSeeksFunding(projects.get(2).getId(), projects.get(2).getApplication().getId(), organisations.get(1).getId())).thenReturn(serviceSuccess(false));
        when(financeRowServiceMock.organisationSeeksFunding(projects.get(2).getId(), projects.get(2).getApplication().getId(), organisations.get(2).getId())).thenReturn(serviceSuccess(false));

        when(projectUsersHelperMock.getPartnerOrganisations(projects.get(0).getId())).thenReturn(organisations);
        when(projectUsersHelperMock.getPartnerOrganisations(projects.get(1).getId())).thenReturn(organisations);
        when(projectUsersHelperMock.getPartnerOrganisations(projects.get(2).getId())).thenReturn(organisations);

        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(projects.get(0).getId())).thenReturn(serviceSuccess(ApprovalType.EMPTY));
        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(projects.get(1).getId())).thenReturn(serviceSuccess(ApprovalType.EMPTY));
        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(projects.get(2).getId())).thenReturn(serviceSuccess(ApprovalType.EMPTY));

        ServiceResult<CompetitionProjectsStatusResource> result = service.getCompetitionStatus(competitionId);

        assertTrue(result.isSuccess());

        CompetitionProjectsStatusResource competitionProjectsStatusResource = result.getSuccessObject();
        assertEquals(3, competitionProjectsStatusResource.getProjectStatusResources().size());
        assertEquals(new Integer(3), competitionProjectsStatusResource.getProjectStatusResources().get(0).getNumberOfPartners());
        assertEquals(new Integer(3), competitionProjectsStatusResource.getProjectStatusResources().get(1).getNumberOfPartners());
        assertEquals(new Integer(3), competitionProjectsStatusResource.getProjectStatusResources().get(2).getNumberOfPartners());
    }

    @Test
    public void getProjectStatusResourceByProject() {
        Long projectId = 2345L;

        Project project = createProjectStatusResource(projectId, ApprovalType.EMPTY, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);

        ServiceResult<ProjectStatusResource> result = service.getProjectStatusByProjectId(projectId);

        ProjectStatusResource returnedProjectStatusResource = result.getSuccessObject();
        assertTrue(result.isSuccess());
        assertEquals(project.getName(), returnedProjectStatusResource.getProjectTitle());
        assertEquals(project.getId(), returnedProjectStatusResource.getProjectNumber());
        assertEquals(Integer.valueOf(1), returnedProjectStatusResource.getNumberOfPartners());

        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getProjectDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getBankDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getFinanceChecksStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getSpendProfileStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getMonitoringOfficerStatus());
        assertEquals(PENDING, returnedProjectStatusResource.getOtherDocumentsStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getGrantOfferLetterStatus());
        HashMap<UserRoleType, ProjectActivityStates> roles = new HashMap<UserRoleType, ProjectActivityStates>();
        roles.put(COMP_ADMIN, NOT_REQUIRED);
        assertTrue(roles.equals(returnedProjectStatusResource.getRoleSpecificGrantOfferLetterState()));


        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<ProjectStatusResource> resultFailure = service.getProjectStatusByProjectId(projectId);
        assertTrue(resultFailure.isFailure());
    }

    @Test
    public void getProjectStatusResourceByProjectGolPrecursorsCompleteAndGolApproved() {
        Long projectId = 2345L;

        Project project = createProjectStatusResource(projectId, ApprovalType.APPROVED, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, LocalDateTime.now());


        ServiceResult<ProjectStatusResource> result = service.getProjectStatusByProjectId(projectId);

        ProjectStatusResource returnedProjectStatusResource = result.getSuccessObject();
        assertTrue(result.isSuccess());
        assertEquals(project.getName(), returnedProjectStatusResource.getProjectTitle());
        assertEquals(project.getId(), returnedProjectStatusResource.getProjectNumber());
        assertEquals(Integer.valueOf(1), returnedProjectStatusResource.getNumberOfPartners());

        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getProjectDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getBankDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getFinanceChecksStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getSpendProfileStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getMonitoringOfficerStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getOtherDocumentsStatus());
        assertEquals(PENDING, returnedProjectStatusResource.getGrantOfferLetterStatus());

        HashMap<UserRoleType, ProjectActivityStates> roles = new HashMap<UserRoleType, ProjectActivityStates>();
        roles.put(COMP_ADMIN, COMPLETE);
        assertTrue(roles.equals(returnedProjectStatusResource.getRoleSpecificGrantOfferLetterState()));


        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<ProjectStatusResource> resultFailure = service.getProjectStatusByProjectId(projectId);
        assertTrue(resultFailure.isFailure());
    }

    @Test
    public void getProjectStatusResourceByProjectGolPrecursorsCompleteAndGolSent() {
        Long projectId = 2345L;

        Project project = createProjectStatusResource(projectId, ApprovalType.APPROVED, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, LocalDateTime.now());

        ServiceResult<ProjectStatusResource> result = service.getProjectStatusByProjectId(projectId);

        ProjectStatusResource returnedProjectStatusResource = result.getSuccessObject();
        assertTrue(result.isSuccess());
        assertEquals(project.getName(), returnedProjectStatusResource.getProjectTitle());
        assertEquals(project.getId(), returnedProjectStatusResource.getProjectNumber());
        assertEquals(Integer.valueOf(1), returnedProjectStatusResource.getNumberOfPartners());

        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getProjectDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getBankDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getFinanceChecksStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getSpendProfileStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getMonitoringOfficerStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getOtherDocumentsStatus());
        assertEquals(PENDING, returnedProjectStatusResource.getGrantOfferLetterStatus());

        HashMap<UserRoleType, ProjectActivityStates> roles = new HashMap<UserRoleType, ProjectActivityStates>();
        roles.put(COMP_ADMIN, PENDING);
        assertTrue(roles.equals(returnedProjectStatusResource.getRoleSpecificGrantOfferLetterState()));


        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<ProjectStatusResource> resultFailure = service.getProjectStatusByProjectId(projectId);
        assertTrue(resultFailure.isFailure());
    }

    @Test
    public void getProjectStatusResourceByProjectGolPrecursorsCompleteAndSignedGolSubmitted() {
        Long projectId = 2345L;

        Project project = createProjectStatusResource(projectId, ApprovalType.APPROVED, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, LocalDateTime.now());

        ServiceResult<ProjectStatusResource> result = service.getProjectStatusByProjectId(projectId);

        ProjectStatusResource returnedProjectStatusResource = result.getSuccessObject();
        assertTrue(result.isSuccess());
        assertEquals(project.getName(), returnedProjectStatusResource.getProjectTitle());
        assertEquals(project.getId(), returnedProjectStatusResource.getProjectNumber());
        assertEquals(Integer.valueOf(1), returnedProjectStatusResource.getNumberOfPartners());

        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getProjectDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getBankDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getFinanceChecksStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getSpendProfileStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getMonitoringOfficerStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getOtherDocumentsStatus());
        assertEquals(PENDING, returnedProjectStatusResource.getGrantOfferLetterStatus());

        HashMap<UserRoleType, ProjectActivityStates> roles = new HashMap<UserRoleType, ProjectActivityStates>();
        roles.put(COMP_ADMIN, ACTION_REQUIRED);
        assertTrue(roles.equals(returnedProjectStatusResource.getRoleSpecificGrantOfferLetterState()));


        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<ProjectStatusResource> resultFailure = service.getProjectStatusByProjectId(projectId);
        assertTrue(resultFailure.isFailure());
    }

    @Test
    public void getProjectStatusResourceByProjectGolPrecursorsCompleteAndGolReadyToSend() {
        Long projectId = 2345L;

        Project project = createProjectStatusResource(projectId, ApprovalType.APPROVED, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, LocalDateTime.now());

        ServiceResult<ProjectStatusResource> result = service.getProjectStatusByProjectId(projectId);

        ProjectStatusResource returnedProjectStatusResource = result.getSuccessObject();
        assertTrue(result.isSuccess());
        assertEquals(project.getName(), returnedProjectStatusResource.getProjectTitle());
        assertEquals(project.getId(), returnedProjectStatusResource.getProjectNumber());
        assertEquals(Integer.valueOf(1), returnedProjectStatusResource.getNumberOfPartners());

        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getProjectDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getBankDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getFinanceChecksStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getSpendProfileStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getMonitoringOfficerStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getOtherDocumentsStatus());
        assertEquals(PENDING, returnedProjectStatusResource.getGrantOfferLetterStatus());

        HashMap<UserRoleType, ProjectActivityStates> roles = new HashMap<UserRoleType, ProjectActivityStates>();
        roles.put(COMP_ADMIN, ACTION_REQUIRED);
        assertTrue(roles.equals(returnedProjectStatusResource.getRoleSpecificGrantOfferLetterState()));


        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<ProjectStatusResource> resultFailure = service.getProjectStatusByProjectId(projectId);
        assertTrue(resultFailure.isFailure());
    }

    @Test
    public void getProjectStatusResourceByProjectOtherDocumentsRejected() {
        Long projectId = 2345L;

        Project project = createProjectStatusResource(projectId, ApprovalType.APPROVED, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, LocalDateTime.now());

        ServiceResult<ProjectStatusResource> result = service.getProjectStatusByProjectId(projectId);

        ProjectStatusResource returnedProjectStatusResource = result.getSuccessObject();
        assertTrue(result.isSuccess());
        assertEquals(project.getName(), returnedProjectStatusResource.getProjectTitle());
        assertEquals(project.getId(), returnedProjectStatusResource.getProjectNumber());
        assertEquals(Integer.valueOf(1), returnedProjectStatusResource.getNumberOfPartners());

        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getProjectDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getBankDetailsStatus());
        assertEquals(ACTION_REQUIRED, returnedProjectStatusResource.getFinanceChecksStatus());
        assertEquals(COMPLETE, returnedProjectStatusResource.getSpendProfileStatus());
        assertEquals(NOT_STARTED, returnedProjectStatusResource.getMonitoringOfficerStatus());
        assertEquals(REJECTED, returnedProjectStatusResource.getOtherDocumentsStatus());
        assertEquals(PENDING, returnedProjectStatusResource.getGrantOfferLetterStatus());

        HashMap<UserRoleType, ProjectActivityStates> roles = new HashMap<UserRoleType, ProjectActivityStates>();
        roles.put(COMP_ADMIN, NOT_REQUIRED);
        assertTrue(roles.equals(returnedProjectStatusResource.getRoleSpecificGrantOfferLetterState()));


        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        ServiceResult<ProjectStatusResource> resultFailure = service.getProjectStatusByProjectId(projectId);
        assertTrue(resultFailure.isFailure());
    }

    private Project createProjectStatusResource(Long projectId, ApprovalType spendProfileStatus, Boolean otherDocsApproved, Boolean golReadyToApprove, Boolean golIsSent, Boolean golIsApproved, LocalDateTime otherDocsSubmittedDate) {

        Role role = newRole().build();
        ProcessRole processRole = newProcessRole().withRole(role).build();
        Application application = newApplication().withProcessRoles(processRole).build();

        Organisation organisation = newOrganisation().build();
        PartnerOrganisation partnerOrganisation = PartnerOrganisationBuilder.newPartnerOrganisation().withOrganisation(organisation).build();
        Project project;
        if(otherDocsSubmittedDate != null)
            project = newProject().withId(projectId).withApplication(application).withPartnerOrganisations(asList(partnerOrganisation)).withOtherDocumentsApproved(otherDocsApproved).withOtherDocumentsSubmittedDate(otherDocsSubmittedDate).build();
        else
            project = newProject().withId(projectId).withApplication(application).withPartnerOrganisations(asList(partnerOrganisation)).withOtherDocumentsApproved(otherDocsApproved).build();
        BankDetails bankDetail = newBankDetails().withProject(project).build();
        SpendProfile spendprofile = newSpendProfile().withOrganisation(organisation).build();
        MonitoringOfficer monitoringOfficer = newMonitoringOfficer().build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(projectId)).thenReturn(serviceSuccess(ApprovalType.EMPTY));
        when(projectUsersHelperMock.getPartnerOrganisations(project.getId())).thenReturn(asList(organisation));
        when(bankDetailsRepositoryMock.findByProjectIdAndOrganisationId(project.getId(), organisation.getId())).thenReturn(bankDetail);
        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(project.getId(), organisation.getId())).thenReturn(Optional.of(spendprofile));
        when(monitoringOfficerRepositoryMock.findOneByProjectId(project.getId())).thenReturn(monitoringOfficer);

        when(projectFinanceServiceMock.getSpendProfileStatusByProjectId(projectId)).thenReturn(serviceSuccess(spendProfileStatus));
        when(golWorkflowHandlerMock.isApproved(project)).thenReturn(golIsApproved);
        if(!golIsApproved) {
            when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(golReadyToApprove);
            if (!golReadyToApprove)
                when(golWorkflowHandlerMock.isSent(project)).thenReturn(golIsSent);
        }
        return project;
    }
}
