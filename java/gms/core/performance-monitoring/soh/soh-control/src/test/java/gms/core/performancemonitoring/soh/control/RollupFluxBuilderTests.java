package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class RollupFluxBuilderTests {

  private static Stream<Arguments> testSpammer() {

    return IntStream.range(0, 50).mapToObj(
      Arguments::arguments
    );
  }

  @ParameterizedTest
  @MethodSource("testSpammer")
  void testFluxBuilder(int dummy) throws IOException {

    SecureRandom random = new SecureRandom("0xDEADBEEF".getBytes());

    List<AcquiredStationSohExtract> extracts = TestFixture.loadExtracts();

    Set<CapabilitySohRollupDefinition> rollupDefinitions = TestFixture
      .computeCapabilitySohRollupDefinitions(extracts, random);

    Set<StationSohDefinition> stationSohDefinitions = TestFixture
      .computeStationSohDefinitions(extracts, random);

    var cache = new AcquiredSampleTimesByChannel();
    cache.setLatestChannelToEndTime(Map.of());

    RollupFluxBuilder rollupFluxBuilder = new RollupFluxBuilder(
      Set.copyOf(extracts),
      stationSohDefinitions,
      rollupDefinitions,
      cache
    );

    var stationSohFlux1 = rollupFluxBuilder.getStationSohFlux();
    var stationSohFlux2 = rollupFluxBuilder.getStationSohFlux();

    var capabilityFlux1 = rollupFluxBuilder.getCapabilitySohRollupFlux();
    var capabilityFlux2 = rollupFluxBuilder.getCapabilitySohRollupFlux();

    StepVerifier.create(stationSohFlux1)
      .expectNextCount(stationSohDefinitions.size())
      .verifyComplete();

    StepVerifier.create(capabilityFlux1)
      .expectNextCount(rollupDefinitions.size())
      .verifyComplete();

    StepVerifier.create(stationSohFlux2)
      .expectNextCount(stationSohDefinitions.size())
      .verifyComplete();

    StepVerifier.create(capabilityFlux2)
      .expectNextCount(rollupDefinitions.size())
      .verifyComplete();
  }
}
