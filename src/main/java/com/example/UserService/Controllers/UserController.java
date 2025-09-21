package com.example.UserService.Controllers;

import com.example.UserService.Dtos.UserDto;
import com.example.UserService.Models.User;
import com.example.UserService.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserDetails(@PathVariable("id") Long id){
        User user = userService.getUserDetails(id);
        return getUserDto(user);
    }

    private UserDto getUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setRoles(user.getRoles());
        userDto.setEmail(user.getEmail());
        return userDto;
    }


}
