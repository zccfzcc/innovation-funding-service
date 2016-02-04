package com.worth.ifs.user.controller;

import com.worth.ifs.organisation.domain.Address;
import com.worth.ifs.organisation.repository.AddressRepository;
import com.worth.ifs.security.NotSecured;
import com.worth.ifs.user.domain.*;
import com.worth.ifs.user.mapper.OrganisationMapper;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.repository.OrganisationTypeRepository;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.resource.OrganisationResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This RestController exposes CRUD operations to both the
 * {@link com.worth.ifs.user.service.OrganisationRestServiceImpl} and other REST-API users
 * to manage {@link Organisation} related data.
 */
@RestController
@RequestMapping("/organisation")
public class OrganisationController {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    OrganisationTypeRepository organisationTypeRepository;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    ProcessRoleRepository processRoleRepository;

    @Autowired
    OrganisationMapper organisationMapper;

    @RequestMapping("/findByApplicationId/{applicationId}")
    public Set<Organisation> findByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        List<ProcessRole> roles = processRoleRepository.findByApplicationId(applicationId);
        Set<Organisation> organisations = roles.stream().map(role -> organisationRepository.findByProcessRoles(role)).collect(Collectors.toCollection(LinkedHashSet::new));

        return organisations;
    }

    @RequestMapping("/findById/{organisationId}")
    public Organisation findById(@PathVariable("organisationId") final Long organisationId) {
        return organisationRepository.findOne(organisationId);
    }

    @NotSecured("When creating a application, this methods is called before creating a user account, so there his no way to authenticate.")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public OrganisationResource create(@RequestBody Organisation organisation){
        log.debug("OrganisationController , create method");
        log.debug("OrganisationController , create method " + organisation.getName());
        if(organisation.getOrganisationType()==null){
            organisation.setOrganisationType(organisationTypeRepository.findOne(OrganisationTypeEnum.BUSINESS.getOrganisationTypeId()));
        }
        organisation = organisationRepository.save(organisation);
        return organisationMapper.mapOrganisationToResource(organisation);
    }

    @NotSecured("When creating a application, this methods is called before creating a user account, so there his no way to authenticate.")
    @RequestMapping(value = "/saveResource", method = RequestMethod.POST)
    public OrganisationResource saveResource(@RequestBody OrganisationResource organisationResource){
        log.debug("OrganisationController , create method");
        Organisation organisation = organisationMapper.resourceToOrganisation(organisationResource);
        if(organisation.getOrganisationType()==null){
            organisation.setOrganisationType(organisationTypeRepository.findOne(OrganisationTypeEnum.BUSINESS.getOrganisationTypeId()));
        }
        log.debug("OrganisationController , create method " + organisation.getName());

        organisation = organisationRepository.save(organisation);
        return organisationMapper.mapOrganisationToResource(organisation);
    }

    @NotSecured("When creating a application, this methods is called before creating a user account, so there his no way to authenticate.")
    @RequestMapping(value = "/addAddress/{organisationId}", method = RequestMethod.POST)
    public OrganisationResource addAddress(@PathVariable("organisationId") final Long organisationId, @RequestParam("addressType") final AddressType addressType, @RequestBody Address address){
        log.info("OrganisationController , add address");
        log.info("OrganisationController , add address2 "+ organisationId);
        log.info("OrganisationController , add addresstype "+ addressType.name());
        log.info("OrganisationController , add getAddressLine1 "+ address.getAddressLine1());
        address = addressRepository.save(address);
        Organisation organisation = organisationRepository.findOne(organisationId);
        log.info("existing addresses: " + organisation.getAddresses().size());
        organisation.addAddress(address, addressType);
        organisation = organisationRepository.save(organisation);
        return organisationMapper.mapOrganisationToResource(organisation);
    }
}
