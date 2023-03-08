package gms.shared.frameworks.client;

/**
 * Convenience interface that combines request and response content protocol interfaces.
 *
 * @param <Q> the serialized request body type, e.g. String or byte[]
 * @param <S> the response body type provided to the deserializer, e.g. String, byte[], InputStream
 */
interface ContentProtocol<Q, S>
  extends RequestContentProtocol<Q>, ResponseContentProtocol<S> {

}

