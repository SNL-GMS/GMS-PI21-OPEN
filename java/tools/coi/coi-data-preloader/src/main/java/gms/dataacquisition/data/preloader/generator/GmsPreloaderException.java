package gms.dataacquisition.data.preloader.generator;

/**
 * Custom pre-loader exception
 */
public class GmsPreloaderException extends RuntimeException {

  /**
   * Constructor
   *
   * @param message relevant explanation
   * @param cause a {@link Throwable}
   */
  public GmsPreloaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
