package com.example.UserService.Controllers;

import com.example.UserService.Dtos.LoginRequestDto;
import com.example.UserService.Dtos.SignupRequestDto;
import com.example.UserService.Dtos.UserDto;
import com.example.UserService.Dtos.ValidateRequestDto;
import com.example.UserService.Models.User;
import com.example.UserService.Services.AuthService;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto signupRequestDto){
        User user = authService.SignUp(signupRequestDto.getEmail(),signupRequestDto.getPassword());
        UserDto userDto = getUserDto(user);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto){
        try {
            Pair<User, MultiValueMap<String,String>> user = authService.LogIn(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            UserDto userDto = getUserDto(user.a);
            return new ResponseEntity<>(userDto,user.b, HttpStatus.OK);
        }catch (Exception ex) {
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody ValidateRequestDto validateRequestDto) {
        Boolean isValid = authService.validateToken(validateRequestDto.getToken(),validateRequestDto.getUserId());
        return new ResponseEntity<>(isValid,HttpStatus.OK);
    }

    private UserDto getUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }

}
