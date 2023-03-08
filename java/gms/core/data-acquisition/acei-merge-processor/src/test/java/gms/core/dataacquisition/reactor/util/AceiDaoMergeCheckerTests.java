package gms.core.dataacquisition.reactor.util;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClipped46;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock24;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock46;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock46F;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock56;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.badgerClocklock46;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AceiDaoMergeCheckerTests {

  @Mock
  ConfigurationConsumerUtility mockConfig;

  AceiDaoMergeChecker mergeChecker;

  @BeforeEach
  void setUp() {
    mergeChecker = AceiDaoMergeChecker.create(mockConfig);
  }

  @ParameterizedTest
  @MethodSource("testMergeableSource")
  void testMergeable(AcquiredChannelEnvironmentIssueBooleanDao acei1,
    AcquiredChannelEnvironmentIssueBooleanDao acei2,
    boolean expectToleranceCheck, Duration mergeTolerance,
    boolean expectMergeable) {

    if (expectToleranceCheck) {
      Mockito.when(mockConfig.resolve(Mockito.anyString(), Mockito.anyList()))
        .thenReturn(Map.of("merge-tolerance", mergeTolerance.toString()));
    }

    assertEquals(expectMergeable, mergeChecker.canMerge(acei1, acei2));
  }

  private static Stream<Arguments> testMergeableSource() {
    return Stream.of(
      Arguments.arguments(
        aardvarkClocklock24(), aardvarkClocklock46(), true, Duration.ofMillis(500), true),
      Arguments.arguments(
        aardvarkClocklock46(), aardvarkClocklock24(), true, Duration.ofMillis(500), true),
      Arguments.arguments(
        aardvarkClocklock24(), aardvarkClocklock56(), true, Duration.ofMillis(500), false),
      Arguments.arguments(
        aardvarkClocklock24(), aardvarkClocklock56(), true, Duration.ofMillis(1025), true),
      Arguments.arguments(
        aardvarkClocklock24(), aardvarkClipped46(), false, Duration.ZERO, false),
      Arguments.arguments(
        aardvarkClocklock24(), badgerClocklock46(), false, Duration.ZERO, false),
      Arguments.arguments(
        aardvarkClocklock24(), aardvarkClocklock46F(), false, Duration.ZERO, false)
    );
  }
}