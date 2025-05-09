package com.heslin.postopia;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.heslin.postopia.jpa.repository")
@EnableElasticsearchRepositories(basePackages = "com.heslin.postopia.elasticsearch.repository")
public class PostopiaApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
        System.setProperty("CLOUDINARY_URL", dotenv.get("CLOUDINARY_URL"));
        System.setProperty("SILICON_API_KEY", dotenv.get("SILICON_API_KEY"));
        SpringApplication.run(PostopiaApplication.class, args);
    }

}
