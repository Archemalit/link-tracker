package backend.academy.scrapper.dto.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answer {
    @JsonProperty("creation_date")
    private long creationDate;

    @JsonProperty("body")
    private String body;

    @JsonProperty("owner")
    private Owner owner;

    public LocalDateTime getLocalDateTimeCreationDate() {
        return Instant.ofEpochSecond(creationDate).atOffset(ZoneOffset.UTC).toLocalDateTime();
    }

    public String getShortBody() {
        return body.length() > 200 ? body.substring(0, 200) + "..." : body;
    }

    public String getOwnerDisplayName() {
        return owner.displayName();
    }

    @Override
    public String toString() {
        return "Ответ: \n" + "Дата создания: "
                + getLocalDateTimeCreationDate() + "\nТело: '"
                + getShortBody() + '\'' + "\nАвтор: "
                + getOwnerDisplayName();
    }
}
