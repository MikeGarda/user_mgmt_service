package com.example.jwt.domain.modules;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Aufgabe 6: Absicherung der synchronen REST-Kommunikation zum module_service.
 *
 * <ul>
 *   <li><b>Timeout</b>: Verbindungsaufbau und Antwort je max. 2 s (JDK-HttpClient).</li>
 *   <li><b>Retry</b>: bis zu 3 Versuche mit exponentiellem Backoff (300 ms, 600 ms) - nur bei
 *       voruebergehenden Fehlern (Timeout, Verbindungsfehler, HTTP 5xx), nie bei 404.</li>
 *   <li><b>Circuit Breaker</b>: schlagen von den letzten 10 Aufrufen >= 50 % fehl, wird der
 *       module_service 15 s lang gar nicht mehr aufgerufen (sofort HTTP 503, schont beide
 *       Seiten). Danach testen 2 Probe-Aufrufe, ob er wieder gesund ist.</li>
 * </ul>
 */
@Configuration
public class ModuleServiceClientConfig {

  public static final String NAME = "moduleService";

  @Bean
  public RestClient moduleServiceRestClient(
      @Value("${module-service.url}") String baseUrl,
      @Value("${module-service.connect-timeout-ms:2000}") long connectTimeoutMs,
      @Value("${module-service.read-timeout-ms:2000}") long readTimeoutMs,
      ObjectProvider<ObservationRegistry> observationRegistry) {
    HttpClient httpClient = HttpClient.newBuilder()
        // HTTP/1.1 fest: kein h2c-Upgrade-Versuch gegenueber uvicorn
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofMillis(connectTimeoutMs))
        .build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory)
        // Metriken http_client_requests_* (Aufrufe Backend -> module_service) fuer Prometheus
        .observationRegistry(observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP))
        .build();
  }

  @Bean
  public CircuitBreaker moduleServiceCircuitBreaker(ObjectProvider<MeterRegistry> meterRegistry) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(10)
        .minimumNumberOfCalls(5)
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(15))
        .permittedNumberOfCallsInHalfOpenState(2)
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        // Nur echte Stoerungen zaehlen als Fehler - ein unbekanntes Modul (404) ist
        // eine gueltige fachliche Antwort und darf den Breaker nicht oeffnen.
        .recordExceptions(ModuleServiceUnavailableException.class)
        .ignoreExceptions(ModuleNotFoundException.class, ModuleServiceException.class)
        .build();
    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
    meterRegistry.ifAvailable(
        m -> TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(m));
    return registry.circuitBreaker(NAME);
  }

  @Bean
  public Retry moduleServiceRetry(ObjectProvider<MeterRegistry> meterRegistry) {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(300), 2.0))
        // Nur voruebergehende Fehler wiederholen. Nicht wiederholt werden: 404 (Modul
        // existiert nicht) und CallNotPermittedException (Breaker offen -> sofort 503).
        .retryExceptions(ModuleServiceUnavailableException.class)
        .build();
    RetryRegistry registry = RetryRegistry.of(config);
    meterRegistry.ifAvailable(m -> TaggedRetryMetrics.ofRetryRegistry(registry).bindTo(m));
    return registry.retry(NAME);
  }
}
