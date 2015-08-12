package com.worth.ifs.controller;

import com.worth.ifs.domain.User;
import com.worth.ifs.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController exposes User data through a REST API.
 */

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserRepository repository;

    private final Log log = LogFactory.getLog(getClass());

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleException(Exception e) {
        e.printStackTrace();
        return "return error object instead";
    }

    @RequestMapping("/token/{token}")
     public User getUserByToken(@PathVariable("token") final String token) {
        List<User> users = repository.findByToken(token);
        if (users.size() > 0){
            return users.get(0);
        }else{
            return null;
        }
    }

    @RequestMapping("/email/{email}/password/{password}")
    public User getUserByEmailandPassword(@PathVariable("email") final String email, @PathVariable("password") final String password) {
        List<User> users = repository.findByEmail(email);

        if (users.size() > 0 ){
            User user = users.get(0);
            if(user.passwordEquals(password)){
                return user;
            }else{
                return null;
            }
        }else{
            log.warn("Return null");
            return null;
        }
    }

    @RequestMapping("/id/{id}")
    public User getUserById(@PathVariable("id") final Long id) {
        User user = repository.findById(id).get(0);
        user.setPassword("test");
        repository.save(user);
        return user;
    }

    @RequestMapping("/name/{name}")
    public List<User> getUserByName(@PathVariable("name") final String name) {
        List<User> users = repository.findByName(name);
        return users;
    }
    @RequestMapping("/findAll/")
    public List<User> findAll() {
        List<User> users = repository.findAll();
        return users;
    }
}
