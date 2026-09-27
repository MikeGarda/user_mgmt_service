package com.example.jwt.domain.modules;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aufgabe 6: Neuer Endpoint - einem User ein Modul zuweisen.
 *
 * <pre>
 * PUT /users/{id}/modules/{moduleId}
 *   204 No Content            Modul zugewiesen (idempotent)
 *   400 Bad Request           id / moduleId ist keine UUID
 *   401 / 403                 nicht eingeloggt / keine Berechtigung
 *   404 USER_NOT_FOUND        User existiert nicht
 *   404 MODULE_NOT_FOUND      Modul existiert beim module_service nicht
 *   502 MODULE_SERVICE_ERROR  unerwartete Antwort vom module_service
 *   503 MODULE_SERVICE_UNAVAILABLE  module_service down / Timeout / Circuit Breaker offen
 * </pre>
 * Berechtigt: wer USER_MODIFY hat, oder jeder User fuer sich selbst.
 */
@RestController
@RequestMapping("/users")
public class UserModuleController {

  private final ModuleAssignmentService moduleAssignmentService;

  public UserModuleController(ModuleAssignmentService moduleAssignmentService) {
    this.moduleAssignmentService = moduleAssignmentService;
  }

  @PutMapping("/{id}/modules/{moduleId}")
  @PreAuthorize("hasAuthority('USER_MODIFY') || #id == authentication.principal.user.id")
  public ResponseEntity<Void> assignModule(@PathVariable UUID id, @PathVariable UUID moduleId) {
    moduleAssignmentService.assignModuleToUser(id, moduleId);
    return ResponseEntity.noContent().build();
  }
}
