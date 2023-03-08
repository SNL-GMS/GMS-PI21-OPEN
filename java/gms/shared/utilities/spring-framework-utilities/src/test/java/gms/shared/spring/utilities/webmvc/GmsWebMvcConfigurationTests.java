package gms.shared.spring.utilities.webmvc;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GmsWebMvcConfigurationTests {

  @Test
  void jsonMessageConverter() {
    var converter = new GmsWebMvcConfiguration().jsonMessageConverter();
    assertTrue(converter.canRead(String.class, MediaType.APPLICATION_JSON));
  }

  @Test
  void messagePackMessageConverter() {
    var converter = new GmsWebMvcConfiguration().messagePackMessageConverter();
    assertTrue(converter.canRead(String.class, new MediaType("application", "msgpack")));
  }
}