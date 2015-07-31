package com.worth.ifs.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;

    @OneToMany(mappedBy = "role")
    private List<UserApplicationRole> userApplicationRoles = new ArrayList<UserApplicationRole>();

    public Role() {
    }

    protected boolean canEqual(Object other) {
        return other instanceof Role;
    }


    public List<UserApplicationRole> getUserApplicationRoles() {
        return userApplicationRoles;
    }

    public void setUserApplicationRoles(List<UserApplicationRole> userApplicationRoles) {
        this.userApplicationRoles = userApplicationRoles;
    }

    public long getId() {
        return id;
    }
}
