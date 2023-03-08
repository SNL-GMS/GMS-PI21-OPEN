package gms.shared.featureprediction.plugin.api.correction.ellipticity;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.plugin.Plugin;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.stationdefinition.coi.channel.Location;

import java.util.Optional;

public interface EllipticityCorrectorPlugin extends Plugin {

  Optional<FeaturePredictionComponent<DurationValue>> correct(
    String earthModel,
    EventLocation sourceLocation,
    Location receiverLocation,
    PhaseType phaseType
  );

}

