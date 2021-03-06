package org.innovateuk.ifs.application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.innovateuk.ifs.application.mapper.IneligibleOutcomeMapper;
import org.innovateuk.ifs.application.resource.*;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private IneligibleOutcomeMapper ineligibleOutcomeMapper;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/{id}")
    public RestResult<ApplicationResource> getApplicationById(@PathVariable("id") final Long id) {
        return applicationService.getApplicationById(id).toGetResponse();
    }

    @GetMapping("/")
    public RestResult<List<ApplicationResource>> findAll() {
        return applicationService.findAll().toGetResponse();
    }

    @GetMapping("/findByUser/{userId}")
    public RestResult<List<ApplicationResource>> findByUserId(@PathVariable("userId") final Long userId) {
        return applicationService.findByUserId(userId).toGetResponse();
    }

    @PostMapping("/saveApplicationDetails/{id}")
    public RestResult<Void> saveApplicationDetails(@PathVariable("id") final Long id,
                                                   @RequestBody ApplicationResource application) {

        return applicationService.saveApplicationDetails(id, application).toPostResponse();
    }

    @GetMapping("/getProgressPercentageByApplicationId/{applicationId}")
    public RestResult<CompletedPercentageResource> getProgressPercentageByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        return applicationService.getProgressPercentageByApplicationId(applicationId).toGetResponse();
    }

    @PutMapping("/updateApplicationState")
    public RestResult<Void> updateApplicationState(@RequestParam("applicationId") final Long id,
                                                   @RequestParam("state") final ApplicationState state) {
        ServiceResult<ApplicationResource> updateStatusResult = applicationService.updateApplicationState(id, state);

        if (updateStatusResult.isSuccess() && ApplicationState.SUBMITTED == state) {
            applicationService.saveApplicationSubmitDateTime(id, ZonedDateTime.now());
            applicationService.sendNotificationApplicationSubmitted(id);
        }

        return updateStatusResult.toPutResponse();
    }

    @GetMapping("/applicationReadyForSubmit/{applicationId}")
    public RestResult<Boolean> applicationReadyForSubmit(@PathVariable("applicationId") final Long applicationId) {
        return applicationService.applicationReadyForSubmit(applicationId).toGetResponse();
    }

    @GetMapping("/getApplicationsByCompetitionIdAndUserId/{competitionId}/{userId}/{role}")
    public RestResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(@PathVariable("competitionId") final Long competitionId,
                                                                                         @PathVariable("userId") final Long userId,
                                                                                         @PathVariable("role") final UserRoleType role) {

        return applicationService.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role).toGetResponse();
    }

    @PostMapping("/createApplicationByName/{competitionId}/{userId}")
    public RestResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(
            @PathVariable("competitionId") final Long competitionId,
            @PathVariable("userId") final Long userId,
            @RequestBody JsonNode jsonObj) {

        String name = jsonObj.get("name").textValue();
        ServiceResult<ApplicationResource> applicationResult =
                applicationService.createApplicationByApplicationNameForUserIdAndCompetitionId(name, competitionId, userId);
        return applicationResult.toPostCreateResponse();
    }

    @PostMapping("/{applicationId}/ineligible")
    public RestResult<Void> markAsIneligible(@PathVariable("applicationId") long applicationId,
                                             @RequestBody IneligibleOutcomeResource reason) {
        return applicationService
                .markAsIneligible(applicationId, ineligibleOutcomeMapper.mapToDomain(reason))
                .toPostWithBodyResponse();
    }

    @PostMapping("/informIneligible/{applicationId}")
    public RestResult<Void> informIneligible(@PathVariable("applicationId") final long applicationId,
                                             @RequestBody ApplicationIneligibleSendResource applicationIneligibleSendResource) {
        return applicationService.informIneligible(applicationId, applicationIneligibleSendResource).toPostResponse();
    }

    // IFS-43 added to ease future expansion as application team members are expected to have access to the application team page, but the location of links to that page (enabled by tis method) is as yet unknown
    @GetMapping("/showApplicationTeam/{applicationId}/{userId}")
    public RestResult<Boolean> showApplicationTeam(@PathVariable("applicationId") final Long applicationId,
                                                   @PathVariable("userId") final Long userId) {
        return applicationService.showApplicationTeam(applicationId, userId).toGetResponse();
    }
}
