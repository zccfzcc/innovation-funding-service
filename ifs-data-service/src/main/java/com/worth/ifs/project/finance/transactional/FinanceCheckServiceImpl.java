package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.finance.domain.Cost;
import com.worth.ifs.project.finance.domain.CostGroup;
import com.worth.ifs.project.finance.domain.FinanceCheck;
import com.worth.ifs.project.finance.repository.FinanceCheckRepository;
import com.worth.ifs.project.finance.resource.CostGroupResource;
import com.worth.ifs.project.finance.resource.CostResource;
import com.worth.ifs.project.finance.resource.FinanceCheckResource;
import com.worth.ifs.project.resource.ProjectOrganisationCompositeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.Collections.emptyList;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

/**
 * A transactional service for finance check functionality
 */
@Service
public class FinanceCheckServiceImpl implements FinanceCheckService {

    @Autowired
    private FinanceCheckRepository financeCheckRepository;

    @Override
    public ServiceResult<FinanceCheckResource> getByProjectAndOrganisation(ProjectOrganisationCompositeId key) {
        return find(financeCheckRepository.findByProjectIdAndOrganisationId(key.getProjectId(), key.getOrganisationId()),
                notFoundError(FinanceCheck.class, id)).
                andOnSuccessReturn(this::mapToResource);
    }

    @Override
    public ServiceResult<Void> save(FinanceCheckResource financeCheckResource) {
        FinanceCheck toSave = mapToDomain(financeCheckResource);
        financeCheckRepository.save(toSave);
        return serviceSuccess();
    }

    @Override
    public ServiceResult<FinanceCheckResource> generate(ProjectOrganisationCompositeId key) {
        throw new UnsupportedOperationException();
    }

    private FinanceCheck mapToDomain(FinanceCheckResource financeCheckResource){
        FinanceCheck fc = financeCheckRepository.findByProjectIdAndOrganisationId(financeCheckResource.getProject(), financeCheckResource.getOrganisation());
        for(CostResource cr : financeCheckResource.getCostGroup().getCosts()){
            Optional<Cost> oc = fc.getCostGroup().getCostById(cr.getId());
            if(oc.isPresent()){
                Cost c = oc.get();
                c.setValue(cr.getValue());
            }
        }
        return fc;
    }

    private FinanceCheckResource mapToResource(FinanceCheck fc) {
        FinanceCheckResource financeCheckResource = new FinanceCheckResource();
        financeCheckResource.setId(fc.getId());
        financeCheckResource.setOrganisation(fc.getOrganisation().getId());
        financeCheckResource.setProject(fc.getProject().getId());
        financeCheckResource.setCostGroup(mapCostGroupToResource(fc.getCostGroup()));
        return financeCheckResource;
    }

    private CostGroupResource mapCostGroupToResource(CostGroup costGroup){
        CostGroupResource costGroupResource = new CostGroupResource();
        if(costGroup != null) {
            costGroupResource.setId(costGroup.getId());
            costGroupResource.setDescription(costGroup.getDescription());
            costGroupResource.setCosts(mapCostsToCostResource(costGroup.getCosts()));
        }
        return costGroupResource;
    }

    private List<CostResource> mapCostsToCostResource(List<Cost> costs){
        if(costs == null){
            return emptyList();
        }
        return simpleMap(costs, c -> {
            CostResource cr = new CostResource();
            cr.setId(c.getId());
            cr.setValue(c.getValue());
            return cr;
        });
    }
}
