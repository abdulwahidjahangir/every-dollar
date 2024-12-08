package com.wahid.everyDollar.services.users;

import com.wahid.everyDollar.models.users.User;
import com.wahid.everyDollar.requests.users.SignUpRequest;
import com.wahid.everyDollar.requests.users.UserUpdateRequest;

public interface UserService {
    void createUser(SignUpRequest signUpRequest);

    void getAnotherVerifyToken(String email);

    void getPasswordResetToken(String email);

    void verifyUser(String token);

    void resetPassword(String token, String password);

    User getUser(String email);

    void updatePassword(String email, String password);

    User updateUserData(String email, UserUpdateRequest userUpdateRequest);
}
