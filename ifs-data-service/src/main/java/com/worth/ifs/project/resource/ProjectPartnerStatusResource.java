package com.worth.ifs.project.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.worth.ifs.project.constant.ProjectActivityStates;
import com.worth.ifs.user.resource.OrganisationTypeEnum;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Used for returning status of each partner (except lead, for which there is a more specific class with constructor)
 * There is a constructor here which is used by subclass but is package local.
 */

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=ProjectLeadStatusResource.class, name="projectLeadStatusResource")
})
public class ProjectPartnerStatusResource {
    private String name;
    private OrganisationTypeEnum organisationType;
    private ProjectActivityStates projectDetailsStatus;
    private ProjectActivityStates bankDetailsStatus;
    private ProjectActivityStates financeChecksStatus;
    private ProjectActivityStates spendProfileStatus;

    /* Following properties are only applicable to lead partner */
    private ProjectActivityStates monitoringOfficerStatus;
    private ProjectActivityStates otherDocumentsStatus;
    private ProjectActivityStates grantOfferLetterStatus;

    //Required for Json Mapping.
    public ProjectPartnerStatusResource() {}

    public ProjectPartnerStatusResource(String name, OrganisationTypeEnum organisationType, ProjectActivityStates projectDetailsStatus, ProjectActivityStates bankDetailsStatus, ProjectActivityStates financeChecksStatus, ProjectActivityStates spendProfileStatus) {
        this(name, organisationType, projectDetailsStatus, bankDetailsStatus, financeChecksStatus, spendProfileStatus, ProjectActivityStates.NOT_REQUIRED, ProjectActivityStates.NOT_REQUIRED, ProjectActivityStates.NOT_REQUIRED);
    }

    ProjectPartnerStatusResource(String name, OrganisationTypeEnum organisationType, ProjectActivityStates projectDetailsStatus, ProjectActivityStates bankDetailsStatus, ProjectActivityStates financeChecksStatus, ProjectActivityStates spendProfileStatus, ProjectActivityStates monitoringOfficerStatus, ProjectActivityStates otherDocumentsStatus, ProjectActivityStates grantOfferLetterStatus) {
        this.name = name;
        this.organisationType = organisationType;
        this.projectDetailsStatus = projectDetailsStatus;
        this.bankDetailsStatus = bankDetailsStatus;
        this.financeChecksStatus = financeChecksStatus;
        this.spendProfileStatus = spendProfileStatus;
        this.monitoringOfficerStatus = monitoringOfficerStatus;
        this.otherDocumentsStatus = otherDocumentsStatus;
        this.grantOfferLetterStatus = grantOfferLetterStatus;
    }

    public String getName() {
        return name;
    }

    public OrganisationTypeEnum getOrganisationType() {
        return organisationType;
    }

    public ProjectActivityStates getProjectDetailsStatus() {
        return projectDetailsStatus;
    }

    public ProjectActivityStates getBankDetailsStatus() {
        return bankDetailsStatus;
    }

    public ProjectActivityStates getFinanceChecksStatus() {
        return financeChecksStatus;
    }

    public ProjectActivityStates getSpendProfileStatus() {
        return spendProfileStatus;
    }

    public ProjectActivityStates getMonitoringOfficerStatus() {
        return monitoringOfficerStatus;
    }

    public ProjectActivityStates getOtherDocumentsStatus() {
        return otherDocumentsStatus;
    }

    public ProjectActivityStates getGrantOfferLetterStatus() {
        return grantOfferLetterStatus;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganisationType(OrganisationTypeEnum organisationType) {
        this.organisationType = organisationType;
    }

    public void setProjectDetailsStatus(ProjectActivityStates projectDetailsStatus) {
        this.projectDetailsStatus = projectDetailsStatus;
    }

    public void setBankDetailsStatus(ProjectActivityStates bankDetailsStatus) {
        this.bankDetailsStatus = bankDetailsStatus;
    }

    public void setFinanceChecksStatus(ProjectActivityStates financeChecksStatus) {
        this.financeChecksStatus = financeChecksStatus;
    }

    public void setSpendProfileStatus(ProjectActivityStates spendProfileStatus) {
        this.spendProfileStatus = spendProfileStatus;
    }

    public void setMonitoringOfficerStatus(ProjectActivityStates monitoringOfficerStatus) {
        this.monitoringOfficerStatus = monitoringOfficerStatus;
    }

    public void setOtherDocumentsStatus(ProjectActivityStates otherDocumentsStatus) {
        this.otherDocumentsStatus = otherDocumentsStatus;
    }

    public void setGrantOfferLetterStatus(ProjectActivityStates grantOfferLetterStatus) {
        this.grantOfferLetterStatus = grantOfferLetterStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectPartnerStatusResource that = (ProjectPartnerStatusResource) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(organisationType, that.organisationType)
                .append(projectDetailsStatus, that.projectDetailsStatus)
                .append(bankDetailsStatus, that.bankDetailsStatus)
                .append(financeChecksStatus, that.financeChecksStatus)
                .append(spendProfileStatus, that.spendProfileStatus)
                .append(monitoringOfficerStatus, that.monitoringOfficerStatus)
                .append(otherDocumentsStatus, that.otherDocumentsStatus)
                .append(grantOfferLetterStatus, that.grantOfferLetterStatus)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(organisationType)
                .append(projectDetailsStatus)
                .append(bankDetailsStatus)
                .append(financeChecksStatus)
                .append(spendProfileStatus)
                .append(monitoringOfficerStatus)
                .append(otherDocumentsStatus)
                .append(grantOfferLetterStatus)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("organisationType", organisationType)
                .append("projectDetailsStatus", projectDetailsStatus)
                .append("bankDetailsStatus", bankDetailsStatus)
                .append("financeChecksStatus", financeChecksStatus)
                .append("spendProfileStatus", spendProfileStatus)
                .append("monitoringOfficerStatus", monitoringOfficerStatus)
                .append("otherDocumentsStatus", otherDocumentsStatus)
                .append("grantOfferLetterStatus", grantOfferLetterStatus)
                .toString();
    }
}
