package gms.shared.frameworks.osd.coi;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utilities for working with FieldMaps (String to Object map representations of objects)
 */
public class FieldMapUtilities {

  /**
   * Field maps created by {@link FieldMapUtilities#toFieldMap(Object)} will have this type.
   */
  private static final JavaType mapType = TypeFactory.defaultInstance()
    .constructMapType(HashMap.class, String.class, Object.class);

  /**
   * Used to convert objects to and from field maps.
   */
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  /**
   * Prevents instantiation of FieldMapUtilities
   */
  private FieldMapUtilities() {
  }

  /**
   * Converts the fieldMap into an instance of outputClass.
   *
   * @param fieldMap String to object map representation of an outputClass instance, not null
   * @param outputClass class type represented by fieldMap, not null
   * @param <T> output class type
   * @return a instance of T corresponding to the fieldMap's contents, not null
   * @throws NullPointerException if fieldMap or outputClass are null
   * @throws IllegalArgumentException if the field map cannot be converted into an instance of
   * outputClass
   */
  public static <T> T fromFieldMap(Map<String, Object> fieldMap, Class<T> outputClass) {
    Objects.requireNonNull(fieldMap, "fieldMap can't be null");
    Objects.requireNonNull(outputClass, "outputClass can't be null");

    return objectMapper.convertValue(fieldMap, outputClass);
  }

  /**
   * Converts the object into a field map (string to object map).
   *
   * @param object object to convert to a FieldMap, not null
   * @return string to object map (field map) representation of the object, not null
   * @throws NullPointerException if object is null
   * @throws IllegalArgumentException if the object cannot be converted to a field map
   */
  public static Map<String, Object> toFieldMap(Object object) {
    Objects.requireNonNull(object, "object can't be null");

    return objectMapper.convertValue(object, mapType);
  }
}
