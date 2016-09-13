package com.worth.ifs.assessment.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.workflow.resource.ActivityStateResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

public class AssessmentResource {
    private Long id;
    private String event;
    // TODO DW - INFUND-4902 - can we just transfer the AssessmentStates instead of this resource?
    private ActivityStateResource activityState;
    private Calendar lastModified;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> processOutcomes;
    private Long processRole;
    private Boolean submitted;
    private Boolean started;
    private Long application;
    private Long competition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public ActivityStateResource getActivityState() {
        return activityState;
    }

    public void setActivityState(ActivityStateResource activityState) {
        this.activityState = activityState;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Long> getProcessOutcomes() {
        return processOutcomes;
    }

    public void setProcessOutcomes(List<Long> processOutcomes) {
        this.processOutcomes = processOutcomes;
    }

    public Long getProcessRole() {
        return processRole;
    }

    public void setProcessRole(Long processRole) {
        this.processRole = processRole;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Long getApplication() {
        return application;
    }

    public void setApplication(Long application) {
        this.application = application;
    }

    public Long getCompetition() {
        return competition;
    }

    public void setCompetition(Long competition) {
        this.competition = competition;
    }

    @JsonIgnore
    public AssessmentStates getAssessmentState() {
        if (getActivityState() == null) {
            return null;
        }
        return AssessmentStates.fromState(getActivityState().getState());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssessmentResource that = (AssessmentResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(event, that.event)
                .append(activityState, that.activityState)
                .append(lastModified, that.lastModified)
                .append(startDate, that.startDate)
                .append(endDate, that.endDate)
                .append(processOutcomes, that.processOutcomes)
                .append(processRole, that.processRole)
                .append(submitted, that.submitted)
                .append(started, that.started)
                .append(application, that.application)
                .append(competition, that.competition)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(event)
                .append(activityState)
                .append(lastModified)
                .append(startDate)
                .append(endDate)
                .append(processOutcomes)
                .append(processRole)
                .append(submitted)
                .append(started)
                .append(application)
                .append(competition)
                .toHashCode();
    }
}
