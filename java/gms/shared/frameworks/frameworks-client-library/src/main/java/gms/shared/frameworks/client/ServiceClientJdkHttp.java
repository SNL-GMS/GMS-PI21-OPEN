package gms.shared.frameworks.client;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.common.GmsCommonRoutes;
import gms.shared.frameworks.common.HttpStatus;
import gms.shared.frameworks.configuration.RetryConfig;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * HTTP client abstraction using the built-in JDK HttpClient.
 */
public class ServiceClientJdkHttp {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceClientJdkHttp.class);

  private final HttpClient httpClient;

  private final RetryConfig sendRetryConfig;

  private final RetryPolicy<HttpResponse<String>> upgradeRetryPolicy;
  private boolean upgradedToHTTP2 = false;

  private ServiceClientJdkHttp(HttpClient client, RetryConfig sendRetryConfig, RetryConfig upgradeRetryConfig) {
    this.httpClient = Objects.requireNonNull(client);
    this.sendRetryConfig = sendRetryConfig;
    this.upgradeRetryPolicy = upgradeRetryConfig.<HttpResponse<String>>toBaseRetryPolicy()
      .handle(List.of(ConnectException.class, HttpConnectTimeoutException.class, HttpTimeoutException.class));
  }

  /**
   * Create a client.
   *
   * @return a ServiceClientJdkHttp
   */
  public static ServiceClientJdkHttp create(RetryConfig sendRetryConfig, RetryConfig upgradeRetryConfig) {
    return create(HttpClient.newHttpClient(), sendRetryConfig, upgradeRetryConfig);
  }

  /**
   * Create a client that uses the provided JDK HttpClient.
   *
   * @param client the JDK HttpClient to use for communications
   * @return a ServiceClientJdkHttp
   */
  public static ServiceClientJdkHttp create(HttpClient client, RetryConfig sendRetryConfig,
    RetryConfig upgradeRetryConfig) {
    return new ServiceClientJdkHttp(client, sendRetryConfig, upgradeRetryConfig);
  }

  /**
   * Send a request and gets a response, with a retry policy.
   *
   * @param request the request to send
   * @param <T> type param of the expected response
   * @return a ResponseType
   * @throws ConnectionFailed with wrapped exception when the connection fails (e.g.
   * hostname not found)
   * @throws InternalServerError with body of response from server if server responds it had
   * internal error
   * @throws BadRequest with body of response from server if server rejects the client
   * request
   * @throws IllegalArgumentException if the url in the request is invalid, the requestFormat or
   * responseFormat's in the request are unsupported, or request
   * serialization fails.
   * @throws IllegalStateException if response deserialization fails
   */
  public <T> T send(ServiceRequest request) {
    if (!upgradedToHTTP2) {
      upgradedToHTTP2 = requestSucceeded(throwIfErrorResponse(performHTTP2Upgrade(request)));
    }
    final RetryPolicy<T> retryPolicy = sendRetryConfig.<T>toBaseRetryPolicy()
      .handle(List.of(ConnectionFailed.class, InternalServerError.class))
      .onFailedAttempt(e -> LOGGER
        .warn("Failed service request to {} with error {}, will try again...", request.getUrl(),
          e));
    return Failsafe.with(retryPolicy).get(() -> sendSingleRequest(request));
  }

  <T, F> T sendSingleRequest(ServiceRequest request) {
    Objects.requireNonNull(request, "Cannot send null request");
    final ResponseContentProtocol<F> responseProtocol
      = ContentProtocols.from(request.getResponseFormat());
    final HttpRequest httpRequest = createHttpRequest(request);
    final HttpResponse<F> httpResponse = throwIfErrorResponse(
      sendHttp(httpRequest, responseProtocol.bodyHandler()));
    return tryDeserialize(httpResponse.body(), responseProtocol, request.getResponseType());
  }

  private HttpRequest createHttpRequest(ServiceRequest request) {
    try {
      return HttpRequest.newBuilder().uri(request.getUrl().toURI())
        .timeout(request.getTimeout())
        .POST(bodyProcessor(request.getBody(), request.getRequestFormat()))
        .header("Content-Type", request.getRequestFormat().toString())
        .header("Accept", request.getResponseFormat().toString())
        .build();
    } catch (Exception ex) {
      throw new IllegalArgumentException("Could not prepare the request", ex);
    }
  }

  private static <F> BodyPublisher bodyProcessor(
    Object body, ContentType requestFormat) {
    final RequestContentProtocol<F> requestProtocol
      = ContentProtocols.from(requestFormat);
    return requestProtocol.bodyEncoder().apply(trySerialize(requestProtocol, body));
  }

  private static <F> F trySerialize(
    RequestContentProtocol<F> requestProtocol, Object data) {
    try {
      return requestProtocol.serialize(data);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Could not serialize request body", ex);
    }
  }

  private <F> HttpResponse<F> sendHttp(HttpRequest request, BodyHandler<F> bodyHandler) {
    try {
      return this.httpClient.send(request, bodyHandler);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ConnectionFailed("Could not connect to " + request.uri() + " due to interrupt.", ex);
    } catch (Exception ex) {
      throw new ConnectionFailed("Could not connect to " + request.uri(), ex);
    }
  }

  private HttpResponse<String> performHTTP2Upgrade(ServiceRequest request) {
    try {
      HttpRequest httpRequest = HttpRequest
        .newBuilder(
          URI.create(String.format("%s://%s/%s%s",
            request.getUrl().getProtocol(),
            request.getUrl().getAuthority(),
            request.getUrl().getHost(),
            GmsCommonRoutes.CONNECTION_UPGRADE_PATH)))
        .GET()
        .timeout(request.getTimeout())
        .build();


      return Failsafe.with(upgradeRetryPolicy
          .onFailedAttempt(e -> LOGGER
            .warn("Failed service request to {} with error {}, will try again...", request.getUrl(),
              e)))
        .get(() -> httpClient.send(httpRequest, BodyHandlers.ofString()));
    } catch (Exception e) {
      throw new ConnectionFailed("Could not upgrade connection to " + request.getUrl().getHost(), e);
    }
  }

  private static <T, F> T tryDeserialize(
    F data,
    ResponseContentProtocol<F> responseProtocol,
    Type type) {
    try {
      return responseProtocol.deserialize(data, type);
    } catch (Exception ex) {
      throw new IllegalStateException("Could not deserialize response body", ex);
    }
  }

  private static <X> HttpResponse<X> throwIfErrorResponse(HttpResponse<X> response) {
    return throwIfInternalServerError(throwIfBadRequest(response));
  }

  private static <X> HttpResponse<X> throwIfBadRequest(
    HttpResponse<X> response) {
    if (isBadRequest(response.statusCode())) {
      throw new BadRequest(getErrorMessage(response));
    }
    return response;
  }

  private static boolean isBadRequest(int status) {
    return HttpStatus.isClientError(status);
  }

  private static <X> HttpResponse<X> throwIfInternalServerError(HttpResponse<X> response) {
    if (isInternalServerError(response.statusCode())) {
      throw new InternalServerError(getErrorMessage(response));
    }
    return response;
  }

  private static boolean isInternalServerError(int status) {
    return HttpStatus.isServerError(status);
  }

  private static String getErrorMessage(HttpResponse<?> response) {
    return Optional.ofNullable(response.body()).map(Object::toString).orElse("");
  }

  private static <F> boolean requestSucceeded(HttpResponse<F> response) {
    return HttpStatus.isSuccess(response.statusCode());
  }

  /**
   * Exception for when the server has an internal error.
   */
  public static class InternalServerError extends RuntimeException {

    InternalServerError(String msg) {
      super(msg);
    }
  }

  /**
   * Exception for when the server rejects the client request.
   */
  public static class BadRequest extends RuntimeException {

    BadRequest(String msg) {
      super(msg);
    }
  }

  /**
   * Exception for when the server cannot be reached because of network issues, an unknown hostname,
   * etc.
   */
  public static class ConnectionFailed extends RuntimeException {

    ConnectionFailed(String msg, Throwable t) {
      super(msg, t);
    }
  }
}