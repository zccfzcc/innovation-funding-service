package com.worth.ifs.user.controller;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import com.worth.ifs.user.transactional.OrganisationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/organisationtype")
public class OrganisationTypeController {

    @Autowired
    private OrganisationTypeService service;

    @Autowired
    private OrganisationService organisationService;


    @RequestMapping("/{id}")
    public RestResult<OrganisationTypeResource> findById(@PathVariable("id") final Long id) {
        return service.findOne(id).toGetResponse();
    }

    @RequestMapping("/getAll")
    public RestResult<List<OrganisationTypeResource>> findAll() {
        return service.findAll().toGetResponse();
    }


    @RequestMapping("/getTypeForOrganisation/{organisationId}")
    public RestResult<OrganisationTypeResource> findTypeForOrganisation(@PathVariable Long organisationId) {
        return organisationService.findById(organisationId).
                andOnSuccess(organisation -> service.findOne(organisation.getOrganisationType())).
                toGetResponse();
    }
}