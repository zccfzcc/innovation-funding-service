package com.worth.ifs.application.service;

import com.worth.ifs.BaseServiceMocksTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.when;

public class ApplicationServiceImplTest extends BaseServiceMocksTest<ApplicationService> {

    @Mock
    ApplicationRestService applicationRestService;
    private List<Application> applications;
    private Long userId;


    @Override
    @Before
    public void setUp() {
        super.setUp();

        ApplicationStatus created = new ApplicationStatus(1L, "created");
        ApplicationStatus submitted = new ApplicationStatus(2L, "submitted");
        ApplicationStatus something = new ApplicationStatus(3L, "something");
        ApplicationStatus finished = new ApplicationStatus(4L, "finished");
        ApplicationStatus approved = new ApplicationStatus(5L, "approved");
        ApplicationStatus rejected = new ApplicationStatus(6L, "rejected");
        applications = newApplication().withApplicationState(created, submitted, something, finished, approved, rejected).build(6);

        userId = 1L;
        when(applicationRestService.getApplicationsByUserId(userId)).thenReturn(applications);
        when(applicationRestService.getCompleteQuestionsPercentage(applications.get(0).getId())).thenReturn(20.5d);

    }

    @Override
    protected ApplicationService supplyServiceUnderTest() {
        return new ApplicationServiceImpl();
    }

    @Test
     public void testGetById() throws Exception {
        Long applicationId = 1L;
        List<Application> applications = newApplication().withId(applicationId).build(1);
        applications.get(0).setId(applicationId);
        when(applicationRestService.getApplicationById(applicationId)).thenReturn(applications.get(0));

        Application returnedApplication = service.getById(applicationId);
        assertEquals(applications.get(0), returnedApplication);
    }
    @Test
    public void testGetByIdNotFound() throws Exception {
        Long applicationId = 5L;
        Application returnedApplication = service.getById(applicationId);
        assertEquals(null, returnedApplication);
    }

    @Test
    public void testGetByIdNullValue() throws Exception {
        Long applicationId = null;
        Application returnedApplication = service.getById(applicationId);
        assertEquals(null, returnedApplication);
    }


    @Test
    public void testGetInProgress() throws Exception {
        List<Application> returnedApplications = service.getInProgress(userId);
        returnedApplications.stream().forEach(a ->
                assertThat(a.getApplicationStatus().getName(), Matchers.either(Matchers.is("submitted")).or(Matchers.is("created")))
                );
    }

    @Test
    public void testGetFinished() throws Exception {
        List<Application> returnedApplications = service.getFinished(userId);
        returnedApplications.stream().forEach(a ->
                        assertThat(a.getApplicationStatus().getName(), Matchers.either(Matchers.is("approved")).or(Matchers.is("rejected")))
        );
    }
    @Test
     public void testGetProgress() throws Exception {
        Map<Long, Integer> progress = service.getProgress(userId);
        assertEquals(20, progress.get(applications.get(0).getId()).intValue(), 0d);
    }
    @Test
    public void testGetProgressNull() throws Exception {
        Map<Long, Integer> progress = service.getProgress(userId);
        assertNull(progress.get(2L));
    }

    @Test
    public void testUpdateStatus() throws Exception {
        Long statusId = 1L;
        service.updateStatus(applications.get(0).getId(), statusId);
        Mockito.inOrder(applicationRestService).verify(applicationRestService, calls(1)).updateApplicationStatus(applications.get(0).getId(), statusId);
    }

    @Test
    public void testGetCompleteQuestionsPercentage() throws Exception {
        // somehow the progress is rounded, because we use a long as the return type.
        assertEquals(20, service.getCompleteQuestionsPercentage(applications.get(0).getId()));
    }

    @Test
    public void testSave() throws Exception {
        service.save(applications.get(0));
        Mockito.inOrder(applicationRestService).verify(applicationRestService, calls(1)).saveApplication(applications.get(0));
    }


}