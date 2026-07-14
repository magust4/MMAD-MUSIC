package com.MMAD.Service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

// import com.resend.Resend;
// import com.resend.services.emails.model.CreateEmailOptions;

@Service
public class EmailService {

        @Autowired
        private JavaMailSender mailSender;

        private final String FROM_EMAIL = "YOUR_GMAIL@gmail.com";

        /*
         * // -------- RESEND VERSION --------
         * private final Resend resend;
         * 
         * public EmailService(@Value("${resend.api.key}") String apiKey) {
         * this.resend = new Resend(apiKey);
         * }
         */

        public void sendVerificationEmail(String email, String code) {

                try {

                        MimeMessage message = mailSender.createMimeMessage();

                        MimeMessageHelper helper = new MimeMessageHelper(message, true);

                        helper.setFrom(FROM_EMAIL);
                        helper.setTo(email);
                        helper.setSubject("MMAD Music Verification Code");

                        helper.setText("""
                                        <h2>Welcome to MMAD Music!</h2>

                                        <p>Your verification code is:</p>

                                        <h1>%s</h1>

                                        <p>Enter this code to verify your account.</p>
                                        """.formatted(code), true);

                        mailSender.send(message);

                } catch (Exception e) {
                        throw new RuntimeException("Failed to send verification email", e);
                }
        }

        public void sendPasswordResetEmail(String email, String code) {

                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(FROM_EMAIL);
                message.setTo(email);
                message.setSubject("MMAD Music Password Reset");

                message.setText(
                                "Password Reset Request\n\n"
                                                + "Your password reset code is:\n\n"
                                                + code
                                                + "\n\nEnter this code to reset your password.");

                mailSender.send(message);
        }

}