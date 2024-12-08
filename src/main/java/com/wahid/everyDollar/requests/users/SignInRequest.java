package com.wahid.everyDollar.requests.users;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {

    @NotNull(message = "Email can not be null")
    @NotEmpty(message = "Email can not be empty")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotNull(message = "Password can not be null")
    @NotEmpty(message = "Password can not be empty")
    private String password;
}
