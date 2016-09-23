package com.worth.ifs;

import com.worth.ifs.commons.security.UserAuthentication;
import com.worth.ifs.security.SecurityRuleUtil;
import com.worth.ifs.user.mapper.UserMapper;
import com.worth.ifs.user.repository.UserRepository;
import com.worth.ifs.user.resource.UserResource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for integration tests that need to provide authenticate users.
 */
public abstract class BaseAuthenticationAwareIntegrationTest extends BaseIntegrationTest {

    public static final int USER_COUNT  = 16;
    public static final List<String> ALL_USERS_EMAIL = Arrays.asList("steve.smith@empire.com", "jessica.doe@ludlow.co.uk", "paul.plum@gmail.com", "competitions@innovateuk.gov.uk", "finance@innovateuk.gov.uk", "pete.tom@egg.com", "felix.wilson@gmail.com", "ewan+1@hiveit.co.uk", "ifs_web_user@innovateuk.org", "compadmin@innovateuk.test", "comp_exec1@innovateuk.test", "comp_exec2@innovateuk.test", "comp_technologist1@innovateuk.test", "comp_technologist2@innovateuk.test");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    /**
     * Set a user on the Spring Security ThreadLocals
     *
     * @param user
     */
    protected void setLoggedInUser(UserResource user) {
        SecurityContextHolder.getContext().setAuthentication(new UserAuthentication(user));
    }

    /**
     * Get the user on the Spring Security ThreadLocals
     */
    protected UserResource getLoggedInUser() {
        return ((UserAuthentication) SecurityContextHolder.getContext().getAuthentication()).getDetails();
    }

    protected UserResource getSteveSmith() {
        return getByEmail("steve.smith@empire.com");
    }

    protected UserResource getPeteTom() {
        return getByEmail("pete.tom@egg.com");
    }

    protected UserResource getCompAdmin() {
        return getByEmail("compadmin@innovateuk.test");
    }

    protected UserResource getPaulPlum() {
        return getByEmail("paul.plum@gmail.com");
    }

    protected UserResource getFelixWilson() {
        return getByEmail("felix.wilson@gmail.com");
    }

    protected UserResource getAnonUser() {
        return SecurityRuleUtil.getAnonymous();
    }

    protected UserResource getSystemRegistrationUser() {
        return getByEmail("ifs_web_user@innovateuk.org");
    }

    protected UserResource getByEmail(String email) {
        return userMapper.mapToResource(userRepository.findByEmail(email).get());
    }

    protected void loginSteveSmith() {
        setLoggedInUser(getSteveSmith());
    }

    protected void loginCompAdmin() {
        setLoggedInUser(getCompAdmin());
    }

    protected void loginSystemRegistrationUser() {
        setLoggedInUser(getSystemRegistrationUser());
    }

    protected void loginPeteTom() {
        setLoggedInUser(getPeteTom());
    }

    protected void loginPaulPlum() {
        setLoggedInUser(getPaulPlum());
    }

    protected void loginFelixWilson() {
        setLoggedInUser(getFelixWilson());
    }
}