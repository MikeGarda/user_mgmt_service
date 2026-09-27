package com.example.jwt.domain.modules;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(UUID userId) {
    super(String.format("User '%s' existiert nicht", userId));
  }
}
