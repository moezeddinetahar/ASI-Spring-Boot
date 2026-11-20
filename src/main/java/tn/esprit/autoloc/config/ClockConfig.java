package tn.esprit.autoloc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Horloge de l'application (correction java:S8688).
 * Le fuseau horaire est explicite et l'horloge peut être remplacée dans les tests.
 */
@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
