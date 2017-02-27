package org.innovateuk.ifs.application.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Holder of model attributes for the Application Team view.
 */
public class ApplicationTeamViewModel {

    private long applicationId;
    private String applicationName;
    private List<ApplicationTeamOrganisationRowViewModel> organisations;
    private boolean userLeadApplicant;

    public ApplicationTeamViewModel(long applicationId,
                                    String applicationName,
                                    List<ApplicationTeamOrganisationRowViewModel> organisations,
                                    boolean userLeadApplicant) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.organisations = organisations;
        this.userLeadApplicant = userLeadApplicant;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<ApplicationTeamOrganisationRowViewModel> getOrganisations() {
        return organisations;
    }

    public boolean isUserLeadApplicant() {
        return userLeadApplicant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationTeamViewModel that = (ApplicationTeamViewModel) o;

        return new EqualsBuilder()
                .append(applicationId, that.applicationId)
                .append(userLeadApplicant, that.userLeadApplicant)
                .append(applicationName, that.applicationName)
                .append(organisations, that.organisations)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(applicationId)
                .append(applicationName)
                .append(organisations)
                .append(userLeadApplicant)
                .toHashCode();
    }
}