package com.example.UserService.Services;

import com.example.UserService.Models.User;
import com.example.UserService.Repositries.UserRepositries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepositries userRepositries;

    public User getUserDetails(Long id){
        User user = userRepositries.findById(id).get();
        return user;
    }

}
