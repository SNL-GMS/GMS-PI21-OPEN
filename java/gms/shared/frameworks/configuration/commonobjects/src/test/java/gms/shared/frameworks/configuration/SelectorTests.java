package gms.shared.frameworks.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.configuration.constraints.DoubleRange;
import gms.shared.frameworks.configuration.constraints.NumericRangeConstraint;
import gms.shared.frameworks.configuration.constraints.StringConstraint;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectorTests {

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private static final String criterion = "criteron";

  @Test
  void testSerializationString() throws Exception {
    testStringBasedSelector(Selector.from(criterion, "geres"));
  }

  @Test
  void testSerializationUUID() throws Exception {
    testStringBasedSelector(Selector.from(criterion, UUID.randomUUID()));
  }

  private static <T> void testStringBasedSelector(Selector<T> selector) throws Exception {
    final Selector deserializedSelector = serializeAndDeserialize(selector);
    assertNotNull(deserializedSelector);
    final StringConstraint sc = StringConstraint.from(
      criterion, Operator.from(Type.EQ, false), Set.of(selector.getValue().toString()), 1);
    assertAll(
      () -> assertEquals(selector.getCriterion(), deserializedSelector.getCriterion()),
      () -> assertEquals(selector.getValue().toString(),
        deserializedSelector.getValue().toString()),
      () -> assertTrue(sc.test(selector.getValue().toString()))
    );
  }

  @Test
  void testSerializationDoubleNumericRange() throws Exception {
    final double value = 1.2345;
    final Selector<Double> selector = Selector.from(criterion, value);
    final Selector deserializedSelector = serializeAndDeserialize(selector);
    final NumericRangeConstraint c = NumericRangeConstraint.from(
      criterion, Operator.from(Type.IN, false), DoubleRange.from(1.0, 2.0), 1);

    assertAll(
      () -> assertEquals(selector, deserializedSelector),
      () -> assertTrue(c.test(selector.getValue()))
    );
  }

  private static <T> Selector serializeAndDeserialize(Selector<T> s) throws Exception {
    final String json = objectMapper.writeValueAsString(s);
    assertNotNull(json);
    assertFalse(json.isEmpty());
    return objectMapper.readValue(json, Selector.class);
  }
}
