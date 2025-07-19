package com.tsc.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TimezoneHelper {

    private static final Logger logger = LoggerFactory.getLogger(TimezoneHelper.class);
    private static final ZoneId TORONTO_ZONE = ZoneId.of("America/Toronto");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * Get current time in Toronto timezone
     */
    public LocalDateTime getCurrentTorontoTime() {
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        logger.debug("Current Toronto time: {}", torontoNow);
        return torontoNow.toLocalDateTime();
    }

    /**
     * Get start of day in Toronto timezone (00:00:00) This ensures we get
     * Toronto's midnight, not the server's midnight
     */
    public LocalDateTime getTorontoStartOfDay() {
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        ZonedDateTime torontoStartOfDay = torontoNow.toLocalDate().atStartOfDay(TORONTO_ZONE);
        LocalDateTime startOfDay = torontoStartOfDay.toLocalDateTime();
        logger.debug("Toronto start of day: {}", startOfDay);
        return startOfDay;
    }

    /**
     * Get start of day for a specific date in Toronto timezone
     */
    public LocalDateTime getTorontoStartOfDay(LocalDate date) {
        ZonedDateTime torontoStartOfDay = date.atStartOfDay(TORONTO_ZONE);
        LocalDateTime startOfDay = torontoStartOfDay.toLocalDateTime();
        logger.debug("Toronto start of day for {}: {}", date, startOfDay);
        return startOfDay;
    }

    /**
     * Get end of day in Toronto timezone (start of next day)
     */
    public LocalDateTime getTorontoEndOfDay() {
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        ZonedDateTime torontoEndOfDay = torontoNow.toLocalDate().plusDays(1).atStartOfDay(TORONTO_ZONE);
        LocalDateTime endOfDay = torontoEndOfDay.toLocalDateTime();
        logger.debug("Toronto end of day: {}", endOfDay);
        return endOfDay;
    }

    /**
     * Get end of day for a specific date in Toronto timezone
     */
    public LocalDateTime getTorontoEndOfDay(LocalDate date) {
        ZonedDateTime torontoEndOfDay = date.plusDays(1).atStartOfDay(TORONTO_ZONE);
        LocalDateTime endOfDay = torontoEndOfDay.toLocalDateTime();
        logger.debug("Toronto end of day for {}: {}", date, endOfDay);
        return endOfDay;
    }

    /**
     * Convert any LocalDateTime to Toronto timezone This method assumes the
     * input LocalDateTime is in the system's default timezone
     */
    public LocalDateTime convertToTorontoTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return getCurrentTorontoTime();
        }

        // Get system timezone
        ZoneId systemZone = ZoneId.systemDefault();

        // If system is already Toronto, return as-is
        if (TORONTO_ZONE.equals(systemZone)) {
            logger.debug("System already in Toronto timezone, returning: {}", dateTime);
            return dateTime;
        }

        // Convert from system timezone to Toronto timezone
        ZonedDateTime systemZoned = dateTime.atZone(systemZone);
        ZonedDateTime torontoZoned = systemZoned.withZoneSameInstant(TORONTO_ZONE);

        logger.debug("Converted {} ({}) to {} (Toronto)", dateTime, systemZone, torontoZoned.toLocalDateTime());
        return torontoZoned.toLocalDateTime();
    }

    /**
     * SIMPLIFIED: Get current timestamp for database storage Since we've set
     * the JVM timezone to Toronto, LocalDateTime.now() will be Toronto time
     */
    public LocalDateTime getCurrentTimestampForDatabase() {
        return LocalDateTime.now(); // This will be Toronto time due to JVM timezone setting
    }

    /**
     * Convert LocalDateTime from UTC to Toronto timezone
     */
    public LocalDateTime convertFromUtcToToronto(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return getCurrentTorontoTime();
        }

        ZonedDateTime utcZoned = utcDateTime.atZone(UTC_ZONE);
        ZonedDateTime torontoZoned = utcZoned.withZoneSameInstant(TORONTO_ZONE);

        logger.debug("Converted UTC {} to Toronto {}", utcDateTime, torontoZoned.toLocalDateTime());
        return torontoZoned.toLocalDateTime();
    }

    /**
     * Convert LocalDateTime from Toronto to UTC timezone
     */
    public LocalDateTime convertFromTorontoToUtc(LocalDateTime torontoDateTime) {
        if (torontoDateTime == null) {
            return ZonedDateTime.now(UTC_ZONE).toLocalDateTime();
        }

        ZonedDateTime torontoZoned = torontoDateTime.atZone(TORONTO_ZONE);
        ZonedDateTime utcZoned = torontoZoned.withZoneSameInstant(UTC_ZONE);

        logger.debug("Converted Toronto {} to UTC {}", torontoDateTime, utcZoned.toLocalDateTime());
        return utcZoned.toLocalDateTime();
    }

    /**
     * Ensure a LocalDateTime is in Toronto timezone If it's already in Toronto
     * context, return as-is If it's from another timezone context, convert it
     */
    public LocalDateTime ensureTorontoTime(LocalDateTime dateTime, ZoneId sourceZone) {
        if (dateTime == null) {
            return getCurrentTorontoTime();
        }

        if (TORONTO_ZONE.equals(sourceZone)) {
            return dateTime; // Already in Toronto timezone
        }

        ZonedDateTime sourceZoned = dateTime.atZone(sourceZone);
        ZonedDateTime torontoZoned = sourceZoned.withZoneSameInstant(TORONTO_ZONE);

        logger.debug("Ensured Toronto time: {} ({}) -> {} (Toronto)",
                dateTime, sourceZone, torontoZoned.toLocalDateTime());
        return torontoZoned.toLocalDateTime();
    }

    /**
     * Get a LocalDateTime for database storage (always in Toronto timezone)
     */
    public LocalDateTime getTimestampForDatabase() {
        return getCurrentTorontoTime();
    }

    /**
     * Get a LocalDateTime for database storage from a specific instant
     */
    public LocalDateTime getTimestampForDatabase(Instant instant) {
        ZonedDateTime torontoZoned = instant.atZone(TORONTO_ZONE);
        return torontoZoned.toLocalDateTime();
    }

    /**
     * Format Toronto time for display
     */
    public String formatTorontoTime(LocalDateTime torontoDateTime) {
        if (torontoDateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return torontoDateTime.format(formatter);
    }

    /**
     * Format Toronto time for display with timezone indicator
     */
    public String formatTorontoTimeWithZone(LocalDateTime torontoDateTime) {
        if (torontoDateTime == null) {
            return "";
        }
        ZonedDateTime zonedDateTime = torontoDateTime.atZone(TORONTO_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return zonedDateTime.format(formatter);
    }

    /**
     * Get the hour offset between server timezone and Toronto Useful for
     * debugging
     */
    public int getServerToTorontoHourOffset() {
        ZonedDateTime serverNow = ZonedDateTime.now();
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);

        long hoursDiff = Duration.between(serverNow.toLocalDateTime(), torontoNow.toLocalDateTime()).toHours();
        logger.debug("Server timezone: {}, Toronto timezone: {}, Hour difference: {}",
                ZoneId.systemDefault(), TORONTO_ZONE, hoursDiff);
        return (int) hoursDiff;
    }

    /**
     * Check if we're currently in Daylight Saving Time in Toronto
     */
    public boolean isTorontoDaylightSavingTime() {
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        return torontoNow.getZone().getRules().isDaylightSavings(torontoNow.toInstant());
    }

    /**
     * Get current timezone offset from UTC (accounting for DST)
     */
    public ZoneOffset getTorontoOffset() {
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        return torontoNow.getOffset();
    }

    /**
     * For debugging - get comprehensive timezone info
     */
    public String getTimezoneDebugInfo() {
        StringBuilder info = new StringBuilder();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        ZonedDateTime utcNow = ZonedDateTime.now(UTC_ZONE);

        info.append("=== TIMEZONE DEBUG INFO ===\n");
        info.append("System Timezone: ").append(ZoneId.systemDefault()).append("\n");
        info.append("System Time: ").append(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))).append("\n");
        info.append("Toronto Time: ").append(torontoNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))).append("\n");
        info.append("UTC Time: ").append(utcNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))).append("\n");
        info.append("Toronto Offset: ").append(getTorontoOffset()).append("\n");
        info.append("DST Active: ").append(isTorontoDaylightSavingTime()).append("\n");
        info.append("Hour Offset: ").append(getServerToTorontoHourOffset()).append("\n");
        info.append("Toronto Start of Day: ").append(getTorontoStartOfDay()).append("\n");
        info.append("Toronto End of Day: ").append(getTorontoEndOfDay()).append("\n");
        info.append("============================\n");

        return info.toString();
    }

    /**
     * Parse a date string and convert to Toronto timezone
     */
    public LocalDateTime parseToTorontoTime(String dateString, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime parsed = LocalDateTime.parse(dateString, formatter);
            return convertToTorontoTime(parsed);
        } catch (Exception e) {
            logger.error("Failed to parse date string '{}' with pattern '{}': {}", dateString, pattern, e.getMessage());
            return getCurrentTorontoTime();
        }
    }

    /**
     * Get Toronto timezone ID
     */
    public ZoneId getTorontoZoneId() {
        return TORONTO_ZONE;
    }

    /**
     * Get UTC timezone ID
     */
    public ZoneId getUtcZoneId() {
        return UTC_ZONE;
    }
}
