package com.worth.ifs.competition.controller;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.competition.resource.MilestoneType;
import com.worth.ifs.competition.transactional.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MilestoneController exposes Milestone data and operations through a REST API
 */
@RestController
@RequestMapping("/milestone")
public class MilestoneController {

    @Autowired
    private MilestoneService milestoneService;

    @RequestMapping(value = "/{competitionId}", method = RequestMethod.GET)
    public RestResult<List<MilestoneResource>> getAllMilestonesByCompetitionId(
            @PathVariable("competitionId") final Long competitionId){
        return milestoneService.getAllMilestonesByCompetitionId(competitionId).toGetResponse();
    }

    @RequestMapping(value = "/{competitionId}/getByType", method = RequestMethod.GET)
    public RestResult<MilestoneResource> getMilestoneByTypeAndCompetitionId(@RequestParam final MilestoneType type,
                                                                            @PathVariable("competitionId") final Long competitionId) {
        return milestoneService.getMilestoneByTypeAndCompetitionId(type, competitionId).toGetResponse();
    }

    @RequestMapping(value = "/{competitionId}", method = RequestMethod.POST)
    public RestResult<MilestoneResource> create(@RequestBody final MilestoneType type,
                                                @PathVariable("competitionId") final Long competitionId) {
        return milestoneService.create(type, competitionId).toPostCreateResponse();
    }

    @RequestMapping(value = "/{competitionId}", method = RequestMethod.PUT)
    public RestResult<Void> saveMilestones(@RequestBody final List<MilestoneResource> milestones,
                                           @PathVariable("competitionId") final Long competitionId) {
         return milestoneService.updateMilestones(competitionId, milestones).toPutResponse();
    }

    @RequestMapping(value = "/", method = RequestMethod.PUT)
    public RestResult<Void> saveMilestone(@RequestBody final MilestoneResource milestone) {
        return milestoneService.updateMilestone(milestone).toPutResponse();
    }
 }
