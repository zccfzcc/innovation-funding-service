package com.worth.ifs.project.sections;

import com.worth.ifs.BaseUnitTest;
import com.worth.ifs.commons.error.exception.ForbiddenActionException;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.function.Consumer;

import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class ProjectSetupSectionsPartnerAccessorTest extends BaseUnitTest {

    @Mock
    private ProjectSetupProgressChecker projectSetupProgressCheckerMock;

    @InjectMocks
    private ProjectSetupSectionPartnerAccessor accessor;

    private ProjectResource project = newProjectResource().build();
    private UserResource user = newUserResource().build();
    private OrganisationResource organisation = newOrganisationResource().build();

    @Test
    public void testCheckAccessToCompaniesHouseSectionHappyPath() {

        when(projectSetupProgressCheckerMock.isBusinessOrganisationType(project, user, organisation)).thenReturn(true);

        accessor.checkAccessToCompaniesHouseSection(project, user, organisation);

        verifyInteractions(mock -> mock.isBusinessOrganisationType(project, user, organisation));
    }

    @Test(expected = ForbiddenActionException.class)
    public void testCheckAccessToCompaniesHouseSectionButOrganisationIsNotBusiness() {

        when(projectSetupProgressCheckerMock.isBusinessOrganisationType(project, user, organisation)).thenReturn(false);

        accessor.checkAccessToCompaniesHouseSection(project, user, organisation);

        verifyInteractions(mock -> mock.isBusinessOrganisationType(project, user, organisation));
    }

    @Test
    public void testCheckAccessToProjectDetailsSectionHappyPathForBusinessOrganisation() {

        when(projectSetupProgressCheckerMock.isBusinessOrganisationType(project, user, organisation)).thenReturn(true);
        when(projectSetupProgressCheckerMock.isCompaniesHouseDetailsComplete(project, user, organisation)).thenReturn(true);

        accessor.checkAccessToProjectDetailsSection(project, user, organisation);

        verifyInteractions(
                mock -> mock.isBusinessOrganisationType(project, user, organisation),
                mock -> mock.isCompaniesHouseDetailsComplete(project, user, organisation)
        );
    }

    @Test
    public void testCheckAccessToProjectDetailsSectionHappyPathForNonBusinessTypeOrganisation() {

        when(projectSetupProgressCheckerMock.isBusinessOrganisationType(project, user, organisation)).thenReturn(false);

        accessor.checkAccessToProjectDetailsSection(project, user, organisation);

        verifyInteractions(mock -> mock.isBusinessOrganisationType(project, user, organisation));
    }

    @Test(expected = ForbiddenActionException.class)
    public void testCheckAccessToProjectDetailsSectionButCompaniesHouseSectionIncomplete() {

        when(projectSetupProgressCheckerMock.isBusinessOrganisationType(project, user, organisation)).thenReturn(true);
        when(projectSetupProgressCheckerMock.isCompaniesHouseDetailsComplete(project, user, organisation)).thenReturn(false);

        accessor.checkAccessToProjectDetailsSection(project, user, organisation);

        verifyInteractions(
                mock -> mock.isBusinessOrganisationType(project, user, organisation),
                mock -> mock.isCompaniesHouseDetailsComplete(project, user, organisation)
        );
    }

    @Test
    public void testCheckAccessToMonitoringOfficerSectionHappyPath() {

        when(projectSetupProgressCheckerMock.isProjectDetailsSectionComplete(project, user, organisation)).thenReturn(true);

        accessor.checkAccessToMonitoringOfficerSection(project, user, organisation);

        verifyInteractions(mock -> mock.isProjectDetailsSectionComplete(project, user, organisation));
    }

    @Test(expected = ForbiddenActionException.class)
    public void testCheckAccessToMonitoringOfficerSectionButProjectDetailsSectionIncomplete() {
        when(projectSetupProgressCheckerMock.isProjectDetailsSectionComplete(project, user, organisation)).thenReturn(false);
        accessor.checkAccessToMonitoringOfficerSection(project, user, organisation);
    }

    @SafeVarargs
    private final void verifyInteractions(Consumer<ProjectSetupProgressChecker>... verifiers) {
        asList(verifiers).forEach(verifier -> verifier.accept(verify(projectSetupProgressCheckerMock)));
        verifyNoMoreInteractions(projectSetupProgressCheckerMock);
    }
}
