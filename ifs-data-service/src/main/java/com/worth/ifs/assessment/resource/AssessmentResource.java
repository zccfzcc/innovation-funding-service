package com.worth.ifs.assessment.resource;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

public class AssessmentResource {
    private Long id;
    private String event;
    private String status;
    private Calendar lastModified;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> processOutcomes;
    Long processRole;
    private Boolean submitted;
    private Boolean started;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
