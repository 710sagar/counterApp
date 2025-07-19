package com.tsc.config;

import java.time.ZoneId;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class TimezoneConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TimezoneConfiguration.class);
    private static final String TORONTO_TIMEZONE = "America/Toronto";

    @PostConstruct
    public void init() {
        logger.info("=== TIMEZONE CONFIGURATION INITIALIZATION ===");

        // Log current system timezone before any changes
        logger.info("Original JVM Timezone: {}", TimeZone.getDefault().getID());
        logger.info("Original ZoneId.systemDefault(): {}", ZoneId.systemDefault());
        logger.info("Original System Property user.timezone: {}", System.getProperty("user.timezone"));

        // FORCE set the system property first
        System.setProperty("user.timezone", TORONTO_TIMEZONE);

        // Set the JVM default timezone to Toronto
        TimeZone torontoTimeZone = TimeZone.getTimeZone(TORONTO_TIMEZONE);
        TimeZone.setDefault(torontoTimeZone);

        // Clear any cached timezone data
        ZoneId.systemDefault(); // This will refresh the system default

        // Log after setting Toronto timezone
        logger.info("NEW JVM Default Timezone set to: {}", TimeZone.getDefault().getID());
        logger.info("NEW ZoneId.systemDefault(): {}", ZoneId.systemDefault());
        logger.info("NEW System Property user.timezone: {}", System.getProperty("user.timezone"));
        logger.info("Timezone Display Name: {}", torontoTimeZone.getDisplayName());
        logger.info("Is Toronto in DST? {}", torontoTimeZone.inDaylightTime(new java.util.Date()));
        logger.info("Current DST Offset: {} hours", torontoTimeZone.getDSTSavings() / (1000 * 60 * 60));

        // Verify the change worked
        if (TORONTO_TIMEZONE.equals(TimeZone.getDefault().getID())) {
            logger.info("✓ SUCCESS: JVM timezone successfully set to Toronto!");
        } else {
            logger.error("✗ FAILED: JVM timezone was not set correctly!");
            logger.error("Expected: {}, Actual: {}", TORONTO_TIMEZONE, TimeZone.getDefault().getID());
        }

        // Test LocalDateTime.now() behavior
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.ZonedDateTime torontoNow = java.time.ZonedDateTime.now(ZoneId.of(TORONTO_TIMEZONE));
        logger.info("LocalDateTime.now(): {}", now);
        logger.info("ZonedDateTime.now(Toronto): {}", torontoNow.toLocalDateTime());

        if (now.getHour() == torontoNow.getHour() && now.getMinute() == torontoNow.getMinute()) {
            logger.info("✓ SUCCESS: LocalDateTime.now() is using Toronto timezone!");
        } else {
            logger.error("✗ WARNING: LocalDateTime.now() might not be using Toronto timezone!");
        }

        logger.info("===============================================");
    }
}
