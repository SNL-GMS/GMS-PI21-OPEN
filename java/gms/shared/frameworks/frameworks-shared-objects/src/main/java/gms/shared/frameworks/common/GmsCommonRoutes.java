package gms.shared.frameworks.common;

/**
 * Holds a list of HTTP routes that is required to exist across all GMS applications
 */
public class GmsCommonRoutes {
  /**
   * Path reserved for basic health check routes.
   */
  public static final String HEALTHCHECK_PATH = "/alive";

  /**
   * Path reserved for upgrading connections to HTTP/2
   */
  public static final String CONNECTION_UPGRADE_PATH = "/upgrade";

}
