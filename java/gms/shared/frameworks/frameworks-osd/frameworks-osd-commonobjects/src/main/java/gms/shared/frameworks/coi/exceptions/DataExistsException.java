package gms.shared.frameworks.coi.exceptions;

public class DataExistsException extends RepositoryException {

  public DataExistsException(Throwable cause) {
    super(cause);
  }

  public DataExistsException(String msg) {
    super(msg);
  }

  public DataExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public DataExistsException() {
    super();
  }
}
