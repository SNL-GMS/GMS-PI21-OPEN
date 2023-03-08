package gms.shared.featureprediction.plugin.prediction;

import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.ElevationCorrectionDefinition;
import gms.shared.event.coi.featureprediction.EllipticityCorrectionDefinition;
import gms.shared.event.coi.featureprediction.EllipticityCorrectionType;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponentType;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.featureprediction.plugin.api.correction.ellipticity.EllipticityCorrectorPlugin;
import gms.shared.featureprediction.plugin.api.lookuptable.TravelTimeDepthDistanceLookupTablePlugin;
import gms.shared.featureprediction.plugin.correction.elevation.ElevationCorrector;
import gms.shared.featureprediction.utilities.view.Immutable2dArray;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.stationdefinition.coi.channel.Location;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class BicubicSplineFeaturePredictorTests {

  private AutoCloseable openMocks;

  @Mock
  private BicubicSplineFeaturePredictorConfiguration mockConfiguration;

  @Mock
  private ElevationCorrector mockElevationCorrector;

  @Mock
  private PluginRegistry mockPluginRegistry;

  @InjectMocks
  private BicubicSplineFeaturePredictor bicubicSplineFeaturePredictor;

  @Mock
  private BicubicSplineFeaturePredictorDefinition mockDefinition;

  @Mock
  private TravelTimeDepthDistanceLookupTablePlugin mockTravelTimePlugin;

  @Mock
  private EllipticityCorrectorPlugin mockEllipticityCorrectorPlugin;

  @BeforeEach
  void initTest() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void endTest() throws Exception {
    openMocks.close();
  }

  @Test
  void testInit() {

    Mockito.when(mockDefinition.getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel())
      .thenReturn(Map.of("Ak135", "MyFunnyTablePlugin"));

    Mockito.when(mockDefinition.getEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType())
      .thenReturn(Map.of(EllipticityCorrectionType.DZIEWONSKI_GILBERT, "MyAmazingCorrector"));

    Mockito.when(mockConfiguration.getCurrentBicubicSplineFeaturePredictorDefinition())
      .thenReturn(mockDefinition);

    Mockito.when(mockPluginRegistry.getPlugin("MyFunnyTablePlugin", TravelTimeDepthDistanceLookupTablePlugin.class))
      .thenReturn(Optional.of(mockTravelTimePlugin));

    Mockito.when(mockPluginRegistry.getPlugin("MyAmazingCorrector", EllipticityCorrectorPlugin.class))
      .thenReturn(Optional.of(mockEllipticityCorrectorPlugin));

    bicubicSplineFeaturePredictor.initialize();

    Mockito.verify(mockTravelTimePlugin).initialize();

  }

  @Test
  void testPredict() {

    Mockito.when(mockDefinition.getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel())
      .thenReturn(Map.of("Ak135", "MyFunnyTablePlugin"));

    Mockito.when(mockDefinition.getEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType())
      .thenReturn(Map.of(EllipticityCorrectionType.DZIEWONSKI_GILBERT, "MyAmazingCorrector"));

    Mockito.when(mockConfiguration.getCurrentBicubicSplineFeaturePredictorDefinition())
      .thenReturn(mockDefinition);

    Mockito.when(mockElevationCorrector.correct(eq("Ak135"), any(), anyDouble(), any()))
      .thenReturn(Optional.of(FeaturePredictionComponent.from(
        DurationValue.from(
          Duration.ofSeconds(1),
          null
        ),
        false,
        FeaturePredictionComponentType.ELEVATION_CORRECTION
      )));
    //
    // With the below death, distance, and travel time values for the table, the predicted travel time should
    // be 3.
    //
    Mockito.when(mockTravelTimePlugin.getDepthsKmForData(PhaseType.P)).thenReturn(
      ImmutableDoubleArray.copyOf(new double[]{1, 2, 3, 4, 5})
    );
    Mockito.when(mockTravelTimePlugin.getDistancesDegForData(PhaseType.P)).thenReturn(
      ImmutableDoubleArray.copyOf(new double[]{1, 2, 3, 4, 5})
    );
    Mockito.when(mockTravelTimePlugin.getValues(PhaseType.P)).thenReturn(Immutable2dArray.from(Duration.class, new Duration[][]{
      {Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(4), Duration.ofSeconds(5)},
      {Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(4), Duration.ofSeconds(5)},
      {Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(4), Duration.ofSeconds(5)},
      {Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(4), Duration.ofSeconds(5)},
      {Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(3), Duration.ofSeconds(4), Duration.ofSeconds(5)}
    }));

    //Output should have no ellipticty correction since this returns empty.
    Mockito.when(mockEllipticityCorrectorPlugin.correct(eq("Ak135"), any(), any(), eq(PhaseType.P)))
      .thenReturn(Optional.<FeaturePredictionComponent<DurationValue>>empty());

    Mockito.when(mockPluginRegistry.getPlugin("MyFunnyTablePlugin", TravelTimeDepthDistanceLookupTablePlugin.class))
      .thenReturn(Optional.of(mockTravelTimePlugin));

    Mockito.when(mockPluginRegistry.getPlugin("MyAmazingCorrector", EllipticityCorrectorPlugin.class))
      .thenReturn(Optional.of(mockEllipticityCorrectorPlugin));

    var sourceLocation = EventLocation.from(3, 0, 3, Instant.EPOCH);
    var receiverLocation = Location.from(0, 0, 0, 0);
    var actual = bicubicSplineFeaturePredictor.predict(
      FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE,
      sourceLocation,
      receiverLocation,
      PhaseType.P,
      "Ak135",
      List.of(
        ElevationCorrectionDefinition.from("Ak135"),
        EllipticityCorrectionDefinition.from(EllipticityCorrectionType.DZIEWONSKI_GILBERT),

        // Exercise the "unrecognized definition" branch of logic, which
        // will return an empty correction, thus should not add to the output
        // list of corrections.
        // Note, may need to change this if SOURCE_DEPENDENT_CORRECTION ever
        // gets implemented.
        (FeaturePredictionCorrectionDefinition) () ->
          FeaturePredictionComponentType.SOURCE_DEPENDENT_CORRECTION
      )
    );

    System.out.println(actual);

    Assertions.assertEquals(
      FeaturePrediction.<ArrivalTimeFeaturePredictionValue>builder()
        .setPredictionType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
        .setChannel(Optional.empty())
        .setExtrapolated(false)
        .setPhase(PhaseType.P)
        .setPredictionChannelSegment(Optional.empty())
        .setReceiverLocation(receiverLocation)
        .setSourceLocation(sourceLocation)
        .setPredictionValue(ArrivalTimeFeaturePredictionValue.create(
          ArrivalTimeMeasurementValue.from(
            InstantValue.from(
              Instant.EPOCH.plusSeconds(4),
              Duration.ZERO
            ),
            Optional.of(DurationValue.from(
              Duration.ofSeconds(4),
              Duration.ZERO
            ))
          ),
          Map.of(),
          Set.of(
            FeaturePredictionComponent.from(
              DurationValue.from(
                Duration.ofSeconds(1),
                null
              ),
              false,
              FeaturePredictionComponentType.ELEVATION_CORRECTION
            ),
            FeaturePredictionComponent.from(
              DurationValue.from(
                Duration.ofSeconds(3),
                null
              ),
              false,
              FeaturePredictionComponentType.BASELINE_PREDICTION
            )
          )
        )).build(),
      actual
    );

  }

}
