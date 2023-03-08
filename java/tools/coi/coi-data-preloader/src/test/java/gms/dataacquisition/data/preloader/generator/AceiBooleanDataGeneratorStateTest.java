package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;

class AceiBooleanDataGeneratorStateTest {

  @Test
  void instantiationAndPublicMethodsTest() {

    GenerationSpec generationSpecMock = Mockito.mock(GenerationSpec.class);
    Mockito.when(generationSpecMock.getStartTime())
      .thenReturn(Instant.parse("2021-01-10T12:00:00.00Z"));
    Mockito.when(generationSpecMock.getSampleDuration()).thenReturn(Duration.ofMillis(500L));
    Mockito.when(generationSpecMock.getDuration()).thenReturn(Duration.ofMillis(100L));

    OsdRepositoryInterface osdRepositoryMock = Mockito.mock(OsdRepositoryInterface.class);

    AceiBooleanDataGeneratorState aceiBooleanDataGeneratorState = new AceiBooleanDataGeneratorState(
      generationSpecMock,
      "seed name",
      osdRepositoryMock);

    Assertions.assertNotNull(aceiBooleanDataGeneratorState.getStateSupplier());
    Assertions.assertNotNull(aceiBooleanDataGeneratorState.getGenerator());

  }
}
