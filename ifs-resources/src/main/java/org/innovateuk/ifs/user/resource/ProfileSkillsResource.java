package org.innovateuk.ifs.user.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;

import java.util.List;

/**
 * Profile Skills Data Transfer Object
 */
public class ProfileSkillsResource {

    private Long user;
    private List<InnovationAreaResource> innovationAreas;
    private String skillsAreas;
    private BusinessType businessType;

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public List<InnovationAreaResource> getInnovationAreas() {
        return innovationAreas;
    }

    public void setInnovationAreas(List<InnovationAreaResource> innovationAreas) {
        this.innovationAreas = innovationAreas;
    }

    public String getSkillsAreas() {
        return skillsAreas;
    }

    public void setSkillsAreas(String skillsAreas) {
        this.skillsAreas = skillsAreas;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProfileSkillsResource that = (ProfileSkillsResource) o;

        return new EqualsBuilder()
                .append(user, that.user)
                .append(innovationAreas, that.innovationAreas)
                .append(skillsAreas, that.skillsAreas)
                .append(businessType, that.businessType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(user)
                .append(innovationAreas)
                .append(skillsAreas)
                .append(businessType)
                .toHashCode();
    }
}
