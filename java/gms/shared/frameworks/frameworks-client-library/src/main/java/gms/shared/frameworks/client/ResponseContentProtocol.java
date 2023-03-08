package gms.shared.frameworks.client;

import java.lang.reflect.Type;
import java.net.http.HttpResponse.BodyHandler;

/**
 * A protocol for decoding HTTP response body content.
 * Wire is the type of the data on the wire, e.g. String or byte[].
 *
 * @param <W> the type on the wire, e.g. String or byte[]
 */
interface ResponseContentProtocol<W> {

  /**
   * A body handler for W.  This is typically
   * implemented using a method on BodyHandler, e.g. BodyHandler.asString().
   */
  BodyHandler<W> bodyHandler();

  /**
   * Deserializes data from the wire into the specified type.
   * This is typically implemented using a Jackson ObjectMapper.
   *
   * @param data the data to deserialize
   * @param type the type of the data
   * @param <X> the type param of the data, matches the class
   * @return an instance of the desired type
   * @throws Exception on deserialization failure, etc.
   */
  <X> X deserialize(W data, Type type) throws Exception;
}
