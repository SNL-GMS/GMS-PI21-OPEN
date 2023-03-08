package gms.shared.frameworks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.common.GmsCommonRoutes;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.HttpResources;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.lang.String.format;

/**
 * Class for controlling a service. The main entry point of this library.
 */
public class HttpService {

  private static final Logger logger = LoggerFactory.getLogger(HttpService.class);
  private static final String TEXT_PLAIN = "text/plain";
  private static final String APPLICATION_JSON = "application/json";

  private boolean isRunning = false;

  private final ServiceDefinition definition;

  private DisposableServer nettyService;

  HttpService(ServiceDefinition def) {
    this.definition = Objects.requireNonNull(def, "Cannot create HttpService will null definition");
    this.nettyService = null;
  }

  /**
   * Indicates whether the service is currently running.
   *
   * @return true if the service is running, false otherwise.
   */
  public boolean isRunning() {
    return this.isRunning;
  }

  /**
   * Returns the definition of this service.
   *
   * @return the definition
   */
  public ServiceDefinition getDefinition() {
    return this.definition;
  }

  /**
   * Starts the service.  If the service is already running (e.g. this method has been called
   * before), this call throws an exception.  This method configures the HTTP server (e.g. sets
   * port), registers service routes and exception handlers, and launches the service.
   */
  public void start() {
    // if service is running, throw an exception.
    if (isRunning) {
      throw new IllegalStateException("Service is already running");
    }
    logger.info("Starting service...");

    String healthCheckEndpoint = "/" + definition.getContextRoot() + GmsCommonRoutes.HEALTHCHECK_PATH;
    String upgradeEndpoint = "/" + definition.getContextRoot() + GmsCommonRoutes.CONNECTION_UPGRADE_PATH;
    logger.info("Registering healthcheck route at {}", healthCheckEndpoint);
    logger.info("Registering upgrade route at {}", upgradeEndpoint);
    // register routes
    // start the service
    logger.info("Starting the service...");
    this.nettyService = configureServer().route(routes -> {
      routes.get(healthCheckEndpoint,
        (req, res) -> res
          .header(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON)
          .sendString(Mono.just("{\"aliveAt\":\"" + Instant.now() + "\"}")));
      routes.get(upgradeEndpoint,
        (req, res) -> res.header(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN)
          .sendString(Mono.just("Connection Upgraded to HTTP/2 at " + Instant.now())));
      logger.info("Registering {} routes", this.definition.getRoutes().size());
      for (Route r : this.definition.getRoutes()) {
        logger.info("Registering route with path {}", r.getPath());
        routes.post(r.getPath(), nettyRoute(r.getHandler()));
      }
    }).bindNow();
    isRunning = true;
    logger.info("Service is now running on port {}",
      this.definition.getServerConfig().getPort());
    // Register a handler that stops the service if the JVM is shutting down
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    // start waiting for requests
    (new Thread(() -> this.nettyService.onDispose().block())).start();
  }

  /**
   * Stops the service.  If the service was not running, this call does nothing.
   */
  public void stop() {
    logger.info("Stopping the service...");
    logger.info("Awaiting the service to be stopped...");
    this.nettyService.disposeNow(Duration.ofSeconds(5));
    // clean up http resources
    HttpResources
      .disposeLoopsAndConnectionsLater(Duration.ofSeconds(10), Duration.ofSeconds(30))
      .block(Duration.ofSeconds(30));
    isRunning = false;
    logger.info("Service is stopped");
  }

  /**
   * Configures the HTTP server
   */
  private HttpServer configureServer() {
    final var config = this.definition.getServerConfig();
    return HttpServer.create()
      .protocol(HttpProtocol.H2C, HttpProtocol.HTTP11)
      .port(config.getPort());
  }

  /**
   * Creates an I/O handler {@link BiFunction} required by the Netty Server. The BiFunction has
   * inputs that are a {@link HttpServerRequest} representing the client request, including the
   * serialized body, and a {@link HttpServerResponse} returned to the client. The BiFunction
   * producers a Publisher created by populating the HttpServerResponse with a body, headers, and
   * status; the Publisher is used to signal success or failure, see {@link HttpServerResponse} and
   * {@link reactor.netty.NettyOutbound} for details.
   * <p>
   * Invokes the {@link RequestHandler} operation on {@link Schedulers#boundedElastic()} since the
   * operation might be blocking, I/O bound, or otherwise slow to return.
   *
   * @param handler the request handler operation backed by application logic
   * @return a Spark Route function that uses the provided RequestHandler and serialization objects
   */
  private BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> nettyRoute(
    RequestHandler<?> handler) {
    return (nettyRequest, nettyResponse) ->
      nettyRequest.receive()
        .aggregate()
        .asByteArray()
        .publishOn(Schedulers.boundedElastic())
        .defaultIfEmpty(new ByteArrayOutputStream().toByteArray())
        .flatMapMany(buf -> handleRequest(nettyRequest, nettyResponse, handler, buf));
  }

  /**
   * Handles the given data, sent as a byte[], with the given {@link RequestHandler} object
   *
   * @param nettyRequest - {@link HttpServerRequest} holding the header information that was
   * provided by the incoming request.
   * @param nettyResponse - {@link HttpServerResponse} created by the Netty server
   * @param handler - {@link RequestHandler} holding the callback to handle the request with
   * @param buf - data that was aggregated by netty before invoking the handler
   * @return a {@link Mono} object that writes out the serialized HTTP response to the wire.
   */
  private Publisher<Void> handleRequest(HttpServerRequest nettyRequest,
    HttpServerResponse nettyResponse, RequestHandler<?> handler, byte[] buf) {
    // wrap the Request
    logger.info("Handling request: {}", nettyRequest);
    final Request request = new NettyRequest(nettyRequest, buf);
    // get the proper deserializer for the Request
    final ObjectMapper deserializer =
      request.clientSentMsgpack() ? this.definition.getMsgpackMapper()
        : this.definition.getJsonMapper();
    // invoke the route handler
    return Mono.just(invokeHandler(handler, request, deserializer))
      .flatMapMany(routeResponse ->
        writeOutResponse(nettyResponse, routeResponse, request.clientAcceptsMsgpack()));
  }

  /**
   * This method wraps the appropriate {@link Response} object as a {@link Publisher} that is
   * returned across the wire through a given {@link HttpServerResponse} object.
   *
   * @param nettyResponse - {@link HttpServerResponse} object to write the response to
   * @param routeResponse - {@link Response} object returned from our handler
   * @param acceptsMsgPack - true if we are writing msgpack, false otherwise.
   * @return a {@link Publisher} object that writes out the serialized HTTP response to the wire.
   */
  private Publisher<Void> writeOutResponse(HttpServerResponse nettyResponse,
    Response<?> routeResponse, boolean acceptsMsgPack) {
    // appropriately set the attributes of the Response object
    nettyResponse.status(routeResponse.getHttpStatus().getStatusCode());
    nettyResponse.header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

    // check if error message is set; return the error message as plain text if so.
    Optional<String> errorMessage = routeResponse.getErrorMessage();
    Optional<?> bodyOptional = routeResponse.getBody();

    if (errorMessage.isPresent()) {
      return sendError(nettyResponse, errorMessage.get());
    } else if (bodyOptional.isPresent()) {
      final Object body = bodyOptional.get();
      return asFlux(body)
        .map(flux -> sendResponse(nettyResponse, acceptsMsgPack, flux))
        .orElseGet(() -> sendResponse(nettyResponse, acceptsMsgPack, body));
    } else {
      throw new IllegalArgumentException(
        format("Invalid response, no error message or body present: %s", routeResponse));
    }
  }

  /**
   * Sends a single plaintext string containing the provided error message.
   *
   * @param response send the error over this {@link HttpServerResponse}
   * @param errorMessage error message text
   * @return {@link Publisher} with results of sending the error message.
   */
  private Publisher<Void> sendError(HttpServerResponse response, String errorMessage) {
    response.header(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN);
    return response
      .sendString(Mono.just(errorMessage))
      .then();
  }

  /**
   * Returns the provided object cast to a {@link Flux}, or an empty optional if it isn't one.
   *
   * @param obj object that might be a Flux
   * @return {@link Optional} containing the object cast as a Flux, or empty if the object is not a
   * Flux.
   */
  private static Optional<Flux<?>> asFlux(Object obj) {
    if (Flux.class.isAssignableFrom(obj.getClass())) {
      return Optional.of((Flux<?>) obj);
    }
    return Optional.empty();
  }

  /**
   * Sends a streaming response containing each element from the provided {@link Flux}.
   *
   * @param response stream the Flux's elements over this {@link HttpServerResponse}
   * @param sendMsgPack true if the response should be serialized in MessagePack
   * @param body {@link Flux} providing elements for the response.
   * @return {@link Publisher} with results of sending the response.
   */
  private Publisher<Void> sendResponse(
    HttpServerResponse response, boolean sendMsgPack, Flux<?> body) {

    return sendMsgPack
      ? sendMsgPackStream(response, body)
      : sendJsonStream(response, body);
  }

  /**
   * Sends a streaming response containing each element from the provided {@link Flux} serialized in
   * MessagePack.
   *
   * @param response stream the Flux's elements over this {@link HttpServerResponse}
   * @param body {@link Flux} providing elements for the response.
   * @return {@link Publisher} with results of sending the response.
   */
  private Publisher<Void> sendMsgPackStream(HttpServerResponse response, Flux<?> body) {
    return response
      .header(HttpHeaderNames.CONTENT_TYPE, ContentType.MSGPACK_STREAM_NAME)
      .sendByteArray(body
        .map(this::serializeObjectMsgPack)
        .filter(Optional::isPresent)
        .map(Optional::get)
      );
  }

  /**
   * Serialize the provided object in MessagePack. Returns an empty optional if the serialization
   * fails for any reason.
   *
   * @param obj object to serialize
   * @return {@link Optional} with the MessagePack representation of the provided object.
   */
  private Optional<byte[]> serializeObjectMsgPack(Object obj) {
    try {
      return Optional.of(definition.getMsgpackMapper().writeValueAsBytes(obj));
    } catch (IOException e) {
      logger.error(
        "Omitting object from MessagePack streaming response as it could not be serialized", e);
      logger.trace("Object omitted from MessagePack response is: {}", obj);
    }
    return Optional.empty();
  }

  /**
   * Sends a streaming response containing each element from the provided {@link Flux} serialized in
   * JSON.
   *
   * @param response stream the Flux's elements over this {@link HttpServerResponse}
   * @param body {@link Flux} providing elements for the response.
   * @return {@link Publisher} with results of sending the response.
   */
  private Publisher<Void> sendJsonStream(HttpServerResponse response, Flux<?> body) {
    return response
      .header(HttpHeaderNames.CONTENT_TYPE, ContentType.JSON_STREAM_NAME)
      .sendString(body
        .map(this::serializeObjectJson)
        .filter(Optional::isPresent)
        .map(Optional::get)
      );
  }

  /**
   * Serialize the provided object in JSON. Returns an empty optional if the serialization fails for
   * any reason.
   *
   * @param obj object to serialize
   * @return {@link Optional} with the JSON representation of the provided object.
   */
  private Optional<String> serializeObjectJson(Object obj) {
    try {
      // Use a space to separate each element, which is needed to separate primitive numbers
      return Optional.of(definition.getJsonMapper().writeValueAsString(obj) + " ");
    } catch (IOException e) {
      logger.error("Omitting object from JSON streaming response as it could not be serialized", e);
      logger.trace("Object omitted from JSON response is: {}", obj);
    }
    return Optional.empty();
  }

  /**
   * Sends a single response containing the provided body.
   *
   * @param response send the object over this {@link HttpServerResponse}
   * @param sendMsgPack true if the response should be serialized in MessagePack
   * @param body response body
   * @return {@link Publisher} with results of sending the response.
   */
  private Publisher<Void> sendResponse(
    HttpServerResponse response, boolean sendMsgPack, Object body) {

    return sendMsgPack
      ? sendMsgPackResponse(response, body)
      : sendJsonResponse(response, body);
  }

  /**
   * Sends a response containing the provided body serialized as MessagePack.
   *
   * @param response send the body over this {@link HttpServerResponse}
   * @param body object containing the non-serialized response body.
   * @return {@link Publisher} with results of sending the response.
   */
  private Publisher<Void> sendMsgPackResponse(HttpServerResponse response, Object body) {
    final ObjectMapper msgpackMapper = definition.getMsgpackMapper();
    try {
      final byte[] data = msgpackMapper.writeValueAsBytes(body);
      return response
        .header(HttpHeaderNames.CONTENT_TYPE, ContentType.MSGPACK_NAME)
        .header(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(data.length))
        .sendObject(Flux.just(ByteBufAllocator.DEFAULT.buffer().writeBytes(data)))
        .then();
    } catch (JsonProcessingException e) {
      return response
        .sendObject(Flux.just("Error sending MsgPack response", e.getMessage()))
        .then();
    }
  }

  /**
   * Sends a response containing the provided body serialized as JSON.
   *
   * @param response send the body over this {@link HttpServerResponse}
   * @param body object containing the non-serialized response body.
   * @return {@link Publisher} with results of sending the response.
   */
  private Publisher<Void> sendJsonResponse(HttpServerResponse response, Object body) {
    final ObjectMapper jsonMapper = definition.getJsonMapper();
    try {
      final var data = jsonMapper.writeValueAsString(body);
      return response
        .header(HttpHeaderNames.CONTENT_TYPE, ContentType.JSON_NAME)
        .header(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(data.length()))
        .sendString(Flux.just(data))
        .then();
    } catch (JsonProcessingException e) {
      return response
        .sendObject(Flux.just("Error sending MsgPack response", e.getMessage()))
        .then();
    }
  }

  /**
   * Convenience function for calling a request handler on a request safely, returning either the
   * Response from the handler or a server error if the handler throws an exception
   *
   * @param handler the request handler
   * @param request the request
   * @param deserializer the deserializer
   * @return a {@link Response}; if the handler runs without throwing an exception this is just the
   * result of handler.handle(request, deserializer)... if it throws an exception, a server error
   * Response is returned.
   */
  private static Response<?> invokeHandler(RequestHandler<?> handler, Request request,
    ObjectMapper deserializer) {
    try {
      return handler.handle(request, deserializer);
    } catch (Exception ex) {
      logger.error("Route handler threw exception", ex);
      return Response.serverError(ex.getMessage());
    }
  }
}
