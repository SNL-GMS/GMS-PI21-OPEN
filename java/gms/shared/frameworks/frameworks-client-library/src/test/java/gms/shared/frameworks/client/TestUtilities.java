package gms.shared.frameworks.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class TestUtilities {

  /**
   * Uses the provided {@link ObjectMapper} to individually serialize each element of the provided
   * Collection into an {@link InputStream}
   *
   * @param collection {@link Collection} to serialize
   * @param contentType {@link ContentType} describing the serialization type
   * @return {@link InputStream} containing the serialized input collection
   */
  static InputStream collectionToInputStream(Collection<?> collection, ContentType contentType) {
    return new ByteArrayInputStream(collectionToByteArray(collection, contentType));
  }

  /**
   * Serializes the provided collection into a byte[] using the serialization format defined by the
   * {@link ContentType} provided in the serializerType parameter. Each element from the collection
   * is serialied independently, so the serialization will contain a sequence of serialized objects
   * rather than a single object containing the serialized collection.
   *
   * @param collection Collection containing the elements to serialize
   * @param contentType {@link ContentType} describing the serialization type
   * @return byte[] containing serializations of each element from the input collection, in order.
   */
  static byte[] collectionToByteArray(Collection<?> collection, ContentType contentType) {
    final Function<Object, Optional<byte[]>> serializer = createSerializer(contentType);

    final List<byte[]> responseByteArrays = collection.stream()
      .map(serializer)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());

    final int serializedLength = responseByteArrays.stream().mapToInt(Array::getLength).sum();
    final ByteBuffer responseByteBuffer = ByteBuffer.allocate(serializedLength);
    responseByteArrays.forEach(responseByteBuffer::put);

    return responseByteBuffer.array();
  }

  /**
   * @return a {@link FunctionalInterface} using the {@link ObjectMapper} for the provided {@link
   * ContentType} to serialize an Object as a {@link Optional) byte[]. The optional is empty on
   * serialization failures.
   */
  private static Function<Object, Optional<byte[]>> createSerializer(ContentType contentType) {
    final ObjectMapper mapper = contentTypeToMapper(contentType);

    return obj -> {
      try {
        return Optional.of(
          (ContentType.JSON_STREAM == contentType)
            ? (mapper.writeValueAsString(obj) + " ").getBytes()
            : mapper.writeValueAsBytes(obj));
      } catch (JsonProcessingException e) {
        return Optional.empty();
      }
    };
  }

  /**
   * @return the {@link ObjectMapper} to use with the provided {@link ContentType}
   */
  static ObjectMapper contentTypeToMapper(ContentType contentType) {
    return contentType.equals(ContentType.MSGPACK_STREAM)
      ? CoiObjectMapperFactory.getMsgpackObjectMapper()
      : CoiObjectMapperFactory.getJsonObjectMapper();
  }

  /**
   * @param clazz {@link Class} lookup method in this class
   * @param methodName name of the method to lookup
   * @return {@link Type} corresponding to the provided method in the provided class.
   */
  static Type getResponseType(Class<?> clazz, String methodName) {
    try {
      return clazz
        .getDeclaredMethod(methodName)
        .getGenericReturnType();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }
}
