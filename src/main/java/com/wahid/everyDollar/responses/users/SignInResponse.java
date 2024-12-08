package com.wahid.everyDollar.responses.users;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponse {

    private String token;
    private String name;
}
