package backend.academy.bot.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

public record AddNotificationTimeRequest(@JsonProperty("notificationTime") LocalTime notificationTime) {}
