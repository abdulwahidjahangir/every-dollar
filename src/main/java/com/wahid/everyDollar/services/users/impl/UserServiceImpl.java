package com.wahid.everyDollar.services.users.impl;

import com.wahid.everyDollar.errors.CustomException;
import com.wahid.everyDollar.models.users.*;
import com.wahid.everyDollar.repositories.users.PasswordResetTokenRepository;
import com.wahid.everyDollar.repositories.users.RoleRepository;
import com.wahid.everyDollar.repositories.users.UserRepository;
import com.wahid.everyDollar.repositories.users.UserVerifyTokenRepository;
import com.wahid.everyDollar.requests.users.SignUpRequest;
import com.wahid.everyDollar.requests.users.UserUpdateRequest;
import com.wahid.everyDollar.services.users.UserService;
import com.wahid.everyDollar.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserVerifyTokenRepository userVerifyTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void createUser(SignUpRequest signUpRequest) {
        signUpRequest.setEmail(signUpRequest.getEmail().toLowerCase().trim());
        if (this.checkIfUserExists(signUpRequest.getEmail()).isPresent()) {
            throw new CustomException("Email is already registered. Please use a different email address.", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setFirstName(signUpRequest.getFirstName().toLowerCase().trim());
        user.setLastName(signUpRequest.getLastName().toLowerCase().trim());
        user.setEmail(signUpRequest.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Role role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new CustomException("Error while creating user", HttpStatus.INTERNAL_SERVER_ERROR));

        user.setRole(role);

        try {
            user = userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error while creating new user: {}", e.getMessage(), e);
            throw new CustomException("Unable to create user at the moment. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.sendVerifyOrResetPasswordToken(user, "verify");
    }

    @Override
    public void getAnotherVerifyToken(String email) {
        Optional<User> user = checkIfUserExists(email);
        if (user.isEmpty()) {
            throw new CustomException("Invalid email address. Please check and try again.", HttpStatus.BAD_REQUEST);
        }

        if (user.get().isVerified()) {
            throw new CustomException("User is already verified.", HttpStatus.BAD_REQUEST);
        }

        this.sendVerifyOrResetPasswordToken(user.get(), "verify");
    }

    @Override
    public void getPasswordResetToken(String email) {
        Optional<User> user = checkIfUserExists(email);
        if (user.isEmpty()) {
            logger.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        this.sendVerifyOrResetPasswordToken(user.get(), "password");
    }

    @Override
    public void verifyUser(String token) {
        UserVerifyToken userVerifyToken = userVerifyTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("Invalid token. Please check the link and try again.", HttpStatus.BAD_REQUEST));

        if (userVerifyToken.isUsed()) {
            throw new CustomException("This token has already been used.", HttpStatus.BAD_REQUEST);
        }

        if (userVerifyToken.getExpiryDate().isBefore(Instant.now())) {
            throw new CustomException("This token has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        User user = userVerifyToken.getUser();
        user.setVerified(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error while verifying user: {}", e.getMessage(), e);
            throw new CustomException("Failed to verify user. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        userVerifyToken.setUsed(true);
        try {
            userVerifyTokenRepository.save(userVerifyToken);
        } catch (Exception e) {
            logger.error("Error while marking user verification token as used: {}", e.getMessage(), e);
        }
    }

    @Override
    public void resetPassword(String token, String password) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException("Invalid token. Please check the link and try again.", HttpStatus.BAD_REQUEST));

        if (passwordResetToken.isUsed()) {
            throw new CustomException("This token has already been used.", HttpStatus.BAD_REQUEST);
        }

        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new CustomException("This token has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        try {
            userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error while updating user password: {}", e.getMessage(), e);
            throw new CustomException("Failed to update password. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        passwordResetToken.setUsed(true);
        try {
            passwordResetTokenRepository.save(passwordResetToken);
        } catch (Exception e) {
            logger.error("Error while marking password reset token as used: {}", e.getMessage(), e);
        }
    }

    @Override
    public User getUser(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new CustomException("User not found with the given email.", HttpStatus.NOT_FOUND));
    }

    @Override
    public void updatePassword(String email, String password) {
        User user = getUser(email);
        user.setPassword(passwordEncoder.encode(password));

        try {
            userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error while updating user password: {}", e.getMessage(), e);
            throw new CustomException("Failed to update password. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User updateUserData(String email, UserUpdateRequest userUpdateRequest) {
        userUpdateRequest.setEmail(userUpdateRequest.getEmail().toLowerCase().trim());
        User user = getUser(email);

        if (!user.getEmail().equals(userUpdateRequest.getEmail())) {
            Optional<User> alreadyRegistered = checkIfUserExists(userUpdateRequest.getEmail());
            if (alreadyRegistered.isPresent()) {
                throw new CustomException("Email already registered by another user.", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(userUpdateRequest.getEmail());
            user.setVerified(false);
        }

        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());

        try {
            user = userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error while updating user data: {}", e.getMessage(), e);
            throw new CustomException("Failed to update user data. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return user;
    }

    private Optional<User> checkIfUserExists(String email) {
        return userRepository.findByEmail(email);
    }

    @Async
    private void sendVerifyOrResetPasswordToken(User user, String verifyOrPassword) {
        String randomToken = UUID.randomUUID().toString();

        try {
            if ("password".equals(verifyOrPassword)) {
                PasswordResetToken passwordResetToken = new PasswordResetToken(
                        user,
                        randomToken,
                        Instant.now().plus(3, ChronoUnit.HOURS)
                );
                passwordResetTokenRepository.save(passwordResetToken);
                emailService.sendPasswordResetEmail(
                        user.getEmail(),
                        frontendUrl + "/" + verifyOrPassword + "?token=" + randomToken
                );
            } else if ("verify".equals(verifyOrPassword)) {
                UserVerifyToken userVerifyToken = new UserVerifyToken(
                        user,
                        randomToken,
                        Instant.now().plus(3, ChronoUnit.HOURS)
                );
                userVerifyTokenRepository.save(userVerifyToken);
                emailService.sendUserVerifyEmail(
                        user.getEmail(),
                        frontendUrl + "/" + verifyOrPassword + "?token=" + randomToken
                );
            }


        } catch (Exception e) {
            logger.error("Error while sending {} token to user {}: {}", verifyOrPassword, user.getEmail(), e.getMessage(), e);
        }
    }
}
