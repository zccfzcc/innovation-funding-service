package org.innovateuk.ifs.application.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.user.resource.BusinessType;

import java.util.List;

import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.*;

/**
 * TODO
 */
public class ApplicationAssessorResource {

    private Long userId;
    private String firstName;
    private String lastName;
    private BusinessType businessType;
    private List<InnovationAreaResource> innovationAreas;
    private String skillAreas;
    private String rejectReason;
    private String rejectComment;
    private boolean available;
    private AssessmentStates mostRecentAssessmentState;
    private int totalApplicationsCount;
    private int assignedCount;
    private int submittedCount;

    public ApplicationAssessorResource() {
    }

    public ApplicationAssessorResource(Long userId, String firstName, String lastName, BusinessType businessType, List<InnovationAreaResource> innovationAreas, String skillAreas, String rejectReason, String rejectComment, boolean available, AssessmentStates mostRecentAssessmentState, int totalApplicationsCount, int assignedCount, int submittedCount) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.businessType = businessType;
        this.innovationAreas = innovationAreas;
        this.skillAreas = skillAreas;
        this.rejectReason = rejectReason;
        this.rejectComment = rejectComment;
        this.available = available;
        this.mostRecentAssessmentState = mostRecentAssessmentState;
        this.totalApplicationsCount = totalApplicationsCount;
        this.assignedCount = assignedCount;
        this.submittedCount = submittedCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public List<InnovationAreaResource> getInnovationAreas() {
        return innovationAreas;
    }

    public void setInnovationAreas(List<InnovationAreaResource> innovationAreas) {
        this.innovationAreas = innovationAreas;
    }

    public String getSkillAreas() {
        return skillAreas;
    }

    public void setSkillAreas(String skillAreas) {
        this.skillAreas = skillAreas;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRejectComment() {
        return rejectComment;
    }

    public void setRejectComment(String rejectComment) {
        this.rejectComment = rejectComment;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public AssessmentStates getMostRecentAssessmentState() {
        return mostRecentAssessmentState;
    }

    public void setMostRecentAssessmentState(AssessmentStates mostRecentAssessmentState) {
        this.mostRecentAssessmentState = mostRecentAssessmentState;
    }

    public int getTotalApplicationsCount() {
        return totalApplicationsCount;
    }

    public void setTotalApplicationsCount(int totalApplicationsCount) {
        this.totalApplicationsCount = totalApplicationsCount;
    }

    public int getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(int assignedCount) {
        this.assignedCount = assignedCount;
    }

    public int getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(int submittedCount) {
        this.submittedCount = submittedCount;
    }

    public boolean isAssigned() {
        return complementOf(of(REJECTED, WITHDRAWN)).contains(mostRecentAssessmentState);
    }

    public boolean isAccepted() {
        return of(ACCEPTED, OPEN, DECIDE_IF_READY_TO_SUBMIT, READY_TO_SUBMIT, SUBMITTED).contains(mostRecentAssessmentState);
    }

    public boolean isNotified() {
        return complementOf(of(CREATED)).contains(mostRecentAssessmentState);
    }

    public boolean isStarted() {
        return complementOf(of(CREATED, PENDING, ACCEPTED, REJECTED, WITHDRAWN)).contains(mostRecentAssessmentState);
    }

    public boolean isSubmitted() {
        return SUBMITTED == mostRecentAssessmentState;
    }

    public boolean isRejected() {
        return REJECTED == mostRecentAssessmentState;
    }

    public boolean isWithdrawn() {
        return WITHDRAWN == mostRecentAssessmentState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationAssessorResource that = (ApplicationAssessorResource) o;

        return new EqualsBuilder()
                .append(available, that.available)
                .append(totalApplicationsCount, that.totalApplicationsCount)
                .append(assignedCount, that.assignedCount)
                .append(submittedCount, that.submittedCount)
                .append(userId, that.userId)
                .append(firstName, that.firstName)
                .append(lastName, that.lastName)
                .append(businessType, that.businessType)
                .append(innovationAreas, that.innovationAreas)
                .append(skillAreas, that.skillAreas)
                .append(rejectReason, that.rejectReason)
                .append(rejectComment, that.rejectComment)
                .append(mostRecentAssessmentState, that.mostRecentAssessmentState)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(userId)
                .append(firstName)
                .append(lastName)
                .append(businessType)
                .append(innovationAreas)
                .append(skillAreas)
                .append(rejectReason)
                .append(rejectComment)
                .append(available)
                .append(mostRecentAssessmentState)
                .append(totalApplicationsCount)
                .append(assignedCount)
                .append(submittedCount)
                .toHashCode();
    }
}