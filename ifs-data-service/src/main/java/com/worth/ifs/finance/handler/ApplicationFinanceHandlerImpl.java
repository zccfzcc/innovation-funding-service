package com.worth.ifs.finance.handler;

import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.repository.ApplicationFinanceRepository;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.ApplicationFinanceResourceId;
import com.worth.ifs.finance.resource.category.CostCategory;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.OrganisationTypeEnum;
import com.worth.ifs.user.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ApplicationFinanceHandlerImpl handles the finance information on application level.
 */
@Service
public class ApplicationFinanceHandlerImpl implements ApplicationFinanceHandler {

    @Autowired
    ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationFinanceDelegate organiastionFinanceDelegate;

    @Override
    public ApplicationFinanceResource getApplicationOrganisationFinances(ApplicationFinanceResourceId applicationFinanceResourceId) {
        ApplicationFinance applicationFinance = applicationFinanceRepository.findByApplicationIdAndOrganisationId(
                applicationFinanceResourceId.getApplicationId(), applicationFinanceResourceId.getOrganisationId());
        ApplicationFinanceResource applicationFinanceResource = null;

        if(applicationFinance!=null) {
            applicationFinanceResource = new ApplicationFinanceResource(applicationFinance);
            setFinanceDetails(applicationFinanceResource);
        }
        return applicationFinanceResource;
    }

    @Override
    public List<ApplicationFinanceResource> getApplicationTotals(Long applicationId) {
        List<ApplicationFinance> applicationFinances = applicationFinanceRepository.findByApplicationId(applicationId);
        List<ApplicationFinanceResource> applicationFinanceResources = new ArrayList<>();

        for(ApplicationFinance applicationFinance : applicationFinances) {
            ApplicationFinanceResource applicationFinanceResource = new ApplicationFinanceResource(applicationFinance);
            OrganisationFinanceHandler organisationFinanceHandler = organiastionFinanceDelegate.getOrganisationFinanceHandler(applicationFinance.getOrganisation().getOrganisationType().getName());
            EnumMap<CostType, CostCategory> costs = new EnumMap<>(organisationFinanceHandler.getOrganisationFinanceTotals(applicationFinanceResource.getId()));
            applicationFinanceResource.setFinanceOrganisationDetails(costs);
            applicationFinanceResources.add(applicationFinanceResource);
        }
        return applicationFinanceResources;
    }

    @Override
    public BigDecimal getResearchParticipationPercentage(Long applicationId){
        List<ApplicationFinanceResource> applicationFinanceResources = this.getApplicationTotals(applicationId);

        BigDecimal totalCosts = applicationFinanceResources.stream()
                .map(ApplicationFinanceResource::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal researchCosts = applicationFinanceResources.stream()
                .filter(f ->
                                OrganisationTypeEnum.isResearch(organisationRepository.findOne(f.getOrganisation()).getOrganisationType())
                )
                .map(ApplicationFinanceResource::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal researchParticipation = BigDecimal.ZERO;

        if(totalCosts.compareTo(BigDecimal.ZERO)!=0) {
            researchParticipation = researchCosts.divide(totalCosts, 6, BigDecimal.ROUND_HALF_UP);
        }
        researchParticipation = researchParticipation.multiply(BigDecimal.valueOf(100));
        return researchParticipation.setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    protected void setFinanceDetails(ApplicationFinanceResource applicationFinanceResource) {
        Organisation organisation = organisationRepository.findOne(applicationFinanceResource.getOrganisation());
        OrganisationFinanceHandler organisationFinanceHandler = organiastionFinanceDelegate.getOrganisationFinanceHandler(organisation.getOrganisationType().getName());
        Map<CostType, CostCategory> costs = organisationFinanceHandler.getOrganisationFinances(applicationFinanceResource.getId());
        applicationFinanceResource.setFinanceOrganisationDetails(costs);
    }
}