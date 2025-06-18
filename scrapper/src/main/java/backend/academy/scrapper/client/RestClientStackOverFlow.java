package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.response.stackoverflow.Answer;
import java.time.LocalDateTime;
import java.util.List;

public interface RestClientStackOverFlow {
    Long getLastAnswerDate(String url);

    List<Answer> getNewAnswers(String questionId, LocalDateTime lastAnswerDate);
}
