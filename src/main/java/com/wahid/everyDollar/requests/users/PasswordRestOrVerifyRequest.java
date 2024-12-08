package com.wahid.everyDollar.requests.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRestOrVerifyRequest {

    @NotNull(message = "Email can not be null")
    @Email(message = "Please provide a valid email address")
    private String email;
}
