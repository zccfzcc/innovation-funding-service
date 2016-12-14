package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import com.google.common.collect.Lists;
import org.innovateuk.ifs.application.service.CategoryService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.category.builder.CategoryResourceBuilder;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.InitialDetailsForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupMilestoneService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InitialDetailsSectionSaverTest {

    @InjectMocks
    private InitialDetailsSectionSaver service;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private MilestoneService milestoneService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CompetitionSetupMilestoneService competitionSetupMilestoneService;

    //TODO: Create tests for situations surrounding Milestone saving
    //TODO: Create test for invalid date handling
    //TODO: Create test for situations surrounding retrieval of innovation sector

    @Test
    public void testSaveCompetitionSetupSection() {
        Long executiveUserId = 1L;
        Long competitionTypeId = 2L;
        Long leadTechnologistId = 3L;
        Long innovationAreaId = 4L;
        Long innovationSectorId = 5L;

        LocalDateTime openingDate = LocalDateTime.of(2020, 12, 1, 0, 0);

        InitialDetailsForm competitionSetupForm = new InitialDetailsForm();
        competitionSetupForm.setTitle("title");
        competitionSetupForm.setExecutiveUserId(executiveUserId);
        competitionSetupForm.setOpeningDateDay(openingDate.getDayOfMonth());
        competitionSetupForm.setOpeningDateMonth(openingDate.getMonthValue());
        competitionSetupForm.setOpeningDateYear(openingDate.getYear());
        competitionSetupForm.setLeadTechnologistUserId(leadTechnologistId);
        competitionSetupForm.setCompetitionTypeId(competitionTypeId);
        competitionSetupForm.setInnovationAreaCategoryId(innovationAreaId);
        competitionSetupForm.setInnovationSectorCategoryId(innovationSectorId);

        CategoryResource innovationArea = CategoryResourceBuilder.newCategoryResource().withId(innovationAreaId).build();
        competitionSetupForm.setInnovationAreaCategoryId(innovationArea.getId());

        List<MilestoneResource> milestones = new ArrayList<>();
        milestones.add(getMilestone());

        List<Long> milestonesIds = new ArrayList<>();
        milestonesIds.add(10L);

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionCode("compcode").build();
        competition.setMilestones(milestonesIds);
        competition.setSetupComplete(false);

        when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(milestones);
        when(categoryService.getCategoryByParentId(innovationSectorId)).thenReturn(Lists.newArrayList(innovationArea));
        when(competitionService.initApplicationFormByCompetitionType(competition.getId(), competitionSetupForm.getCompetitionTypeId())).thenReturn(serviceSuccess());
        when(competitionService.update(competition)).thenReturn(serviceSuccess());

        service.saveSection(competition, competitionSetupForm);

        assertEquals("title", competition.getName());
        assertEquals(competition.getExecutive(), executiveUserId);
        assertEquals(competition.getCompetitionType(), competitionTypeId);
        assertEquals(competition.getLeadTechnologist(), leadTechnologistId);
        assertEquals(competition.getInnovationArea(), innovationAreaId);
        assertEquals(competition.getInnovationSector(), innovationSectorId);
        assertEquals(openingDate, competition.getStartDate());
        assertEquals(competition.getCompetitionType(), competitionTypeId);
        assertEquals(innovationSectorId, competition.getInnovationSector());

        verify(competitionService).update(competition);
        verify(competitionService).initApplicationFormByCompetitionType(competition.getId(), competitionSetupForm.getCompetitionTypeId());
    }

    @Test
    public void testAutoSaveCompetitionSetupSection() {
        when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));

        CompetitionResource competition = newCompetitionResource().build();
        competition.setMilestones(asList(10L));
        when(competitionService.update(competition)).thenReturn(serviceSuccess());

        ServiceResult<Void> errors = service.autoSaveSectionField(competition, null, "openingDate", "20-10-2020", null);

        assertTrue(errors.isSuccess());
        verify(competitionService).update(competition);
    }

    @Test
    public void testAutoSaveCompetitionSetupSectionUnknown() {
        CompetitionResource competition = newCompetitionResource().build();

        ServiceResult<Void> errors = service.autoSaveSectionField(competition, null, "notExisting", "Strange!@#1Value", null);

        assertTrue(!errors.isSuccess());
        verify(competitionService, never()).update(competition);
    }

    @Test
    public void testCompletedCompetitionCanSetOnlyLeadTechnologistAndExecutive() {
        String newTitle = "New title";
        Long newExec = 1L;
        Long leadTechnologistId = 2L;
        Long competitionTypeId = 3L;
        Long innovationSectorId = 4L;

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        CompetitionResource competition = newCompetitionResource()
                .withSetupComplete(true)
                .withStartDate(yesterday)
                .withFundersPanelDate(tomorrow)
                .build();

        InitialDetailsForm form = new InitialDetailsForm();
        form.setTitle(newTitle);
        form.setExecutiveUserId(newExec);
        form.setLeadTechnologistUserId(leadTechnologistId);
        form.setCompetitionTypeId(competitionTypeId);
        form.setInnovationSectorCategoryId(innovationSectorId);

        when(competitionService.update(competition)).thenReturn(serviceSuccess());

        service.saveSection(competition, form);

        assertNull(competition.getName());
        assertEquals(competition.getLeadTechnologist(), leadTechnologistId);
        assertEquals(competition.getExecutive(), newExec);
        assertNull(competition.getCompetitionType());
        assertNull(competition.getInnovationSector());
    }

    private MilestoneResource getMilestone(){
        MilestoneResource milestone = new MilestoneResource();
        milestone.setId(10L);
        milestone.setType(MilestoneType.OPEN_DATE);
        milestone.setDate(LocalDateTime.of(2020, 12, 1, 0, 0));
        milestone.setCompetition(1L);
        return milestone;
    }

    @Test
    public void testsSupportsForm() {
        assertTrue(service.supportsForm(InitialDetailsForm.class));
        assertFalse(service.supportsForm(CompetitionSetupForm.class));
    }
}
