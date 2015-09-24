package com.worth.ifs.assessment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.worth.ifs.assessment.domain.Assessment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Set;

/**
 * Created by nunoalexandre on 16/09/15.
 */
@RestController
@RequestMapping("/assessment")
public class AssessmentController {

    @Autowired
    AssessmentHandler assessmentHandler;


    @RequestMapping("/findAssessmentsByCompetition/{userId}/{competitionId}")
    public Set<Assessment> findAssessmentsByCompetition( @PathVariable("userId") final Long userId, @PathVariable("competitionId") final Long competitionId ) {
        return assessmentHandler.getAllByCompetitionAndUser(competitionId, userId);
    }

    @RequestMapping("/findAssessmentByApplication/{userId}/{applicationId}")
    public Assessment getAssessmentByUserAndApplication( @PathVariable("userId") final Long userId, @PathVariable("applicationId") final Long applicationId ) {
        return assessmentHandler.getOneByAssessorAndApplication(userId, applicationId);
    }

    @RequestMapping("/totalAssignedAssessmentsByCompetition/{userId}/{competitionId}")
    public Integer getTotalAssignedAssessmentsByCompetition( @PathVariable("userId") final Long userId, @PathVariable("competitionId") final Long competitionId ) {
       return assessmentHandler.getTotalAssignedAssessmentsByCompetition(competitionId, userId);
    }

    @RequestMapping("/totalSubmittedAssessmentsByCompetition/{userId}/{competitionId}")
    public Integer getTotalSubmittedAssessmentsByCompetition( @PathVariable("userId") final Long userId, @PathVariable("competitionId") final Long competitionId ) {
        return assessmentHandler.getTotalSubmittedAssessmentsByCompetition(competitionId, userId);
    }

    @RequestMapping(value = "/respondToAssessmentInvitation", method = RequestMethod.POST)
    public Boolean respondToAssessmentInvitation(@RequestBody JsonNode formData) {

        //unpacks all the response form data fields
        Long assessorId = formData.get("assessorId").asLong();
        Long applicationId = formData.get("applicationId").asLong();
        Boolean decision = formData.get("decision").asBoolean();
        String reason = formData.get("reason").asText("");
        String observations = HtmlUtils.htmlUnescape(formData.get("observations").asText(""));

        // delegates to the handler and returns its operation success
        return assessmentHandler.respondToAssessmentInvitation(assessorId, applicationId, decision, reason, observations);
    }

}
