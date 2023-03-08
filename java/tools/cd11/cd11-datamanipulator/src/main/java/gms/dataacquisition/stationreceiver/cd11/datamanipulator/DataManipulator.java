package gms.dataacquisition.stationreceiver.cd11.datamanipulator;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import org.apache.commons.lang3.Validate;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class DataManipulator {
  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  /**
   * The entry into this application
   *
   * @param commandLineArgs Outlined in the DataManipulatorArgs class
   * @throws IOException if file is not provided correctly
   */
  public static void main(String[] commandLineArgs) throws IOException {

    objMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    DataManipulatorArgs parsedArguments = loadAndValidateArgs(commandLineArgs);

    if (parsedArguments.isFromCustom()) {
      fromCustomToSource(parsedArguments);
    } else {
      fromSourceToCustom(parsedArguments);
    }
  }

  /**
   * Parses the command line arguments provided to the main and puts them into a DataManipulatorArgs object
   *
   * @param commandLineArgs the arguments passed in by command line and parsed into an accessible object
   * @return DataManipulatorArgs object, which splits arguments into fields and partially validates them
   */
  private static DataManipulatorArgs loadAndValidateArgs(String[] commandLineArgs) {
    final DataManipulatorArgs args = new DataManipulatorArgs();
    final var parser = new CmdLineParser(args);
    try {
      parser.parseArgument(commandLineArgs);
      Validate.isTrue((args.getFileLocation() == null) != (args.getFolderLocation() == null),
        "Provide either a folder or a file location, but not neither");
      Validate.isTrue((args.getFileLocation() == null) == (args.getOutputFile() == null),
        "If you provide a file location, provide a file output. If you provide a folder input location" +
          " provide a folder output location ");
      return args;
    } catch (Exception ex) {
      parser.printUsage(System.err); // NOSONAR parser's print usage has to be done with System.err instead of to logger
      throw new IllegalArgumentException("Arg loader failed", ex);
    }
  }

  /**
   * Converts a rawstationdataframe into a readstationdataframe
   *
   * @param rawStationDataFrame the frame to be converted
   * @return ReadStationDataFrame
   * @throws IOException if field in input is wrong causes conversion to fail
   */
  private static ReadStationDataFrame convertRawToRead(RawStationDataFrame rawStationDataFrame)
    throws IOException {
    Cd11OrMalformedFrame cd11OrMalformed = Cd11FrameReader
      .readFrame(ByteBuffer.wrap(rawStationDataFrame.getRawPayload()
        .orElseThrow(() -> new IllegalStateException("rsdf did not contain RawPayload"))));
    if (Cd11OrMalformedFrame.Kind.MALFORMED.equals(cd11OrMalformed.getKind())) {
      throw new IOException("Error reading Rsdf Payload", cd11OrMalformed.malformed().getCause());
    }

    Cd11Frame cd11Frame = cd11OrMalformed.cd11();
    if (!(cd11Frame.getType().equals(FrameType.DATA) ||
      cd11Frame.getType().equals(FrameType.CD_ONE_ENCAPSULATION))) {
      throw new IllegalArgumentException("Rsdf Payload is of unexpected type " + cd11Frame.getType()
        + ", must be DATA or CD_ONE_ENCAPSULATION");
    }
    Cd11DataFrameSoh dataFrameSoh = new Cd11DataFrameSoh(cd11Frame);
    return new ReadStationDataFrame(dataFrameSoh, rawStationDataFrame);
  }

  /**
   * Same as the function above but converts a list of frames instead of a single
   *
   * @param rawStationDataFrames list of RawStationDataFrames
   * @return list of ReadStationDataFrames
   * @throws IOException if field in input is wrong causes conversion to fail
   */
  private static List<ReadStationDataFrame> convertRawToRead(
    List<RawStationDataFrame> rawStationDataFrames) throws IOException {
    List<ReadStationDataFrame> readStationDataFrames = new ArrayList<>();
    for (RawStationDataFrame rawStationDataFrame : rawStationDataFrames) {
      readStationDataFrames.add(convertRawToRead(rawStationDataFrame));
    }
    return readStationDataFrames;
  }

  /**
   * Converts a readstationdataframe to a rawstationdataframe
   *
   * @param readStationDataFrame frame to be converted
   * @return RawStationDataFrame
   */
  private static RawStationDataFrame convertReadToRaw(ReadStationDataFrame readStationDataFrame) {
    // this creates a new Cd11Data using the soh strings as the truth, and changes the bytes to match
    // this is because a human could have edited these fields and the bytes need to change, since a human wouldn't
    // know how to change those
    Cd11Frame newCd11Data = readStationDataFrame.getCd11DataFrameSoh().transcribeSohToBytes();
    RawStationDataFrameMetadata rawStationDataFrameMetadata = RawStationDataFrameMetadata.builder()
      .setStationName(readStationDataFrame.getRsdf().getMetadata().getStationName())
      .setChannelNames(readStationDataFrame.getRsdf().getMetadata().getChannelNames())
      .setPayloadStartTime(readStationDataFrame.getRsdf().getMetadata().getPayloadStartTime())
      .setPayloadEndTime(readStationDataFrame.getRsdf().getMetadata().getPayloadEndTime())
      .setReceptionTime(readStationDataFrame.getRsdf().getMetadata().getReceptionTime())
      .setAuthenticationStatus(readStationDataFrame.getRsdf().getMetadata().getAuthenticationStatus())
      .setWaveformSummaries(readStationDataFrame.getRsdf().getMetadata().getWaveformSummaries())
      .setPayloadFormat(readStationDataFrame.getRsdf().getMetadata().getPayloadFormat())
      .build();

    return RawStationDataFrame.builder()
      .setId(readStationDataFrame.getRsdf().getId())
      .setMetadata(rawStationDataFrameMetadata)
      .setRawPayload(newCd11Data.toBytes())
      .build();
  }

  private static List<RawStationDataFrame> convertReadToRaw(List<ReadStationDataFrame> readStationDataFrames) {
    List<RawStationDataFrame> rawStationDataFrames = new ArrayList<>();
    for (ReadStationDataFrame readStationDataFrame : readStationDataFrames) {
      rawStationDataFrames.add(convertReadToRaw(readStationDataFrame));
    }
    return rawStationDataFrames;
  }

  /**
   * Generates custom files from source input
   *
   * @param parsedArgs DataManipulatorArgs, parsed command line arguments
   * @throws IOException if file is not provided correctly
   */
  private static void fromSourceToCustom(DataManipulatorArgs parsedArgs) throws IOException {
    // logic for handling either file or folder input
    if (parsedArgs.getFileLocation() != null) {
      readSourceJsonFileAndWriteCustom(parsedArgs.getFileLocation(),
        parsedArgs.getOutputFile());

    } else {
      File[] files = getJsonFilesFromFolder(parsedArgs.getFolderLocation());
      String outputFolderLocation;
      if (parsedArgs.getOutputFolder() == null) {
        outputFolderLocation = parsedArgs.getFolderLocation() + "/customDfs";
      } else {
        outputFolderLocation = parsedArgs.getOutputFolder();
      }
      if (files != null) {
        for (File jsonFile : files) {
          readSourceJsonFileAndWriteCustom(jsonFile.getCanonicalPath(),
            outputFolderLocation + "/" + jsonFile.getName() + "read.json");
        }
      }

    }
  }

  /**
   * Returns list of json files in a given folder
   *
   * @param folderLocation where the folder is
   * @return array of File
   */
  private static File[] getJsonFilesFromFolder(String folderLocation) {
    var folder = new File(folderLocation);
    return folder.listFiles((File dir, String name) -> name.endsWith(".json"));
  }

  /**
   * Checks if a json file is a single rsdf file
   *
   * @param fileLocation where the file is located in string format
   * @return boolean, true if json file is a single rsdf, false if not
   */
  private static boolean isJsonFileSingleClass(String fileLocation, Class<?> c) {
    try {
      objMapper.readValue(new File(fileLocation), c);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Retrieves a list of RSDF files from a JSON file
   *
   * @param fileLocation location of the JSON file
   * @return List of RawStationDataFrames
   * @throws IOException if the json parse fails
   */
  private static List<RawStationDataFrame> getRsdfListFromJson(String fileLocation) throws IOException {

    return objMapper.readValue(new File(fileLocation), objMapper.getTypeFactory().constructCollectionType(List.class, RawStationDataFrame.class));
  }

  /**
   * Retrieves a list of readstationdataframes from a JSON file
   *
   * @param fileLocation location of the JSON file
   * @return list of ReadStationDataFrames
   * @throws IOException if the json parse fails
   */
  private static List<ReadStationDataFrame> getReadListFromJson(String fileLocation) throws IOException {
    return objMapper.readValue(new File(fileLocation), objMapper.getTypeFactory().constructCollectionType(List.class, ReadStationDataFrame.class));
  }

  /**
   * Reads a source data json file and returns a ReadStationDataFrame, a custom object with human readable fields
   *
   * @param location string specifying location of the json file
   * @throws IOException if file is provided incorrectly
   */
  private static void readSourceJsonFileAndWriteCustom(String location, String output)
    throws IOException {

    try (var outputStream = new FileOutputStream(output)) {

      if (isJsonFileSingleClass(location, RawStationDataFrame.class)) {
        RawStationDataFrame frame = objMapper
          .readValue(new File(location), RawStationDataFrame.class);
        ReadStationDataFrame readStationDataFrame = convertRawToRead(frame);
        objMapper.writeValue(outputStream, readStationDataFrame);

      } else {
        List<RawStationDataFrame> frames = getRsdfListFromJson(location);
        List<ReadStationDataFrame> readStationDataFrames = convertRawToRead(frames);
        objMapper.writeValue(outputStream, readStationDataFrames);
      }
    }

  }

  /**
   * Function that writes out a source rsdf json from a custom json
   *
   * @param parsedArgs DataManipulatorArgs, parsed command line arguments that specify config
   * @throws IOException if file is not provided correctly
   */
  private static void fromCustomToSource(DataManipulatorArgs parsedArgs) throws IOException {
    if (parsedArgs.getFileLocation() != null) {
      readCustomJsonFileAndWriteCustom(parsedArgs.getFileLocation(), parsedArgs.getOutputFile());

    } else {
      File[] files = getJsonFilesFromFolder(parsedArgs.getFolderLocation());
      String folderLocation;
      if (parsedArgs.getOutputFolder() == null) {
        folderLocation = parsedArgs.getFolderLocation() + "/rawstationDfs";
      } else folderLocation = parsedArgs.getOutputFolder();

      if (files != null) {
        for (File jsonFile : files) {
          readCustomJsonFileAndWriteCustom(jsonFile.getCanonicalPath(), folderLocation + "/" + jsonFile.getName() + "read.json");
        }
      }
    }
  }

  /**
   * Reads a custom ReadStationDataFrame JSON file and back converts it into a RawStationDataFrame,
   * then writes it out as a JSON
   *
   * @param location string pointing to the custom json file to read
   * @throws IOException if file is not provided correctly
   */
  private static void readCustomJsonFileAndWriteCustom(String location, String outputLocation) throws IOException {
    if (isJsonFileSingleClass(location, ReadStationDataFrame.class)) {
      var readOut = new File(location);
      ReadStationDataFrame readFrame = objMapper.readValue(readOut, ReadStationDataFrame.class);
      var rawStationDataFrame = convertReadToRaw(readFrame);
      try (var fos = new FileOutputStream(outputLocation)) {
        objMapper.writeValue(fos, rawStationDataFrame);
      }
    } else {
      List<ReadStationDataFrame> readStationDataFrames = getReadListFromJson(location);
      List<RawStationDataFrame> rawStationDataFrames = convertReadToRaw(readStationDataFrames);
      try (var fos = new FileOutputStream(outputLocation)) {
        objMapper.writeValue(fos, rawStationDataFrames);
      }

    }


  }

}
