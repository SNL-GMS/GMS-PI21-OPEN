package gms.shared.utilities.coidataloader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.cssreader.waveformreaders.FlatFileWaveformReader;
import gms.shared.frameworks.client.generation.ClientGenerator;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.dataacquisition.SegmentClaimCheck;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.event.Event;
import gms.shared.frameworks.osd.coi.signaldetection.QcMask;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.SignalDetection;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectra;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import gms.shared.frameworks.osd.repository.OsdRepositoryFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.commons.lang3.Validate;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoiLoaderApplication {

  private static final Logger logger = LoggerFactory.getLogger(CoiLoaderApplication.class);

  static final JavaType fkSegmentType;

  static {
    fkSegmentType = new ObjectMapper().getTypeFactory()
      .constructParametricType(ChannelSegment.class, FkSpectra.class);
  }

  private final CoiLoaderArgs args;
  private final ObjectMapper mapper;
  private final FlatFileWaveformReader wfReader;
  private final CoiLoader loader;

  public CoiLoaderApplication(CoiLoaderArgs args, ObjectMapper mapper,
    FlatFileWaveformReader wfReader, CoiLoader loader) {
    this.args = Objects.requireNonNull(args, "null args");
    this.mapper = Objects.requireNonNull(mapper, "null mapper");
    this.wfReader = Objects.requireNonNull(wfReader, "null waveform reader");
    this.loader = Objects.requireNonNull(loader, "null loader");
  }

  public void execute() {
    loader.load(CoiDataSet.builder()
      .setStationReference(readStationReference())
      .setStationGroups(readForArg(args.getStationGroups(), StationGroup[].class))
      .setResponses(readForArg(args.getProcessingResponses(), Response[].class))
      .setEvents(readForArg(args.getEvents(), Event[].class))
      .setSignalDetections(readForArg(args.getSigDets(), SignalDetection[].class))
      .setMasks(readForArg(args.getMasks(), QcMask[].class))
      .setWaveforms(readWaveforms())
      .setFks(readFks())
      .build());
  }

  public static void main(String[] commandLineArgs) {
    try {
      final CoiLoaderArgs args = loadAndValidateArgs(commandLineArgs);
      SystemConfig config = SystemConfig.create("coi-loader");
      final OsdRepositoryInterface osd = args.getUseService()
        ? ClientGenerator.createClient(OsdRepositoryInterface.class, config)
        : OsdRepositoryFactory.createOsdRepository(config);
      new CoiLoaderApplication(args,
        CoiObjectMapperFactory.getJsonObjectMapper(),
        new FlatFileWaveformReader(),
        CoiLoader.create(osd))
        .execute();
      logger.info("Done reading and loading data without error");
      System.exit(0);
    } catch (Exception ex) {
      logger.error("Error in reading or loading data", ex);
      System.exit(-1);
    }
  }

  private static CoiLoaderArgs loadAndValidateArgs(String[] commandLineArgs) {
    final CoiLoaderArgs args = new CoiLoaderArgs();
    final CmdLineParser parser = new CmdLineParser(args);
    try {
      parser.parseArgument(commandLineArgs);
      Validate.isTrue((args.getWaveformClaimCheck() == null) == (args.getWfDir() == null),
        "Either both or neither of -waveformClaimCheck and -wfDir can be provided");
      return args;
    } catch (Exception ex) {
      parser.printUsage(System.err);
      throw new IllegalArgumentException("Loader failed, invalid args: ", ex);
    }
  }

  private StationReference readStationReference() {
    return StationReference.builder()
      .setNetworks(readForArg(args.getRefNetworks(), ReferenceNetwork[].class))
      .setStations(readForArg(args.getRefStations(), ReferenceStation[].class))
      .setSites(readForArg(args.getRefSites(), ReferenceSite[].class))
      .setChannels(readForArg(args.getRefChans(), ReferenceChannel[].class))
      .setSensors(readForArg(args.getRefSensors(), ReferenceSensor[].class))
      .setResponses(readResponses())
      .setNetworkMemberships(
        readForArg(args.getRefNetMemberships(), ReferenceNetworkMembership[].class))
      .setStationMemberships(
        readForArg(args.getRefStaMemberships(), ReferenceStationMembership[].class))
      .setSiteMemberships(
        readForArg(args.getRefSiteMemberships(), ReferenceSiteMembership[].class))
      .build();
  }

  private Stream<ChannelSegment<FkSpectra>> readFks() {
    return args.getFkDir() == null ?
      Stream.of()
      : Arrays.stream(listFiles(args.getFkDir()))
      .map(f -> read(f, fkSegmentType));
  }

  private Stream<ChannelSegment<Waveform>> readWaveforms() {
    if (args.getWaveformClaimCheck() == null) {
      return Stream.of();
    }
    logger.info("Using segment claim check file at {}", args.getWaveformClaimCheck());
    final Collection<SegmentClaimCheck> segmentClaimChecks = readArray(
      new File(args.getWaveformClaimCheck()), SegmentClaimCheck[].class);
    logger.info("Read {} segment claim checks", segmentClaimChecks.size());
    return segmentClaimChecks.stream().map(segmentClaimCheck -> {
      try {
        return readFromClaimCheck(segmentClaimCheck);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private ChannelSegment<Waveform> readFromClaimCheck(SegmentClaimCheck cc) throws IOException {
    final double[] samples = wfReader.readWaveform(
      args.getWfDir() + File.separator + cc.getWaveformFile(),
      cc.getfOff(), cc.getSampleCount(), cc.getDataType());
    return ChannelSegment.from(cc.getSegmentId(), cc.getChannel(),
      cc.getSegmentName(), cc.getSegmentType(),
      List.of(Waveform.from(cc.getStartTime(), cc.getSampleRate(), samples)));
  }

  private <T> Collection<T> readForArg(String arg, Class<T[]> type) {
    return arg == null ? List.of() : readArray(new File(arg), type);
  }

  private Collection<ReferenceResponse> readResponses() {
    return args.getRefResponseDir() == null ? List.of()
      : Arrays.stream(listFiles(args.getRefResponseDir()))
      .map(f -> read(f, ReferenceResponse.class))
      .collect(Collectors.toList());
  }

  // not private because it's mocked in a test
  File[] listFiles(String dir) {
    return new File(dir).listFiles();
  }

  private <T> Collection<T> readArray(File f, Class<T[]> clazz) {
    try {
      return Arrays.asList(mapper.readValue(f, clazz));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private <T> T read(File f, Class<T> clazz) {
    try {
      return mapper.readValue(f, clazz);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private <T> T read(File f, JavaType type) {
    try {
      return mapper.readValue(f, type);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
