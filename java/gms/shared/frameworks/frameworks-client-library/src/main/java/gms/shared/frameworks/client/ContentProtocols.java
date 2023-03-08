package gms.shared.frameworks.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static gms.shared.frameworks.common.ContentType.JSON;
import static gms.shared.frameworks.common.ContentType.JSON_STREAM;
import static gms.shared.frameworks.common.ContentType.MSGPACK;
import static gms.shared.frameworks.common.ContentType.MSGPACK_STREAM;

/**
 * Provides content protocols for {@link ContentType}s.
 */
class ContentProtocols {

  private ContentProtocols() {
  }

  private static final Map<ContentType, ContentProtocol> typeToProtocol = Map.of(
    JSON, new Json(),
    JSON_STREAM, new JsonStream(),
    MSGPACK, new Msgpack(),
    MSGPACK_STREAM, new MsgpackStream()
  );

  /**
   * Gets a ContentProtocol given a ContentType.
   *
   * @param contentType the content type
   * @param <T> the type param returned - unchecked, but marked here so callers don't have to
   * suppress.
   * @return a ContentProtocol that handles the specified content type
   * @throws IllegalArgumentException if there is no ContentProtocol implementation for the
   * specified content type
   */
  @SuppressWarnings("unchecked")
  public static <T, U> ContentProtocol<T, U> from(ContentType contentType) {
    Objects.requireNonNull(contentType, "ContentType can't be null");
    if (!typeToProtocol.containsKey(contentType)) {
      throw new IllegalArgumentException("Unknown content type: " + contentType);
    }
    return typeToProtocol.get(contentType);
  }

  private static final class Json implements ContentProtocol<String, String> {

    private final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

    @Override
    public Function<String, BodyPublisher> bodyEncoder() {
      return BodyPublishers::ofString;
    }

    @Override
    public String serialize(Object data) throws Exception {
      return mapper.writeValueAsString(data);
    }

    @Override
    public BodyHandler<String> bodyHandler() {
      return BodyHandlers.ofString();
    }

    @Override
    public <T> T deserialize(String data, Type type) throws Exception {
      return mapper.readValue(data, mapper.constructType(type));
    }
  }

  private static final class Msgpack implements ContentProtocol<byte[], byte[]> {

    private final ObjectMapper mapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

    @Override
    public Function<byte[], BodyPublisher> bodyEncoder() {
      return BodyPublishers::ofByteArray;
    }

    @Override
    public byte[] serialize(Object data) throws Exception {
      return mapper.writeValueAsBytes(data);
    }

    @Override
    public BodyHandler<byte[]> bodyHandler() {
      return BodyHandlers.ofByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) throws Exception {
      return mapper.readValue(data, mapper.constructType(type));
    }
  }

  private static final class JsonStream implements ContentProtocol<String, InputStream> {

    private final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    private final StreamingResponseDeserializer deserializer =
      StreamingResponseDeserializer.create(mapper);

    @Override
    public Function<String, BodyPublisher> bodyEncoder() {
      return BodyPublishers::ofString;
    }

    @Override
    public String serialize(Object data) throws Exception {
      return mapper.writeValueAsString(data);
    }

    @Override
    public BodyHandler<InputStream> bodyHandler() {
      return BodyHandlers.ofInputStream();
    }

    /**
     * Deserializes individual elements from the {@link InputStream} and makes them available as a
     * {@link Flux}.
     *
     * @param data {@link InputStream} providing the elements to deserialize
     * @param fluxType the type of the deserialized data, must be a Flux of some element type that
     * can be deserialized with an {@link ObjectMapper}.
     * @param <T> deserialized type, will be a Flux
     * @return a {@link Flux} of elements deserialized from the InputStream
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(InputStream data, Type fluxType) {
      return (T) deserializer.readToFlux(data, fluxType);
    }
  }

  private static final class MsgpackStream implements ContentProtocol<byte[], InputStream> {

    private final ObjectMapper mapper = CoiObjectMapperFactory.getMsgpackObjectMapper();
    private final StreamingResponseDeserializer deserializer =
      StreamingResponseDeserializer.create(mapper);

    @Override
    public Function<byte[], BodyPublisher> bodyEncoder() {
      return BodyPublishers::ofByteArray;
    }

    @Override
    public byte[] serialize(Object data) throws Exception {
      return mapper.writeValueAsBytes(data);
    }

    @Override
    public BodyHandler<InputStream> bodyHandler() {
      return BodyHandlers.ofInputStream();
    }

    /**
     * Deserializes individual elements from the {@link InputStream} and makes them available as a
     * {@link Flux}.
     *
     * @param data {@link InputStream} providing the elements to deserialize
     * @param fluxType the type of the deserialized data, must be a Flux of some element type that
     * can be deserialized with an {@link ObjectMapper}.
     * @param <T> deserialized type, will be a Flux
     * @return a {@link Flux} of elements deserialized from the InputStream
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(InputStream data, Type fluxType) {
      return (T) deserializer.readToFlux(data, fluxType);
    }
  }
}
