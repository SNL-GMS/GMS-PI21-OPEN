package gms.shared.frameworks.configuration.repository.client;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.ConfigurationTestFixtures;
import static gms.shared.frameworks.configuration.repository.ConfigurationTestFixtures.configurationKey;
import static gms.shared.frameworks.configuration.repository.ConfigurationTestFixtures.filterDescConfig;
import gms.shared.frameworks.configuration.repository.FooParameters;
import gms.shared.frameworks.configuration.repository.TestUtilities;

@ExtendWith(MockitoExtension.class)
class ConfigurationConsumerUtilityTests {

  private static final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);
  private static final Duration defaultSelectorCacheExpiration = Duration.ofDays(1);

  @Mock
  private ConfigurationRepository configurationRepository;

  /**
   * Mocks configurationRepository to return an List of Configurations containing
   * configurationSnrIs5 when queried with configurationKey.  Does not return any configurations for
   * the global configuration key prefix.
   */
  private void mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults() {
    Mockito.when(configurationRepository.getKeyRange(Mockito.anyString()))
      .thenAnswer(invocation -> {
        Object argument = invocation.getArguments()[0];
        if (argument.equals(configurationKey) || "unknown-key".equals(argument)) {
          return List.of(ConfigurationTestFixtures.configurationSnrIs5);
        } else if (argument
          .equals(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)) {
          return List.of();
        }
        throw new InvalidUseOfMatchersException(
          String.format("Argument %s does not match", argument)
        );
      });
  }

  /**
   * Mocks configurationRepository to return a List of Configurations containing
   * global reference configurations for the global configuration key prefix.
   */
  private void mockGmsConfigurationToReturnGlobalReferences() {
    Mockito.when(configurationRepository.getKeyRange(Mockito.anyString()))
      .thenAnswer(invocation -> {
        Object argument = invocation.getArguments()[0];
        if (argument.toString().contains(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)) {
          return List.of(ConfigurationTestFixtures.filterDefNestedConfig, filterDescConfig);
        } else if (argument.equals(configurationKey) || "unknown-key".equals(argument)) {
          return List.of();
        }
        throw new InvalidUseOfMatchersException(
          String.format("Argument %s does not match", argument)
        );
      });
  }

  private ConfigurationConsumerUtility getClientUtilMockGmsConfig() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();
    return ConfigurationConsumerUtility.builder(configurationRepository)
      .retryConfiguration(retryConfig)
      .configurationNamePrefixes(List.of(configurationKey)).build();
  }

  private ConfigurationConsumerUtility getClientUtilOverrideExpiration(Duration expiration) {
    return ConfigurationConsumerUtility.builder(configurationRepository)
      .selectorCacheExpiration(expiration)
      .retryConfiguration(retryConfig)
      .configurationNamePrefixes(List.of(configurationKey)).build();
  }

  /**
   * Mocks configurationRepository to return an empty List when queried with configurationKey
   */
  private void mockGmsConfigurationToReturnEmptyListNoGlobalDefaults() {
    Mockito.when(configurationRepository.getKeyRange(Mockito.anyString()))
      .thenAnswer(invocation -> {
        Object argument = invocation.getArguments()[0];
        if (argument.equals(configurationKey)) {
          return List.of();
        } else if (argument
          .equals(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)) {
          return List.of();
        }
        throw new InvalidUseOfMatchersException(
          String.format("Argument %s does not match", argument)
        );
      });
  }

  @Test
  void testBuildNoKeys() {
    final ConfigurationConsumerUtility clientUtility =
      ConfigurationConsumerUtility.builder(configurationRepository)
        .retryConfiguration(retryConfig).build();

    // When no keys are provided only the globally reference configuration should be loaded
    verify(configurationRepository, Mockito.times(1)).getKeyRange(Mockito.anyString());
    verify(configurationRepository, Mockito.times(1))
      .getKeyRange(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX);

    // TODO: verify watches are setup

    assertNotNull(clientUtility);
  }

  @Test
  void testBuildNullConfigurationRepositoryExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
      () -> ConfigurationConsumerUtility.builder(null),
      NullPointerException.class, "Requires non-null ConfigurationRepository");
  }

  @Test
  void testBuildLoadsKeys() {
    final ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();

    verify(configurationRepository, Mockito.times(1)).getKeyRange(configurationKey);

    // TODO: verify watches are setup
    assertNotNull(clientUtility);
  }

  @Test
  void testBuildWithOrWithoutExpiration() throws NoSuchFieldException, IllegalAccessException {
    ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();
    Duration selectorCacheExpiration = clientUtility.getSelectorCacheExpiration();
    assertEquals(defaultSelectorCacheExpiration, selectorCacheExpiration);

    Duration expirationOverride = Duration.ofSeconds(1);
    ConfigurationConsumerUtility clientUtilityExpirationOverride = getClientUtilOverrideExpiration(
      expirationOverride);
    selectorCacheExpiration = clientUtilityExpirationOverride.getSelectorCacheExpiration();
    assertEquals(expirationOverride, selectorCacheExpiration);
  }

  @Test
  void testBuildNullConfigurationKeysExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
      () -> ConfigurationConsumerUtility.builder(configurationRepository)
        .configurationNamePrefixes(null),
      NullPointerException.class, "Requires non-null configurationNamePrefixes"
    );
  }

  @Test
  void testBuildLoadsGlobalDefaults() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    ConfigurationConsumerUtility.builder(configurationRepository)
      .configurationNamePrefixes(List.of(configurationKey))
      .retryConfiguration(retryConfig)
      .build();

    verify(configurationRepository, Mockito.times(1))
      .getKeyRange(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX);
  }

  @Test
  void testToBuilder() {
    var ccu = ConfigurationConsumerUtility.builder(configurationRepository)
      .configurationNamePrefixes(List.of(configurationKey))
      .retryConfiguration(retryConfig)
      .build();

    var ccuBuilder = ccu.toBuilder();

    assertAll(
      () -> assertEquals(1, ccuBuilder.getConfigurationNamePrefixes().size()),
      () -> assertTrue(ccuBuilder.getConfigurationNamePrefixes().contains(configurationKey)),
      () -> assertEquals(configurationRepository, ccuBuilder.getConfigurationRepository()),
      () -> assertEquals(retryConfig, ccuBuilder.getRetryConfiguration()));
  }

  @Test
  void testGlobalDefaultsCanBeEmpty() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    Assertions.assertDoesNotThrow(() ->
      ConfigurationConsumerUtility.builder(configurationRepository)
        .configurationNamePrefixes(List.of(configurationKey))
        .retryConfiguration(retryConfig)
        .build());
  }

  @Test
  void testLoadConfigurations() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    final ConfigurationConsumerUtility clientUtility = ConfigurationConsumerUtility
      .builder(configurationRepository)
      .retryConfiguration(retryConfig)
      .build();
    assertNotNull(clientUtility);

    verify(configurationRepository, Mockito.times(0)).getKeyRange(configurationKey);

    clientUtility.loadConfigurations(List.of(configurationKey));
    verify(configurationRepository, Mockito.times(1)).getKeyRange(configurationKey);
    Mockito.verifyNoMoreInteractions(configurationRepository);
  }

  @Test
  void testLoadConfigurationGlobalReferences() {
    mockGmsConfigurationToReturnGlobalReferences();

    final ConfigurationConsumerUtility clientUtility = ConfigurationConsumerUtility
      .builder(configurationRepository)
      .retryConfiguration(retryConfig)
      .build();
    assertNotNull(clientUtility);

    verify(configurationRepository, Mockito.times(0)).getKeyRange(configurationKey);

    Mockito.verifyNoMoreInteractions(configurationRepository);
  }

  @Test
  void testLoadConfigurationsDoesNotReloadExistingConfigurations() {
    final ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();
    clientUtility.loadConfigurations(List.of(configurationKey));
    Mockito.verifyNoMoreInteractions(configurationRepository);
  }

  @Test
  void testLoadConfigurationsValidatesParameters() {
    final ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();

    TestUtilities.expectExceptionAndMessage(
      () -> clientUtility.loadConfigurations(null),
      NullPointerException.class,
      "Requires non-null configurationNamePrefixes"
    );
  }

  private static class NamedInt {

    @JsonProperty
    private int named;

    @JsonCreator
    private NamedInt(
      @JsonProperty("named") int named) {
      this.named = named;
    }
  }

  @Test
  void testResolveToFieldMap() {
    final Map<String, Object> resolvedParamsFieldMap = getClientUtilMockGmsConfig()
      .resolve(configurationKey, List.of(Selector.from("snr", -5.0)));

    assertAll(
      () -> assertNotNull(resolvedParamsFieldMap),
      () -> assertEquals(ConfigurationTestFixtures.fooParamsDefaultsMap, resolvedParamsFieldMap)
    );
  }

  @Test
  void testResolveUnknownConfigurationKeyExpectIllegalArgumentException() {
    final String unknownKey = "unknown-key";

    TestUtilities.expectExceptionAndMessage(
      () -> getClientUtilMockGmsConfig().resolve(unknownKey, List.of(), Number.class),
      IllegalArgumentException.class, "No Configuration named " + unknownKey + " is in this"
    );
  }

  @Test
  void testResolveToObjectFromClass() {
    final FooParameters resolvedParams = getClientUtilMockGmsConfig()
      .resolve(configurationKey, List.of(Selector.from("snr", -5.0)), FooParameters.class);

    assertAll(
      () -> assertNotNull(resolvedParams),
      () -> assertEquals(ConfigurationTestFixtures.fooParamsDefaults, resolvedParams)
    );
  }

  @Test
  void testParameterClassNotCreatableExpectIllegalArgumentException() {
    TestUtilities.expectExceptionAndMessage(
      () -> getClientUtilMockGmsConfig().resolve(configurationKey, List.of(), Number.class),
      IllegalArgumentException.class,
      "Resolved Configuration is not a valid instance of " + Number.class.getCanonicalName()
    );
  }

  @Test
  void testResolveNullConfigurationNameExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
      () -> getClientUtilMockGmsConfig().resolve(null, List.of()),
      NullPointerException.class,
      "Cannot resolve Configuration for null configurationName"
    );
  }

  @Test
  void testResolveNullSelectorsExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
      () -> getClientUtilMockGmsConfig().resolve(configurationKey, null),
      NullPointerException.class,
      "Cannot resolve Configuration for null selectors"
    );
  }

  @Test
  void testResolveNullParametersClassExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
      () -> getClientUtilMockGmsConfig().resolve(configurationKey, List.of(), null),
      NullPointerException.class,
      "Cannot resolve Configuration to null parametersClass"
    );
  }

  @Test
  void testRetryWindow() {
    RetryConfig retryConfig = RetryConfig.create(1, 2, ChronoUnit.MILLIS, 10);

    ConfigurationRepository mockRepo = Mockito.mock(ConfigurationRepository.class);

    when(mockRepo.getKeyRange(configurationKey)).thenAnswer(new Answer() {
      private int count = 1;

      public Object answer(InvocationOnMock invocation) {
        if (count < 5) {
          count++;
          return List.of();
        }
        return List.of(ConfigurationTestFixtures.configurationSnrIs5);
      }
    });

    ConfigurationConsumerUtility util = ConfigurationConsumerUtility.builder(mockRepo)
      .retryConfiguration(retryConfig)
      .configurationNamePrefixes(List.of(configurationKey)).build();

    FooParameters resolvedParams = util.resolve(configurationKey, List.of(Selector.from("snr", -5.0)), FooParameters.class);

    Mockito.verify(mockRepo, times(5)).getKeyRange(configurationKey);
    assertEquals(ConfigurationTestFixtures.fooParamsDefaults, resolvedParams);
  }

  @Test
  void testRetryWindowFail() {
    var maxAttempts = 10;
    RetryConfig retryConfig = RetryConfig.create(1, 2, ChronoUnit.MILLIS, maxAttempts);

    ConfigurationRepository mockRepo = Mockito.mock(ConfigurationRepository.class);

    when(mockRepo.getKeyRange(configurationKey)).thenReturn(Collections.emptyList());

    ConfigurationConsumerUtility util = ConfigurationConsumerUtility.builder(mockRepo)
      .retryConfiguration(retryConfig)
      .configurationNamePrefixes(List.of(configurationKey)).build();

    List<Selector> selectors = List.of(Selector.from("snr", -5.0));

    final Throwable actualThrowable = assertThrows(IllegalArgumentException.class, () -> util.resolve(configurationKey, selectors, FooParameters.class));
    assertTrue(actualThrowable.getMessage().contains("No Configuration named [component-name]-configuration is in this ConfigurationConsumerUtility"));

    Mockito.verify(mockRepo, times(maxAttempts + 1)).getKeyRange(configurationKey);
  }
}
