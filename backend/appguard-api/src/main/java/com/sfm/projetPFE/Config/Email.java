package com.sfm.projetPFE.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Email {
    @Bean
    public EmailService emailService() {
        return new EmailService();
    }
}
