package gms.shared.event.manager;

/**
 * Thrown when a request body provided to {@link EventManager} is determined to be invalid. Handled by
 * EventManager.ApiExceptionHandler.handle(EventRequestException).
 */
public class EventRequestException extends RuntimeException {

  /**
   * {@inheritDoc}
   */
  public EventRequestException(String message) {
    super(message);
  }
}
