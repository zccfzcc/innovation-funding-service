package com.worth.ifs.invite.domain;

import com.worth.ifs.user.domain.Organisation;

import javax.persistence.*;
import java.util.List;

/*
* The InviteOrganisation entity serves the purpose of grouping Invites by Organisation name entered in the application.
* When an actual Organisation exists the InviteOrganisation can link the associated Invites to that Organisation.
* */

@Entity
public class InviteOrganisation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String organisationName;

    @ManyToOne
    @JoinColumn(name = "organisationId", referencedColumnName = "id")
    private Organisation organisation;

    @OneToMany(mappedBy = "inviteOrganisation")
    private List<Invite> invites;

    public InviteOrganisation() {

    }

    public InviteOrganisation(String organisationName, Organisation organisation, List<Invite> invites) {
        this.organisationName = organisationName;
        this.organisation = organisation;
        this.invites = invites;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public List<Invite> getInvites() {
        return invites;
    }

    public void setInvites(List<Invite> invites) {
        this.invites = invites;
    }
}
