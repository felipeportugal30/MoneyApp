package com.moneyapp.v1.factory;

import org.springframework.stereotype.Component;

import com.moneyapp.v1.enums.Role;
import com.moneyapp.v1.model.User;

@Component
public class UserFactory {
    
    public User create(String name, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        return user;
    }
}
