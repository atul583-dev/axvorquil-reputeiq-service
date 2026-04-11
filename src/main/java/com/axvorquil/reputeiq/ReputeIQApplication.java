package com.axvorquil.reputeiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ReputeIQApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReputeIQApplication.class, args);
    }
}
