package com.worth.ifs.assessment.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.AssessorFeedback;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.assessment.dto.Feedback;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import org.junit.Test;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.application.builder.ResponseBuilder.newResponse;
import static com.worth.ifs.commons.error.Errors.internalServerErrorError;
import static com.worth.ifs.commons.error.Errors.notFoundError;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.domain.UserRoleType.ASSESSOR;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AssessorServiceImpl}
 * <p>
 * Created by dwatson on 07/10/15.
 */
public class AssessorServiceImplMockTest extends BaseServiceUnitTest<AssessorService> {

    @Override
    protected AssessorService supplyServiceUnderTest() {
        return new AssessorServiceImpl();
    }

    @Test
    public void test_responseNotFound() {

        long responseId = 123L;
        when(responseRepositoryMock.findOne(responseId)).thenReturn(null);

        ServiceResult<Feedback> serviceResult = service.updateAssessorFeedback(
                new Feedback.Id(responseId, 2L), empty(), empty());

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(Response.class, responseId)));
    }

    @Test
    public void test_roleNotFound() {

        when(responseRepositoryMock.findOne(123L)).thenReturn(newResponse().build());
        when(roleRepositoryMock.findByName(ASSESSOR.getName())).thenReturn(emptyList());

        ServiceResult<Feedback> serviceResult = service.updateAssessorFeedback(
                new Feedback.Id(123L, 2L), empty(), empty());

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(Role.class, ASSESSOR.getName())));
    }

    @Test
    public void test_processRole_notFound() {

        long responseId = 1L;
        long applicationId = 3L;
        long userId = 4L;

        Role assessorRole = newRole().withType(ASSESSOR).build();

        Application application =
                newApplication().
                        withId(applicationId).
                        build();

        Response response =
                newResponse().
                        withApplication(application).
                        build();

        when(responseRepositoryMock.findOne(responseId)).thenReturn(response);
        when(roleRepositoryMock.findByName(ASSESSOR.getName())).thenReturn(singletonList(assessorRole));
        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(userId, assessorRole, applicationId)).thenReturn(emptyList());

        ServiceResult<Feedback> serviceResult = service.updateAssessorFeedback(
                new Feedback.Id(responseId, userId), of("newFeedbackValue"), of("newFeedbackText"));

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(ProcessRole.class, userId, ASSESSOR.getName(), applicationId)));
    }

    @Test
    public void test_uncaughtExceptions_handled() {

        long responseId = 1L;
        when(responseRepositoryMock.findOne(responseId)).thenThrow(new RuntimeException());
        ServiceResult<Feedback> serviceResult = service.updateAssessorFeedback(
                new Feedback.Id(responseId, 2L), empty(), empty());
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(internalServerErrorError()));
    }

    @Test
    public void test_happyPath_assessmentFeedbackUpdated() {

        long responseId = 1L;
        long processRoleId = 2L;
        long applicationId = 3L;
        long userId = 4L;

        Role assessorRole = newRole().build();

        Application application =
                newApplication().
                        withId(applicationId).
                        build();

        ProcessRole processRole =
                newProcessRole().
                        withId(processRoleId).
                        withRole(newRole().withType(ASSESSOR)).
                        withApplication(application).
                        build();

        Response response =
                newResponse().
                        withApplication(application).
                        build();

        when(responseRepositoryMock.findOne(responseId)).thenReturn(response);
        when(roleRepositoryMock.findByName(ASSESSOR.getName())).thenReturn(singletonList(assessorRole));
        when(processRoleRepositoryMock.findByUserIdAndRoleAndApplicationId(userId, assessorRole, applicationId)).thenReturn(singletonList(processRole));
        when(responseRepositoryMock.save(response)).thenReturn(response);

        ServiceResult<Feedback> serviceResult = service.updateAssessorFeedback(
                new Feedback.Id(responseId, userId), of("newFeedbackValue"), of("newFeedbackText"));

        assertTrue(serviceResult.isSuccess());

        AssessorFeedback feedback = response.getResponseAssessmentForAssessor(processRole).orElse(null);

        assertNotNull(feedback);
        assertEquals("newFeedbackValue", feedback.getAssessmentValue());
        assertEquals("newFeedbackText", feedback.getAssessmentFeedback());
    }

}
