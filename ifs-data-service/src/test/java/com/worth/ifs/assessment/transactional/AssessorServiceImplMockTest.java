package com.worth.ifs.assessment.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.AssessorFeedback;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.assessment.dto.Feedback;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.domain.ProcessRole;
import org.junit.Test;

import static com.worth.ifs.BuilderAmendFunctions.id;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.application.builder.ResponseBuilder.newResponse;
import static com.worth.ifs.commons.error.Errors.*;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.domain.UserRoleType.ASSESSOR;
import static com.worth.ifs.user.domain.UserRoleType.COLLABORATOR;
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
        ServiceResult<Feedback> serviceResult
                = service.updateAssessorFeedback(
                new Feedback()
                        .setResponseId(responseId)
                        .setAssessorProcessRoleId(2L)
                        .setValue(empty())
                        .setText(empty()));
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(Response.class, responseId)));
    }

    @Test
    public void test_processRoleNotFound() {

        long responseId = 1L;
        long processRoleId = 2L;

        when(responseRepositoryMock.findOne(responseId)).thenReturn(newResponse().build());
        when(processRoleRepositoryMock.findOne(processRoleId)).thenReturn(null);

        ServiceResult<Feedback> serviceResult
                = service.updateAssessorFeedback(
                new Feedback()
                        .setResponseId(responseId)
                        .setAssessorProcessRoleId(processRoleId)
                        .setValue(empty())
                        .setText(empty()));
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(ProcessRole.class, processRoleId)));
    }

    @Test
    public void test_processRoleNotCorrectType() {

        long responseId = 1L;
        long processRoleId = 2L;

        ProcessRole incorrectTypeProcessRole = newProcessRole().
                with(id(processRoleId)).
                withRole(newRole().withType(COLLABORATOR)).
                build();

        when(responseRepositoryMock.findOne(responseId)).thenReturn(newResponse().build());
        when(processRoleRepositoryMock.findOne(processRoleId)).thenReturn(incorrectTypeProcessRole);

        ServiceResult<Feedback> serviceResult
                = service.updateAssessorFeedback(
                new Feedback()
                        .setResponseId(responseId)
                        .setAssessorProcessRoleId(processRoleId)
                        .setValue(empty())
                        .setText(empty()));
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(incorrectTypeError(ProcessRole.class, processRoleId)));
    }

    @Test
    public void test_processRoleNotCorrectApplication() {

        long responseId = 1L;
        long processRoleId = 2L;
        long correctApplicationId = 3L;
        long incorrectApplicationId = -999L;

        ProcessRole incorrectApplicationProcessRole =
                newProcessRole().
                        with(id(processRoleId)).
                        withRole(newRole().withType(ASSESSOR)).
                        withApplication(newApplication().withId(incorrectApplicationId)).
                        build();

        Response response =
                newResponse().
                        withApplication(newApplication().withId(correctApplicationId)).
                        build();

        when(responseRepositoryMock.findOne(responseId)).thenReturn(response);
        when(processRoleRepositoryMock.findOne(processRoleId)).thenReturn(incorrectApplicationProcessRole);

        ServiceResult<Feedback> serviceResult
                = service.updateAssessorFeedback(
                new Feedback()
                        .setResponseId(responseId)
                        .setAssessorProcessRoleId(processRoleId)
                        .setValue(empty())
                        .setText(empty()));
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(incorrectTypeError(ProcessRole.class, processRoleId)));
    }

    @Test
    public void test_uncaughtExceptions_handled() {

        long responseId = 1L;
        when(responseRepositoryMock.findOne(responseId)).thenThrow(new RuntimeException());
        ServiceResult<Feedback> serviceResult
                = service.updateAssessorFeedback(
                new Feedback()
                        .setResponseId(responseId)
                        .setAssessorProcessRoleId(2L)
                        .setValue(empty())
                        .setText(empty()));
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(internalServerErrorError()));
    }

    @Test
    public void test_happyPath_assessmentFeedbackUpdated() {

        long responseId = 1L;
        long processRoleId = 2L;
        long applicationId = 3L;

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
        when(processRoleRepositoryMock.findOne(processRoleId)).thenReturn(processRole);
        when(responseRepositoryMock.save(response)).thenReturn(response);

        ServiceResult<Feedback> serviceResult
                = service.updateAssessorFeedback(
                new Feedback()
                        .setResponseId(responseId)
                        .setAssessorProcessRoleId(processRoleId)
                        .setValue(of("newFeedbackValue"))
                        .setText(of("newFeedbackText")));
        assertTrue(serviceResult.isSuccess());

        AssessorFeedback feedback = response.getResponseAssessmentForAssessor(processRole).orElse(null);

        assertNotNull(feedback);
        assertEquals("newFeedbackValue", feedback.getAssessmentValue());
        assertEquals("newFeedbackText", feedback.getAssessmentFeedback());
    }

}
