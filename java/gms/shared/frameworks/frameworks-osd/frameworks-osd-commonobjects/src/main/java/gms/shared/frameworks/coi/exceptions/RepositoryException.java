package gms.shared.frameworks.coi.exceptions;

public class RepositoryException extends RuntimeException {

  public RepositoryException(Throwable cause) {
    super(cause);
  }

  public RepositoryException(String msg) {
    super(msg);
  }

  public RepositoryException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public RepositoryException() {
    super();
  }
}
