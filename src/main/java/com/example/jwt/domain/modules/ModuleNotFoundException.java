package com.example.jwt.domain.modules;

import java.util.UUID;

public class ModuleNotFoundException extends RuntimeException {

  public ModuleNotFoundException(UUID moduleId) {
    super(String.format("Modul '%s' existiert nicht", moduleId));
  }
}
