package gms.shared.featureprediction.plugin.api.lookuptable;

import gms.shared.stationdefinition.coi.utils.Units;
import java.util.List;
import org.apache.commons.lang3.tuple.Triple;

public interface DziewonskiGilbertEllipticityCorrectionLookupTablePlugin extends
  EarthModelDepthDistanceLookupTablePlugin<Triple<List<List<Double>>, List<List<Double>>, List<List<Double>>>, Triple<Units, Units, Units>> {
}
