package gms.shared.frameworks.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

/**
 * Utility class for deserializing an {@link InputStream} into a Flux of elements where all of the
 * elements have the same type.
 */
class StreamingResponseDeserializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamingResponseDeserializer.class);

  private final ObjectMapper mapper;

  private StreamingResponseDeserializer(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Construct a new {@link StreamingResponseDeserializer} which uses the provided {@link
   * ObjectMapper} to parse {@link InputStream}s and convert objects from serializations.
   *
   * @param mapper {@link ObjectMapper}, not null
   * @return {@link StreamingResponseDeserializer}, not null
   * @throws NullPointerException if mapper is null
   */
  static StreamingResponseDeserializer create(ObjectMapper mapper) {
    return new StreamingResponseDeserializer(
      Objects.requireNonNull(mapper, "ObjectMapper can't be null"));
  }

  // closes input stream

  /**
   * Reads the provided {@link InputStream} into a Flux of elements. Each element has the same type
   * and the type is determined using the fluxType's type parameter (see {@link
   * StreamingResponseDeserializer#getElementType(Type)}).
   * <p>
   * The inputStream must contain individually serialized elements, e.g. a Flux of Strings should be
   * serialized as `"one" "two" ...` rather than `["one", "two", ...]`. If a portion of the stream
   * contains a serialization that can't be converted into an instance of the element type then that
   * portion of the stream is skipped, e.g. if a stream contains a Bar in what is supposed to be a
   * Flux of Foos, then an error is logged and the Bar is omitted from the Flux.
   * <p>
   * This operation will emit an exception to the Flux if any deserialization error other than an
   * invalid element occurs.
   * <p>
   * Closes the inputStream when done.
   *
   * @param inputStream the {@link InputStream} to read, not null. Will be closed by this
   * operaiton.
   * @param fluxType {@link Type} corresponding to a Flux of T's, not null
   * @param <T> type of elements in the Flux
   * @return {@link Flux<T>}, not null
   * @throws NullPointerException if inputStream or fluxType are null
   * @throws IllegalArgumentException if the fluxType does not correspond to a Flux, or if it
   * corresponds to a Flux but the element type is not-concrete (i.e. wildcard or type-parameter)
   */
  <T> Flux<T> readToFlux(InputStream inputStream, Type fluxType) {

    Objects.requireNonNull(inputStream, "inputStream can't be null");
    Objects.requireNonNull(fluxType, "fluxType can't be null");

    final JavaType elementType = mapper.getTypeFactory().constructType(getElementType(fluxType));

    return Flux.push(emitter -> {
      try {
        final JsonParser parser = mapper.getFactory().createParser(inputStream);
        while (!parser.isClosed() && parser.nextToken() != null) {
          emitElement(emitter, elementType, parser);
        }
      } catch (JsonEOFException ex) {

        // MessagePack JsonParser implementation throws this when EOF is reached in nextToken().
        // Trap the exception since this is expected behavior.
      } catch (IOException ex) {

        // Can't recover from errors occurring in the parser unrelated to skipping an element that
        // couldn't be read or converted.
        final var message = "Error parsing InputStream when emitting to Flux";
        LOGGER.error(message, ex);

        emitter.error(new IOException(message, ex));

      } finally {
        emitter.complete();

        try {
          inputStream.close();
        } catch (IOException ex) {
          LOGGER.error("Could not close InputStream after reading entire response body", ex);
        }
      }
    });
  }

  /**
   * Assuming the provided fluxType is a Flux of elements of a single type, find the element {@link
   * Type}.
   *
   * @param fluxType {@link Type} that should correspond to a Flux of elements, not null
   * @return {@link Type} of element produced by the Flux, not null
   * @throws IllegalArgumentException if the fluxType does not correspond to a Flux, or if it
   * corresponds to a Flux but the element type is not-concrete (i.e. wildcard or type-parameter)
   */
  private static Type getElementType(Type fluxType) {

    // Make sure the responseType is a Flux
    if (!ParameterizedType.class.isAssignableFrom(fluxType.getClass())) {
      throw new IllegalArgumentException(
        "responseType must be a Flux but can't be as it is not a ParameterizedType");
    }

    final var parameterizedType = (ParameterizedType) fluxType;
    if (Flux.class != parameterizedType.getRawType()) {
      throw new IllegalArgumentException("responseType must be a Flux");
    }

    // Make sure the element type is concrete (i.e. Flux<?> and Flux<T> aren't supported)
    final var elementType = parameterizedType.getActualTypeArguments()[0];
    if (elementType instanceof WildcardType) {
      throw new IllegalArgumentException(
        "responseType must be a Flux of a concrete type but is a Flux<?>");
    }

    if (elementType instanceof TypeVariable) {
      throw new IllegalArgumentException(
        "responseType must be a Flux of a concrete type but is a Flux<T>");
    }

    return elementType;
  }

  /**
   * Reads a single object from the {@link JsonParser}, converts it to the provided {@link JavaType},
   * and emits it with the provided {@link FluxSink}.
   *
   * @param emitter {@link FluxSink} emitting parsed elements
   * @param elementType {@link JavaType} describing the type of elements to emit
   * @param parser {@link JsonParser} used to read elements
   */
  private void emitElement(FluxSink<?> emitter, JavaType elementType, JsonParser parser) {
    // Read each element from the InputStream into a Jackson Tree, use the mapper to
    // convert the tree into an element, and then emit that element.
    try {
      emitter.next(mapper.convertValue(parser.readValueAsTree(), elementType));
    } catch (IOException | IllegalArgumentException ex) {
      LOGGER.error(
        "Omitting object from streaming response as it could not be deserialized as an {}",
        elementType, ex);
    }
  }
}
