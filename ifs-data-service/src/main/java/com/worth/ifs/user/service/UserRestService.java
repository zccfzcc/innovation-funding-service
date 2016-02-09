package com.worth.ifs.user.service;

import com.worth.ifs.commons.resource.ResourceEnvelope;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * Interface for CRUD operations on {@link User} related data.
 */
public interface UserRestService {
    RestResult<User> retrieveUserByToken(String token);
    RestResult<User> retrieveUserByEmailAndPassword(String email, String password);
    RestResult<User> retrieveUserById(Long id);

    RestResult<List<User>> findAll();
    RestResult<ProcessRole> findProcessRole(Long userId, Long applicationId);
    RestResult<List<ProcessRole>> findProcessRole(Long applicationId);
    RestResult<List<User>> findAssignableUsers(Long applicationId);
    RestResult<List<UserResource>> findUserByEmail(String email);
    ListenableFuture<List<ProcessRole>> findAssignableProcessRoles(Long applicationId);
    RestResult<List<User>> findRelatedUsers(Long applicationId);
    ListenableFuture<ProcessRole> findProcessRoleById(Long processRoleId);
    RestResult<ResourceEnvelope<UserResource>> createLeadApplicantForOrganisation(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId);
    RestResult<ResourceEnvelope<UserResource>> updateDetails(String email, String firstName, String lastName, String title, String phoneNumber);

}
