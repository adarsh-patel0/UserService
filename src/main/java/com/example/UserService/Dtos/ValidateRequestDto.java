package com.example.UserService.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateRequestDto {

    String token;

    Long userId;
}
