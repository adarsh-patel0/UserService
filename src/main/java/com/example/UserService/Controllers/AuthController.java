package com.example.UserService.Controllers;

import com.example.UserService.Dtos.LoginRequestDto;
import com.example.UserService.Dtos.SignupRequestDto;
import com.example.UserService.Dtos.UserDto;
import com.example.UserService.Models.User;
import com.example.UserService.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/auth/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto signupRequestDto){
        User user = authService.SignUp(signupRequestDto.getEmail(),signupRequestDto.getPassword());
        UserDto userDto = getUserDto(user);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserDto>login(@RequestBody LoginRequestDto loginRequestDto){
        try{
            User user = authService.SignUp(loginRequestDto.getEmail(),loginRequestDto.getPassword());
            UserDto userDto = getUserDto(user);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }
    }

    private UserDto getUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }

}
