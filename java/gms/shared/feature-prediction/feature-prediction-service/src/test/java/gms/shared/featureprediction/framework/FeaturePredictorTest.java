package gms.shared.featureprediction.framework;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.LocationRestraint;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.LocationUncertainty;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.featureprediction.configuration.FeaturePredictorConfiguration;
import gms.shared.featureprediction.configuration.FeaturePredictorDefinition;
import gms.shared.featureprediction.configuration.TypeSafePluginByTypeMap;
import gms.shared.featureprediction.plugin.api.FeaturePredictorPlugin;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class FeaturePredictorTest {

  private AutoCloseable openMocks;

  @Mock
  private PluginRegistry mockPluginRegistry;

  @Mock
  private FeaturePredictorConfiguration mockFeaturePredictorConfiguration;

  @InjectMocks
  private FeaturePredictor featurePredictor;

  @Mock
  private FeaturePredictorPlugin mockFeaturePredictorPlugin;

  @Mock
  private FeaturePredictorDefinition mockFeaturePredictorDefinition;

  @BeforeEach
  void initTest() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @Test
  void testInit() {
    Mockito.when(mockFeaturePredictorDefinition.getPluginByPredictionTypeMap()).thenReturn(
      new TypeSafePluginByTypeMap(
        Map.of(
          FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE,
          "not-ignored-for-test"
        ))
    );

    Mockito.when(mockPluginRegistry.getPlugin("not-ignored-for-test", FeaturePredictorPlugin.class))
      .thenReturn(Optional.of(mockFeaturePredictorPlugin));

    Mockito.when(mockFeaturePredictorConfiguration.getCurrentFeaturePredictorDefinition())
      .thenReturn(mockFeaturePredictorDefinition);

    featurePredictor.init();

    Mockito.verify(mockFeaturePredictorPlugin).initialize();
  }

  @Test
  void testEventAndReceiverLocation() {

    var sourceLocation = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);
    var receiverLocation = List.of(Location.from(1.0, 1.0, 1.0, 1.0));
    var phase = PhaseType.P;

    var value = ArrivalTimeFeaturePredictionValue.create(
      ArrivalTimeMeasurementValue.from(
        InstantValue.from(
          Instant.EPOCH.plusSeconds(1),
          Duration.ZERO
        ),
        Optional.empty()
      ),
      Map.of(),
      Set.of()
    );

    Mockito.when(mockFeaturePredictorPlugin.predict(
      eq(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
      any(EventLocation.class),
      any(Location.class),
      any(PhaseType.class),
      eq("Ak135"),
      any(List.class)
    )).thenAnswer(answer -> {

      EventLocation sourceLocationArgument = answer.getArgument(1);
      Location receiverLocationArgument = answer.getArgument(2);
      PhaseType phaseArgument = answer.getArgument(3);

      Assertions.assertEquals(sourceLocation, sourceLocationArgument);
      Assertions.assertEquals(receiverLocation.get(0), receiverLocationArgument);

      // Enums will be the exact same reference.
      Assertions.assertSame(phase, phaseArgument);

      return FeaturePrediction.<ArrivalTimeFeaturePredictionValue>builder()
        .setPredictionType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
        .setPredictionChannelSegment(Optional.empty())
        .setChannel(Optional.empty())
        .setReceiverLocation(receiverLocationArgument)
        .setSourceLocation(sourceLocationArgument)
        .setExtrapolated(false)
        .setPhase(phaseArgument)
        .setPredictionValue(value)
        .build();
    });

    Mockito.when(mockFeaturePredictorDefinition.getPluginNameByType(
      FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE
    )).thenReturn("ignored-for-test");

    Mockito.when(mockPluginRegistry.getPlugin(anyString(), eq(FeaturePredictorPlugin.class)))
      .thenReturn(Optional.of(mockFeaturePredictorPlugin));

    Mockito.when(mockFeaturePredictorConfiguration.getCurrentFeaturePredictorDefinition())
      .thenReturn(mockFeaturePredictorDefinition);

    var newFeaturePredictionContainer = featurePredictor.predict(
      List.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
      sourceLocation,
      receiverLocation,
      List.of(PhaseType.P),
      "Ak135",
      List.of()
    );

    var newFeaturePredictions = newFeaturePredictionContainer.getFeaturePredictionsForType(
      FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE
    );

    Assertions.assertEquals(1, newFeaturePredictions.size());

    var newFeaturePrediction = newFeaturePredictions.stream().findFirst().get();

    Assertions.assertEquals(value, newFeaturePrediction.getPredictionValue());
  }

  @ParameterizedTest
  @MethodSource("testSource")
  void testLocationSolutionAndChannel(
    Channel channel1,
    Channel channel2
  ) {

    var sourceLocation = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);
    var phase = PhaseType.P;
    var receiverLocation = channel1.getLocation();

    var value1 = ArrivalTimeFeaturePredictionValue.create(
      ArrivalTimeMeasurementValue.from(
        InstantValue.from(
          Instant.EPOCH.plusSeconds(1),
          Duration.ZERO
        ),
        Optional.empty()
      ),
      Map.of(),
      Set.of()
    );

    var value2 = ArrivalTimeFeaturePredictionValue.create(
      ArrivalTimeMeasurementValue.from(
        InstantValue.from(
          Instant.EPOCH.plusSeconds(2),
          Duration.ZERO
        ),
        Optional.empty()
      ),
      Map.of(),
      Set.of()
    );

    var inputFeaturePrediction = FeaturePrediction.<ArrivalTimeFeaturePredictionValue>builder()
      .setPredictionType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
      .setPredictionChannelSegment(Optional.empty())
      .setChannel(Optional.of(channel2))
      .setReceiverLocation(receiverLocation)
      .setSourceLocation(sourceLocation)
      .setExtrapolated(false)
      .setPhase(phase)
      .setPredictionValue(value2)
      .build();

    var inputLocationSolutionDate = LocationSolution.Data.builder()
      .setLocation(sourceLocation)
      .setLocationBehaviors(Set.of())
      .setLocationUncertainty(LocationUncertainty.builder()
        .setEllipsoids(Set.of())
        .setEllipses(Set.of())
        .setStDevOneObservation(0)
        .setXx(0)
        .setXy(0)
        .setXz(0)
        .setXt(0)
        .setYy(0)
        .setYz(0)
        .setYt(0)
        .setZz(0)
        .setZt(0)
        .setTt(0)
        .build())
      .setNetworkMagnitudeSolutions(Set.of())
      .setLocationRestraint(LocationRestraint.free())
      .setFeaturePredictions(
        FeaturePredictionContainer.of(inputFeaturePrediction)
      ).build();

    var uuid = UUID.randomUUID();
    var inputLocationSolution = LocationSolution.builder()
      .setId(uuid)
      .setData(inputLocationSolutionDate)
      .build();

    var intermediateOutputFeaturePrediction = FeaturePrediction.<ArrivalTimeFeaturePredictionValue>builder()
      .setPredictionType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
      .setPredictionChannelSegment(Optional.empty())
      .setChannel(Optional.empty())
      .setReceiverLocation(receiverLocation)
      .setSourceLocation(sourceLocation)
      .setExtrapolated(false)
      .setPhase(phase)
      .setPredictionValue(value1)
      .build();

    var outputFeaturePrediction = intermediateOutputFeaturePrediction
      .toBuilder()
      .setChannel(Optional.of(channel1))
      .build();

    if (channel1.equals(channel2)) {
      Mockito.verify(mockFeaturePredictorPlugin, Mockito.never()).predict(
        any(), any(), any(), any(), any(), any()
      );
    } else {
      Mockito.when(mockFeaturePredictorPlugin.predict(
        eq(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
        any(EventLocation.class),
        any(Location.class),
        any(PhaseType.class),
        eq("Ak135"),
        any(List.class)
      )).thenAnswer(answer -> {

        EventLocation sourceLocationArgument = answer.getArgument(1);
        Location receiverLocationArgument = answer.getArgument(2);
        PhaseType phaseArgument = answer.getArgument(3);

        Assertions.assertEquals(sourceLocation, sourceLocationArgument);
        Assertions.assertEquals(receiverLocation, receiverLocationArgument);

        // Enums will be the exact same reference.
        Assertions.assertSame(phase, phaseArgument);

        return intermediateOutputFeaturePrediction;
      });

      Mockito.when(mockFeaturePredictorDefinition.getPluginNameByType(
        FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE
      )).thenReturn("ignored-for-test");

      Mockito.when(mockPluginRegistry.getPlugin(anyString(), eq(FeaturePredictorPlugin.class)))
        .thenReturn(Optional.of(mockFeaturePredictorPlugin));

      Mockito.when(mockFeaturePredictorConfiguration.getCurrentFeaturePredictorDefinition())
        .thenReturn(mockFeaturePredictorDefinition);
    }

    var newLocationSolution = featurePredictor.predict(
      List.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE),
      inputLocationSolution,
      List.of(channel1),
      List.of(phase),
      "Ak135",
      List.of()
    );

    if (channel1.equals(channel2)) {
      Assertions.assertEquals(inputLocationSolution, newLocationSolution);
    } else {
      var newFeaturePredictions = newLocationSolution.getData().get().getFeaturePredictions()
        .getFeaturePredictionsForType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE);

      Assertions.assertTrue(newFeaturePredictions.contains(inputFeaturePrediction));
      Assertions.assertTrue(newFeaturePredictions.contains(outputFeaturePrediction));
    }
    System.out.println(newLocationSolution);
  }

  private static Stream<Arguments> testSource() {

    var receiverLocation = List.of(Location.from(1.0, 1.0, 1.0, 1.0));
    var channel1 = Mockito.mock(Channel.class);
    Mockito.when(channel1.getName()).thenReturn("Channel1");
    Mockito.when(channel1.getLocation()).thenReturn(receiverLocation.get(0));
    var channel2 = Mockito.mock(Channel.class);

    return Stream.of(
      Arguments.arguments(channel1, channel2),
      Arguments.arguments(channel1, channel1)
    );
  }

  @AfterEach
  void endTest() throws Exception {
    openMocks.close();
  }
}
