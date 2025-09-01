package com.example.UserService.Services;

import com.example.UserService.Clients.KafkaProducerClient;
import com.example.UserService.Dtos.SendEmailMessageDto;
import com.example.UserService.Models.Session;
import com.example.UserService.Models.SessionStatus;
import com.example.UserService.Models.User;
import com.example.UserService.Repositries.SessionRepo;
import com.example.UserService.Repositries.UserRepositries;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.crypto.SecretKey;
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

    @Autowired
    private SecretKey secretKey;

    @Autowired
    private KafkaProducerClient kafkaProducerClient;

    @Autowired
    private ObjectMapper objectMapper;

    public User SignUp(String email, String password) {

        Optional<User> userOptional = userRepositries.findByEmail(email);

        if (userOptional.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            User saveUser = userRepositries.save(user);
            return saveUser;
        }

        //Put message in Queue
        try {
            SendEmailMessageDto sendEmailMessageDto = new SendEmailMessageDto();
            sendEmailMessageDto.setTo(email);
            sendEmailMessageDto.setFrom("admin@scaler.com");
            sendEmailMessageDto.setSubject("Welcome to Scaler");
            sendEmailMessageDto.setBody("Have a pleasant stay");
            kafkaProducerClient.sendMessage("sendEmail", objectMapper.writeValueAsString(sendEmailMessageDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userOptional.get();
    }

    public Pair<User,MultiValueMap<String,String>> LogIn(String email, String password) {

        Optional<User> userOptional = userRepositries.findByEmail(email);

        if (userOptional.isEmpty()) {
            return null;
        }
        User user = userOptional.get();

//        if(!user.getPassword().equals(password)){
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())){
            return null;
        }

//                String message = "{\n" +
//                "   \"email\": \"adarsh@scaler.com\",\n" +
//                "   \"roles\": [\n" +
//                "      \"instructor\",\n" +
//                "      \"buddy\"\n" +
//                "   ],\n" +
//                "   \"expirationDate\": \"5thjuly2025\"\n" +
//                "}";
//
//        byte[] content = message.getBytes(StandardCharsets.UTF_8);

        Map<String,Object> jwtData = new HashMap<>();
        jwtData.put("email",user.getEmail());
        jwtData.put("roles",user.getRoles());
        long nowInMillis = System.currentTimeMillis();
        jwtData.put("expiryTime",new Date(nowInMillis+100000));
        jwtData.put("createdAt",new Date(nowInMillis));


        String token = Jwts.builder().claims(jwtData).signWith(secretKey).compact();


        Session session = new Session();
        session.setSessionStatus(SessionStatus.Active);
        session.setUser(user);
        session.setToken(token);
        session.setExpiringAt(new Date(nowInMillis+100000));
        sessionRepo.save(session);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE,token);
        return new Pair<User,MultiValueMap<String,String>>(user,headers);
    }

     public Boolean validateToken(String token, Long id) {
            Optional<Session> optionalSession = sessionRepo.findByTokenAndUser_Id(token, id);

            if (optionalSession.isEmpty()) {
                System.out.println("No Token or User found");
                return false;
            }

            Session session = optionalSession.get();
            String storedToken = session.getToken();

            JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
            Claims claims = jwtParser.parseSignedClaims(storedToken).getPayload();
            System.out.println(claims);

            long nowInMillis = System.currentTimeMillis();
            long tokenExpiry = (Long) claims.get("expiryTime");

            if (nowInMillis > tokenExpiry) {
                System.out.println(nowInMillis);
                System.out.println(tokenExpiry);
                System.out.println("Token has expired");
                return false;
            }

            Optional<User> optionalUser = userRepositries.findById(id);
            if (optionalUser.isEmpty()) {
                return false;
            }

            String email = optionalUser.get().getEmail();

            if (!email.equals(claims.get("email"))) {
                System.out.println(email);
                System.out.println(claims.get("email"));
                System.out.println("User doesn't match");
                return false;
            }

            return true;
        }
}
