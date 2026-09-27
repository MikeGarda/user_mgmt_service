package com.example.jwt.domain.modules;

public class ModuleServiceUnavailableException extends RuntimeException {

  public ModuleServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
