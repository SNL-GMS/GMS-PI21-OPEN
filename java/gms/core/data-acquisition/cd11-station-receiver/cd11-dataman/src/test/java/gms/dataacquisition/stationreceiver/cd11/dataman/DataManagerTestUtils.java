package gms.dataacquisition.stationreceiver.cd11.dataman;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataManagerTestUtils {
  private static final String RSDF_RESOURCE = "LBTB-RSDF.json";
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static RawStationDataFrame readRsdf() throws IOException {
    return readRsdf(RSDF_RESOURCE);
  }

  public static RawStationDataFrame readRsdf(String rsdfResource) throws IOException {
    byte[] rsdfBytes = Files.readAllBytes(Paths.get("src", "test", "resources", rsdfResource));
    return objectMapper.readValue(rsdfBytes, RawStationDataFrame.class);
  }

  public static Flux<Tuple2<RawStationDataFrame, Long>> mockSequencedRsdfs(RawStationDataFrame seedRsdf, int size) {
    return Flux.range(1, size)
      .map(i -> Tuples.of(seedRsdf, (long) i));
  }

  public static Flux<MalformedFrame> mockUniqueMalformedFrames(MalformedFrame seedRsdf, int size) {
    return Flux.range(1, size)
      .map(i -> seedRsdf.toBuilder().setStation(i.toString()).build());
  }

}
