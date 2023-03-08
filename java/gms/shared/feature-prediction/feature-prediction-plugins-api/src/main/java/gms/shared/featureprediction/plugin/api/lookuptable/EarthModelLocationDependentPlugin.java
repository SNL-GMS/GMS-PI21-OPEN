package gms.shared.featureprediction.plugin.api.lookuptable;

import gms.shared.plugin.Plugin;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Location;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public interface EarthModelLocationDependentPlugin<T, U> extends Plugin {

  U getUnits();

  Set<PhaseType> getAvailablePhaseTypes();

  T getValue(PhaseType phaseType, Location location);

  T getStandardDeviation(PhaseType phaseType, Location location);
}
