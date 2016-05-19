package com.worth.ifs.finance.service;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.finance.resource.CostValueId;
import com.worth.ifs.finance.resource.CostValueResource;
import com.worth.ifs.finance.security.CostPermissionRules;
import com.worth.ifs.finance.transactional.CostValueService;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.finance.builder.CostValueResourceBuilder.newCostValue;
import static org.mockito.Matchers.isA;

/**
 * Testing how the secured methods in {@link CostValueService} interact with Spring Security
 */
public class CostValueServiceSecurityTest extends BaseServiceSecurityTest<CostValueService> {


    private CostPermissionRules costPermissionsRules;


    @Before
    public void lookupPermissionRules() {
        costPermissionsRules = getMockPermissionRulesBean(CostPermissionRules.class);
    }


    @Test
    public void testFindApplicationFinanceByApplicationIdAndOrganisation() {
        final CostValueId costValueId = new CostValueId();
        assertAccessDenied(
                () -> service.findOne(costValueId),
                () -> costPermissionsRules.consortiumCanReadACostValueForTheirApplicationAndOrganisation(isA(CostValueResource.class), isA(UserResource.class))
        );
    }


    @Override
    protected Class<TestCostValueService> getServiceClass() {
        return TestCostValueService.class;
    }

    public static class TestCostValueService implements CostValueService {

        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public ServiceResult<CostValueResource> findOne(CostValueId id) {
            return serviceSuccess(newCostValue().build());
        }
    }
}

