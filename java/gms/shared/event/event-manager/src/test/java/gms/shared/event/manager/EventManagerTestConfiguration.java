package gms.shared.event.manager;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@TestConfiguration
class EventManagerTestConfiguration implements WebMvcConfigurer {

  @Bean
  ConfigurationConsumerUtility configurationConsumerUtility() {
    var configurationRoot = checkNotNull(
      Thread.currentThread().getContextClassLoader().getResource("EventManagerTest-configuration-base")
    ).getPath();

    return ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(ObjectMapperFactory.getJsonObjectMapper());
    converters.add(converter);
  }
}
