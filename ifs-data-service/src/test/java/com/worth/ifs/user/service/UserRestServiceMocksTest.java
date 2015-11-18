package com.worth.ifs.user.service;

import com.worth.ifs.BaseRestServiceMocksTest;
import com.worth.ifs.user.domain.User;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class UserRestServiceMocksTest extends BaseRestServiceMocksTest<UserRestServiceImpl> {

    private static final String usersUrl = "/users";
    private static final String processRolesUrl = "/processroles";

    @Override
    protected UserRestServiceImpl registerRestServiceUnderTest(Consumer<UserRestServiceImpl> registrar) {
        UserRestServiceImpl userRestService = new UserRestServiceImpl();
        userRestService.setUserRestUrl(usersUrl);
        userRestService.setProcessRoleRestUrl(processRolesUrl);
        registrar.accept(userRestService);
        return userRestService;
    }

    @Test
    public void test_findAll() {

        User user1 = new User();
        user1.setPassword("user1");
        User user2 = new User();
        user2.setPassword("user2");

        User[] userList = new User[] { user1, user2 };
        ResponseEntity<User[]> responseEntity = new ResponseEntity<User[]>(userList, HttpStatus.OK);
        when(mockRestTemplate.getForEntity(dataServicesUrl + usersUrl + "/findAll/", User[].class)).thenReturn(responseEntity);

        List<User> users = service.findAll();
        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));
    }
}
