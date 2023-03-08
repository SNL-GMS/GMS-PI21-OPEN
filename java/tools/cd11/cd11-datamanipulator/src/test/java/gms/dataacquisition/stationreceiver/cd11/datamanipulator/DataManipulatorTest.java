package gms.dataacquisition.stationreceiver.cd11.datamanipulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DataManipulatorTest {

  @Test
  void testDataManipulator() throws IOException {

    String resourcesDir = "./src/test/resources/";
    // define file names and directories at the top to easily change
    String customFileName = String.format("%stestOut.json", resourcesDir);
    String inputFileName = String.format("%stest.json", resourcesDir);
    String outputFileName = String.format("%sfinalOut.json", resourcesDir);
    String secondCustomFileName = String
      .format("%stest2.json", resourcesDir);
    String secondOutputFileName = String.format("%sfinalOut2.json", resourcesDir);

    String fileLocationArgumentTag = "-fileLocation";
    String outputFileArgumentTag = "-outputFile";

    DataManipulator.main(new String[]{
      fileLocationArgumentTag,
      inputFileName,
      outputFileArgumentTag,
      customFileName});

    // check to make sure first file was created
    File intermediate = new File(customFileName);
    assertTrue(intermediate.exists());

    DataManipulator.main(new String[]{"-fromCustom",
      "-fileLocation",
      intermediate.getAbsolutePath(),
      "-outputFile",
      outputFileName});

    // check to make sure output was created
    File output = new File(outputFileName);
    assertTrue(output.exists());

    ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    RawStationDataFrame frame1 = objMapper
      .readValue(new File(inputFileName), RawStationDataFrame.class);
    RawStationDataFrame frame2 = objMapper
      .readValue(new File(outputFileName), RawStationDataFrame.class);

    // make sure deserialization and re-serialization didn't change the file (no user edits)
    assertEquals(frame1, frame2);

    // this is testing a hand changed file
    DataManipulator.main(new String[]{"-fromCustom",
      fileLocationArgumentTag,
      secondCustomFileName,
      outputFileArgumentTag,
      secondOutputFileName});

    RawStationDataFrame frame3 = objMapper
      .readValue(new File(secondOutputFileName), RawStationDataFrame.class);

    // because the intermediate was changed, these shouldn't be equal
    assertNotEquals(frame3, frame1);

    File secondOutput = new File(secondOutputFileName);
    assertTrue(secondOutput.exists());

    // delete the files now that we're done with them
    Files.delete(intermediate.toPath());
    Files.delete(output.toPath());
    Files.delete(secondOutput.toPath());

  }
}
