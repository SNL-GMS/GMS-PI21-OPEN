package gms.dataacquisition.stationreceiver.cd11.parser.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class GmsObjectUtility {

  private static final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static <T> Optional<T> getGmsObject(String fileName, Class<T> clazz) throws IOException {
    URL testFrameUrl = Thread.currentThread().getContextClassLoader().getResource(fileName);
    return Optional.of(jsonObjectMapper.readValue(testFrameUrl, clazz));
  }
}
