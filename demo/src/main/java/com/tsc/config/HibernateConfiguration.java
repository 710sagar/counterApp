package com.tsc.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class HibernateConfiguration {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return new HibernatePropertiesCustomizer() {
            @Override
            public void customize(Map<String, Object> hibernateProperties) {
                // Force Hibernate to use Toronto timezone for all JDBC operations
                hibernateProperties.put(AvailableSettings.JDBC_TIME_ZONE, "America/Toronto");

                // Additional timezone-related settings
                hibernateProperties.put("hibernate.type.preferred_instant_jdbc_type", "TIMESTAMP");
                hibernateProperties.put("hibernate.jdbc.lob.non_contextual_creation", true);

                System.out.println("=== HIBERNATE TIMEZONE CONFIGURATION ===");
                System.out.println("Set JDBC_TIME_ZONE to: America/Toronto");
                System.out.println("==========================================");
            }
        };
    }
}
