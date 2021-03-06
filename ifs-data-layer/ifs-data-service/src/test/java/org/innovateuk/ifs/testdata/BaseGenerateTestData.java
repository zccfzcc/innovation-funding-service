package org.innovateuk.ifs.testdata;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.flywaydb.core.Flyway;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.authentication.service.IdentityProviderService;
import org.innovateuk.ifs.commons.BaseIntegrationTest;
import org.innovateuk.ifs.email.resource.EmailAddress;
import org.innovateuk.ifs.email.service.EmailService;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.notifications.service.senders.NotificationSender;
import org.innovateuk.ifs.notifications.service.senders.email.EmailNotificationSender;
import org.innovateuk.ifs.organisation.transactional.OrganisationService;
import org.innovateuk.ifs.project.bankdetails.transactional.BankDetailsService;
import org.innovateuk.ifs.publiccontent.repository.PublicContentRepository;
import org.innovateuk.ifs.sil.experian.resource.AccountDetails;
import org.innovateuk.ifs.sil.experian.resource.SILBankDetails;
import org.innovateuk.ifs.sil.experian.resource.ValidationResult;
import org.innovateuk.ifs.sil.experian.resource.VerificationResult;
import org.innovateuk.ifs.sil.experian.service.SilExperianEndpoint;
import org.innovateuk.ifs.testdata.builders.*;
import org.innovateuk.ifs.testdata.builders.data.BaseUserData;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.transactional.RegistrationService;
import org.innovateuk.ifs.user.transactional.UserService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.testdata.CsvUtils.*;
import static org.innovateuk.ifs.testdata.builders.AssessmentDataBuilder.newAssessmentData;
import static org.innovateuk.ifs.testdata.builders.AssessorDataBuilder.newAssessorData;
import static org.innovateuk.ifs.testdata.builders.AssessorInviteDataBuilder.newAssessorInviteData;
import static org.innovateuk.ifs.testdata.builders.AssessorResponseDataBuilder.newAssessorResponseData;
import static org.innovateuk.ifs.testdata.builders.BaseDataBuilder.COMP_ADMIN_EMAIL;
import static org.innovateuk.ifs.testdata.builders.CompetitionDataBuilder.newCompetitionData;
import static org.innovateuk.ifs.testdata.builders.CompetitionFunderDataBuilder.newCompetitionFunderData;
import static org.innovateuk.ifs.testdata.builders.ExternalUserDataBuilder.newExternalUserData;
import static org.innovateuk.ifs.testdata.builders.InternalUserDataBuilder.newInternalUserData;
import static org.innovateuk.ifs.testdata.builders.OrganisationDataBuilder.newOrganisationData;
import static org.innovateuk.ifs.testdata.builders.ProjectDataBuilder.newProjectData;
import static org.innovateuk.ifs.testdata.builders.PublicContentDateDataBuilder.newPublicContentDateDataBuilder;
import static org.innovateuk.ifs.testdata.builders.PublicContentGroupDataBuilder.newPublicContentGroupDataBuilder;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.*;
import static org.innovateuk.ifs.util.CollectionFunctions.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Generates web test data based upon csvs in /src/test/resources/testdata using data builders
 */
@ActiveProfiles({"integration-test,seeding-db"})
@DirtiesContext
abstract class BaseGenerateTestData extends BaseIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseGenerateTestData.class);

    @Value("${flyway.url}")
    private String databaseUrl;

    @Value("${flyway.user}")
    private String databaseUser;

    @Value("${flyway.password}")
    private String databasePassword;

    @Value("${flyway.locations}")
    private String locations;

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    protected OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private NotificationSender emailNotificationSender;

    @Autowired
    private BankDetailsService bankDetailsService;

    @Autowired
    protected PublicContentRepository publicContentRepository;

    @Autowired
    private TestService testService;

    private CompetitionDataBuilder competitionDataBuilder;
    private CompetitionFunderDataBuilder competitionFunderDataBuilder;
    private PublicContentGroupDataBuilder publicContentGroupDataBuilder;
    private PublicContentDateDataBuilder publicContentDateDataBuilder;
    private ExternalUserDataBuilder externalUserBuilder;
    private InternalUserDataBuilder internalUserBuilder;
    private OrganisationDataBuilder organisationBuilder;
    private AssessorDataBuilder assessorUserBuilder;
    private AssessorInviteDataBuilder assessorInviteUserBuilder;
    private AssessmentDataBuilder assessmentDataBuilder;
    private AssessorResponseDataBuilder assessorResponseDataBuilder;
    private ProjectDataBuilder projectDataBuilder;

    private static List<OrganisationLine> organisationLines;

    /**
     * select "Competition", "Application", "Question", "Answer", "File upload", "Answered by", "Assigned to", "Marked as complete" UNION ALL select c.name, a.name, q.name, fir.value, fir.file_entry_id, updater.email, assignee.email, qs.marked_as_complete from competition c join application a on a.competition = c.id join question q on q.competition_id = c.id join form_input fi on fi.question_id = q.id join form_input_type fit on fi.form_input_type_id = fit.id left join form_input_response fir on fir.form_input_id = fi.id left join process_role updaterrole on updaterrole.id = fir.updated_by_id left join user updater on updater.id = updaterrole.user_id join question_status qs on qs.application_id = a.id and qs.question_id = q.id left join process_role assigneerole on assigneerole.id = qs.assignee_id left join user assignee on assignee.id = assigneerole.user_id where fit.title in ('textinput','textarea','date','fileupload','percentage') INTO OUTFILE '/var/lib/mysql-files/application-questions3.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n';
     */
    private static List<CompetitionLine> competitionLines;

    private static List<CompetitionFunderLine> competitionFunderLines;

    private static List<PublicContentGroupLine> publicContentGroupLines;

    private static List<PublicContentDateLine> publicContentDateLines;

    private static List<ApplicationLine> applicationLines;

    /**
     * select "Email", "Hash", "Name", "Status", "Type", "Target", "Owner" UNION ALL select i.email, i.hash, i.name, i.status, i.type, i.target_id, i.owner_id from invite i where not exists (select 1 from user where email = i.email) into OUTFILE '/tmp/invites3.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '';
     */
    private static List<InviteLine> inviteLines;

    /**
     * select "Email","First name","Last name","Status","Organisation name","Organisation type","Organisation address line 1", "Line 2","Line3","Town or city","Postcode","County","Address Type" UNION ALL SELECT u.email, u.first_name, u.last_name, u.status, o.name, ot.name, a.address_line1, a.address_line2, a.address_line3, a.town, a.postcode, a.county, at.name from user u join user_organisation uo on uo.user_id = u.id join organisation o on o.id = uo.organisation_id join organisation_type ot on ot.id = o.organisation_type_id join user_role ur on ur.user_id = u.id and ur.role_id = 4 left join organisation_address oa on oa.organisation_id = o.id left join address a on oa.address_id = a.id left join address_type at on at.id = oa.address_type_id INTO OUTFILE '/var/lib/mysql-files/external-users8.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n';
     */
    private static List<ExternalUserLine> externalUserLines;

    private static List<AssessorUserLine> assessorUserLines;

    /**
     * select "Email","First name","Last name","Status","Organisation name","Organisation type","Organisation address line 1", "Line 2","Line3","Town or city","Postcode","County","Address Type","Role" UNION ALL SELECT u.email, u.first_name, u.last_name, u.status, o.name, ot.name, a.address_line1, a.address_line2, a.address_line3, a.town, a.postcode, a.county, at.name, r.name from user u join user_organisation uo on uo.user_id = u.id join organisation o on o.id = uo.organisation_id join organisation_type ot on ot.id = o.organisation_type_id left join organisation_address oa on oa.organisation_id = o.id left join address a on oa.address_id = a.id left join address_type at on at.id = oa.address_type_id join user_role ur on ur.user_id = u.id and ur.role_id != 4 join role r on ur.role_id = r.id INTO OUTFILE '/var/lib/mysql-files/internal-users3.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n';
     **/
    private static List<InternalUserLine> internalUserLines;

    /**
     * select "Competition", "Application", "Question", "Answer", "File upload", "Answered by", "Assigned to", "Marked as complete" UNION ALL select c.name, a.name, q.name, fir.value, fir.file_entry_id, updater.email, assignee.email, qs.marked_as_complete from competition c join application a on a.competition = c.id join question q on q.competition_id = c.id join form_input fi on fi.question_id = q.id join form_input_type fit on fi.form_input_type_id = fit.id left join form_input_response fir on fir.form_input_id = fi.id left join process_role updaterrole on updaterrole.id = fir.updated_by_id left join user updater on updater.id = updaterrole.user_id left join question_status qs on qs.application_id = a.id and qs.question_id = q.id left join process_role assigneerole on assigneerole.id = qs.assignee_id left join user assignee on assignee.id = assigneerole.user_id where fit.title in ('textinput','textarea','date','fileupload','percentage') order by 1,2,3 INTO OUTFILE '/var/lib/mysql-files/application-questions5.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n';
     * <p>
     * And applied search-and-replace for:
     * <p>
     * How many balls can you juggle?
     * What is the size of the potential market for your project?
     * <p>
     * What do you wear when juggling?
     * How will you exploit and market your project?
     * <p>
     * What is your preferred juggling pattern?
     * What economic, social and environmental benefits do you expect your project to deliver and when?
     * <p>
     * What mediums can you juggle with?
     * What technical approach will you use and how will you manage your project?
     * <p>
     * Fifth Question
     * What is innovative about your project?
     */
    private static List<ApplicationQuestionResponseLine> questionResponseLines;

    /**
     * select "Competition name", "Application name", "Organisation name", "Category" union all select row.compname, row.appname, row.orgname, '' from (select distinct o.name as orgname, a.name as appname, c.name as compname, '' from competition c join application a on a.competition = c.id join process_role pr on pr.role_id in (1,2) and pr.application_id = a.id join organisation o on o.id = pr.organisation_id group by c.name, a.name, o.name) as row  INTO OUTFILE '/var/lib/mysql-files/application-finances.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n';
     */
    private static List<ApplicationOrganisationFinanceBlock> applicationFinanceLines;

    private static List<AssessmentLine> assessmentLines;

    private static List<AssessorResponseLine> assessorResponseLines;

    private static List<ProjectLine> projectLines;

    @Before
    public void setup() throws Exception {
        if (cleanDbFirst()) {
            freshDb();
        }
    }

    @Before
    public void readCsvs() throws Exception {
        organisationLines = readOrganisations();
        competitionLines = readCompetitions();
        competitionFunderLines = readCompetitionFunders();
        publicContentGroupLines = readPublicContentGroups();
        publicContentDateLines = readPublicContentDates();
        applicationLines = readApplications();
        inviteLines = readInvites();
        externalUserLines = readExternalUsers();
        internalUserLines = readInternalUsers();
        assessorUserLines = readAssessorUsers();
        questionResponseLines = readApplicationQuestionResponses();
        applicationFinanceLines = readApplicationFinances();
        assessmentLines = readAssessments();
        assessorResponseLines = readAssessorResponses();
        projectLines = readProjects();
    }

    @PostConstruct
    public void replaceExternalDependencies() {

        IdentityProviderService idpServiceMock = mock(IdentityProviderService.class);
        EmailService emailServiceMock = mock(EmailService.class);
        SilExperianEndpoint silExperianEndpointMock = mock(SilExperianEndpoint.class);

        when(idpServiceMock.createUserRecordWithUid(isA(String.class), isA(String.class))).thenAnswer(
                user -> serviceSuccess(UUID.randomUUID().toString()));
        when(idpServiceMock.activateUser(isA(String.class))).thenAnswer(uuid -> serviceSuccess(uuid));
        when(idpServiceMock.deactivateUser(isA(String.class))).thenAnswer(uuid -> serviceSuccess(uuid));

        when(emailServiceMock.sendEmail(isA(EmailAddress.class), isA(List.class), isA(String.class), isA(String.class), isA(String.class))).
                thenReturn(serviceSuccess(emptyList()));

        when(silExperianEndpointMock.validate(isA(SILBankDetails.class))).thenReturn(serviceSuccess(new ValidationResult(true, "", emptyList())));
        when(silExperianEndpointMock.verify(isA(AccountDetails.class))).thenReturn(serviceSuccess(new VerificationResult("10", "10", "10", "10", emptyList())));

        RegistrationService registrationServiceUnwrapped = (RegistrationService) unwrapProxy(registrationService);
        ReflectionTestUtils.setField(registrationServiceUnwrapped, "idpService", idpServiceMock);

        EmailNotificationSender notificationSenderUnwrapped = (EmailNotificationSender) unwrapProxy(emailNotificationSender);
        ReflectionTestUtils.setField(notificationSenderUnwrapped, "emailService", emailServiceMock);

        BankDetailsService bankDetailsServiceUnwrapped = (BankDetailsService) unwrapProxy(bankDetailsService);
        ReflectionTestUtils.setField(bankDetailsServiceUnwrapped, "silExperianEndpoint", silExperianEndpointMock);
    }

    @PostConstruct
    public void setupBaseBuilders() {

        ServiceLocator serviceLocator = new ServiceLocator(applicationContext);

        competitionDataBuilder = newCompetitionData(serviceLocator);
        competitionFunderDataBuilder = newCompetitionFunderData(serviceLocator);
        externalUserBuilder = newExternalUserData(serviceLocator);
        internalUserBuilder = newInternalUserData(serviceLocator);
        organisationBuilder = newOrganisationData(serviceLocator);
        assessorUserBuilder = newAssessorData(serviceLocator);
        assessorInviteUserBuilder = newAssessorInviteData(serviceLocator);
        assessmentDataBuilder = newAssessmentData(serviceLocator);
        assessorResponseDataBuilder = newAssessorResponseData(serviceLocator);
        projectDataBuilder = newProjectData(serviceLocator);
        publicContentGroupDataBuilder = newPublicContentGroupDataBuilder(serviceLocator);
        publicContentDateDataBuilder = newPublicContentDateDataBuilder(serviceLocator);
    }

    @Test
    public void generateTestData() throws IOException {

        long before = System.currentTimeMillis();
        fixUpDatabase();
        createInternalUsers();
        createExternalUsers();
        createCompetitions();
        createCompetitionFunders();
        createPublicContentGroups();
        createPublicContentDates();
        createAssessors();
        createNonRegisteredAssessorInvites();
        createAssessments();
        createProjects();

        long after = System.currentTimeMillis();

        LOG.info("Finished generating data in " + ((after - before) / 1000) + " seconds");
        System.out.println("Finished generating data in " + ((after - before) / 1000) + " seconds");
    }

    private void createProjects() {
        projectLines.forEach(this::createProject);
    }

    private void createProject(ProjectLine line) {

        ProjectDataBuilder baseBuilder = this.projectDataBuilder.
                withExistingProject(line.name).
                withStartDate(line.startDate);

        UnaryOperator<ProjectDataBuilder> assignProjectManagerIfNecessary =
                builder -> !isBlank(line.projectManager) ? builder.withProjectManager(line.projectManager) : builder;

        UnaryOperator<ProjectDataBuilder> setProjectAddressIfNecessary =
                builder -> line.projectAddressAdded ? builder.withProjectAddressOrganisationAddress() : builder;

        UnaryOperator<ProjectDataBuilder> setMonitoringOfficerIfNecessary =
                builder -> !isBlank(line.moFirstName) ?
                        builder.withMonitoringOfficer(line.moFirstName, line.moLastName, line.moEmail, line.moPhoneNumber) : builder;

        UnaryOperator<ProjectDataBuilder> selectFinanceContactsIfNecessary = builder -> {

            ProjectDataBuilder currentBuilder = builder;

            for (Pair<String, String> fc : line.financeContactsForOrganisations) {
                currentBuilder = currentBuilder.withFinanceContact(fc.getLeft(), fc.getRight());
            }

            return currentBuilder;
        };

        UnaryOperator<ProjectDataBuilder> submitBankDetailsIfNecessary = builder -> {

            ProjectDataBuilder currentBuilder = builder;

            for (Triple<String, String, String> bd : line.bankDetailsForOrganisations) {
                currentBuilder = currentBuilder.withBankDetails(bd.getLeft(), bd.getMiddle(), bd.getRight());
            }

            return currentBuilder;
        };

        testService.doWithinTransaction(() ->
            assignProjectManagerIfNecessary.
                    andThen(setProjectAddressIfNecessary).
                    andThen(setMonitoringOfficerIfNecessary).
                    andThen(selectFinanceContactsIfNecessary).
                    andThen(submitBankDetailsIfNecessary).
                    apply(baseBuilder).
                    build());

    }

    private void createExternalUsers() {
        testService.doWithinTransaction(() ->
            externalUserLines.forEach(line -> createUser(externalUserBuilder, line)));
    }

    private void createAssessors() {
        assessorUserLines.forEach(this::createAssessor);
    }

    private void createNonRegisteredAssessorInvites() {
        testService.doWithinTransaction(() -> {
            List<InviteLine> assessorInvites = simpleFilter(inviteLines, invite -> "COMPETITION".equals(invite.type));
            List<InviteLine> nonRegisteredAssessorInvites = simpleFilter(assessorInvites, invite -> !userRepository.findByEmail(invite.email).isPresent());
            nonRegisteredAssessorInvites.forEach(line -> createAssessorInvite(assessorInviteUserBuilder, line));
        });
    }

    private void createAssessments() {
        LOG.info("Creating assessments...");

        testService.doWithinTransaction(() -> assessmentLines.forEach(this::createAssessment));
        testService.doWithinTransaction(() -> assessorResponseLines.forEach(this::createAssessorResponse));
        testService.doWithinTransaction(() -> assessmentLines.forEach(this::submitAssessment));
    }

    private void createAssessment(AssessmentLine line) {
        assessmentDataBuilder.withAssessmentData(
                line.assessorEmail,
                line.applicationName,
                line.rejectReason,
                line.rejectComment,
                line.state,
                line.feedback,
                line.recommendComment
        )
                .build();
    }

    private void createAssessorResponse(AssessorResponseLine line) {
        assessorResponseDataBuilder.withAssessorResponseData(line.competitionName,
                line.applicationName,
                line.assessorEmail,
                line.shortName,
                line.description,
                line.isResearchCategory,
                line.value)
            .build();
    }

    private void submitAssessment(AssessmentLine line) {
        assessmentDataBuilder.withSubmission(
                line.applicationName,
                line.assessorEmail,
                line.state
        )
                .build();
    }

    private void createCompetitionFunders() {
        testService.doWithinTransaction(() -> competitionFunderLines.forEach(this::createCompetitionFunder));
    }

    private void createPublicContentGroups() {
        testService.doWithinTransaction(() -> setDefaultCompAdmin());
        testService.doWithinTransaction(() -> publicContentGroupLines.forEach(this::createPublicContentGroup));
    }

    private void createPublicContentDates() {
        testService.doWithinTransaction(() -> publicContentDateLines.forEach(this::createPublicContentDate));
    }

    private void createCompetitionFunder(CompetitionFunderLine line) {
        competitionFunderDataBuilder.withCompetitionFunderData(line.competitionName, line.funder, line.funder_budget, line.co_funder)
                .build();
    }

    private void createPublicContentGroup(PublicContentGroupLine line) {
        publicContentGroupDataBuilder.withPublicContentGroup(line.competitionName, line.heading, line.content, line.section)
                .build();
    }

    private void createPublicContentDate(PublicContentDateLine line) {
        publicContentDateDataBuilder.withPublicContentDate(line.competitionName, line.date, line.content)
                .build();
    }

    private void createInternalUsers() {
        testService.doWithinTransaction(() -> setDefaultSystemRegistrar());
        testService.doWithinTransaction(() ->
            internalUserLines.forEach(line -> {

                List<UserRoleType> roles = simpleMap(line.roles, UserRoleType::fromName);

                InternalUserDataBuilder baseBuilder = internalUserBuilder.withRoles(roles);

                createUser(baseBuilder, line);
            }));
    }

    private void createCompetitions() {
        testService.doWithinTransaction(() -> setDefaultCompAdmin());
        competitionLines.forEach(line -> createCompetitionWithApplications(line, line.name != null && line.name.equals("Connected digital additive manufacturing") ? Optional.of(1L) : Optional.empty()));
    }

    private List<Pair<String, FundingDecision>> createFundingDecisionsFromCsv(String competitionName) {
        List<ApplicationLine> matchingApplications = simpleFilter(applicationLines, a -> a.competitionName.equals(competitionName));
        List<ApplicationLine> applicationsWithDecisions = simpleFilter(matchingApplications, a -> asList(ApplicationState.APPROVED, ApplicationState.REJECTED).contains(a.status));
        return simpleMap(applicationsWithDecisions, ma -> Pair.of(ma.title, ma.status == ApplicationState.APPROVED ? FundingDecision.FUNDED : FundingDecision.UNFUNDED));
    }

    private void createCompetitionWithApplications(CompetitionLine competitionLine, Optional<Long> competitionId) {

        CompetitionDataBuilder basicCompetitionInformation = competitionBuilderWithBasicInformation(competitionLine, competitionId);

        List<ApplicationLine> competitionApplications = simpleFilter(applicationLines, app -> app.competitionName.equals(competitionLine.name));

        List<UnaryOperator<ApplicationDataBuilder>> applicationBuilders = simpleMap(competitionApplications,
                applicationLine -> builder -> createApplicationFromCsv(builder, applicationLine, competitionLine));

        if (applicationBuilders.isEmpty()) {
            basicCompetitionInformation.build();
        } else {

            CompetitionDataBuilder withApplications = basicCompetitionInformation.
                    moveCompetitionIntoOpenStatus().
                    withApplications(applicationBuilders).
                    restoreOriginalMilestones();

            if (competitionLine.fundersPanelEndDate != null && competitionLine.fundersPanelEndDate.isBefore(ZonedDateTime.now())) {

                withApplications = withApplications.
                        moveCompetitionIntoFundersPanelStatus().
                        sendFundingDecisions(createFundingDecisionsFromCsv(competitionLine.name)).
                        restoreOriginalMilestones();
            }

            withApplications.build();
        }
    }

    private List<UnaryOperator<QuestionResponseDataBuilder>> questionResponsesFromCsv(String leadApplicant, List<ApplicationQuestionResponseLine> responsesForApplication) {

        return simpleMap(responsesForApplication, line -> baseBuilder -> {

            String answeringUser = !isBlank(line.answeredBy) ? line.answeredBy : (!isBlank(line.assignedTo) ? line.assignedTo : leadApplicant);

            UnaryOperator<QuestionResponseDataBuilder> withQuestion = builder -> builder.forQuestion(line.questionName);

            UnaryOperator<QuestionResponseDataBuilder> answerIfNecessary = builder ->
                    !isBlank(line.value) ? builder.withAssignee(answeringUser).withAnswer(line.value, answeringUser)
                            : builder;

            UnaryOperator<QuestionResponseDataBuilder> uploadFilesIfNecessary = builder ->
                    !line.filesUploaded.isEmpty() ?
                            builder.withAssignee(answeringUser).withFileUploads(line.filesUploaded, answeringUser) :
                            builder;

            UnaryOperator<QuestionResponseDataBuilder> assignIfNecessary = builder ->
                    !isBlank(line.assignedTo) ? builder.withAssignee(line.assignedTo) : builder;

            UnaryOperator<QuestionResponseDataBuilder> markAsCompleteIfNecessary = builder ->
                    line.markedAsComplete ? builder.markAsComplete() : builder;

            return withQuestion.
                    andThen(answerIfNecessary).
                    andThen(uploadFilesIfNecessary).
                    andThen(assignIfNecessary).
                    andThen(markAsCompleteIfNecessary).
                    apply(baseBuilder);
        });
    }

    private ApplicationDataBuilder createApplicationFromCsv(ApplicationDataBuilder builder, ApplicationLine applicationLine, CompetitionLine competitionLine) {

        UserResource leadApplicant = retrieveUserByEmail(applicationLine.leadApplicant);

        ApplicationDataBuilder baseBuilder = builder.
                withBasicDetails(leadApplicant, applicationLine.title, applicationLine.researchCategory, applicationLine.resubmission).
                withInnovationArea(applicationLine.innovationArea).
                withStartDate(applicationLine.startDate).
                withDurationInMonths(applicationLine.durationInMonths);

        for (String collaborator : applicationLine.collaborators) {
            baseBuilder = baseBuilder.inviteCollaborator(retrieveUserByEmail(collaborator));
        }

        List<InviteLine> pendingInvites = simpleFilter(BaseGenerateTestData.inviteLines,
                invite -> "APPLICATION".equals(invite.type) && applicationLine.title.equals(invite.targetName));

        for (InviteLine invite : pendingInvites) {
            baseBuilder = baseBuilder.inviteCollaboratorNotYetRegistered(invite.email, invite.hash, invite.name,
                    invite.ownerName);
        }

        if (applicationLine.status != ApplicationState.CREATED) {
            baseBuilder = baseBuilder.beginApplication();
        }

        List<String> applicants = combineLists(applicationLine.leadApplicant, applicationLine.collaborators);

        List<Triple<String, String, OrganisationTypeEnum>> organisations = simpleMap(applicants, email -> {
            UserResource user = retrieveUserByEmail(email);
            OrganisationResource organisation = retrieveOrganisationByUserId(user.getId());
            return Triple.of(user.getEmail(), organisation.getName(), OrganisationTypeEnum.getFromId(organisation.getOrganisationType()));
        });

        List<Triple<String, String, OrganisationTypeEnum>> uniqueOrganisations =
                organisations.stream().filter(triple -> isUniqueOrFirstDuplicateOrganisation(triple, organisations)).collect(toList());

        List<UnaryOperator<ApplicationFinanceDataBuilder>> financeBuilders = simpleMap(uniqueOrganisations, orgDetails -> {

            String user = orgDetails.getLeft();
            String organisationName = orgDetails.getMiddle();
            OrganisationTypeEnum organisationType = orgDetails.getRight();

            Optional<ApplicationOrganisationFinanceBlock> organisationFinances = simpleFindFirst(applicationFinanceLines, finances ->
                    finances.competitionName.equals(applicationLine.competitionName) &&
                            finances.applicationName.equals(applicationLine.title) &&
                            finances.organisationName.equals(organisationName));

            if (organisationType.equals(OrganisationTypeEnum.RESEARCH)) {

                if (organisationFinances.isPresent()) {
                    return generateAcademicFinancesFromSuppliedData(user, organisationName, organisationFinances.get(), applicationLine.markFinancesComplete);
                } else {
                    return generateAcademicFinances(user, organisationName, applicationLine.markFinancesComplete);
                }
            } else {
                if (organisationFinances.isPresent()) {
                    return generateIndustrialCostsFromSuppliedData(user, organisationName, organisationFinances.get(), applicationLine.markFinancesComplete);
                } else {
                    return generateIndustrialCosts(user, organisationName, applicationLine.markFinancesComplete);
                }
            }
        });

        List<ApplicationQuestionResponseLine> responsesForApplication =
                simpleFilter(questionResponseLines, r -> r.competitionName.equals(competitionLine.name) && r.applicationName.equals(applicationLine.title));

        // if we have specific answers for questions in the application-questions.csv file, fill them in here now
        if (!responsesForApplication.isEmpty()) {
            baseBuilder = baseBuilder.withQuestionResponses(questionResponsesFromCsv(applicationLine.leadApplicant, responsesForApplication));
        }
        // otherwise provide a default set of marked as complete questions if the application is to be submitted
        else if (applicationLine.submittedDate != null) {
            baseBuilder = baseBuilder.withDefaultQuestionResponses();
        }

        baseBuilder = baseBuilder.withFinances(financeBuilders);

        baseBuilder = baseBuilder.markApplicationDetailsComplete(applicationLine.markDetailsComplete);

        if (applicationLine.submittedDate != null) {
            baseBuilder = baseBuilder.submitApplication();
        }

        if (asLinkedSet(ApplicationState.INELIGIBLE, ApplicationState.INELIGIBLE_INFORMED).contains(applicationLine.status)) {
            baseBuilder = baseBuilder.markApplicationIneligible(applicationLine.ineligibleReason);
            if (applicationLine.status == ApplicationState.INELIGIBLE_INFORMED) {
                baseBuilder = baseBuilder.informApplicationIneligible();
            }
        }

        return baseBuilder;
    }

    private boolean isUniqueOrFirstDuplicateOrganisation(Triple<String, String, OrganisationTypeEnum> currentOrganisation, List<Triple<String, String, OrganisationTypeEnum>> organisationList) {
        return organisationList.stream().filter(triple -> triple.getMiddle().equals(currentOrganisation.getMiddle())).findFirst().get().equals(currentOrganisation);
    }

    private UnaryOperator<ApplicationFinanceDataBuilder> generateIndustrialCostsFromSuppliedData(String user, String organisationName, ApplicationOrganisationFinanceBlock organisationFinances, boolean markAsComplete) {
        return finance -> {

            List<ApplicationFinanceRow> financeRows = organisationFinances.rows;

            ApplicationFinanceDataBuilder baseBuilder = finance.
                    withOrganisation(organisationName).
                    withUser(user);

            UnaryOperator<IndustrialCostDataBuilder> costBuilder = costs -> {

                IndustrialCostDataBuilder costsWithData = costs;

                for (ApplicationFinanceRow financeRow : financeRows) {
                    costsWithData = addFinanceRow(costsWithData, financeRow);
                }

                return costsWithData;
            };


            return baseBuilder.withIndustrialCosts(costBuilder)
                    .markAsComplete(markAsComplete);
        };
    }

    private IndustrialCostDataBuilder addFinanceRow(IndustrialCostDataBuilder builder, ApplicationFinanceRow financeRow) {

        switch (financeRow.category) {
            case "Working days per year":
                return builder.withWorkingDaysPerYear(Integer.valueOf(financeRow.metadata.get(0)));
            case "Grant claim":
                return builder.withGrantClaim(Integer.valueOf(financeRow.metadata.get(0)));
            case "Organisation size":
                return builder.withOrganisationSize(Long.valueOf(financeRow.metadata.get(0)));
            case "Labour":
                return builder.withLabourEntry(financeRow.metadata.get(0), Integer.valueOf(financeRow.metadata.get(1)), Integer.valueOf(financeRow.metadata.get(2)));
            case "Overheads":
                switch (financeRow.metadata.get(0).toLowerCase()) {
                    case "custom":
                        return builder.withAdministrationSupportCostsCustomRate(Integer.valueOf(financeRow.metadata.get(1)));
                    case "default":
                        return builder.withAdministrationSupportCostsDefaultRate();
                    case "none":
                        return builder.withAdministrationSupportCostsNone();
                    default:
                        throw new RuntimeException("Unknown rate type " + financeRow.metadata.get(0).toLowerCase());
                }
            case "Materials":
                return builder.withMaterials(financeRow.metadata.get(0), bd(financeRow.metadata.get(1)), Integer.valueOf(financeRow.metadata.get(2)));
            case "Capital usage":
                return builder.withCapitalUsage(Integer.valueOf(financeRow.metadata.get(4)),
                        financeRow.metadata.get(0), Boolean.parseBoolean(financeRow.metadata.get(1)),
                        bd(financeRow.metadata.get(2)), bd(financeRow.metadata.get(3)), Integer.valueOf(financeRow.metadata.get(5)));
            case "Subcontracting":
                return builder.withSubcontractingCost(financeRow.metadata.get(0), financeRow.metadata.get(1), financeRow.metadata.get(2), bd(financeRow.metadata.get(3)));
            case "Travel and subsistence":
                return builder.withTravelAndSubsistence(financeRow.metadata.get(0), Integer.valueOf(financeRow.metadata.get(1)), bd(financeRow.metadata.get(2)));
            case "Other costs":
                return builder.withOtherCosts(financeRow.metadata.get(0), bd(financeRow.metadata.get(1)));
            case "Other funding":
                return builder.withOtherFunding(financeRow.metadata.get(0), LocalDate.parse(financeRow.metadata.get(1), DATE_PATTERN), bd(financeRow.metadata.get(2)));
            default:
                throw new RuntimeException("Unknown category " + financeRow.category);
        }
    }

    private UnaryOperator<ApplicationFinanceDataBuilder> generateIndustrialCosts(String user, String organisationName, boolean markAsComplete) {
        return finance ->
                finance.withOrganisation(organisationName).
                        withUser(user).
                        withIndustrialCosts(
                                costs -> costs.
                                        withWorkingDaysPerYear(123).
                                        withGrantClaim(30).
                                        withOtherFunding("Lottery", LocalDate.of(2016, 04, 01), bd("2468")).
                                        withLabourEntry("Role 1", 200, 200).
                                        withLabourEntry("Role 2", 400, 300).
                                        withLabourEntry("Role 3", 600, 365).
                                        withAdministrationSupportCostsNone().
                                        withMaterials("Generator", bd("10020"), 10).
                                        withCapitalUsage(12, "Depreciating Stuff", true, bd("2120"), bd("1200"), 60).
                                        withSubcontractingCost("Developers", "UK", "To develop stuff", bd("90000")).
                                        withTravelAndSubsistence("To visit colleagues", 15, bd("398")).
                                        withOtherCosts("Some more costs", bd("1100")).
                                        withOrganisationSize(1L))
                        .markAsComplete(markAsComplete);
    }

    private UnaryOperator<ApplicationFinanceDataBuilder> generateAcademicFinances(String user, String organisationName, boolean markAsComplete) {
        return finance -> finance.
                withOrganisation(organisationName).
                withUser(user).
                withAcademicCosts(costs -> costs.
                        withTsbReference("My REF").
                        withDirectlyIncurredStaff(bd("22")).
                        withDirectlyIncurredTravelAndSubsistence(bd("44")).
                        withDirectlyIncurredOtherCosts(bd("66")).
                        withDirectlyAllocatedInvestigators(bd("88")).
                        withDirectlyAllocatedEstateCosts(bd("110")).
                        withDirectlyAllocatedOtherCosts(bd("132")).
                        withIndirectCosts(bd("154")).
                        withExceptionsStaff(bd("176")).
                        withExceptionsOtherCosts(bd("198")).
                        withUploadedJesForm())
                .markAsComplete(markAsComplete);
    }

    private UnaryOperator<ApplicationFinanceDataBuilder> generateAcademicFinancesFromSuppliedData(String user, String organisationName, ApplicationOrganisationFinanceBlock existingFinances, boolean markAsComplete) {
        return finance -> finance.
                withOrganisation(organisationName).
                withUser(user).
                withAcademicCosts(costs -> costs.withTsbReference("My REF").withUploadedJesForm())
                .markAsComplete(markAsComplete);
    }

    private CompetitionDataBuilder competitionBuilderWithBasicInformation(CompetitionLine line, Optional<Long> existingCompetitionId) {
        CompetitionDataBuilder basicInformation;
                if (line.nonIfs) {
                    basicInformation = nonIfsCompetitionDataBuilder(line);
                } else {
                    basicInformation = ifsCompetitionDataBuilder(line, existingCompetitionId);
                }

        return line.setupComplete ? basicInformation.withSetupComplete() : basicInformation;
    }

    private CompetitionDataBuilder nonIfsCompetitionDataBuilder(CompetitionLine line) {
        return competitionDataBuilder
                .createNonIfsCompetition()
                .withBasicData(line.name, null, line.innovationAreas,
                        line.innovationSector, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, emptyList(), null, null, line.nonIfsUrl)
                .withOpenDate(line.openDate)
                .withSubmissionDate(line.submissionDate)
                .withFundersPanelEndDate(line.fundersPanelEndDate)
                .withReleaseFeedbackDate(line.releaseFeedback)
                .withPublicContent(line.published, line.shortDescription, line.fundingRange, line.eligibilitySummary,
                        line.competitionDescription, line.fundingType, line.projectSize, line.keywords, line.inviteOnly);
    }

    private CompetitionDataBuilder ifsCompetitionDataBuilder(CompetitionLine line, Optional<Long> existingCompetitionId) {
        return existingCompetitionId.map(id -> competitionDataBuilder.
                withExistingCompetition(1L).
                withBasicData(line.name, line.type, line.innovationAreas,
                        line.innovationSector, line.researchCategory, line.leadTechnologist, line.compExecutive,
                        line.budgetCode, line.pafCode, line.code, line.activityCode, line.assessorCount, line.assessorPay, line.hasAssessmentPanel, line.hasInterviewStage,
                        line.multiStream, line.collaborationLevel, line.leadApplicantTypes, line.researchRatio, line.resubmission, null).
                withNewMilestones().
                withFundersPanelEndDate(line.fundersPanelEndDate).
                withReleaseFeedbackDate(line.releaseFeedback).
                withFeedbackReleasedDate(line.feedbackReleased).
                withPublicContent(line.published, line.shortDescription, line.fundingRange, line.eligibilitySummary,
                        line.competitionDescription, line.fundingType, line.projectSize, line.keywords, line.inviteOnly)

        ).orElse(competitionDataBuilder.
                createCompetition().
                withBasicData(line.name, line.type, line.innovationAreas,
                        line.innovationSector, line.researchCategory, line.leadTechnologist, line.compExecutive,
                        line.budgetCode, line.pafCode, line.code, line.activityCode, line.assessorCount, line.assessorPay, line.hasAssessmentPanel, line.hasInterviewStage,
                        line.multiStream, line.collaborationLevel, line.leadApplicantTypes, line.researchRatio, line.resubmission, null).
                withApplicationFormFromTemplate().
                withNewMilestones()).
                withOpenDate(line.openDate).
                withBriefingDate(line.briefingDate).
                withSubmissionDate(line.submissionDate).
                withAllocateAssesorsDate(line.allocateAssessorDate).
                withAssessorBriefingDate(line.assessorBriefingDate).
                withAssessorAcceptsDate(line.assessorAcceptsDate).
                withAssessorsNotifiedDate(line.assessorsNotifiedDate).
                withAssessorEndDate(line.assessorEndDate).
                withAssessmentClosedDate(line.assessmentClosedDate).
                withLineDrawDate(line.drawLineDate).
                withAsessmentPanelDate(line.assessmentPanelDate).
                withPanelDate(line.panelDate).
                withFundersPanelDate(line.fundersPanelDate).
                withFundersPanelEndDate(line.fundersPanelEndDate).
                withReleaseFeedbackDate(line.releaseFeedback).
                withFeedbackReleasedDate(line.feedbackReleased).
                withPublicContent(line.published, line.shortDescription, line.fundingRange, line.eligibilitySummary,
                line.competitionDescription, line.fundingType, line.projectSize, line.keywords, line.inviteOnly);
    }

    private void freshDb() throws Exception {
        try {
            cleanAndMigrateDatabaseWithPatches(locations.split(","));
        } catch (Exception e) {
            fail("Exception thrown migrating with script directories: " + locations.split(",") + e.getMessage());
        }
    }

    private Object unwrapProxy(Object services) {
        try {
            return unwrapProxies(asList(services)).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> unwrapProxies(Collection<Object> services) {
        List<Object> unwrappedProxies = new ArrayList<>();
        for (Object service : services) {
            if (AopUtils.isJdkDynamicProxy(service)) {
                try {
                    unwrappedProxies.add(((Advised) service).getTargetSource().getTarget());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                unwrappedProxies.add(service);
            }
        }
        return unwrappedProxies;
    }

    private void cleanAndMigrateDatabaseWithPatches(String[] patchLocations) {
        Flyway f = new Flyway();
        f.setDataSource(databaseUrl, databaseUser, databasePassword);
        f.setLocations(patchLocations);
        f.clean();
        f.migrate();
    }

    protected UserResource compAdmin() {
        return retrieveUserByEmail(COMP_ADMIN_EMAIL);
    }

    protected UserResource retrieveUserByEmail(String emailAddress) {
        return doAs(systemRegistrar(), () -> userService.findByEmail(emailAddress).getSuccessObjectOrThrowException());
    }

    protected OrganisationResource retrieveOrganisationById(Long id) {
        return doAs(systemRegistrar(), () -> organisationService.findById(id).getSuccessObjectOrThrowException());
    }

    protected OrganisationResource retrieveOrganisationByUserId(Long id) {
        return doAs(systemRegistrar(), () -> organisationService.getPrimaryForUser(id).getSuccessObjectOrThrowException());
    }

    protected UserResource systemRegistrar() {
        return newUserResource().withRolesGlobal(newRoleResource().withType(SYSTEM_REGISTRATION_USER).build(1)).build();
    }

    private <T> T doAs(UserResource user, Supplier<T> action) {
        UserResource currentUser = setLoggedInUser(user);
        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            setLoggedInUser(currentUser);
        }
    }

    private void doAs(UserResource user, Runnable action) {
        UserResource currentUser = setLoggedInUser(user);
        try {
            action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            setLoggedInUser(currentUser);
        }
    }

    private void createAssessor(AssessorUserLine line) {

        List<InviteLine> assessorInvitesForThisAssessor = simpleFilter(inviteLines, invite ->
                invite.email.equals(line.emailAddress) && invite.type.equals("COMPETITION"));

        AssessorDataBuilder builder = assessorUserBuilder;

        Optional<User> existingUser = userRepository.findByEmail(line.emailAddress);
        Optional<User> sentBy = userRepository.findByEmail("john.doe@innovateuk.test");
        Optional<ZonedDateTime> sentOn = Optional.of(ZonedDateTime.now());

        for (InviteLine invite : assessorInvitesForThisAssessor) {
            builder = builder.withInviteToAssessCompetition(
                    invite.targetName,
                    invite.email,
                    invite.name,
                    invite.hash,
                    invite.status,
                    existingUser,
                    invite.innovationAreaName,
                    sentBy,
                    sentOn
            );
        }

        String inviteHash = !isBlank(line.hash) ? line.hash : UUID.randomUUID().toString();
        String innovationArea = !line.innovationAreas.isEmpty() ? line.innovationAreas.get(0) : "";

        AssessorDataBuilder baseBuilder = builder.withInviteToAssessCompetition(
                line.competitionName,
                line.emailAddress,
                line.firstName + " " + line.lastName,
                inviteHash,
                line.inviteStatus,
                existingUser,
                innovationArea,
                sentBy,
                sentOn
        );

        if (!existingUser.isPresent()) {
            baseBuilder = baseBuilder.registerUser(
                    line.firstName,
                    line.lastName,
                    line.emailAddress,
                    line.phoneNumber,
                    line.ethnicity,
                    line.gender,
                    line.disability,
                    inviteHash
            );
        } else {
            baseBuilder = baseBuilder.addAssessorRole();
        }

        baseBuilder = baseBuilder.addSkills(line.skillAreas, line.businessType, line.innovationAreas);
        baseBuilder = baseBuilder.addAffiliations(
                line.principalEmployer,
                line.role,
                line.professionalAffiliations,
                line.appointments,
                line.financialInterests,
                line.familyAffiliations,
                line.familyFinancialInterests
        );

        if (line.agreementSigned) {
            baseBuilder = baseBuilder.addAgreementSigned();
        }

        if (!line.rejectionReason.isEmpty()) {
            baseBuilder = baseBuilder.rejectInvite(inviteHash, line.rejectionReason, line.rejectionComment);
        } else if (InviteStatus.OPENED.equals(line.inviteStatus)) {
            baseBuilder = baseBuilder.acceptInvite(inviteHash);
        }

        baseBuilder.build();
    }

    private void createAssessorInvite(AssessorInviteDataBuilder assessorInviteUserBuilder, InviteLine line) {
        assessorInviteUserBuilder.withInviteToAssessCompetition(
                line.targetName,
                line.email,
                line.name,
                line.hash,
                line.status,
                userRepository.findByEmail(line.email),
                line.innovationAreaName,
                userRepository.findByEmail(line.sentByEmail),
                Optional.of(line.sentOn)
        ).
                build();
    }

    private <T extends BaseUserData, S extends BaseUserDataBuilder<T, S>> void createUser(S baseBuilder, UserLine line) {

        Function<S, S> createOrgIfNecessary = builder -> {

            boolean newOrganisation = organisationRepository.findOneByName(line.organisationName) == null;

            if (!newOrganisation) {
                return builder;
            }

            OrganisationLine matchingOrganisationDetails = simpleFindFirst(organisationLines, orgLine -> orgLine.name.equals(line.organisationName)).get();

            OrganisationDataBuilder organisation = organisationBuilder.
                    createOrganisation(line.organisationName, matchingOrganisationDetails.companyRegistrationNumber, lookupOrganisationType(matchingOrganisationDetails.organisationType));

            for (OrganisationAddressType organisationType : matchingOrganisationDetails.addressType) {
                organisation = organisation.withAddress(organisationType,
                        matchingOrganisationDetails.addressLine1, matchingOrganisationDetails.addressLine2,
                        matchingOrganisationDetails.addressLine3, matchingOrganisationDetails.town,
                        matchingOrganisationDetails.postcode, matchingOrganisationDetails.county);
            }

            return builder.withNewOrganisation(organisation);
        };

        Function<S, S> registerUserIfNecessary = builder -> builder.registerUser(line.firstName, line.lastName, line.emailAddress, line.organisationName, line.phoneNumber);

        Function<S, S> verifyEmail = builder -> builder.verifyEmail();

        Function<S, S> inactivateUserIfNecessary = builder -> !(line.emailVerified) ? builder.deactivateUser() : builder;

        createOrgIfNecessary.andThen(registerUserIfNecessary).andThen(verifyEmail).andThen(inactivateUserIfNecessary).apply(baseBuilder).build();
    }

    private OrganisationTypeEnum lookupOrganisationType(String organisationType) {
        return OrganisationTypeEnum.valueOf(organisationType.toUpperCase().replace(" ", "_"));
    }

    protected abstract boolean cleanDbFirst();

    protected abstract void fixUpDatabase();

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    protected void setDefaultSystemRegistrar() {
        setLoggedInUser(newUserResource().withRolesGlobal(newRoleResource().withType(SYSTEM_REGISTRATION_USER).build(1)).build());
        testService.doWithinTransaction(() ->
                setLoggedInUser(userService.findByEmail(BaseDataBuilder.IFS_SYSTEM_REGISTRAR_USER_EMAIL).getSuccessObjectOrThrowException())
        );
    }

    protected void setDefaultCompAdmin() {
        setLoggedInUser(newUserResource().withRolesGlobal(newRoleResource().withType(SYSTEM_REGISTRATION_USER).build(1)).build());
        testService.doWithinTransaction(() ->
                setLoggedInUser(userService.findByEmail(BaseDataBuilder.COMP_ADMIN_EMAIL).getSuccessObjectOrThrowException())
        );
    }
}
