package com.example.jwt.domain.modules;

import com.example.jwt.domain.user.UserService;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Aufgabe 6: Ablauf einer Modulzuweisung. */
@Service
public class ModuleAssignmentService {

  private final UserService userService;
  private final ModuleServiceClient moduleServiceClient;

  public ModuleAssignmentService(UserService userService,
      ModuleServiceClient moduleServiceClient) {
    this.userService = userService;
    this.moduleServiceClient = moduleServiceClient;
  }

  public void assignModuleToUser(UUID userId, UUID moduleId) {
    // 1) User existiert im user_mgmt_service (eigene PostgreSQL)?
    if (!userService.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
    // 2) Vor der Zuweisung beim module_service pruefen, ob das Modul verfuegbar ist
    moduleServiceClient.ensureModuleAvailable(moduleId);
    // 3) Zuweisung ausfuehren (speichert der module_service in seiner MySQL)
    moduleServiceClient.assignModule(userId, moduleId);
  }
}
