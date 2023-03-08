package gms.shared.spring.utilities.webmvc;

import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class GmsWebMvcConfiguration implements WebMvcConfigurer {

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(jsonMessageConverter());
    converters.add(messagePackMessageConverter());
  }

  public HttpMessageConverter<Object> jsonMessageConverter() {
    return new AbstractJackson2HttpMessageConverter(
      CoiObjectMapperFactory.getJsonObjectMapper(),
      MediaType.APPLICATION_JSON) {
    };
  }

  public HttpMessageConverter<Object> messagePackMessageConverter() {
    return new AbstractJackson2HttpMessageConverter(
      CoiObjectMapperFactory.getMsgpackObjectMapper(),
      new MediaType("application", "msgpack")) {
    };
  }
}
