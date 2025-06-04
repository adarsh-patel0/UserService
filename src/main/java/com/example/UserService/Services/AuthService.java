package com.example.UserService.Services;

import com.example.UserService.Models.Session;
import com.example.UserService.Models.SessionStatus;
import com.example.UserService.Models.User;
import com.example.UserService.Repositries.SessionRepo;
import com.example.UserService.Repositries.UserRepositries;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepositries userRepositries;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SessionRepo sessionRepo;

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

    public Pair<User,MultiValueMap<String,String>> LogIn(String email, String password){

        Optional<User> userOptional = userRepositries.findByEmail(email);

        if(userOptional.isEmpty()){
            return null;
        }
        User user = userOptional.get();
        if(!user.getPassword().equals(password)){
//        if(!bCryptPasswordEncoder.matches(password,user.getPassword())){
            return null;
        }
//        Token Generation
        String message = "{\n" +
                "   \"email\": \"anurag@scaler.com\",\n" +
                "   \"roles\": [\n" +
                "      \"instructor\",\n" +
                "      \"buddy\"\n" +
                "   ],\n" +
                "   \"expirationDate\": \"2ndApril2024\"\n" +
                "}";

        //        byte[] content = message.getBytes(StandardCharsets.UTF_8);

        Map<String,Object> jwtData = new HashMap<>();
        jwtData.put("email",user.getEmail());
        jwtData.put("roles",user.getRoles());
        long nowInMillis = System.currentTimeMillis();
        jwtData.put("expiryTime",new Date(nowInMillis+100000000));
        jwtData.put("createdAt",new Date(nowInMillis));

        MacAlgorithm algorithm = Jwts.SIG.HS256;
        SecretKey secret = algorithm.key().build();
        String token = Jwts.builder()
                .claims(jwtData)
                .signWith(secret)
                .compact();

        Session session = new Session();
        session.setSessionStatus(SessionStatus.Active);
        session.setUser(user);
        session.setToken(token);
        session.setExpireAt(new Date(nowInMillis+10000));
        sessionRepo.save(session);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE,token);
        return new Pair<User,MultiValueMap<String,String>>(user,headers);
    }
}
