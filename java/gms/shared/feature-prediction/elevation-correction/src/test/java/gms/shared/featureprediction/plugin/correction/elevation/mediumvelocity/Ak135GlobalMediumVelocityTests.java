package gms.shared.featureprediction.plugin.correction.elevation.mediumvelocity;

import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.filestore.FileDescriptor;
import gms.shared.utilities.filestore.FileStore;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Ak135GlobalMediumVelocityTests {

  @Mock
  private Ak135GlobalMediumVelocityConfiguration configuration;
  @Mock
  private FileStore fileStore;
  @InjectMocks
  private Ak135GlobalMediumVelocity ak135GlobalMediumVelocity;

  @BeforeEach
  void init() {
    var minIoBucketName = "chumBucket";
    var dataDescriptor = "conchStreet";
    var definition = Ak135GlobalMediumVelocityDefinition.create(dataDescriptor);
    var fileDescriptor = FileDescriptor.create(minIoBucketName, dataDescriptor);
    var phaseTypeToMediumVelocity = new PhaseTypeToMediumVelocity();
    phaseTypeToMediumVelocity.put(PhaseType.P, 1.2);

    when(configuration.minIoBucketName()).thenReturn(minIoBucketName);
    when(configuration.ak135GlobalMediumVelocityDefinition()).thenReturn(definition);
    when(fileStore.findByFileDescriptor(fileDescriptor, PhaseTypeToMediumVelocity.class))
      .thenReturn(phaseTypeToMediumVelocity);

    ak135GlobalMediumVelocity.initialize();
  }

  @Test
  void testGetName() {
    assertEquals("Ak135GlobalMediumVelocity", ak135GlobalMediumVelocity.getName());
  }

  @Test
  void testGetUnits() {
    assertEquals(Units.KILOMETERS_PER_SECOND, ak135GlobalMediumVelocity.getUnits());
  }

  @Test
  void testGetAvailablePhaseTypes() {
    assertEquals(Set.of(PhaseType.P), ak135GlobalMediumVelocity.getAvailablePhaseTypes());
  }

  @Test
  void testGetValue() {
    assertEquals(1.2, ak135GlobalMediumVelocity.getValue(PhaseType.P, null));
  }

  @Test
  void testGetValue_UnsupportedPhase() {
    assertThrows(IllegalArgumentException.class, () -> ak135GlobalMediumVelocity.getValue(PhaseType.I, null));
  }

  @Test
  void testGetStandardDeviation() {
    assertNull(ak135GlobalMediumVelocity.getStandardDeviation(PhaseType.P, null));
  }
}
