package gms.shared.frameworks.service;

/**
 * Indicates that an input is somehow invalid.
 */
public class InvalidInputException extends RuntimeException {

  /**
   * Construct an InvalidInputException with a message.
   *
   * @param msg the error message
   */
  public InvalidInputException(String msg) {
    super(msg);
  }
}