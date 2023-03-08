package gms.shared.frameworks.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NettyRequest implements Request {

  private static final Logger logger = LoggerFactory.getLogger(NettyRequest.class);
  private HttpServerRequest request;
  private byte[] body;

  public NettyRequest(HttpServerRequest request, byte[] body) {
    this.request = request;
    this.body = body;
    logger.debug("Request Headers:{}", request.requestHeaders());
  }

  @Override
  public Optional<String> getPathParam(String name) {
    return Optional.ofNullable(this.request.param(name));
  }

  @Override
  public String getBody() {
    return new String(this.body);
  }

  @Override
  public byte[] getRawBody() {
    return this.body;
  }

  @Override
  public Optional<String> getHeader(String name) {
    return Optional.ofNullable(this.request.requestHeaders().get(name));
  }

  @Override
  public Map<String, String> getHeaders() {
    return this.request.requestHeaders().entries().stream()
      .collect((Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (val1, val2) ->
      {
        var combined = val1.concat(",").concat(val2);
        logger.warn("Duplicate header found with the following keys: [{}] [{}]. Combining into [{}]", val1, val2, combined);
        return combined;
      })));
  }
}
