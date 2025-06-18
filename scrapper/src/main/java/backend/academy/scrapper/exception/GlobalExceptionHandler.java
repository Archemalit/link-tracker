package backend.academy.scrapper.exception;

import backend.academy.scrapper.controller.ChatController;
import backend.academy.scrapper.controller.LinkController;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.github.GitHubApiCallException;
import backend.academy.scrapper.exception.github.GitHubRepositoryNotFound;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowApiCallException;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowNotFound;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = {LinkController.class, ChatController.class})
public class GlobalExceptionHandler {
    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkAlreadyTracked(LinkAlreadyTrackedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Ссылка уже отслеживается", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmptyLinkException.class)
    public ResponseEntity<ApiErrorResponse> handleEmptyLink(EmptyLinkException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Ссылка не была передана в запросе", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnknownLinkTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownLinkType(UnknownLinkTypeException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Неизвестный тип ссылки", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLinkNotFound(LinkNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Ссылка не найдена в отслеживаемых", "404", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChatNotFound(ChatNotFoundException e) {
        ApiErrorResponse response =
                new ApiErrorResponse("Чат не найден", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidType(MethodArgumentTypeMismatchException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Неверный тип данных для параметра id", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Тело запроса отсутствует", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(GitHubRepositoryNotFound.class)
    public ResponseEntity<ApiErrorResponse> handleWrongGitHubUrl(GitHubRepositoryNotFound e) {
        ApiErrorResponse response =
                new ApiErrorResponse("Неверная ссылка", "404", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(StackOverFlowNotFound.class)
    public ResponseEntity<ApiErrorResponse> handleWrongStackOverFlowUrl(StackOverFlowNotFound e) {
        ApiErrorResponse response =
                new ApiErrorResponse("Неверная ссылка", "404", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(GitHubApiCallException.class)
    public ResponseEntity<ApiErrorResponse> handleGitHubApiCall(GitHubApiCallException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Ошибка получения данных с Github", "404", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(StackOverFlowApiCallException.class)
    public ResponseEntity<ApiErrorResponse> handleStackOverFlowApiCall(StackOverFlowApiCallException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Ошибка получения данных с StackOverFlow",
                "404",
                e.getClass().getSimpleName(),
                e.getMessage(),
                List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NotFoundIpException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundIp(NotFoundIpException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Подозрительная активность!", "400", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(TooManyRequests.class)
    public ResponseEntity<ApiErrorResponse> handleTooManyRequests(TooManyRequests e) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Вы отправили слишком много запросов!", "429", e.getClass().getSimpleName(), e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
