package gms.shared.spring.utilities.framework;

import gms.shared.spring.utilities.aspect.Timing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RetryService {

  private final RestTemplate restTemplate;
  private final RetryTemplate retryTemplate;

  @Autowired
  public RetryService(RestTemplate restTemplate, RetryTemplate retryTemplate) {
    this.restTemplate = restTemplate;
    this.retryTemplate = retryTemplate;
  }

  public static RetryService create() {
    return new RetryService(new RestTemplate(), new RetryTemplate());
  }

  @Timing
  public <T> T retry(String url, HttpMethod method, HttpEntity<?> requestEntity,
    ParameterizedTypeReference<T> responseType) {
    ResponseEntity<T> responseEntity =
      retryTemplate.execute(arg0 ->
        restTemplate.exchange(url, method, requestEntity, responseType)
      );
    return responseEntity.getBody();
  }
}
