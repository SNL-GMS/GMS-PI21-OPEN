package gms.shared.utilities.javautilities.objectmapper;

/**
 * The DatabaseLivenessCheck class is used to validate if the target database is available and ready to
 * receive connections
 */
public interface DatabaseLivenessCheck {

  /**
   * Determine if the database is live and ready to receive connections
   *
   * @return whether the database is available to receive connections
   */
  boolean isLive();

}
