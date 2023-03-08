package gms.shared.frameworks.osd.repository.utils;

/**
 * This generic exception will handle all invalid Database storage operations through JPA and
 * wrap them.
 */
public class InvalidStorageException extends RuntimeException {

  public InvalidStorageException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

  @Override
  public String getLocalizedMessage() {
    return super.getLocalizedMessage();
  }
}
