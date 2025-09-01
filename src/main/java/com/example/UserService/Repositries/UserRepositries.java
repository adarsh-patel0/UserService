package com.example.UserService.Repositries;

import com.example.UserService.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepositries extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long Id);
}
