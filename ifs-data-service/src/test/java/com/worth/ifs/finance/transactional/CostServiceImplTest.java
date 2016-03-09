package com.worth.ifs.finance.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.handler.OrganisationFinanceDefaultHandler;
import com.worth.ifs.finance.handler.OrganisationFinanceDelegate;
import com.worth.ifs.finance.handler.OrganisationFinanceHandler;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.OrganisationType;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static com.worth.ifs.BuilderAmendFunctions.id;
import static com.worth.ifs.LambdaMatcher.lambdaMatches;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.finance.builder.ApplicationFinanceBuilder.newApplicationFinance;
import static com.worth.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

/**
 *
 */
public class CostServiceImplTest extends BaseServiceUnitTest<CostServiceImpl> {

    @Mock
    private OrganisationFinanceHandler organisationFinanceHandlerMock;

    @Mock
    private OrganisationFinanceDelegate organisationFinanceDelegateMock;

    @Mock
    private OrganisationFinanceDefaultHandler organisationFinanceDefaultHandlerMock;

    @Override
    protected CostServiceImpl supplyServiceUnderTest() {
        return new CostServiceImpl();
    }

    @Test
    public void testFindApplicationFinanceByApplicationIdAndOrganisation() {

        Organisation organisation = newOrganisation().build();
        Application application = newApplication().build();

        ApplicationFinance existingFinance = newApplicationFinance().withOrganisation(organisation).withApplication(application).build();
        when(applicationFinanceRepository.findByApplicationIdAndOrganisationId(123L, 456L)).thenReturn(existingFinance);

        ServiceResult<ApplicationFinanceResource> result = service.findApplicationFinanceByApplicationIdAndOrganisation(123L, 456L);
        assertTrue(result.isSuccess());

        ApplicationFinanceResource expectedFinance = newApplicationFinanceResource().
                with(id(existingFinance.getId())).
                withOrganisation(organisation.getId()).
                withApplication(application.getId()).
                build();

        assertEquals(expectedFinance, result.getSuccessObject());
    }

    @Test
    public void testFindApplicationFinanceByApplicationId() {

        Organisation organisation = newOrganisation().build();
        Application application = newApplication().build();

        ApplicationFinance existingFinance = newApplicationFinance().withOrganisation(organisation).withApplication(application).build();
        when(applicationFinanceRepository.findByApplicationId(123L)).thenReturn(singletonList(existingFinance));

        ServiceResult<List<ApplicationFinanceResource>> result = service.findApplicationFinanceByApplication(123L);
        assertTrue(result.isSuccess());

        ApplicationFinanceResource expectedFinance = newApplicationFinanceResource().
                with(id(existingFinance.getId())).
                withOrganisation(organisation.getId()).
                withApplication(application.getId()).
                build();

        assertEquals(singletonList(expectedFinance), result.getSuccessObject());
    }

    @Test
    public void testAddCost() {

        Organisation organisation = newOrganisation().withTypes(new OrganisationType("Business", null)).build();
        Application application = newApplication().build();

        when(applicationRepositoryMock.findOne(123L)).thenReturn(application);
        when(organisationRepositoryMock.findOne(456L)).thenReturn(organisation);
        when(organisationFinanceDelegateMock.getOrganisationFinanceHandler("Business")).thenReturn(organisationFinanceDefaultHandlerMock);

        ApplicationFinance newFinance = newApplicationFinance().withOrganisation(organisation).withApplication(application).build();

        ApplicationFinance newFinanceExpectations = argThat(lambdaMatches(finance -> {
            assertEquals(application, finance.getApplication());
            assertEquals(organisation, finance.getOrganisation());
            return true;
        }));

        when(applicationFinanceRepository.save(newFinanceExpectations)).thenReturn(newFinance);

        ServiceResult<ApplicationFinanceResource> result = service.addCost(123L, 456L);
        assertTrue(result.isSuccess());

        ApplicationFinanceResource expectedFinance = newApplicationFinanceResource().
                with(id(newFinance.getId())).
                withOrganisation(organisation.getId()).
                withApplication(application.getId()).
                build();

        assertEquals(expectedFinance, result.getSuccessObject());
    }
}
