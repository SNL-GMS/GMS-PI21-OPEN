package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import gms.shared.utilities.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Plugin for the IASPEI travel time lookup tables.
 */
@Component
public class IaspeiTravelTimeLookupTable extends TravelTimeLookupTable {

  @Autowired
  IaspeiTravelTimeLookupTable(
    FileStore fileStore,
    IaspeiTravelTimeLookupTableConfiguration configuration) {

    super(fileStore, configuration);
  }

}
