package backend.academy.scrapper.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

public record AddNotificationTimeRequest(@JsonProperty("notificationTime") LocalTime notificationTime) {}
