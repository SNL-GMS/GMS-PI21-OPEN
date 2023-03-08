package gms.shared.utilities.test;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Helpful methods for testing.
 */
public class TestUtilities {

  private static final String NULL_OBJECT_STRING = "Cannot test serialization with null object instance";

  private static final String NULL_TYPE_STRING = "Cannot test serialization with null type";

  private static final String NULL_MAPPER_STRING = "Cannot test serialization with null object mapper";

  private static final String NULL_EMPTY_STRING = "serialized string should not be null or empty";

  private static final String DESERIALIZED_EQUAL_STRING = "deserialized object should equal original object";

  private TestUtilities() {
    // prevent instantiation
  }

  /**
   * Tests whether an object can be serialized and deserialized with the COI object mapper.
   *
   * @param object an instance of the object
   * @param type the type of the object
   * @param <T> the type of the object
   */
  public static <T> void assertSerializes(T object, Class<T> type) {
    assertSerializes(object, type, ObjectMapperFactory.getJsonObjectMapper());
  }

  /**
   * Tests whether an object can be serialized and deserialized with the given object mapper.
   *
   * @param <T> the type of the object
   * @param object an instance of the object
   * @param type the type of the object
   * @param objMapper an object mapper to use
   */
  public static <T> void assertSerializes(T object, Class<T> type, ObjectMapper objMapper) {
    assertNotNull(object, NULL_OBJECT_STRING);
    assertNotNull(type, NULL_TYPE_STRING);
    assertNotNull(objMapper, NULL_MAPPER_STRING);

    var serialized = assertDoesNotThrow(() -> objMapper.writeValueAsString(object));
    assertThat(serialized).as(NULL_EMPTY_STRING)
      .isNotNull()
      .isNotEmpty();

    var deserialized = assertDoesNotThrow(() -> objMapper.readValue(serialized, type));
    assertThat(deserialized).as(DESERIALIZED_EQUAL_STRING)
      .isEqualTo(object);
  }

  /**
   * Tests whether an object can be serialized and deserialized with the given object mapper.
   *
   * @param object an instance of the object
   * @param type the type of the object
   * @param objMapper an object mapper to use
   * @param <T> the type of the object
   */
  public static <T> void assertSerializes(T object, JavaType type, ObjectMapper objMapper) {
    assertNotNull(object, NULL_OBJECT_STRING);
    assertNotNull(type, NULL_TYPE_STRING);
    assertNotNull(objMapper, NULL_MAPPER_STRING);

    var serialized = assertDoesNotThrow(() -> objMapper.writeValueAsString(object));
    assertThat(serialized).as(NULL_EMPTY_STRING)
      .isNotNull()
      .isNotEmpty();

    var deserialized = assertDoesNotThrow(() -> objMapper.readValue(serialized, type));
    assertThat(deserialized).as(DESERIALIZED_EQUAL_STRING)
      .isEqualTo(object);
  }

  public static <T> void assertSerializes(T object, TypeReference<T> typeReference) {
    assertSerializes(object, typeReference, ObjectMapperFactory.getJsonObjectMapper());
  }

  private static <T> void assertSerializes(T object, TypeReference<T> typeReference, ObjectMapper objMapper) {
    assertNotNull(object, NULL_OBJECT_STRING);
    assertNotNull(typeReference, NULL_TYPE_STRING);
    assertNotNull(objMapper, NULL_MAPPER_STRING);

    var serialized = assertDoesNotThrow(() -> objMapper.writeValueAsString(object));
    assertThat(serialized).as(NULL_EMPTY_STRING)
      .isNotNull()
      .isNotEmpty();

    var deserialized = assertDoesNotThrow(() -> objMapper.readValue(serialized, typeReference));
    assertThat(deserialized).as(DESERIALIZED_EQUAL_STRING)
      .isEqualTo(object);
  }
}
