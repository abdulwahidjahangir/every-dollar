package com.wahid.everyDollar.controllers.users;

import com.wahid.everyDollar.models.users.User;
import com.wahid.everyDollar.requests.users.*;
import com.wahid.everyDollar.responses.users.SignInResponse;
import com.wahid.everyDollar.responses.users.UserResponse;
import com.wahid.everyDollar.security.jwt.JwtUtils;
import com.wahid.everyDollar.security.service.UserDetailsImpl;
import com.wahid.everyDollar.services.users.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/public/sign-in")
    public ResponseEntity<?> signIn(
            @Valid @RequestBody SignInRequest signInRequest
    ) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword()));
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateJwtToken(userDetails);

        SignInResponse signin = new SignInResponse(jwtToken, userDetails.getFirstName() + " " + userDetails.getLastName());

        return ResponseEntity.ok(signin);
    }

    @PostMapping("/public/sign-up")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        userService.createUser(signUpRequest);

        Map<String, Object> map = new HashMap<>();
        map.put("token", "User registered successfully! We have sent a verification link to your email. Please verify to continue.");

        return ResponseEntity.ok(map);
    }

    @PostMapping("/public/verify")
    public ResponseEntity<?> verifyUser(
            @RequestParam @NotNull(message = "Verify token can not be null") String token
    ) {
        userService.verifyUser(token);
        return ResponseEntity.ok("User successfully verified");
    }

    @PostMapping("/public/get-verify")
    public ResponseEntity<?> getUserVerify(
            @Valid @RequestBody PasswordRestOrVerifyRequest passwordRestOrVerifyRequest
    ) {
        userService.getAnotherVerifyToken(passwordRestOrVerifyRequest.getEmail());
        return ResponseEntity.ok("If the email address matches an account in our system, we will send you a verification email");
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> getPasswordResetToken(
            @Valid @RequestBody PasswordRestOrVerifyRequest passwordRestOrVerifyRequest
    ) {
        userService.getPasswordResetToken(passwordRestOrVerifyRequest.getEmail());
        return ResponseEntity.ok("If the email address matches an account in our system, we will send you a password rest email");
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestParam @NotNull(message = "Password reset token can not be null") String token,
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest
    ) {
        userService.resetPassword(token, updatePasswordRequest.getPassword());
        return ResponseEntity.ok("Your password has been successfully updated");
    }

    @PostMapping("/user/update-password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest
    ) {
        userService.updatePassword(userDetails.getEmail(), updatePasswordRequest.getPassword());
        return ResponseEntity.ok("Your password has been successfully updated");
    }

    @GetMapping("/user/get-me")
    public ResponseEntity<?> getMe(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User user = userService.getUser(userDetails.getEmail());
        UserResponse userResponse = convertUserToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/user/update-me")
    public ResponseEntity<?> updateUserData(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        User user = userService.updateUserData(userDetails.getEmail(), userUpdateRequest);
        UserResponse userResponse = convertUserToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    private UserResponse convertUserToUserResponse(User user) {
        return new UserResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isBlocked(),
                user.isVerified()
        );
    }
}
