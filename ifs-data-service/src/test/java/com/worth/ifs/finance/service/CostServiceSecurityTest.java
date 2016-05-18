package com.worth.ifs.finance.service;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.security.ApplicationLookupStrategy;
import com.worth.ifs.application.security.ApplicationPermissionRules;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.domain.CostField;
import com.worth.ifs.finance.handler.item.CostHandler;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.ApplicationFinanceResourceId;
import com.worth.ifs.finance.resource.CostFieldResource;
import com.worth.ifs.finance.resource.cost.AcademicCost;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.security.*;
import com.worth.ifs.finance.transactional.CostService;
import com.worth.ifs.user.resource.UserResource;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.BuilderAmendFunctions.id;
import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static com.worth.ifs.finance.builder.CostBuilder.newCost;
import static com.worth.ifs.finance.builder.CostFieldResourceBuilder.newCostFieldResource;
import static com.worth.ifs.finance.service.CostServiceSecurityTest.TestCostService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Testing how the secured methods in CostService interact with Spring Security
 */
public class CostServiceSecurityTest extends BaseServiceSecurityTest<CostService> {

    private CostFieldPermissionsRules costFieldPermissionsRules;
    private CostPermissionRules costPermissionsRules;
    private ApplicationFinancePermissionRules applicationFinanceRules;
    private ApplicationPermissionRules applicationRules;
    private ApplicationLookupStrategy applicationLookupStrategy;
    private CostLookupStrategy costLookupStrategy;
    private CostFieldLookupStrategy costFieldLookupStrategy;
    private ApplicationFinanceLookupStrategy applicationFinanceLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        costFieldPermissionsRules = getMockPermissionRulesBean(CostFieldPermissionsRules.class);
        costPermissionsRules = getMockPermissionRulesBean(CostPermissionRules.class);
        applicationFinanceRules = getMockPermissionRulesBean(ApplicationFinancePermissionRules.class);
        applicationRules = getMockPermissionRulesBean(ApplicationPermissionRules.class);
        applicationLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ApplicationLookupStrategy.class);
        costLookupStrategy = getMockPermissionEntityLookupStrategiesBean(CostLookupStrategy.class);
        costFieldLookupStrategy = getMockPermissionEntityLookupStrategiesBean(CostFieldLookupStrategy.class);
        applicationFinanceLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ApplicationFinanceLookupStrategy.class);
    }

    @Test
    public void testFindAllCostFields() {
        service.findAllCostFields();
        verify(costFieldPermissionsRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).loggedInUsersCanReadCostFieldReferenceData(isA(CostFieldResource.class), isA(UserResource.class));
        verifyNoMoreInteractions(costFieldPermissionsRules);
    }

    @Test
    public void testFindApplicationFinanceByApplicationIdAndOrganisation() {
        final Long applicationId = 1L;
        final Long organisationId = 2L;
        assertAccessDenied(
                () -> service.findApplicationFinanceByApplicationIdAndOrganisation(applicationId, organisationId),
                () -> verifyApplicationFinanceResourceReadRulesCalled()
        );
    }

    @Test
    public void testFindApplicationFinanceByApplication() {
        final Long applicationId = 1L;
        ServiceResult<List<ApplicationFinanceResource>> applicationFinanceByApplication = service.findApplicationFinanceByApplication(applicationId);
        assertTrue(applicationFinanceByApplication.getSuccessObject().isEmpty());
        verifyApplicationFinanceResourceReadRulesCalled(ARRAY_SIZE_FOR_POST_FILTER_TESTS);
    }


    @Test
    public void testGetApplicationFinanceById() {
        final Long applicationId = 1L;
        assertAccessDenied(
                () -> service.getApplicationFinanceById(applicationId),
                () -> verifyApplicationFinanceResourceReadRulesCalled()
        );
    }

    @Test
    public void testFinanceDetails() {
        final Long applicationId = 1L;
        final Long organisationId = 1L;
        assertAccessDenied(
                () -> service.financeDetails(applicationId, organisationId),
                () -> verifyApplicationFinanceResourceReadRulesCalled()
        );
    }

    @Test
    public void testFinanceTotals() {
        final Long applicationId = 1L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().with(id(applicationId)).build());
        assertAccessDenied(
                () -> service.financeTotals(applicationId),
                () -> {
                    verify(applicationRules).compAdminCanSeeApplicationFinancesTotals(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).consortiumCanSeeTheApplicationFinanceTotals(isA(ApplicationResource.class), isA(UserResource.class));
                });
    }

    @Test
    public void testGetResearchParticipationPercentage() {
        final Long applicationId = 1L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().with(id(applicationId)).build());
        assertAccessDenied(
                () -> service.getResearchParticipationPercentage(applicationId),
                () -> {
                    verify(applicationRules).assessorCanSeeTheResearchParticipantPercentageInApplicationsTheyAssess(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).compAdminCanSeeTheResearchParticipantPercentageInApplications(isA(ApplicationResource.class), isA(UserResource.class));
                    verify(applicationRules).consortiumCanSeeTheResearchParticipantPercentage(isA(ApplicationResource.class), isA(UserResource.class));
                });
    }

    @Test
    public void testUpdateCost() {
        final Long costId = 1L;
        when(costLookupStrategy.getCost(costId)).thenReturn(newCost().with(id(costId)).build());
        assertAccessDenied(
                () -> service.updateCost(costId, new AcademicCost()),
                () -> {
                    verify(costPermissionsRules).consortiumCanUpdateACostForTheirApplicationAndOrganisation(isA(Cost.class), isA(UserResource.class));
                });
    }

    @Test
    public void testGetCostField() {
        final Long costFieldId = 1L;
        when(costFieldLookupStrategy.getCostField(costFieldId)).thenReturn(newCostFieldResource().with(id(costFieldId)).build());
        assertAccessDenied(
                () -> service.getCostFieldById(costFieldId),
                () -> {
                    verify(costFieldPermissionsRules).loggedInUsersCanReadCostFieldReferenceData(isA(CostFieldResource.class), isA(UserResource.class));
                });
    }

    @Test
    public void testDeleteCost() {
        final Long costId = 1L;
        when(costLookupStrategy.getCost(costId)).thenReturn(newCost().with(id(costId)).build());
        assertAccessDenied(
                () -> service.deleteCost(costId),
                () -> {
                    verify(costPermissionsRules).consortiumCanDeleteACostForTheirApplicationAndOrganisation(isA(Cost.class), isA(UserResource.class));
                });
    }


    @Test
    public void testAddCostOnLongId() {
        final Long applicationFinanceId = 1L;
        final Long questionId = 2L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.addCost(applicationFinanceId, questionId, new AcademicCost()),
                () -> {
                    verify(applicationFinanceRules).consortiumCanAddACostToApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testAddCostOnApplicationFinanceResourceId() {
        final Long applicationId = 1L;
        final Long organisationId = 2L;
        final ApplicationFinanceResourceId applicationFinanceId = new ApplicationFinanceResourceId(applicationId, organisationId);
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.addCost(applicationFinanceId),
                () -> {
                    verify(applicationFinanceRules).consortiumCanAddACostToApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testUpdateCostOnApplicationFinanceId() {
        final Long applicationFinanceId = 1L;
        final ApplicationFinanceResource applicationFinanceResource = new ApplicationFinanceResource();
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.updateCost(applicationFinanceId, applicationFinanceResource),
                () -> {
                    verify(applicationFinanceRules).consortiumCanUpdateACostToApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testGetCostItem() {
        final Long costId = 1L;
        assertAccessDenied(
                () -> service.getCostItem(costId),
                () -> {
                    verify(costPermissionsRules).consortiumCanReadACostForTheirApplicationAndOrganisation(isA(CostItem.class), isA(UserResource.class));
                });
    }

    @Test
    public void testDeleteFinanceFileEntry() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.deleteFinanceFileEntry(applicationFinanceId),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanDeleteAFileForTheApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testCreateFinanceFileEntry() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.createFinanceFileEntry(applicationFinanceId, null, null),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanCreateAFileForTheApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testUpdateFinanceFileEntry() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.updateFinanceFileEntry(applicationFinanceId, null, null),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanUpdateAFileForTheApplicationFinanceForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }

    @Test
    public void testGetFileContents() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> service.getFileContents(applicationFinanceId),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanGetFileEntryResourceByFinanceIdOfACollaborator(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }


    @Test
    public void testGetCosts() {
        final Long costId = 1L;
        final String costTypeName = "academic";
        final Long questionId = 2L;

        service.getCosts(costId, costTypeName, questionId);
        verify(costPermissionsRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).consortiumCanReadACostForTheirApplicationAndOrganisation(isA(Cost.class), isA(UserResource.class));
    }

    @Test
    public void testGetCostItems() {
        final Long costId = 1L;
        final String costTypeName = "academic";
        final Long questionId = 2L;

        service.getCostItems(costId, costTypeName, questionId);
        verify(costPermissionsRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).consortiumCanReadACostForTheirApplicationAndOrganisation(isA(CostItem.class), isA(UserResource.class));
    }

    @Test
    public void testGetCostItems2() {
        final Long applicationFinanceId = 1L;
        final Long questionId = 2L;
        service.getCostItems(applicationFinanceId, questionId);
        verify(costPermissionsRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).consortiumCanReadACostForTheirApplicationAndOrganisation(isA(CostItem.class), isA(UserResource.class));
    }

    private void verifyApplicationFinanceResourceReadRulesCalled() {
        verifyApplicationFinanceResourceReadRulesCalled(1);
    }

    private void verifyApplicationFinanceResourceReadRulesCalled(int nTimes) {
        verify(applicationFinanceRules, times(nTimes)).consortiumCanSeeTheApplicationFinancesForTheirOrganisation(isA(ApplicationFinanceResource.class), isA(UserResource.class));
        verify(applicationFinanceRules, times(nTimes)).assessorCanSeeTheApplicationFinanceForOrganisationsInApplicationsTheyAssess(isA(ApplicationFinanceResource.class), isA(UserResource.class));
        verify(applicationFinanceRules, times(nTimes)).compAdminCanSeeApplicationFinancesForOrganisations(isA(ApplicationFinanceResource.class), isA(UserResource.class));
    }

    @Override
    protected Class<TestCostService> getServiceClass() {
        return TestCostService.class;
    }

    public static class TestCostService implements CostService {


        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public ServiceResult<CostField> getCostFieldById(Long id) {
            return null;
        }

        @Override
        public ServiceResult<List<CostFieldResource>> findAllCostFields() {
            return serviceSuccess(newCostFieldResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<CostItem> getCostItem(Long costItemId) {
            return serviceSuccess(new AcademicCost());
        }

        @Override
        public ServiceResult<CostItem> addCost(Long applicationFinanceId, Long questionId, CostItem newCostItem) {
            return null;
        }

        @Override
        public ServiceResult<CostItem> updateCost(Long id, CostItem newCostItem) {
            return null;
        }

        @Override
        public ServiceResult<List<Cost>> getCosts(Long applicationFinanceId, String costTypeName, Long questionId) {
            return serviceSuccess(newCost().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<List<CostItem>> getCostItems(Long applicationFinanceId, String costTypeName, Long questionId) {
            return getCostItems();
        }

        private ServiceResult<List<CostItem>> getCostItems() {
            final List<CostItem> items = new ArrayList<>();
            for (int i = 0; i < ARRAY_SIZE_FOR_POST_FILTER_TESTS; i++) {
                items.add(new AcademicCost());
            }
            return serviceSuccess(items);
        }

        @Override
        public ServiceResult<List<CostItem>> getCostItems(Long applicationFinanceId, Long questionId) {
            return getCostItems();
        }

        @Override
        public ServiceResult<Void> deleteCost(Long costId) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationFinanceResource> findApplicationFinanceByApplicationIdAndOrganisation(Long applicationId, Long organisationId) {
            return serviceSuccess(newApplicationFinanceResource().build());
        }

        @Override
        public ServiceResult<List<ApplicationFinanceResource>> findApplicationFinanceByApplication(Long applicationId) {
            return serviceSuccess(newApplicationFinanceResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<Double> getResearchParticipationPercentage(Long applicationId) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationFinanceResource> addCost(ApplicationFinanceResourceId applicationFinanceResourceId) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationFinanceResource> getApplicationFinanceById(Long applicationFinanceId) {
            return serviceSuccess(newApplicationFinanceResource().build());
        }

        @Override
        public ServiceResult<ApplicationFinanceResource> updateCost(Long applicationFinanceId, ApplicationFinanceResource applicationFinance) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationFinanceResource> financeDetails(Long applicationId, Long organisationId) {
            return serviceSuccess(newApplicationFinanceResource().build());
        }

        @Override
        public ServiceResult<List<ApplicationFinanceResource>> financeTotals(Long applicationId) {
            return serviceSuccess(newApplicationFinanceResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public CostHandler getCostHandler(Long costItemId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> createFinanceFileEntry(long applicationFinanceId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> updateFinanceFileEntry(long applicationFinanceId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<Void> deleteFinanceFileEntry(long applicationFinanceId) {
            return null;
        }

        @Override
        public ServiceResult<Pair<FileEntryResource, Supplier<InputStream>>> getFileContents(@P("applicationFinanceId") long applicationFinance) {
            return null;
        }
    }
}

