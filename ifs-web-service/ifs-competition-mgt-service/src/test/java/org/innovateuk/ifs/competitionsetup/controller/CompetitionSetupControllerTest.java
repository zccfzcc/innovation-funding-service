package org.innovateuk.ifs.competitionsetup.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.resource.InnovationSectorResource;
import org.innovateuk.ifs.category.service.CategoryRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.resource.CompetitionTypeResource;
import org.innovateuk.ifs.competitionsetup.form.AdditionalInfoForm;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.InitialDetailsForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupService;
import org.innovateuk.ifs.fixtures.CompetitionFundersFixture;
import org.innovateuk.ifs.user.builder.UserResourceBuilder;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.category.builder.InnovationSectorResourceBuilder.newInnovationSectorResource;
import static org.innovateuk.ifs.category.resource.CategoryType.INNOVATION_AREA;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.builder.CompetitionTypeResourceBuilder.newCompetitionTypeResource;
import static org.innovateuk.ifs.competitionsetup.controller.CompetitionSetupController.COMPETITION_SETUP_FORM_KEY;
import static org.innovateuk.ifs.competitionsetup.service.sectionupdaters.InitialDetailsSectionSaver.OPENINGDATE_FIELDNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class for testing public functions of {@link CompetitionSetupController}
 */
@RunWith(MockitoJUnitRunner.class)
public class CompetitionSetupControllerTest extends BaseControllerMockMVCTest<CompetitionSetupController> {

    private static final Long COMPETITION_ID = 12L;
    private static final String URL_PREFIX = "/competition/setup";

    @Mock
    private CategoryRestService categoryRestService;

    @Mock
    private CompetitionSetupService competitionSetupService;

    @Mock
    private Validator validator;

    @Override
    protected CompetitionSetupController supplyControllerUnderTest() {
        return new CompetitionSetupController();
    }

    @Before
    public void setUp() {
        super.setUp();

        when(userService.findUserByType(UserRoleType.COMP_ADMIN)).thenReturn(asList(UserResourceBuilder.newUserResource().withFirstName("Comp").withLastName("Admin").build()));

        when(userService.findUserByType(UserRoleType.COMP_TECHNOLOGIST)).thenReturn(asList(UserResourceBuilder.newUserResource().withFirstName("Comp").withLastName("Technologist").build()));

        List<InnovationSectorResource> innovationSectorResources = newInnovationSectorResource()
                .withName("A Innovation Sector")
                .withId(1L)
                .build(1);
        when(categoryRestService.getInnovationSectors()).thenReturn(restSuccess(innovationSectorResources));

        List<InnovationAreaResource> innovationAreaResources = newInnovationAreaResource()
                .withName("A Innovation Area")
                .withId(2L)
                .withSector(1L)
                .build(1);
        when(categoryRestService.getInnovationAreas()).thenReturn(restSuccess(innovationAreaResources));

        List<CompetitionTypeResource> competitionTypeResources = newCompetitionTypeResource()
                .withId(1L)
                .withName("Comptype with stateAid")
                .withStateAid(true)
                .withCompetitions(asList(COMPETITION_ID))
                .build(1);
        when(competitionService.getAllCompetitionTypes()).thenReturn(competitionTypeResources);
    }

    @Test
    public void initCompetitionSetupSection() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("competition/setup"));
    }

    @Test
    public void editCompetitionSetupSectionInitial() throws Exception {
        InitialDetailsForm competitionSetupInitialDetailsForm = new InitialDetailsForm();
        competitionSetupInitialDetailsForm.setTitle("Test competition");
        competitionSetupInitialDetailsForm.setCompetitionTypeId(2L);

        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withName("Test competition")
                .withCompetitionCode("Code")
                .withCompetitionType(2L)
                .build();
        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        CompetitionSetupForm compSetupForm = mock(CompetitionSetupForm.class);
        when(competitionSetupService.getSectionFormData(competition, CompetitionSetupSection.INITIAL_DETAILS)).thenReturn(compSetupForm);

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup"))
                .andExpect(model().attribute("competitionSetupForm", compSetupForm));

        verify(competitionSetupService).populateCompetitionSectionModelAttributes(isA(Model.class), eq(competition), eq(CompetitionSetupSection.INITIAL_DETAILS));
    }

    @Test
    public void editCompetitionSetupSection_redirectsIfInitialDetailsNotCompleted() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID + "/section/application"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    public void setSectionAsIncomplete() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).withName("Test competition").withCompetitionCode("Code").withCompetitionType(2L).build();
        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionService.setSetupSectionMarkedAsIncomplete(anyLong(), any(CompetitionSetupSection.class))).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial"));
    }

    @Test
    public void getInnovationAreas() throws Exception {
        Long innovationSectorId = 1L;
        InnovationAreaResource category = newInnovationAreaResource()
                .withId(1L)
                .withName("Innovation Area 1")
                .build();

        when(categoryRestService.getInnovationAreasBySector(innovationSectorId)).thenReturn(restSuccess(singletonList(category)));

        mockMvc.perform(get(URL_PREFIX + "/getInnovationArea/" + innovationSectorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]id", is(1)))
                .andExpect(jsonPath("[0]name", is("Innovation Area 1")))
                .andExpect(jsonPath("[0]type", is(INNOVATION_AREA.toString())));

    }

    @Test
    public void submitAutoSave() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        String fieldName = "title";
        String value = "New Title";
        Long objectId = 2L;

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionSetupService.autoSaveCompetitionSetupSection(
                isA(CompetitionResource.class),
                eq(CompetitionSetupSection.INITIAL_DETAILS),
                eq(fieldName),
                eq(value),
                eq(Optional.of(objectId)))
        ).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial/saveFormElement")
                .param("fieldName", fieldName)
                .param("value", value)
                .param("objectId", String.valueOf(objectId)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("success", is("true")));

        verify(competitionSetupService).autoSaveCompetitionSetupSection(isA(CompetitionResource.class), eq(CompetitionSetupSection.INITIAL_DETAILS), eq(fieldName), eq(value), eq(Optional.of(objectId)));
    }

    @Test
    public void submitAutoSaveValidationErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        String fieldName = "openingDate";
        String value = "20-02-2002";
        String errorKey = "competition.setup.opening.date.not.in.future";
        Long objectId = 2L;

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionSetupService.autoSaveCompetitionSetupSection(
                isA(CompetitionResource.class),
                eq(CompetitionSetupSection.INITIAL_DETAILS),
                eq(fieldName),
                eq(value),
                eq(Optional.of(objectId)))
        ).thenReturn(serviceFailure(fieldError(OPENINGDATE_FIELDNAME, value, errorKey)));

        when(messageSource.getMessage(anyString(), anyObject(), any(Locale.class))).thenReturn("Please enter a future date");

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial/saveFormElement")
                .param("fieldName", fieldName)
                .param("value", value)
                .param("objectId", String.valueOf(objectId)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("success", is("true")));

        verify(competitionSetupService).autoSaveCompetitionSetupSection(isA(CompetitionResource.class), eq(CompetitionSetupSection.INITIAL_DETAILS), eq(fieldName), eq(value), eq(Optional.of(objectId)));
    }

    @Test
    public void generateCompetitionCode() throws Exception {
        ZonedDateTime time = ZonedDateTime.of(2016, 12, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).withName("Test competition").withCompetitionCode("Code").withCompetitionType(2L).build();
        competition.setStartDate(time);
        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionService.generateCompetitionCode(COMPETITION_ID, time)).thenReturn("1612-1");

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID + "/generateCompetitionCode?day=01&month=12&year=2016"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message", is("1612-1")));
    }

    @Test
    public void submitUnrestrictedSectionInitialDetailsInvalidWithRequiredFieldsEmpty() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        MvcResult mvcResult = mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial")
                .param("unrestricted", "1"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors(COMPETITION_SETUP_FORM_KEY,
                        "executiveUserId",
                        "title",
                        "leadTechnologistUserId",
                        "openingDateDay",
                        "openingDateMonth",
                        "openingDateYear",
                        "openingDate",
                        "innovationSectorCategoryId",
                        "innovationAreaCategoryIds",
                        "competitionTypeId"))
                .andExpect(view().name("competition/setup"))
                .andReturn();

        InitialDetailsForm initialDetailsForm = (InitialDetailsForm) mvcResult.getModelAndView().getModel().get(COMPETITION_SETUP_FORM_KEY);

        BindingResult bindingResult = initialDetailsForm.getBindingResult();

        bindingResult.getAllErrors();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(10, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("executiveUserId"));
        assertEquals("Please select a competition executive.", bindingResult.getFieldError("executiveUserId").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("title"));
        assertEquals("Please enter a title.", bindingResult.getFieldError("title").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("leadTechnologistUserId"));
        assertEquals("Please select an Innovation Lead.", bindingResult.getFieldError("leadTechnologistUserId").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateDay"));
        assertEquals("Please enter an opening day.", bindingResult.getFieldError("openingDateDay").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateMonth"));
        assertEquals("Please enter an opening month.", bindingResult.getFieldError("openingDateMonth").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateYear"));
        assertEquals("Please enter an opening year.", bindingResult.getFieldError("openingDateYear").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDate"));
        assertEquals("Please enter a future date.", bindingResult.getFieldError("openingDate").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("innovationSectorCategoryId"));
        assertEquals("Please select an innovation sector.", bindingResult.getFieldError("innovationSectorCategoryId").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("innovationAreaCategoryIds"));
        assertEquals("Please select an innovation area.", bindingResult.getFieldError("innovationAreaCategoryIds").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("competitionTypeId"));
        assertEquals("Please select a competition type.", bindingResult.getFieldError("competitionTypeId").getDefaultMessage());

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void submitSectionInitialDetailsInvalidWithRequiredFieldsEmpty() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        MvcResult mvcResult = mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors(COMPETITION_SETUP_FORM_KEY,
                        "executiveUserId",
                        "title",
                        "leadTechnologistUserId",
                        "openingDateDay",
                        "openingDateMonth",
                        "openingDateYear",
                        "innovationSectorCategoryId",
                        "innovationAreaCategoryIds"))
                .andExpect(view().name("competition/setup"))
                .andReturn();

        InitialDetailsForm initialDetailsForm = (InitialDetailsForm) mvcResult.getModelAndView().getModel().get(COMPETITION_SETUP_FORM_KEY);

        BindingResult bindingResult = initialDetailsForm.getBindingResult();

        bindingResult.getAllErrors();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(8, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("executiveUserId"));
        assertEquals("Please select a competition executive.", bindingResult.getFieldError("executiveUserId").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("title"));
        assertEquals("Please enter a title.", bindingResult.getFieldError("title").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("leadTechnologistUserId"));
        assertEquals("Please select an Innovation Lead.", bindingResult.getFieldError("leadTechnologistUserId").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateDay"));
        assertEquals("Please enter an opening day.", bindingResult.getFieldError("openingDateDay").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateMonth"));
        assertEquals("Please enter an opening month.", bindingResult.getFieldError("openingDateMonth").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateYear"));
        assertEquals("Please enter an opening year.", bindingResult.getFieldError("openingDateYear").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("innovationSectorCategoryId"));
        assertEquals("Please select an innovation sector.", bindingResult.getFieldError("innovationSectorCategoryId").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("innovationAreaCategoryIds"));
        assertEquals("Please select an innovation area.", bindingResult.getFieldError("innovationAreaCategoryIds").getDefaultMessage());

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void submitUnrestrictedSectionInitialDetailsWithInvalidOpenDate() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        Integer invalidDateDay = 1;
        Integer invalidDateMonth = 1;
        Integer invalidDateYear = 1999;

        MvcResult mvcResult = mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial")
                .param("executiveUserId", "1")
                .param("openingDateDay", invalidDateDay.toString())
                .param("openingDateMonth", invalidDateMonth.toString())
                .param("openingDateYear", invalidDateYear.toString())
                .param("innovationSectorCategoryId", "1")
                .param("innovationAreaCategoryIds", "1", "2", "3")
                .param("competitionTypeId", "1")
                .param("leadTechnologistUserId", "1")
                .param("title", "My competition")
                .param("unrestricted", "1"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrors(COMPETITION_SETUP_FORM_KEY, "openingDate"))
                .andExpect(view().name("competition/setup"))
                .andReturn();

        InitialDetailsForm initialDetailsForm = (InitialDetailsForm) mvcResult.getModelAndView().getModel().get(COMPETITION_SETUP_FORM_KEY);

        assertEquals(new Long(1L), initialDetailsForm.getExecutiveUserId());
        assertEquals(invalidDateDay, initialDetailsForm.getOpeningDateDay());
        assertEquals(invalidDateMonth, initialDetailsForm.getOpeningDateMonth());
        assertEquals(invalidDateYear, initialDetailsForm.getOpeningDateYear());
        assertEquals(new Long(1L), initialDetailsForm.getInnovationSectorCategoryId());
        assertEquals(asList(1L, 2L, 3L), initialDetailsForm.getInnovationAreaCategoryIds());
        assertEquals(new Long(1L), initialDetailsForm.getCompetitionTypeId());
        assertEquals(new Long(1L), initialDetailsForm.getLeadTechnologistUserId());
        assertEquals("My competition", initialDetailsForm.getTitle());

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void submitUnrestrictedSectionInitialDetailsWithInvalidFieldsExceedRangeMax() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        Integer invalidDateDay = 32;
        Integer invalidDateMonth = 13;
        Integer invalidDateYear = 10000;

        MvcResult mvcResult = mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial")
                .param("executiveUserId", "1")
                .param("openingDateDay", invalidDateDay.toString())
                .param("openingDateMonth", invalidDateMonth.toString())
                .param("openingDateYear", invalidDateYear.toString())
                .param("innovationSectorCategoryId", "1")
                .param("innovationAreaCategoryIds", "1", "2", "3")
                .param("competitionTypeId", "1")
                .param("leadTechnologistUserId", "1")
                .param("title", "My competition")
                .param("unrestricted", "1"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors(COMPETITION_SETUP_FORM_KEY,
                        "openingDateDay",
                        "openingDateMonth",
                        "openingDateYear",
                        "openingDate"
                ))
                .andExpect(view().name("competition/setup"))
                .andReturn();

        InitialDetailsForm initialDetailsForm = (InitialDetailsForm) mvcResult.getModelAndView().getModel().get(COMPETITION_SETUP_FORM_KEY);

        assertEquals(new Long(1L), initialDetailsForm.getExecutiveUserId());
        assertEquals(invalidDateDay, initialDetailsForm.getOpeningDateDay());
        assertEquals(invalidDateMonth, initialDetailsForm.getOpeningDateMonth());
        assertEquals(invalidDateYear, initialDetailsForm.getOpeningDateYear());
        assertEquals(new Long(1L), initialDetailsForm.getInnovationSectorCategoryId());
        assertEquals(asList(1L, 2L, 3L), initialDetailsForm.getInnovationAreaCategoryIds());
        assertEquals(new Long(1L), initialDetailsForm.getCompetitionTypeId());
        assertEquals(new Long(1L), initialDetailsForm.getLeadTechnologistUserId());
        assertEquals("My competition", initialDetailsForm.getTitle());

        BindingResult bindingResult = initialDetailsForm.getBindingResult();

        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(4, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("openingDateDay"));
        assertEquals("Please enter a valid date.", bindingResult.getFieldError("openingDateDay").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateMonth"));
        assertEquals("Please enter a valid date.", bindingResult.getFieldError("openingDateMonth").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateYear"));
        assertEquals("Please enter a valid date.", bindingResult.getFieldError("openingDateYear").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDate"));
        assertEquals("Please enter a future date.", bindingResult.getFieldError("openingDate").getDefaultMessage());

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void submitUnrestrictedSectionInitialDetailsWithInvalidFieldsExceedRangeMin() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        Integer invalidDateDay = 0;
        Integer invalidDateMonth = 0;
        Integer invalidDateYear = 1899;

        MvcResult mvcResult = mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial")
                .param("executiveUserId", "1")
                .param("openingDateDay", invalidDateDay.toString())
                .param("openingDateMonth", invalidDateMonth.toString())
                .param("openingDateYear", invalidDateYear.toString())
                .param("innovationSectorCategoryId", "1")
                .param("innovationAreaCategoryIds", "1", "2", "3")
                .param("competitionTypeId", "1")
                .param("leadTechnologistUserId", "1")
                .param("title", "My competition")
                .param("unrestricted", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(COMPETITION_SETUP_FORM_KEY,
                        "openingDateDay",
                        "openingDateMonth",
                        "openingDateYear",
                        "openingDate"
                ))
                .andExpect(view().name("competition/setup"))
                .andReturn();

        InitialDetailsForm initialDetailsForm = (InitialDetailsForm) mvcResult.getModelAndView().getModel().get(COMPETITION_SETUP_FORM_KEY);

        BindingResult bindingResult = initialDetailsForm.getBindingResult();

        assertEquals(new Long(1L), initialDetailsForm.getExecutiveUserId());
        assertEquals(invalidDateDay, initialDetailsForm.getOpeningDateDay());
        assertEquals(invalidDateMonth, initialDetailsForm.getOpeningDateMonth());
        assertEquals(invalidDateYear, initialDetailsForm.getOpeningDateYear());
        assertEquals(new Long(1L), initialDetailsForm.getInnovationSectorCategoryId());
        assertEquals(asList(1L, 2L, 3L), initialDetailsForm.getInnovationAreaCategoryIds());
        assertEquals(new Long(1L), initialDetailsForm.getCompetitionTypeId());
        assertEquals(new Long(1L), initialDetailsForm.getLeadTechnologistUserId());
        assertEquals("My competition", initialDetailsForm.getTitle());

        bindingResult.getAllErrors();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(4, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("openingDateDay"));
        assertEquals("Please enter a valid date.", bindingResult.getFieldError("openingDateDay").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateMonth"));
        assertEquals("Please enter a valid date.", bindingResult.getFieldError("openingDateMonth").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDateYear"));
        assertEquals("Please enter a valid date.", bindingResult.getFieldError("openingDateYear").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("openingDate"));
        assertEquals("Please enter a future date.", bindingResult.getFieldError("openingDate").getDefaultMessage());

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void submitSectionInitialDetailsWithoutErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionSetupService.saveCompetitionSetupSection(isA(CompetitionSetupForm.class), eq(competition), eq(CompetitionSetupSection.INITIAL_DETAILS))).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial")
                .param("executiveUserId", "1")
                .param("openingDateDay", "10")
                .param("openingDateMonth", "10")
                .param("openingDateYear", "2100")
                .param("innovationSectorCategoryId", "1")
                .param("innovationAreaCategoryIds", "1", "2", "3")
                .param("competitionTypeId", "1")
                .param("leadTechnologistUserId", "1")
                .param("title", "My competition"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial"));

        verify(competitionSetupService).saveCompetitionSetupSection(isA(CompetitionSetupForm.class), eq(competition), eq(CompetitionSetupSection.INITIAL_DETAILS));
    }

    @Test
    public void submitSectionEligibilityWithErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/eligibility"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void submitSectionEligibilityWithoutErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionSetupService.saveCompetitionSetupSection(isA(CompetitionSetupForm.class), eq(competition), eq(CompetitionSetupSection.ELIGIBILITY))).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/eligibility")
                .param("multipleStream", "yes")
                .param("streamName", "stream")
                .param("researchCategoryId", "1", "2", "3")
                .param("singleOrCollaborative", "collaborative")
                .param("leadApplicantTypes", "1", "2", "3")
                .param("researchParticipationAmountId", "1")
                .param("resubmission", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/" + COMPETITION_ID + "/section/eligibility"));

        verify(competitionSetupService).saveCompetitionSetupSection(isA(CompetitionSetupForm.class), eq(competition), eq(CompetitionSetupSection.ELIGIBILITY));
    }

    @Test
    public void submitSectionEligibilityWithoutStreamName() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/eligibility")
                .param("multipleStream", "yes")
                .param("streamName", "")
                .param("researchCategoryId", "1", "2", "3")
                .param("singleOrCollaborative", "collaborative")
                .param("leadApplicantType", "business")
                .param("researchParticipationAmountId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testCoFundersForCompetition() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .withActivityCode("Activity Code")
                .withCompetitionCode("c123")
                .withPafCode("p123")
                .withBudgetCode("b123")
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withFunders(CompetitionFundersFixture.getTestCoFunders())
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionSetupService.saveCompetitionSetupSection(any(AdditionalInfoForm.class),
                any(CompetitionResource.class), any(CompetitionSetupSection.class))).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/additional")
                .param("activityCode", "a123")
                .param("pafNumber", "p123")
                .param("competitionCode", "c123")
                .param("funders[0].funder", "asdf")
                .param("funders[0].funderBudget", "93129")
                .param("funders[0].coFunder", "false")
                .param("budgetCode", "b123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/" + COMPETITION_ID + "/section/additional"));

        verify(competitionSetupService, atLeastOnce()).saveCompetitionSetupSection(any(AdditionalInfoForm.class),
                any(CompetitionResource.class), any(CompetitionSetupSection.class));

        verify(validator).validate(any(AdditionalInfoForm.class), any(BindingResult.class));
    }

    @Test
    public void testSetCompetitionAsReadyToOpen() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.READY_TO_OPEN)
                .withId(COMPETITION_ID).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID + "/ready-to-open"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/competition/setup/" + COMPETITION_ID));
    }

    @Test
    public void testInitialDetailsRestriction() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        Map<CompetitionSetupSection, Boolean> sectionSetupStatus = new HashMap<>();
        sectionSetupStatus.put(CompetitionSetupSection.INITIAL_DETAILS, Boolean.TRUE);
        competition.setSectionSetupStatus(sectionSetupStatus);

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("competition/setup"))
                .andExpect(model().attribute("restrictInitialDetailsEdit", Boolean.TRUE));
    }

    @Test
    public void testInitialDetailsNoRestriction() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .withId(COMPETITION_ID)
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(get(URL_PREFIX + "/" + COMPETITION_ID + "/section/initial"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("competition/setup"))
                .andExpect(model().attribute("restrictInitialDetailsEdit", nullValue()));
    }


    @Test
    public void testSubmitAssessorsSectionDetailsWithErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors"))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testSubmitAssessorsSectionDetailsWithoutErrors() throws Exception {
        CompetitionResource competition = newCompetitionResource()
                .withId(COMPETITION_ID)
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionSetupService.saveCompetitionSetupSection(isA(CompetitionSetupForm.class), eq(competition), eq(CompetitionSetupSection.ASSESSORS))).thenReturn(serviceSuccess());

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors")
                .param("assessorCount", "1")
                .param("assessorPay", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors"));

        verify(competitionSetupService).saveCompetitionSetupSection(isA(CompetitionSetupForm.class), eq(competition), eq(CompetitionSetupSection.ASSESSORS));
    }

    @Test
    public void testSubmitAssessorsSectionDetailsWithInvalidAssessorCount() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors")
                .param("assessorCount", "")
                .param("assessorPay", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("competitionSetupForm", "assessorCount"))
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testSubmitAssessorsSectionDetailsWithInvalidAssessorPay() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors")
                .param("assessorCount", "3")
                .param("assessorPay", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("competitionSetupForm", "assessorPay"))
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testSubmitAssessorsSectionDetailsWithInvalidAssessorPay_Bignumber() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors")
                .param("assessorCount", "3")
                .param("assessorPay", "12345678912334"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("competitionSetupForm", "assessorPay"))
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testSubmitAssessorsSectionDetailsWithInvalidAssessorPay_NegativeNumber() throws Exception {
        CompetitionResource competition = newCompetitionResource().withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP).build();

        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(post(URL_PREFIX + "/" + COMPETITION_ID + "/section/assessors")
                .param("assessorCount", "3")
                .param("assessorPay", "-1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("competitionSetupForm", "assessorPay"))
                .andExpect(view().name("competition/setup"));

        verify(competitionService, never()).update(competition);
    }
}
