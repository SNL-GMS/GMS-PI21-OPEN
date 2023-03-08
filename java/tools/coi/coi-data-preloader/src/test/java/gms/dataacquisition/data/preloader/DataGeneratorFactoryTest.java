package gms.dataacquisition.data.preloader;

import gms.dataacquisition.data.preloader.generator.AceiAnalogDataGenerator;
import gms.dataacquisition.data.preloader.generator.AceiBooleanDataGenerator;
import gms.dataacquisition.data.preloader.generator.CapabilitySohRollupDataGenerator;
import gms.dataacquisition.data.preloader.generator.CoiDataGenerator;
import gms.dataacquisition.data.preloader.generator.RsdfDataGenerator;
import gms.dataacquisition.data.preloader.generator.StationSohDataGenerator;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataGeneratorFactoryTest {

  @Mock
  private final OsdRepositoryInterface mockSohRepository = Mockito
    .mock(OsdRepositoryInterface.class);

  @ParameterizedTest
  @MethodSource("factoryCases")
  void test(GenerationType generationType, Class<CoiDataGenerator<?, ?>> dataGeneratorClass) {
    final var initialConditions = new HashMap<InitialCondition, String>();
    initialConditions.put(InitialCondition.STATION_GROUPS, "");
    final var startTime = Instant.now();
    final var generationSpec = GenerationSpec.builder()
      .setType(generationType)
      .setBatchSize(1)
      .setSampleDuration(Duration.ofSeconds(1))
      .setDuration(Duration.ofSeconds(1))
      .setInitialConditions(initialConditions)
      .setStartTime(startTime)
      .build();

    final CoiDataGenerator<?, ?> result = DataGeneratorFactory
      .getDataGenerator(generationSpec, mockSohRepository);

    assertEquals(dataGeneratorClass, result.getClass());
  }

  private static Stream<Arguments> factoryCases() {
    return Stream.of(
      Arguments.arguments(GenerationType.RAW_STATION_DATA_FRAME, RsdfDataGenerator.class),
      Arguments.arguments(GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN,
        AceiBooleanDataGenerator.class),
      Arguments.arguments(GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_ANALOG,
        AceiAnalogDataGenerator.class),
      Arguments.arguments(GenerationType.CAPABILITY_SOH_ROLLUP,
        CapabilitySohRollupDataGenerator.class),
      Arguments.arguments(GenerationType.STATION_SOH, StationSohDataGenerator.class)
    );
  }

}
