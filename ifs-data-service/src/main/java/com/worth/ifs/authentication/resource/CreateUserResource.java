package com.worth.ifs.authentication.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a request to an Identity Provider to create a new User record
 */
public class CreateUserResource {

    private String emailAddress;
    private String plainTextPassword;

    /**
     * For JSON marshalling
     */
    public CreateUserResource() {
    }

    public CreateUserResource(String emailAddress, String plainTextPassword) {
        this.emailAddress = emailAddress;
        this.plainTextPassword = plainTextPassword;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPlainTextPassword() {
        return plainTextPassword;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setPlainTextPassword(String plainTextPassword) {
        this.plainTextPassword = plainTextPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CreateUserResource that = (CreateUserResource) o;

        return new EqualsBuilder()
                .append(emailAddress, that.emailAddress)
                .append(plainTextPassword, that.plainTextPassword)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(emailAddress)
                .append(plainTextPassword)
                .toHashCode();
    }
}
