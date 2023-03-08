package gms.shared.utilities.validation;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathValidationTest {
  @Test
  void validateDirectoryPathStringTest() {
    assertTrue(PathValidation.validateDirectoryPath("/test/someFile/", "/test"));
    assertFalse(PathValidation.validateDirectoryPath("/test/someFile/../../", "/test"));
  }

  @Test
  void validateDirectoryPathURLTest() throws MalformedURLException {
    URL testUrl = new URL("file:///test/someFile");
    assertTrue(PathValidation.validateDirectoryPath(testUrl, "/test"));
  }

  @Test
  void validateDirectoryPathFileTest() {
    File testFile = new File("/test/someFile");
    assertTrue(PathValidation.validateDirectoryPath(testFile, "/test"));
  }

  @Test
  void validateDirectoryPathPathTest() {
    Path testPath = FileSystems.getDefault().getPath("/test/someFile");
    assertTrue(PathValidation.validateDirectoryPath(testPath, "/test"));
  }

  @Test
  void validateDirectoryIsValid() {
    Path testPath = FileSystems.getDefault().getPath("/test/someFile/../../");
    assertFalse(PathValidation.validateDirectoryPath(testPath, "/test"));

  }

  @Test
  void getValidPathPathManipulation() {

    var testPath = "/a/rand/path";
    var expectedBasePath = "/a/different/path";

    assertThrows(IllegalArgumentException.class, () -> PathValidation.getValidatedPath(testPath, expectedBasePath));
  }

  @Test
  void getValidPathExpectedBasePath() {

    var testPath = "/a/rand/path";
    var expectedBasePath = "/a/rand";
    var validPath = PathValidation.getValidatedPath(testPath, expectedBasePath);

    assertEquals(Path.of(testPath), validPath);
  }
}
