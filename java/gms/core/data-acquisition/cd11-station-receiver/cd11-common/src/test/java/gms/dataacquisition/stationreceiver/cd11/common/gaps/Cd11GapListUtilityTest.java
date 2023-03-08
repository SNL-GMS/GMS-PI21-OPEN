package gms.dataacquisition.stationreceiver.cd11.common.gaps;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

class Cd11GapListUtilityTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void clearGapStateTest(@TempDir Path tempDir) throws IOException {

    var gapListUtility = Cd11GapListUtility.create(tempDir.toString(), ".json");
    Path tempPath = Path.of(tempDir + "clear.json");
    Files.createFile(tempPath);
    Assertions.assertTrue(Files.exists(tempPath));

    StepVerifier.create(gapListUtility.clearGapState("clear"))
      .verifyComplete();

    Assertions.assertTrue(Files.notExists(tempPath));

    StepVerifier.create(gapListUtility.clearGapState("clear-non-exists"))
      .verifyComplete();
  }

  @Test
  void loadGapStateTest(@TempDir Path tempDir) throws IOException {
    var gapListUtility = Cd11GapListUtility.create(tempDir.toString(), ".json");
    GapList gp = new GapList(0, 100);
    var path = tempDir + "temp.json";
    var tempPath = Path.of(path);
    var f = Files.createFile(tempPath);
    objectMapper.findAndRegisterModules();
    objectMapper.writeValue(Files.newOutputStream(f), gp);
    var loadedGp = gapListUtility.loadGapState("temp");
    Assertions.assertEquals(loadedGp.getGapList(), gp);
    loadedGp = gapListUtility.loadGapState("non-existed");
    Assertions.assertNotEquals(loadedGp.getGapList(), gp);

    //test for the incorrect filetype
    objectMapper.writeValue(Files.newOutputStream(f), "not correct");
    loadedGp = gapListUtility.loadGapState("temp");
    Assertions.assertNotEquals(loadedGp.getGapList(), gp);
  }

  @Test
  void testInstantiation() {
    Assertions.assertThrows(NullPointerException.class,
      () -> Cd11GapListUtility.create(null, ".json"));

    Assertions.assertThrows(NullPointerException.class,
      () -> Cd11GapListUtility.create("path/", null));

    Assertions.assertThrows(IllegalArgumentException.class,
      () -> Cd11GapListUtility.create("", ".json"));

    Assertions.assertThrows(IllegalArgumentException.class,
      () -> Cd11GapListUtility.create("path", ""));

    Assertions.assertThrows(IllegalStateException.class,
      () -> Cd11GapListUtility.create("\0", ".json"));
  }

  @Test
  void testInstantiateDirectoryNotExist(@TempDir Path tempDir) {
    var directoryToCreate = Paths.get(tempDir.toString(), "new");

    assertFalse(directoryToCreate.toFile().exists());
    assertDoesNotThrow(() -> Cd11GapListUtility.create(directoryToCreate.toString(), ".json"));
    assertTrue(directoryToCreate.toFile().exists());
  }

  @Test
  void testPersistGapState(@TempDir Path tempDir) {
    var gapListUtility = Cd11GapListUtility.create(tempDir.toString(), ".json");
    GapList gp = new GapList(0, 100);

    StepVerifier.create(gapListUtility.persistGapState("test", gp))
      .verifyComplete();
    var path = Paths.get(tempDir + "test.json");
    Assertions.assertTrue(Files.exists(path));
  }

  @Test
  void testPersistGapStateFailPropagatesException(@TempDir Path tempDir) throws IOException {
    var mockObjectMapper = Mockito.mock(ObjectMapper.class);
    var nope = new JsonGenerationException("NOPE", mock(JsonGenerator.class));
    willThrow(nope).given(mockObjectMapper).writeValue(any(OutputStream.class), any());
    var gapListUtility = new Cd11GapListUtility(tempDir.toString(), ".json", mockObjectMapper);

    GapList gp = new GapList(0, 100);

    StepVerifier.create(gapListUtility.persistGapState("test", gp))
      .expectErrorSatisfies(err -> assertEquals(err.getCause(), nope))
      .verify();
  }

}
