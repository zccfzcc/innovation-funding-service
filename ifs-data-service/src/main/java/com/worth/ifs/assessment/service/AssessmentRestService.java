package com.worth.ifs.assessment.service;

import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.user.domain.User;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Set;

/**
 * AssessmentRestRestService is a utility to use client-side to retrieve Assessment data from the data-service controllers.
 */
public interface AssessmentRestService {

    public List<Assessment> getAllByAssessorAndCompetition(Long userId, Long competitionId);

    public Assessment getOneByAssessorAndApplication(Long userId, Long applicationId);

    public Integer getTotalAssignedByAssessorAndCompetition(Long userId, Long competitionId);

    public Integer getTotalSubmittedByAssessorAndCompetition(Long userId, Long competitionId);

    public Boolean respondToAssessmentInvitation(Long assessorId, Long applicationId, Boolean decision, String decisionReason, String observations);

    public Boolean submitAssessments(Long assessorId, Set<Long> assessmentIds);

    public Boolean saveAssessmentSummary(Long assessorId, Long applicationId, String suitableValue, String suitableFeedback, String comments);


}
