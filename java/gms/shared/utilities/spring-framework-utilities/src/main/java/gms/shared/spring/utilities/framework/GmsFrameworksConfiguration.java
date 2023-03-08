package gms.shared.spring.utilities.framework;

import gms.shared.frameworks.client.generation.ClientGenerator;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.EnvironmentSystemConfigRepository;
import gms.shared.frameworks.systemconfig.FileSystemConfigRepository;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.validation.PathValidation;
import gms.shared.frameworks.utilities.Validation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Configuration
public class GmsFrameworksConfiguration {

  private static final char WORD_SEPARATOR = '-';
  public static final String LOCAL_BASEPATH = "/opt/gms";

  @Value("${spring.application.name}")
  private String gmsApplicationName;

  @Value("${spring.processing.configuration-names}")
  private String[] processingConfigurationNames;

  @Value("${service.run-state.system-config.local-path:}")
  private String systemConfigLocalPath;

  @Value("${service.run-state.processing-config.local-path:}")
  private String processingConfigDirLocalPath;

  @Bean("serviceBasedSystemConfig")
  @ConditionalOnProperty(prefix = "service.run-state.system-config", name = "state", havingValue = "service",
    matchIfMissing = true)
  @Primary
  public SystemConfig systemConfigService() {
    return SystemConfig.create(gmsApplicationName);
  }

  @Bean("localSystemConfig")
  @ConditionalOnProperty(prefix = "service.run-state.system-config", name = "state", havingValue = "local")
  public SystemConfig systemConfigLocal() {

    final var cleansedPath = PathValidation.getValidatedPath(
      Validation.cleanseInputString(systemConfigLocalPath),
      LOCAL_BASEPATH);

    var fileSystemConfigRepository = FileSystemConfigRepository.builder()
      .setFilename(cleansedPath.toString())
      .build();

    return SystemConfig.create(gmsApplicationName,
      List.of(EnvironmentSystemConfigRepository.builder().build(),
        fileSystemConfigRepository));
  }

  @Bean("serviceBasedProcessingConfig")
  @ConditionalOnProperty(prefix = "service.run-state.processing-config", name = "state", havingValue = "service",
    matchIfMissing = true)
  public ConfigurationConsumerUtility processingConfigService(@Autowired SystemConfig systemConfig) {

    var retryConfig = RetryConfig.create(
      systemConfig.getValueAsInt("processing-retry-initial-delay"),
      systemConfig.getValueAsInt("processing-retry-max-delay"),
      ChronoUnit.valueOf(systemConfig.getValue("processing-retry-delay-units")),
      systemConfig.getValueAsInt("processing-retry-max-attempts"));

    return ConfigurationConsumerUtility
      .builder(ClientGenerator.createClient(ConfigurationRepository.class, systemConfig))
      .retryConfiguration(retryConfig)
      .selectorCacheExpiration(systemConfig.getValueAsDuration("config-cache-expiration"))
      .configurationNamePrefixes(Arrays.asList(processingConfigurationNames))
      .build();
  }

  @Bean("localProcessingConfig")
  @ConditionalOnProperty(prefix = "service.run-state.processing-config", name = "state", havingValue = "local")
  public ConfigurationConsumerUtility processingConfigLocal() {


    var validatedPath = PathValidation.getValidatedPath(processingConfigDirLocalPath, LOCAL_BASEPATH);
    return ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(
        validatedPath))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
      .setConnectTimeout(Duration.ofMillis(60000))
      .setReadTimeout(Duration.ofMillis(60000))
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .build();
  }

  @Bean
  public RetryTemplate retryTemplate() {
    var retryTemplate = new RetryTemplate();

    var backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(500);
    backOffPolicy.setMultiplier(2);
    retryTemplate.setBackOffPolicy(backOffPolicy);

    var retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(5);
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }

  @Bean
  public OpenAPI openAPI() {
    String gmsApi = gmsApplicationNameParser(gmsApplicationName);

    return new OpenAPI().info(new Info().title(gmsApi)
        .description("Documentation for " + gmsApi).version("v0.0.1"))
      .openapi("3.0.1");
  }

  public static String gmsApplicationNameParser(String gmsApplicationName) {
    var sb = new StringBuilder();

    if (gmsApplicationName != null && !gmsApplicationName.isBlank()) {
      sb.append("GMS ");
      sb.append(Character.toUpperCase(gmsApplicationName.charAt(0)));

      CharacterIterator iterator = new StringCharacterIterator(gmsApplicationName.substring(1));
      for (char i = iterator.current(); i != CharacterIterator.DONE; i = iterator.next()) {
        if (i == WORD_SEPARATOR) {
          sb.append(' ');
          if (iterator.getIndex() < iterator.getEndIndex() - 1) {
            sb.append(Character.toUpperCase(iterator.next()));
          }
        } else {
          sb.append(i);
        }
      }

      sb.append(" API");
      return sb.toString();
    }

    return "GMS Default Service Name API";
  }
}