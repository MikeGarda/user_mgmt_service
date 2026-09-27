package com.example.jwt.domain.modules;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Aufgabe 6: Synchroner REST-Client zum module_service. Aufruf ueber den Kubernetes-Service
 * (module-service.url, Default http://module-service:8080) - der user_mgmt_service hat KEINEN
 * Zugriff auf die MySQL-Datenbank des module_service, nur auf dessen API.
 */
@Component
public class ModuleServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleServiceClient.class);

  private final RestClient restClient;
  private final CircuitBreaker circuitBreaker;
  private final Retry retry;

  public ModuleServiceClient(RestClient moduleServiceRestClient,
      CircuitBreaker moduleServiceCircuitBreaker, Retry moduleServiceRetry) {
    this.restClient = moduleServiceRestClient;
    this.circuitBreaker = moduleServiceCircuitBreaker;
    this.retry = moduleServiceRetry;
  }

  /** Prueft, ob das Modul beim module_service existiert (GET, 404 -> ModuleNotFoundException). */
  public void ensureModuleAvailable(UUID moduleId) {
    call(moduleId, () -> restClient.get()
        .uri("/api/v1/modules/{moduleId}", moduleId)
        .retrieve()
        .toBodilessEntity());
  }

  /** Weist das Modul dem User zu (PUT ist idempotent -> Retry ist gefahrlos). */
  public void assignModule(UUID userId, UUID moduleId) {
    call(moduleId, () -> restClient.put()
        .uri("/api/v1/users/{userId}/modules/{moduleId}", userId, moduleId)
        .retrieve()
        .toBodilessEntity());
  }

  private void call(UUID moduleId, Supplier<ResponseEntity<Void>> request) {
    // 1) Technische Fehler in fachliche Ausnahmen uebersetzen
    Supplier<ResponseEntity<Void>> translated = () -> {
      try {
        return request.get();
      } catch (HttpClientErrorException.NotFound e) {
        throw new ModuleNotFoundException(moduleId);
      } catch (HttpServerErrorException | ResourceAccessException e) {
        // 5xx, Timeout oder Verbindungsfehler: voruebergehend -> Retry + Circuit Breaker
        LOG.warn("module_service-Aufruf fehlgeschlagen: {}", e.getMessage());
        throw new ModuleServiceUnavailableException("module_service nicht erreichbar", e);
      } catch (RestClientException e) {
        throw new ModuleServiceException("Unerwartete Antwort vom module_service", e);
      }
    };
    // 2) Circuit Breaker innen (zaehlt jeden einzelnen Versuch), Retry aussen
    Supplier<ResponseEntity<Void>> resilient =
        Retry.decorateSupplier(retry, CircuitBreaker.decorateSupplier(circuitBreaker, translated));
    try {
      resilient.get();
    } catch (CallNotPermittedException e) {
      LOG.warn("Circuit Breaker '{}' offen - module_service wird nicht aufgerufen",
          circuitBreaker.getName());
      throw new ModuleServiceUnavailableException(
          "module_service voruebergehend gesperrt (Circuit Breaker offen)", e);
    }
  }
}
