package com.worth.ifs.invite.resource;

import com.worth.ifs.invite.constant.InviteStatusConstants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * DTO to transfer Project Invite related Entities
 */
public class InviteProjectResource {

    private Long id;
    private Long user;
    private String name;
    private String nameConfirmed;
    private String email;
    private Long project;
    private Long applicationId;
    private Long organisation;
    private String projectName;
    private String hash;
    private InviteStatusConstants status;


    public InviteProjectResource() {
        // no-arg constructor
    }

    public InviteProjectResource(Long id, Long user, String name, String email, Long project, Long organisation, Long applicationId, String hash, InviteStatusConstants status) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.email = email;
        this.project = project;
        this.organisation = organisation;
        this.applicationId  = applicationId;
        this.hash = hash;
        this.status = status;
    }

    public InviteProjectResource(String name, String email, Long project) {
        this.name = name;
        this.email = email;
        this.project = project;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getUser() { return user; }

    public void setUser(Long user) { this.user = user; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getNameConfirmed() { return nameConfirmed; }

    public void setNameConfirmed(String nameConfirmed) { this.nameConfirmed = nameConfirmed; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public Long getProject() { return project; }

    public void setProject(Long project) { this.project = project; }

    public Long getOrganisation() { return organisation; }

    public void setOrganisation(Long organisation) { this.organisation = organisation; }

    public Long getApplicationId() { return applicationId; }

    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getProjectName() { return projectName; }

    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getHash() { return hash; }

    public void setHash(String hash) { this.hash = hash; }

    public InviteStatusConstants getStatus() { return status; }

    public void setStatus(InviteStatusConstants status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InviteProjectResource that = (InviteProjectResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(user, that.user)
                .append(name, that.name)
                .append(nameConfirmed, that.nameConfirmed)
                .append(email, that.email)
                .append(project, that.project)
                .append(applicationId, that.applicationId)
                .append(organisation, that.organisation)
                .append(projectName, that.projectName)
                .append(hash, that.hash)
                .append(status, that.status)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(user)
                .append(name)
                .append(nameConfirmed)
                .append(email)
                .append(project)
                .append(applicationId)
                .append(organisation)
                .append(projectName)
                .append(hash)
                .append(status)
                .toHashCode();
    }
}