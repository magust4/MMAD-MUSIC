package com.MMAD.Service.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

@Service
public class EmailService {

        private final Resend resend;
        private final String fromEmail;

        public EmailService(
                        @Value("${resend.api-key}") String apiKey,
                        @Value("${resend.from-email}") String fromEmail) {

                this.resend = new Resend(apiKey);
                this.fromEmail = fromEmail;
        }

        public void sendVerificationEmail(
                        String email,
                        String code) {

                try {

                        CreateEmailOptions params = CreateEmailOptions.builder()
                                        .from("MMAD Music <" + fromEmail + ">")
                                        .to(email)
                                        .subject("MMAD Music Verification Code")
                                        .html("""
                                                        <h2>Welcome to MMAD Music!</h2>

                                                        <p>Your verification code is:</p>

                                                        <h1>%s</h1>

                                                        <p>
                                                            Enter this code to verify your account.
                                                        </p>

                                                        <p>
                                                            This code expires in 15 minutes.
                                                        </p>
                                                        """.formatted(code))
                                        .build();

                        resend.emails().send(params);

                } catch (Exception e) {

                        e.printStackTrace();

                        throw new RuntimeException(
                                        "Failed to send verification email: " + e.getMessage(),
                                        e);
                }
        }

        public void sendPasswordResetEmail(
                        String email,
                        String code) {

                try {

                        CreateEmailOptions params = CreateEmailOptions.builder()
                                        .from("MMAD Music <" + fromEmail + ">")
                                        .to(email)
                                        .subject("MMAD Music Password Reset")
                                        .html("""
                                                        <h2>MMAD Music Password Reset</h2>

                                                        <p>Your password reset code is:</p>

                                                        <h1>%s</h1>

                                                        <p>
                                                            Enter this code to reset your password.
                                                        </p>

                                                        <p>
                                                            This code expires in 15 minutes.
                                                        </p>
                                                        """.formatted(code))
                                        .build();

                        resend.emails().send(params);

                } catch (Exception e) {

                        throw new RuntimeException(
                                        "Failed to send password reset email",
                                        e);
                }
        }
}