package com.wahid.everyDollar.responses.users;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String firstName;
    private String lastName;
    private String email;
    private boolean isBlocked;
    private boolean isVerified;
}
