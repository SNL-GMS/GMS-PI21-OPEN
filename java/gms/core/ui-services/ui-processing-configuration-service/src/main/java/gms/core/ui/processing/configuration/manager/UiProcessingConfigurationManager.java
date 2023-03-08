package gms.core.ui.processing.configuration.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import gms.core.ui.processing.configuration.ConfigQuery;
import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * UI Processing Configuration Spring Manager providing configuration
 * access to the interactive analysis components.
 */
@RestController
public class UiProcessingConfigurationManager {
  private static final Logger logger = LoggerFactory.getLogger(UiProcessingConfigurationManager.class);

  private final ReadWriteLock ccuUpdateLock;
  private ConfigurationConsumerUtility configurationConsumerUtility;

  @Autowired
  public UiProcessingConfigurationManager(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.ccuUpdateLock = new ReentrantReadWriteLock();
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Resolve configuration for the provided query
   * @param query Query to resolve for the given name and selectors
   * @return Resolved configuration field map
   */
  @PostMapping(value = "/resolve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
  @Operation(description = "Resolves a configuration")
  public Map<String, Object> resolve(@io.swagger.v3.oas.annotations.parameters.RequestBody(
    description = "A query", required = true)
  @RequestBody ConfigQuery query) {
    ccuUpdateLock.readLock().lock();
    try {
      return configurationConsumerUtility.resolve(query.getConfigurationName(), query.getSelectors());
    } catch (Exception ex) {
      return Map.of();
    } finally {
      ccuUpdateLock.readLock().unlock();
    }
  }
  
  /**
   * Updates the backing configuration for this manager
   * @return Empty JSON node to comply with expected no-value response behavior
   */
  @PostMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
  @Operation(description = "Updates the backing configuration")
  public ResponseEntity<JsonNode> update() {
    ccuUpdateLock.writeLock().lock();
    try {
        this.configurationConsumerUtility = this.configurationConsumerUtility.toBuilder().build();
        return ResponseEntity.ok(JsonNodeFactory.instance.objectNode());
    } catch (Exception ex) {
      logger.error("Failed to update configuration", ex);
      return ResponseEntity.internalServerError()
        .body(JsonNodeFactory.instance.textNode(
          String.format("Failed to update configuration, Reason: %s", ex)));
    } finally {
      ccuUpdateLock.writeLock().unlock();
    }
  }
}
