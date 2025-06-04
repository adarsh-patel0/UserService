package com.example.UserService.Services;

import com.example.UserService.Models.User;
import com.example.UserService.Repositries.UserRepositries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepositries userRepositries;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public User SignUp(String email, String password){

        Optional<User> userOptional = userRepositries.findByEmail(email);

        if(userOptional.isEmpty()){
            User user = new User();
            user.setEmail(email);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            User saveUser = userRepositries.save(user);
            return saveUser;
        }
        return userOptional.get();
    }

    public User LogIn(String email, String password){

        Optional<User> userOptional = userRepositries.findByEmail(email);

        if(userOptional.isEmpty()){
            return null;
        }
        User user = userOptional.get();
        if(!user.getPassword().equals(password)){
//        if(!bCryptPasswordEncoder.matches(password,user.getPassword())){
            return null;
        }
        return user;
    }
}
