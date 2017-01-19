package org.innovateuk.ifs.finance.builder;

import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

/**
 * Builder for ApplicationFinance entities.
 */
public class ApplicationFinanceResourceBuilder extends BaseFinanceResourceBuilder<ApplicationFinanceResource, ApplicationFinanceResourceBuilder> {

    public ApplicationFinanceResourceBuilder withFinanceFileEntry(Long financeFileEntry) {
        return with(finance -> finance.setFinanceFileEntry(financeFileEntry));
    }

    public ApplicationFinanceResourceBuilder withApplication(Long... applicationIds) {
        return withArray((applicationId, applicationFinanceResource) -> applicationFinanceResource.setApplication(applicationId), applicationIds);
    }

    private ApplicationFinanceResourceBuilder(List<BiConsumer<Integer, ApplicationFinanceResource>> newMultiActions) {
        super(newMultiActions);
    }

    public static ApplicationFinanceResourceBuilder newApplicationFinanceResource() {
        return new ApplicationFinanceResourceBuilder(emptyList()).with(uniqueIds());
    }

    public ApplicationFinanceResourceBuilder withResearchCategories(Set<ResearchCategoryResource> categories) {
        return with(applicationFinanceResource -> applicationFinanceResource.setResearchCategories(categories));
    }

    @Override
    protected ApplicationFinanceResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ApplicationFinanceResource>> actions) {
        return new ApplicationFinanceResourceBuilder(actions);
    }

    @Override
    protected ApplicationFinanceResource createInitial() {
        return new ApplicationFinanceResource();
    }
}
