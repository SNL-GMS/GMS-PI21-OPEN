package gms.shared.utilities.bridge.database;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BridgedEntityManagerFactoryProviderTest {

  @Test
  void initialize_default() {
    Assertions.assertDoesNotThrow(
      () -> BridgedEntityManagerFactoryProvider
        .create());
  }

  @Test
  void initialize_specifySchema() {
    Assertions.assertDoesNotThrow(
      () -> BridgedEntityManagerFactoryProvider
        .create("SCHEMA_NAME"));
  }

  @Test
  void initializationErrors_nullSchema() {
    final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
      () -> BridgedEntityManagerFactoryProvider.create(null));
    assertEquals("Schema name provided is missing or blank.", error.getMessage());
  }

  @Test
  void initializationErrors_missingSchema() {
    final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
      () -> BridgedEntityManagerFactoryProvider.create(""));
    assertEquals("Schema name provided is missing or blank.", error.getMessage());
  }

}