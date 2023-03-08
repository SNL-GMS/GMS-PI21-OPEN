package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import gms.shared.utilities.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Plugin for the AK135 travel time lookup tables.
 */
@Service
public class Ak135TravelTimeLookupTable extends TravelTimeLookupTable {

  @Autowired
  Ak135TravelTimeLookupTable(
    FileStore fileStore,
    Ak135TravelTimeLookupTableConfiguration configuration) {

    super(fileStore, configuration);
  }

}
