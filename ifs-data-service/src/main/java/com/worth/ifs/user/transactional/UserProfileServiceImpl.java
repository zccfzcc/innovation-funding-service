package com.worth.ifs.user.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.repository.UserRepository;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.EntityLookupCallbacks.find;

/**
 * A Service for operations regarding Users' profiles.  This implementation delegates some of this work to an Identity Provider Service
 */
@Service
public class UserProfileServiceImpl extends BaseTransactionalService implements UserProfileService {

    public enum ServiceFailures {
        UNABLE_TO_UPDATE_USER
    }

    @Autowired
    private UserRepository userRepository;

    @Override
    public ServiceResult<UserResource> updateProfile(UserResource userResource) {
        return getUserByEmailAddress(userResource).andOnSuccess(existingUser -> updateUser(existingUser, userResource)).andOnSuccessReturn(UserResource::new);
    }

    private ServiceResult<User> updateUser(User existingUser, UserResource updatedUserResource){

        existingUser.setName(concatenateFullName(updatedUserResource.getFirstName(), updatedUserResource.getLastName()));
        existingUser.setPhoneNumber(updatedUserResource.getPhoneNumber());
        existingUser.setTitle(updatedUserResource.getTitle());
        existingUser.setLastName(updatedUserResource.getLastName());
        existingUser.setFirstName(updatedUserResource.getFirstName());

        return serviceSuccess(userRepository.save(existingUser));
    }


    private ServiceResult<User> getUserByEmailAddress(UserResource userResource) {
        return find(userRepository.findByEmail(userResource.getEmail()), notFoundError(User.class, userResource.getEmail()));
    }

    private String concatenateFullName(String firstName, String lastName) {
        return firstName+" "+lastName;
    }
}
