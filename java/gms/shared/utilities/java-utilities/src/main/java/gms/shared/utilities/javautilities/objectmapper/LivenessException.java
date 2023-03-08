package gms.shared.utilities.javautilities.objectmapper;

public class LivenessException extends Exception {


  public LivenessException() {
    super("Database liveness failed");
  }

  public LivenessException(Throwable cause) {
    super("Database liveness check failed", cause);
  }
}
