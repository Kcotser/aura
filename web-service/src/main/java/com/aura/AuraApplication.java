package com.aura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Entry point for the Aura backend application.
 *
 * <p>Annotated with {@link Modulithic} to enable Spring Modulith's module
 * verification and event publication infrastructure.
 */
@SpringBootApplication
@Modulithic
public class AuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuraApplication.class, args);
    }
}
