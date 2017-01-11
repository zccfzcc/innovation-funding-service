package org.innovateuk.ifs.application.viewmodel;

import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.google.common.primitives.Longs.asList;
import static org.innovateuk.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class OpenFinanceSectionViewModelTest {

    private OpenFinanceSectionViewModel viewModel;

    private Long sectionId = 23456L;
    private SectionResource currentSection;
    private UserResource currentUser;
    private SectionAssignableViewModel sectionAssignableViewModel;
    private NavigationViewModel navigationViewModel;
    private SectionApplicationViewModel applicationViewModel;

    @Before
    public void setup() {
        currentSection = newSectionResource().withId(sectionId).build();
        navigationViewModel = new NavigationViewModel();
        currentUser = newUserResource().build();

        viewModel = new OpenFinanceSectionViewModel(navigationViewModel, currentSection, Boolean.TRUE, sectionId, currentUser);

        viewModel.setTitle("Title");
        viewModel.setCurrentSection(currentSection);
        viewModel.setResponses(Collections.emptyMap());
        viewModel.setUserIsLeadApplicant(Boolean.TRUE);
        viewModel.setCompletedSections(asList(2L));
        viewModel.setSections(asMap(sectionId, currentSection));
        viewModel.setQuestionFormInputs(asMap());
        viewModel.setSubSections(asMap());
        viewModel.setSectionQuestions(asMap());
        viewModel.setSubSectionQuestionFormInputs(asMap());
        viewModel.setSectionAssignableViewModel(sectionAssignableViewModel);
        viewModel.setSectionApplicationViewModel(applicationViewModel);
    }

    @Test
    public void testNormalGetters() {
        assertEquals("Title", viewModel.getTitle());
        assertEquals(currentSection, viewModel.getCurrentSection());
        assertEquals(Boolean.TRUE, viewModel.getHasFinanceSection());
        assertEquals(sectionId, viewModel.getFinanceSectionId());
        assertEquals(Boolean.TRUE, viewModel.getUserIsLeadApplicant());
        assertEquals(asList(2L), viewModel.getCompletedSections());
        assertEquals(asMap(sectionId, currentSection), viewModel.getSections());
        assertEquals(sectionAssignableViewModel, viewModel.getAssignable());
        assertEquals(navigationViewModel, viewModel.getNavigation());
        assertEquals(currentUser, viewModel.getCurrentUser());
        assertEquals(applicationViewModel, viewModel.getApplication());
    }

    @Test
    public void testGetCurrentQuestionFormInputs() {
        assertEquals(null, viewModel.getCurrentQuestionFormInputs());
    }

    @Test
    public void testGetIsYourFinancesAndNotCompleted() {
        currentSection.setName("Your finances");
        viewModel.setCurrentSection(currentSection);
        viewModel.setCompletedSections(asList());

        assertEquals(Boolean.TRUE, viewModel.getIsYourFinancesAndNotCompleted());
    }

    @Test
    public void testGetIsYourFinances() {
        currentSection.setName("NOT Your finances");
        viewModel.setCurrentSection(currentSection);

        assertEquals(Boolean.FALSE, viewModel.getIsYourFinances());
    }

    @Test
    public void getIsFinanceOverview() {
        currentSection.setName("Finances overview");
        viewModel.setCurrentSection(currentSection);

        assertEquals(Boolean.TRUE, viewModel.getIsFinanceOverview());
    }

    @Test
    public void testGetIsSection() {
        assertEquals(Boolean.TRUE, viewModel.getIsSection());
    }

}
