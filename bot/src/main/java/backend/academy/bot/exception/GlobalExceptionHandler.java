package backend.academy.bot.exception;

import backend.academy.bot.controller.LinksUpdateController;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Log4j2
@RestControllerAdvice(assignableTypes = {LinksUpdateController.class})
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Void> handleInvalidType(MethodArgumentTypeMismatchException e) {
        log.info("Пустой тело запроса: " + e.getMessage());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
