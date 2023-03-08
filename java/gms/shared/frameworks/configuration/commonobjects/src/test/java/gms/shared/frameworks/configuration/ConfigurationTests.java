package gms.shared.frameworks.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.configuration.constraints.NumericScalarConstraint;
import gms.shared.frameworks.configuration.constraints.WildcardConstraint;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTests {

  private ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testSerialization() throws Exception {
    String json = objectMapper.writeValueAsString(TestUtilities.configurationFilter);
    assertNotNull(json);

    final Configuration deserialized = objectMapper.readValue(json, Configuration.class);
    assertEquals(TestUtilities.configurationFilter, deserialized);
  }

  @Test
  void testFromValidatesConfigurationOptionsNotNull() {
    assertTrue(assertThrows(NullPointerException.class, () -> Configuration
      .from("Config", null)).getMessage().contains("configurationOptions can't be null"));
  }

  @Test
  void testFromEquivalentConstraintsHaveSamePriority() {
    final Constraint constraintPriority1a = NumericScalarConstraint
      .from("A", Operator.from(Type.EQ, false), 3.0, 1);
    final ConfigurationOption configOptPriority1a = ConfigurationOption
      .from("A-3", List.of(constraintPriority1a), Map.of("a", 1));

    final Constraint constraintPriority1b = NumericScalarConstraint
      .from("A", Operator.from(Type.EQ, false), 5.0, 1);
    final ConfigurationOption configOptPriority1b = ConfigurationOption
      .from("A-5", List.of(constraintPriority1b), Map.of("a", 1));

    final Constraint constraintPriority10 = NumericScalarConstraint
      .from("A", Operator.from(Type.EQ, false), 5.0, 10);
    final ConfigurationOption configOptPriority10 = ConfigurationOption
      .from("A-5", List.of(constraintPriority10), Map.of("a", 10));

    final Constraint wildcardConstraint = WildcardConstraint.from("A");
    final ConfigurationOption configOptWildcard = ConfigurationOption
      .from("A-*", List.of(wildcardConstraint), Map.of("a", 100));

    final String expectedMessage = "Constraints for the same criterion must have the same priority "
      + "in all ConfigurationOptions, but this is not true for the following criteria: [A]";

    // Constraints have the same priority, no exception
    assertDoesNotThrow(
      () -> Configuration.from("Config", List.of(configOptPriority1a, configOptPriority1b)));

    // Constraints have different priority but the WildcardConstraint priority is ignored, no exception
    assertAll(
      () -> assertNotEquals(constraintPriority1a.getPriority(), wildcardConstraint.getPriority()),
      () -> assertDoesNotThrow(
        () -> Configuration.from("Config", List.of(configOptPriority1a, configOptWildcard))));

    List<ConfigurationOption> configOptions = List.of(configOptPriority1a, configOptPriority10);
    // Constraints have different priority, exception
    assertEquals(expectedMessage,
      assertThrows(IllegalArgumentException.class, () -> Configuration
        .from("Config", configOptions)).getMessage());
  }
}
