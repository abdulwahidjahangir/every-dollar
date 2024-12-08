package com.wahid.everyDollar.requests.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @NotNull(message = "First name can not be null")
    @NotEmpty(message = "First name can not be empty")
    @Size(min = 2, max = 100)
    private String firstName;


    @NotNull(message = "Last name can not be null")
    @NotEmpty(message = "Last name can not be empty")
    @Size(min = 2, max = 100)
    private String lastName;


    @NotNull(message = "Email can not be null")
    @NotEmpty(message = "Email can not be empty")
    @Email(message = "Please enter a valid email")
    @Size(min = 2, max = 100)
    private String email;
}
