package org.innovateuk.ifs.finance.resource;

import org.innovateuk.ifs.project.finance.resource.Viability;
import org.innovateuk.ifs.user.resource.OrganisationSize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Project finance resource holds the organisation's finance resources for a project during Finance Checks
 */
public class ProjectFinanceResource extends BaseFinanceResource {

    private Viability viability;

    private Boolean isCreditReport;

    public Long getProject() {
        return super.getTarget();
    }

    public void setProject(Long target) {
        super.setTarget(target);
    }

    public Viability getViability() {
        return viability;
    }

    public void setViability(Viability viability) {
        this.viability = viability;
    }

    public Boolean getIsCreditReport() { return isCreditReport; }

    public void setIsCreditReport(Boolean isCreditReport) { this.isCreditReport = isCreditReport; }

    public ProjectFinanceResource(Long id, Long organisation, Long projectId, OrganisationSize organisationSize) {
        super(id, organisation, projectId, organisationSize);
    }

    public ProjectFinanceResource(BaseFinanceResource originalFinance) {
        super(originalFinance);
    }

    // for mapstruct
    public ProjectFinanceResource() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectFinanceResource that = (ProjectFinanceResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(organisation, that.organisation)
                .append(target, that.target)
                .append(organisationSize, that.organisationSize)
                .append(financeOrganisationDetails, that.financeOrganisationDetails)
                .append(isCreditReport, that.isCreditReport)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(organisation)
                .append(target)
                .append(organisationSize)
                .append(financeOrganisationDetails)
                .append(isCreditReport)
                .toHashCode();
    }
}
