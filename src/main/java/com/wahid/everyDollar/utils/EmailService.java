package com.wahid.everyDollar.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {


    @Autowired
    private JavaMailSender javaMailSender;

    public void sendUserVerifyEmail(String to, String verifyUrl) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject("Every-Dollar: Verification Email");

            String emailBody = String.format(
                    "Dear User,\n\n" +
                            "Thank you for registering. To complete your registration, please click on the link below to verify your email address:\n\n" +
                            "%s\n\n" +
                            "If you did not request this verification, please ignore this email.\n\n" +
                            "Best regards\n"
                    , verifyUrl);

            simpleMailMessage.setText(emailBody);

            javaMailSender.send(simpleMailMessage);

            System.out.println("Email successfully send to " + to);
        } catch (Exception e) {
            System.out.println("Failed to send to: " + to);
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(String to, String resetUrl) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject("Every-Dollar: Password Reset");

            String emailBody = String.format(
                    "Dear User,\n\n" +
                            "To reset your password, please click on the link below:\n\n" +
                            "%s\n\n" +
                            "If you did not request this reset, please ignore this email.\n\n" +
                            "Best regards"
                    , resetUrl);

            simpleMailMessage.setText(emailBody);

            javaMailSender.send(simpleMailMessage);

            System.out.println("Password reset email successfully sent to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + to);
            e.printStackTrace();
        }
    }
}
