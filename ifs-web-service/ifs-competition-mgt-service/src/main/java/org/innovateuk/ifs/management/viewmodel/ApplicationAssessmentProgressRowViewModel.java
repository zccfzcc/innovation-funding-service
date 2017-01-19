package org.innovateuk.ifs.management.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Holder of model attributes for an assessor in the Application Progress view.
 */
abstract class ApplicationAssessmentProgressRowViewModel {

    private String name;
    private long totalApplicationsCount;
    private long assignedCount;

    protected ApplicationAssessmentProgressRowViewModel(String name, long totalApplicationsCount, long assignedCount) {
        this.name = name;
        this.totalApplicationsCount = totalApplicationsCount;
        this.assignedCount = assignedCount;
    }

    public String getName() {
        return name;
    }

    public long getTotalApplicationsCount() {
        return totalApplicationsCount;
    }

    public long getAssignedCount() {
        return assignedCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ApplicationAssessmentProgressRowViewModel that = (ApplicationAssessmentProgressRowViewModel) o;

        return new EqualsBuilder()
                .append(totalApplicationsCount, that.totalApplicationsCount)
                .append(assignedCount, that.assignedCount)
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(totalApplicationsCount)
                .append(assignedCount)
                .toHashCode();
    }
}
