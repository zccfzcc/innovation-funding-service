package org.innovateuk.ifs.application;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.populator.*;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.viewmodel.ApplicationOverviewViewModel;
import org.innovateuk.ifs.application.viewmodel.AssessQuestionFeedbackViewModel;
import org.innovateuk.ifs.application.viewmodel.NavigationViewModel;
import org.innovateuk.ifs.assessment.resource.AssessmentFeedbackAggregateResource;
import org.innovateuk.ifs.commons.error.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Optional.ofNullable;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.application.service.Futures.settable;
import static org.innovateuk.ifs.assessment.builder.AssessmentFeedbackAggregateResourceBuilder.newAssessmentFeedbackAggregateResource;
import static org.innovateuk.ifs.category.builder.ResearchCategoryResourceBuilder.newResearchCategoryResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.ASSESSOR_FEEDBACK;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.PROJECT_SETUP;
import static org.innovateuk.ifs.form.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationControllerTest extends BaseControllerMockMVCTest<ApplicationController> {
    @Spy
    @InjectMocks
    private ApplicationOverviewModelPopulator applicationOverviewModelPopulator;

    @Spy
    @InjectMocks
    private ApplicationModelPopulator applicationModelPopulator;

    @Spy
    @InjectMocks
    private AssessorQuestionFeedbackPopulator assessorQuestionFeedbackPopulator;

    @Spy
    @InjectMocks
    private FeedbackNavigationPopulator feedbackNavigationPopulator;

    @Spy
    @InjectMocks
    private ApplicationSectionAndQuestionModelPopulator applicationSectionAndQuestionModelPopulator;

    @Override
    protected ApplicationController supplyControllerUnderTest() {
        return new ApplicationController();
    }

    @Before
    public void setUp() {
        super.setUp();

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.loginDefaultUser();
        this.setupFinances();
        this.setupInvites();
        when(organisationService.getOrganisationForUser(anyLong(), anyList())).thenReturn(ofNullable(organisations.get(0)));
        when(categoryRestServiceMock.getResearchCategories()).thenReturn(restSuccess(newResearchCategoryResource().build(2)));
    }

    @Test
    public void testApplicationDetails() throws Exception {
        ApplicationResource app = applications.get(0);
        Set<Long> sections = newHashSet(1L, 2L);
        Map<Long, Set<Long>> mappedSections = new HashMap<>();
        mappedSections.put(organisations.get(0).getId(), sections);
        when(sectionService.getCompletedSectionsByOrganisation(anyLong())).thenReturn(mappedSections);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));


        LOG.debug("Show dashboard for application: " + app.getId());
        Map<String, Object> model = mockMvc.perform(get("/application/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andReturn().getModelAndView().getModel();

        ApplicationOverviewViewModel viewModel = (ApplicationOverviewViewModel) model.get("model");

        assertEquals(app, viewModel.getCurrentApplication());
        assertEquals(sections, viewModel.getCompleted().getCompletedSections());
        assertEquals(competitionService.getById(app.getCompetition()), viewModel.getCurrentCompetition());

        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().size() == 0);
    }

    @Test
    public void testApplicationDetailsAssign() throws Exception {
        ApplicationResource app = applications.get(0);

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        LOG.debug("Show dashboard for application: " + app.getId());
        mockMvc.perform(post("/application/" + app.getId()).param(ApplicationController.ASSIGN_QUESTION_PARAM, "1_2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/application/" + app.getId()));
    }

    @Test
    public void testNonAcceptedInvitationsAffectPendingAssignableUsersAndPendingOrganisationNames() throws Exception {
        ApplicationResource app = applications.get(0);
        Set<Long> sections = newHashSet(1L, 2L);
        Map<Long, Set<Long>> mappedSections = new HashMap<>();
        mappedSections.put(organisations.get(0).getId(), sections);
        when(sectionService.getCompletedSectionsByOrganisation(anyLong())).thenReturn(mappedSections);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        ApplicationInviteResource inv1 = inviteResource("kirk", "teamA", InviteStatus.CREATED);
        ApplicationInviteResource inv2 = inviteResource("spock", "teamA", InviteStatus.SENT);
        ApplicationInviteResource inv3 = inviteResource("bones", "teamA", InviteStatus.OPENED);

        ApplicationInviteResource inv4 = inviteResource("picard", "teamB", InviteStatus.CREATED);

        InviteOrganisationResource inviteOrgResource1 = inviteOrganisationResource(inv1, inv2, inv3);
        InviteOrganisationResource inviteOrgResource2 = inviteOrganisationResource(inv4);

        List<InviteOrganisationResource> inviteOrgResources = Arrays.asList(inviteOrgResource1, inviteOrgResource2);
        RestResult<List<InviteOrganisationResource>> invitesResult = RestResult.<List<InviteOrganisationResource>>restSuccess(inviteOrgResources, HttpStatus.OK);

        when(inviteRestService.getInvitesByApplication(app.getId())).thenReturn(invitesResult);

        LOG.debug("Show dashboard for application: " + app.getId());
        Map<String, Object> model = mockMvc.perform(get("/application/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andReturn().getModelAndView().getModel();

        ApplicationOverviewViewModel viewModel = (ApplicationOverviewViewModel) model.get("model");

        assertEquals(app, viewModel.getCurrentApplication());
        assertEquals(sections, viewModel.getCompleted().getCompletedSections());
        assertEquals(competitionService.getById(app.getCompetition()), viewModel.getCurrentCompetition());

        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().size() == 3);
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv1));
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv2));
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv4));
    }

    @Test
    public void testPendingOrganisationNamesOmitsEmptyOrganisationName() throws Exception {
        ApplicationResource app = applications.get(0);
        Set<Long> sections = newHashSet(1L, 2L);
        Map<Long, Set<Long>> mappedSections = new HashMap<>();
        mappedSections.put(organisations.get(0).getId(), sections);
        when(sectionService.getCompletedSectionsByOrganisation(anyLong())).thenReturn(mappedSections);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        ApplicationInviteResource inv1 = inviteResource("kirk", "teamA", InviteStatus.CREATED);

        ApplicationInviteResource inv2 = inviteResource("picard", "", InviteStatus.CREATED);

        InviteOrganisationResource inviteOrgResource1 = inviteOrganisationResource(inv1);
        InviteOrganisationResource inviteOrgResource2 = inviteOrganisationResource(inv2);

        List<InviteOrganisationResource> inviteOrgResources = Arrays.asList(inviteOrgResource1, inviteOrgResource2);
        RestResult<List<InviteOrganisationResource>> invitesResult = RestResult.restSuccess(inviteOrgResources, HttpStatus.OK);

        when(inviteRestService.getInvitesByApplication(app.getId())).thenReturn(invitesResult);

        LOG.debug("Show dashboard for application: " + app.getId());
        Map<String, Object> model = mockMvc.perform(get("/application/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andReturn().getModelAndView().getModel();

        ApplicationOverviewViewModel viewModel = (ApplicationOverviewViewModel) model.get("model");

        assertEquals(app, viewModel.getCurrentApplication());
        assertEquals(sections, viewModel.getCompleted().getCompletedSections());
        assertEquals(competitionService.getById(app.getCompetition()), viewModel.getCurrentCompetition());

        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().size() == 2);
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv1));
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv2));
    }

    @Test
    public void testPendingOrganisationNamesOmitsOrganisationNamesThatAreAlreadyCollaborators() throws Exception {
        ApplicationResource app = applications.get(0);
        Set<Long> sections = newHashSet(1L, 2L);
        Map<Long, Set<Long>> mappedSections = new HashMap<>();
        mappedSections.put(organisations.get(0).getId(), sections);
        when(sectionService.getCompletedSectionsByOrganisation(anyLong())).thenReturn(mappedSections);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        ApplicationInviteResource inv1 = inviteResource("kirk", "teamA", InviteStatus.CREATED);
        ApplicationInviteResource inv2 = inviteResource("picard", organisations.get(0).getName(), InviteStatus.CREATED);

        InviteOrganisationResource inviteOrgResource1 = inviteOrganisationResource(inv1);
        InviteOrganisationResource inviteOrgResource2 = inviteOrganisationResource(inv2);

        List<InviteOrganisationResource> inviteOrgResources = Arrays.asList(inviteOrgResource1, inviteOrgResource2);
        RestResult<List<InviteOrganisationResource>> invitesResult = RestResult.restSuccess(inviteOrgResources, HttpStatus.OK);

        when(inviteRestService.getInvitesByApplication(app.getId())).thenReturn(invitesResult);

        LOG.debug("Show dashboard for application: " + app.getId());
        Map<String, Object> model = mockMvc.perform(get("/application/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andReturn().getModelAndView().getModel();

        ApplicationOverviewViewModel viewModel = (ApplicationOverviewViewModel) model.get("model");

        assertEquals(app, viewModel.getCurrentApplication());
        assertEquals(sections, viewModel.getCompleted().getCompletedSections());
        assertEquals(competitionService.getById(app.getCompetition()), viewModel.getCurrentCompetition());

        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().size() == 2);
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv1));
        assertTrue(viewModel.getAssignable().getPendingAssignableUsers().contains(inv2));
    }

    private InviteOrganisationResource inviteOrganisationResource(ApplicationInviteResource... invs) {
        InviteOrganisationResource ior = new InviteOrganisationResource();
        ior.setInviteResources(Arrays.asList(invs));
        return ior;
    }

    private ApplicationInviteResource inviteResource(String name, String organisation, InviteStatus status) {
        ApplicationInviteResource invRes = new ApplicationInviteResource();
        invRes.setName(name);
        invRes.setInviteOrganisationName(organisation);
        invRes.setStatus(status);
        return invRes;
    }

    @Test
    public void applicationAssessorQuestionFeedback() throws Exception {
        long applicationId = 1L;
        long questionId = 2L;

        QuestionResource previousQuestion = newQuestionResource().withId(1L).withShortName("previous").build();
        QuestionResource questionResource = newQuestionResource().withId(questionId).build();
        QuestionResource nextQuestion = newQuestionResource().withId(3L).withShortName("next").build();
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetitionStatus(PROJECT_SETUP).build();
        List<FormInputResponseResource> responseResources = newFormInputResponseResource().build(2);
        AssessmentFeedbackAggregateResource aggregateResource = newAssessmentFeedbackAggregateResource().build();
        NavigationViewModel expectedNavigation = new NavigationViewModel();
        expectedNavigation.setNextText("next");
        expectedNavigation.setNextUrl("/application/1/question/3/feedback");
        expectedNavigation.setPreviousText("previous");
        expectedNavigation.setPreviousUrl("/application/1/question/1/feedback");
        AssessQuestionFeedbackViewModel expectedModel =
                new AssessQuestionFeedbackViewModel(applicationResource, questionResource, responseResources, aggregateResource, expectedNavigation);

        when(questionService.getPreviousQuestion(questionId)).thenReturn(Optional.ofNullable(previousQuestion));
        when(questionService.getById(questionId)).thenReturn(questionResource);
        when(questionService.getNextQuestion(questionId)).thenReturn(Optional.ofNullable(nextQuestion));
        when(applicationService.getById(applicationId)).thenReturn(applicationResource);
        when(formInputResponseRestService.getByApplicationIdAndQuestionId(applicationId, questionId)).thenReturn(restSuccess(responseResources));
        when(assessorFormInputResponseRestService.getAssessmentAggregateFeedback(applicationId, questionId))
                .thenReturn(restSuccess(aggregateResource));

        mockMvc.perform(get("/application/{applicationId}/question/{questionId}/feedback", applicationId, questionId))
                .andExpect(status().isOk())
                .andExpect(view().name("application-assessor-feedback"))
                .andExpect(model().attribute("model", expectedModel));
    }

    @Test
    public void applicationAssessorQuestionFeedback_invalidState() throws Exception {
        long applicationId = 1L;
        long questionId = 2L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetitionStatus(ASSESSOR_FEEDBACK).build();

        when(applicationService.getById(applicationId)).thenReturn(applicationResource);

        mockMvc.perform(get("/application/{applicationId}/question/{questionId}/feedback", applicationId, questionId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/application/" + applicationId + "/summary"));
    }

    @Test
    public void testNotExistingApplicationDetails() throws Exception {
        ApplicationResource app = applications.get(0);

        when(env.acceptsProfiles("debug")).thenReturn(true);
        when(messageSource.getMessage(ObjectNotFoundException.class.getName(), null, Locale.ENGLISH)).thenReturn("Not found");
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(applicationService.getById(1234L)).thenThrow(new ObjectNotFoundException("1234 not found", Collections.singletonList(1234L)));

        LOG.debug("Show dashboard for application: " + app.getId());
        mockMvc.perform(get("/application/1234"))
                .andExpect(view().name("404"))
                .andExpect(model().attribute("url", "http://localhost/application/1234"))
                .andExpect(model().attribute("exception", new InstanceOf(ObjectNotFoundException.class)))
                .andExpect(model().attribute("message", "1234 not found"))
                .andExpect(model().attributeExists("stacktrace"));
    }

    @Test
    public void testApplicationDetailsOpenSection() throws Exception {
        ApplicationResource app = applications.get(0);
        SectionResource section = sectionResources.get(2);

        Map<Long, SectionResource> collectedSections =
                sectionResources.stream().collect(Collectors.toMap(SectionResource::getId,
                        Function.identity()));

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        ProcessRoleResource userApplicationRole = newProcessRoleResource().withApplication(app.getId()).withOrganisation(organisations.get(0).getId()).build();
        when(userRestServiceMock.findProcessRole(loggedInUser.getId(), app.getId())).thenReturn(restSuccess(userApplicationRole));

        LOG.debug("Show dashboard for application: " + app.getId());
        mockMvc.perform(get("/application/" + app.getId() + "/section/" + section.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("currentCompetition", competitionService.getById(app.getCompetition())))
                .andExpect(model().attribute("sections", collectedSections))
                .andExpect(model().attribute("currentSectionId", section.getId()))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(application1Organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(application1Organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(application1Organisations.get(1))))
                .andExpect(model().attribute("responses", formInputsToFormInputResponses))
                .andExpect(model().attribute("pendingAssignableUsers", Matchers.hasSize(0)))
                .andExpect(model().attribute("pendingOrganisationNames", Matchers.hasSize(0)));
    }

    @Test
    public void testApplicationConfirmSubmit() throws Exception {
        ApplicationResource app = applications.get(0);

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        mockMvc.perform(get("/application/1/confirm-submit"))
                .andExpect(view().name("application-confirm-submit"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("responses", formInputsToFormInputResponses));

    }

    @Test
    public void testApplicationSubmitAgreeingToTerms() throws Exception {
        ApplicationResource app = newApplicationResource().withId(1L).withCompetitionStatus(OPEN).build();
        when(userService.isLeadApplicant(users.get(0).getId(), app)).thenReturn(true);
        when(userService.getLeadApplicantProcessRoleOrNull(app)).thenReturn(new ProcessRoleResource());

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(applicationRestService.updateApplicationState(app.getId(), SUBMITTED)).thenReturn(restSuccess());
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        mockMvc.perform(post("/application/1/submit")
                .param("agreeTerms", "yes"))
                .andExpect(view().name("application-submitted"))
                .andExpect(model().attribute("currentApplication", app));

        verify(applicationRestService).updateApplicationState(app.getId(), SUBMITTED);
    }

    @Test
    public void testApplicationSubmitAppisNotSubmittable() throws Exception {
        ApplicationResource app = newApplicationResource().withId(1L).withCompetitionStatus(FUNDERS_PANEL).build();
        when(userService.isLeadApplicant(users.get(0).getId(), app)).thenReturn(true);
        when(userService.getLeadApplicantProcessRoleOrNull(app)).thenReturn(new ProcessRoleResource());

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));


        mockMvc.perform(post("/application/1/submit")
                .param("agreeTerms", "yes"))
                .andExpect(redirectedUrl("/application/1/confirm-submit"));

        verify(cookieFlashMessageFilter).setFlashMessage(isA(HttpServletResponse.class), eq("cannotSubmit"));
        verify(applicationRestService, never()).updateApplicationState(any(Long.class), any(ApplicationState.class));
    }

    @Test
    public void testApplicationCreateView() throws Exception {
        mockMvc.perform(get("/application/create/1"))
                .andExpect(view().name("application-create"));
    }

    @Test
    public void testApplicationTrack() throws Exception {
        ApplicationResource app = applications.get(0);

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        mockMvc.perform(get("/application/1/track"))
                .andExpect(view().name("application-track"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("responses", formInputsToFormInputResponses));

    }

    @Test
    public void testTeesAndCees() throws Exception {

        mockMvc.perform(get("/application/terms-and-conditions"))
                .andExpect(view().name("application-terms-and-conditions"));

    }

    @Test
    public void testApplicationCreateConfirmCompetitionView() throws Exception {
        mockMvc.perform(get("/application/create-confirm-competition"))
                .andExpect(view().name("application-create-confirm-competition"));
    }
}
