package gms.shared.featureprediction.utilities.view;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.utilities.filestore.FileTransformer;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * A class to transform a JSON file representing a travel time lookup table to a
 * TravelTimeLookupView.
 */
public class TravelTimeLookupViewTransformer implements FileTransformer<TravelTimeLookupView> {

  private static final ObjectMapper JSON_OBJECT_MAPPER = ObjectMapperFactory.getJsonObjectMapper();

  @JsonUnwrapped
  TravelTimeLookupView decodedTable = null;

  @Override
  public TravelTimeLookupView transform(InputStream rawDataStream) throws IOException {

    decodedTable = JSON_OBJECT_MAPPER.readValue(rawDataStream, TravelTimeLookupView.class);

    return decodedTable;
  }
}
