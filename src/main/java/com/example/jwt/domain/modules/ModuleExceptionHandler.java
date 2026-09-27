package com.example.jwt.domain.modules;

import com.example.jwt.core.exception.ResponseError;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Aufgabe 6: passende HTTP-Statuscodes fuer erfolgreiche und fehlerhafte Zuweisungen. */
@RestControllerAdvice
public class ModuleExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseError handleUserNotFound(UserNotFoundException ex) {
    return error("USER_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(ModuleNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseError handleModuleNotFound(ModuleNotFoundException ex) {
    return error("MODULE_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(ModuleServiceUnavailableException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ResponseError handleUnavailable(ModuleServiceUnavailableException ex) {
    return error("MODULE_SERVICE_UNAVAILABLE", ex.getMessage());
  }

  @ExceptionHandler(ModuleServiceException.class)
  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  public ResponseError handleBadGateway(ModuleServiceException ex) {
    return error("MODULE_SERVICE_ERROR", ex.getMessage());
  }

  private ResponseError error(String code, String message) {
    return new ResponseError()
        .setTimeStamp(LocalDate.now())
        .setErrors(Map.<String, Object>of("code", code, "message", message))
        .build();
  }
}
