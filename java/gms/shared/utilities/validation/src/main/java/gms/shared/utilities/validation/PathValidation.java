package gms.shared.utilities.validation;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * A class used to validate paths have an expected starting point to prevent potential security risks
 * and manipulation.
 */
public class PathValidation {

  //private constructor
  private PathValidation() {
  }

  /**
   * Returns path from string after validating it against the base path. If the string path is
   * not in the base path, throws and IllegalArgumentException
   *
   * @param path string of path
   * @param expectedBasePath the expected base directory path for the path
   * @param more additional optional strings to concat to path
   * @return Path object for string
   */
  public static Path getValidatedPath(String path, String expectedBasePath,
    String... more) throws IllegalArgumentException {

    if (!validateDirectoryPath(path, expectedBasePath)) {
      throw new IllegalArgumentException("Path manipulation detected");
    }

    return Path.of(path, more);
  }

  /**
   * Validation for string paths
   *
   * @param path path of directory
   * @param expectedBasePath substring that is the expected start of the path
   * @return a boolean representing if start of path matches expectedBasePath
   */
  public static boolean validateDirectoryPath(String path, String expectedBasePath) {
    return Paths.get(path).normalize().toAbsolutePath().startsWith(expectedBasePath);
  }

  /**
   * @param path Url object
   * @param expectedBasePath substring that is the expected start of the path
   * @return a boolean representing if start of path matches expectedBasePath
   */
  public static boolean validateDirectoryPath(URL path, String expectedBasePath) {
    return validateDirectoryPath(path.getPath(), expectedBasePath);
  }

  /**
   * @param path File object
   * @param expectedBasePath substring that is the expected start of the path
   * @return a boolean representing if start of path matches expectedBasePath
   */
  public static boolean validateDirectoryPath(File path, String expectedBasePath) {
    return validateDirectoryPath(path.toURI(), expectedBasePath);
  }

  /**
   * @param path Path object
   * @param expectedBasePath substring that is the expected start of the path
   * @return a boolean representing if start of path matches expectedBasePath
   */
  public static boolean validateDirectoryPath(Path path, String expectedBasePath) {
    return validateDirectoryPath(path.toUri(), expectedBasePath);
  }

  private static boolean validateDirectoryPath(URI path, String expectedBasePath) {
    return Paths.get(path).normalize().startsWith(expectedBasePath);
  }
}
