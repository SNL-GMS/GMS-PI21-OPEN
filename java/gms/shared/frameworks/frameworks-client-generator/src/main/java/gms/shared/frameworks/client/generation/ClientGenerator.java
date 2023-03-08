package gms.shared.frameworks.client.generation;

import com.google.common.annotations.VisibleForTesting;
import gms.shared.frameworks.client.ServiceClientJdkHttp;
import gms.shared.frameworks.client.ServiceRequest;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.AnnotationUtils;
import gms.shared.frameworks.utilities.PathMethod;
import gms.shared.frameworks.utilities.ServiceReflectionUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates client implementations that use HTTP given an interface.
 */
public class ClientGenerator {

  private static final Logger logger = LoggerFactory.getLogger(ClientGenerator.class);

  private ClientGenerator() {
  }

  /**
   * Creates a proxy instantiation of the given client interface that is implemented using HTTP.
   *
   * @param <T> the type of the interface, same as the return type
   * @param clientClass the class of the interface
   * @param systemConfig System Configuration used to retrieve the target url
   * @return an instance of the client interface
   * @throws NullPointerException if clientClass is null
   * @throws IllegalArgumentException if the clientClass doesn't have @Component
   */
  public static <T> T createClient(Class<T> clientClass, SystemConfig systemConfig) {
    var sendRetryConfig = RetryConfig.create(
      systemConfig.getValueAsLong("service-client-send-retry-initial-delay"),
      systemConfig.getValueAsLong("service-client-send-retry-max-delay"),
      ChronoUnit.valueOf(systemConfig.getValue("service-client-send-retry-delay-units").toUpperCase()),
      systemConfig.getValueAsInt("service-client-send-retry-max-attempts"));

    var upgradeRetryConfig = RetryConfig.create(
      systemConfig.getValueAsLong("service-client-upgrade-retry-initial-delay"),
      systemConfig.getValueAsLong("service-client-upgrade-retry-max-delay"),
      ChronoUnit.valueOf(systemConfig.getValue("service-client-upgrade-retry-delay-units")),
      systemConfig.getValueAsInt("service-client-upgrade-retry-max-attempts"));

    return createClient(clientClass, systemConfig, ServiceClientJdkHttp.create(sendRetryConfig, upgradeRetryConfig));
  }

  /**
   * Creates a proxy instantiation of the given client interface that is implemented using the given
   * HTTP client.
   *
   * @param <T> the type of the interface, same as the return type
   * @param clientClass the class of the interface
   * @param sysConfig the system configuration client, used to lookup connection info (hostname,
   * port, etc.) and configuration for HTTP client
   * @param httpClient the HTTP client to use
   * @return an instance of the client interface
   * @throws NullPointerException if clientClass or httpClient is null
   * @throws IllegalArgumentException if the clientClass doesn't have @Component
   */
  @VisibleForTesting
  static <T> T createClient(Class<T> clientClass, SystemConfig sysConfig, ServiceClientJdkHttp httpClient) {
    Objects.requireNonNull(clientClass, "Cannot create client from null class");
    Objects.requireNonNull(httpClient, "Cannot create client from null httpClient");

    var url = sysConfig.getUrlOfComponent(AnnotationUtils.getComponentName(clientClass));
    var timeout = sysConfig.getValueAsDuration(SystemConfig.CLIENT_TIMEOUT);
    Map<Method, PathMethod> pathMethods = pathMethodsByMethod(clientClass);
    return clientClass.cast(Proxy.newProxyInstance(ClientGenerator.class.getClassLoader(), new Class[]{clientClass},
      handler(httpClient, url, timeout, pathMethods)));
  }

  private static <T> Map<Method, PathMethod> pathMethodsByMethod(Class<T> clientClass) {
    return ServiceReflectionUtilities.findPathAnnotatedMethodsOnlyOrThrow(clientClass)
      .stream().collect(Collectors.toMap(PathMethod::getMethod, Function.identity()));
  }

  private static InvocationHandler handler(ServiceClientJdkHttp client,
    URL url, Duration timeout, Map<Method, PathMethod> pathMethods) {
    return (proxyObj, method, args) -> sendRequest(
      client, args[0], url, timeout, pathMethods.get(method));
  }

  private static Object sendRequest(ServiceClientJdkHttp httpClient,
    Object requestBody, URL url, Duration timeout, PathMethod pathMethod)
    throws MalformedURLException {
    var method = pathMethod.getMethod();
    appendToUrl(url, pathMethod.getRelativePath());
    // if the method returns void, deserialize as string (not void, which doesn't work)
    final Type responseType = ServiceReflectionUtilities.methodReturnsVoid(method)
      ? String.class : method.getGenericReturnType();
    var requestUrl = appendToUrl(url, pathMethod.getRelativePath());

    logger.info("Sending request to {}", requestUrl);
    logger.info("Body: {}", requestBody);
    return httpClient.send(ServiceRequest.from(
      appendToUrl(url, pathMethod.getRelativePath()), requestBody,
      timeout, responseType,
      pathMethod.getInputFormat(), pathMethod.getOutputFormat()));
  }

  private static URL appendToUrl(URL url, String path) throws MalformedURLException {
    return new URL(url.getProtocol(), url.getHost(), url.getPort(),
      url.getFile() + path, null);
  }
}
