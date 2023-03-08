package gms.shared.featureprediction.plugin.api;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.FeaturePredictionValue;
import gms.shared.plugin.Plugin;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Location;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface FeaturePredictorPlugin extends Plugin {

  <T extends FeaturePredictionValue<?, ?, ?>> FeaturePrediction<T> predict(
    FeaturePredictionType<T> featurePredictionType,
    EventLocation sourceLocation,
    Location receiverLocation,
    PhaseType phase,
    String earthModel,
    List<FeaturePredictionCorrectionDefinition> featurePredictionCorrectionDefinitions
  );

}
