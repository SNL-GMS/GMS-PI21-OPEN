package gms.shared.featureprediction.plugin.correction.elevation;

import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponentType;
import gms.shared.featureprediction.plugin.api.correction.elevation.mediumvelocity.MediumVelocityEarthModelPlugin;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.stationdefinition.coi.channel.Location;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ElevationCorrectorTest {

  private AutoCloseable closeable;

  @Mock
  private PluginRegistry mockPluginRegistry;

  @Mock
  private ElevationCorrectorConfiguration mockElevationCorrectorConfiguration;

  @InjectMocks
  private ElevationCorrector elevationCorrector;

  @Mock
  private ElevationCorrectorDefinition mockElevationCorrectorDefinition;

  @Mock
  private MediumVelocityEarthModelPlugin mockMediumVelocityEarthModelPlugin;

  @BeforeEach
  void startTest() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach()
  void afterTest() throws Exception {
    closeable.close();
  }

  @Test
  void testCorrect() {

    Mockito.when(mockElevationCorrectorDefinition.getPluginNameForEarthModel("Ak135"))
      .thenReturn("MyBeautifulPlugin");

    Mockito.when(mockElevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition())
      .thenReturn(mockElevationCorrectorDefinition);

    Mockito.when(mockMediumVelocityEarthModelPlugin.getValue(any(), any()))
      .thenReturn(4.0);

    Mockito.when(mockPluginRegistry.getPlugin("MyBeautifulPlugin", MediumVelocityEarthModelPlugin.class))
      .thenReturn(Optional.of(mockMediumVelocityEarthModelPlugin));

    var result = elevationCorrector.correct(
      "Ak135",
      Location.from(
        0.0, 0.0, 0.0, 1.0
      ),
      0.0,
      PhaseType.P
    ).orElse(null);

    Assertions.assertEquals(
      FeaturePredictionComponent.from(
        DurationValue.from(
          Duration.ofMillis(250),
          null
        ),
        false,
        FeaturePredictionComponentType.ELEVATION_CORRECTION
      ),
      result
    );
  }

  @Test
  void testInvalidPhaseReturnsEmpty() {

    var result = elevationCorrector.correct(
      "Ak135",
      Location.from(
        0.0, 0.0, 0.0, 1.0
      ),
      0.0,
      PhaseType.Lg
    );

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testPluginFromDefinitionNotFound() {
    Mockito.when(mockElevationCorrectorDefinition.getPluginNameForEarthModel("Ak135"))
      .thenReturn("MyBeautifulPlugin");

    Mockito.when(mockElevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition())
      .thenReturn(mockElevationCorrectorDefinition);

    Mockito.when(mockPluginRegistry.getPlugin("MyBeautifulPlugin", MediumVelocityEarthModelPlugin.class))
      .thenReturn(Optional.empty());

    var location = Location.from(
      0.0, 0.0, 0.0, 1.0
    );

    var result = elevationCorrector.correct(
      "Ak135",
      location,
      0.0,
      PhaseType.P
    );

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testNoPluginInDefinition() {
    Mockito.when(mockElevationCorrectorDefinition.getPluginNameForEarthModel("Ak135"))
      .thenReturn(null);

    Mockito.when(mockElevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition())
      .thenReturn(mockElevationCorrectorDefinition);

    var location = Location.from(
      0.0, 0.0, 0.0, 1.0
    );

    var result = elevationCorrector.correct(
      "Ak135",
      location,
      0.0,
      PhaseType.P
    );

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testInit() {
    Mockito.when(mockElevationCorrectorDefinition.getMediumVelocityEarthModelPluginNameByModelNameMap())
      .thenReturn(Map.of("Ak135", "MyBeautifulPlugin"));

    Mockito.when(mockElevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition())
      .thenReturn(mockElevationCorrectorDefinition);

    Mockito.when(mockPluginRegistry.getPlugin("MyBeautifulPlugin", MediumVelocityEarthModelPlugin.class))
      .thenReturn(Optional.of(mockMediumVelocityEarthModelPlugin));

    elevationCorrector.init();

    Mockito.verify(mockMediumVelocityEarthModelPlugin).initialize();
  }
}
