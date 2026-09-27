package com.example.jwt.core.exception;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class CustomGlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ResponseError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    return new ResponseError()
        .setTimeStamp(LocalDate.now())
        .setErrors(ex.getBindingResult().getFieldErrors().stream().collect(
            Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage())))
        .build();
  }

  /**
   * Aufgabe 6: Ungueltiger Pfad-Parameter (z. B. keine UUID) -> 400 direkt beantworten.
   * Ohne diesen Handler leitet Spring intern auf /error weiter; dieser zweite Aufruf laeuft
   * ohne JWT durch Spring Security und wuerde faelschlich mit 403 statt 400 enden.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ResponseError handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return new ResponseError()
        .setTimeStamp(LocalDate.now())
        .setErrors(Map.<String, Object>of(
            "code", "INVALID_PARAMETER",
            "message", String.format("Parameter '%s' hat einen ungueltigen Wert: '%s'",
                ex.getName(), ex.getValue())))
        .build();
  }

}
