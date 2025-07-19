package com.tsc.util;

import java.time.*;
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
     * Get start of day in Toronto timezone
     * This ensures we get Toronto's midnight, not the server's midnight
     */
    public LocalDateTime getTorontoStartOfDay() {
        ZonedDateTime torontoNow = ZonedDateTime.now(TORONTO_ZONE);
        ZonedDateTime torontoStartOfDay = torontoNow.toLocalDate().atStartOfDay(TORONTO_ZONE);
        LocalDateTime startOfDay = torontoStartOfDay.toLocalDateTime();
        logger.debug("Toronto start of day: {}", startOfDay);
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
     * Convert any LocalDateTime to Toronto timezone
     * Assumes the input LocalDateTime is in the system's default timezone
     */
    public LocalDateTime convertToTorontoTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return getCurrentTorontoTime();
        }

        // Get system timezone
        ZoneId systemZone = ZoneId.systemDefault();

        // Convert from system timezone to Toronto timezone
        ZonedDateTime systemZoned = dateTime.atZone(systemZone);
        ZonedDateTime torontoZoned = systemZoned.withZoneSameInstant(TORONTO_ZONE);

        logger.debug("Converted {} ({}) to {} (Toronto)", dateTime, systemZone, torontoZoned.toLocalDateTime());
        return torontoZoned.toLocalDateTime();
    }

    /**
     * Get the hour offset between server timezone and Toronto
     * Useful for debugging
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
     * For debugging - get timezone info
     */
    public String getTimezoneDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("System Timezone: ").append(ZoneId.systemDefault()).append("\n");
        info.append("System Time: ").append(LocalDateTime.now()).append("\n");
        info.append("Toronto Time: ").append(getCurrentTorontoTime()).append("\n");
        info.append("UTC Time: ").append(ZonedDateTime.now(UTC_ZONE).toLocalDateTime()).append("\n");
        info.append("Hour Offset: ").append(getServerToTorontoHourOffset()).append("\n");
        info.append("Toronto Start of Day: ").append(getTorontoStartOfDay()).append("\n");
        info.append("Toronto End of Day: ").append(getTorontoEndOfDay()).append("\n");
        return info.toString();
    }
}