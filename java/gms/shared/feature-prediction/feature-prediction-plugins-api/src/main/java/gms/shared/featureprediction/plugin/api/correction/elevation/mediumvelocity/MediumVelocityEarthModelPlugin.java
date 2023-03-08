package gms.shared.featureprediction.plugin.api.correction.elevation.mediumvelocity;

import gms.shared.featureprediction.plugin.api.lookuptable.EarthModelLocationDependentPlugin;
import gms.shared.stationdefinition.coi.utils.Units;
import org.springframework.stereotype.Service;

@Service
public interface MediumVelocityEarthModelPlugin extends EarthModelLocationDependentPlugin<Double, Units> {
}
