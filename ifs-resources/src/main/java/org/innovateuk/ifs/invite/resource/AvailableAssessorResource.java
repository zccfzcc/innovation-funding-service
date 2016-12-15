package org.innovateuk.ifs.invite.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.user.resource.BusinessType;

/**
 * DTO for an assessor that is available to be invited.
 */
public class AvailableAssessorResource extends AssessorInviteResource {

    private String email;
    private BusinessType businessType;
    private boolean added;

    public AvailableAssessorResource() {
    }

    public AvailableAssessorResource(String firstName, String lastName, CategoryResource innovationArea, boolean compliant, String email, BusinessType businessType, boolean added) {
        super(firstName, lastName, innovationArea, compliant);
        this.email = email;
        this.businessType = businessType;
        this.added = added;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AvailableAssessorResource that = (AvailableAssessorResource) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(added, that.added)
                .append(email, that.email)
                .append(businessType, that.businessType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(email)
                .append(businessType)
                .append(added)
                .toHashCode();
    }
}
