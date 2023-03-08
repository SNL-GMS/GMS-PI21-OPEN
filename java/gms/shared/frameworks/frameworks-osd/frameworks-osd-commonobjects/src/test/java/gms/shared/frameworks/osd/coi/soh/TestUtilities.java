package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.Objects;

public class TestUtilities {

  /**
   * Tests whether an object can be serialized and deserialized with the COI object mapper.
   *
   * @param object an instance of the object
   * @param type the type of the object
   * @param <T> the type of the object
   * @throws IOException if serialization/deserialization fails
   */
  public static <T> void testSerialization(T object, Class<T> type)
    throws IOException {
    testSerialization(object, type, getJsonObjectMapper());
  }

  /**
   * Tests whether an object can be serialized and deserialized with the given object mapper.
   *
   * @param object an instance of the object
   * @param type the type of the object
   * @param objMapper an object mapper to use
   * @param <T> the type of the object
   * @throws IOException if serialization/deserialization fails
   */
  public static <T> void testSerialization(T object, Class<T> type, ObjectMapper objMapper)
    throws IOException {
    Objects.requireNonNull(object, "Cannot test serialization with null object instance");
    Objects.requireNonNull(type, "Cannot test serialization with null type");
    Objects.requireNonNull(objMapper, "Cannot test serialization with null object mapper");
    final String serialized = objMapper.writeValueAsString(object);
    Objects.requireNonNull(serialized, "Expected serialized string to be non-null");
    Validate.isTrue(!serialized.isEmpty(), "Expected serialized string to not be empty");
    final T deserialized = objMapper.readValue(serialized, type);
    Objects.requireNonNull(deserialized, "Expected deserialized object to be non-null");
    Validate.isTrue(object.equals(deserialized), "Expected deserialized object to equal original; original: "
      + object + " , deserialized: " + deserialized);
  }

  /**
   * Gets an ObjectMapper for use in JSON serialization. This ObjectMapper can serialize/deserialize any COI object, and
   * has common modules registered such as for Java 8 Instant.
   *
   * @return an ObjectMapper for use with JSON
   */
  public static ObjectMapper getJsonObjectMapper() {
    return configureObjectMapper(new ObjectMapper());
  }

  private static ObjectMapper configureObjectMapper(ObjectMapper objMapper) {
    return objMapper.findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
}
