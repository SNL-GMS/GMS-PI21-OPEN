package gms.shared.utilities.javautilities.objectmapper;

import gms.shared.frameworks.systemconfig.SystemConfig;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DatabaseLivenessCheck class for validating if an Oracle database is ready to receive connections
 */
public class OracleLivenessCheck implements DatabaseLivenessCheck {

  private static final int INVALID_LOGIN_ERROR_CODE = 1017;

  private static final Logger logger = LoggerFactory.getLogger(OracleLivenessCheck.class);

  private final SystemConfig systemConfig;

  private OracleLivenessCheck(SystemConfig systemConfig) {
    this.systemConfig = systemConfig;
  }

  public static OracleLivenessCheck create(SystemConfig systemConfig) {
    Objects.requireNonNull(systemConfig, "SystemConfig cannot be null");
    return new OracleLivenessCheck(systemConfig);
  }

  @Override
  public boolean isLive() {
    RetryPolicy<Boolean> retryPolicy = new RetryPolicy<Boolean>()
      .withBackoff(30, 300, ChronoUnit.SECONDS)
      .withMaxDuration(Duration.ofSeconds(600))
      .withMaxAttempts(-1)
      .handleResult(false)
      .onFailedAttempt(event -> logger.info("Database is not ready, retrying"));

    try {
      Failsafe.with(retryPolicy).run(this::checkConnection);
    } catch (FailsafeException ex) {
      logger.info("Liveness check failed", ex);
      return false;
    }
    return true;
  }

  private void checkConnection() throws LivenessException {
    System.setProperty("oracle.net.tns_admin", systemConfig.getValue("tns_entry_location"));
    Connection connection = null;
    try {
      var dataSource = new OracleDataSource();
      dataSource.setURL("jdbc:oracle:thin:@connection_check");
      // this is expected to throw an exception since we do not provide credentials.  The error details are
      // used to determine the state of the database
      connection = dataSource.getConnection();
    } catch (SQLException ex) {
      int errorCode = ex.getErrorCode();
      // The invalid login error code indicates that the database expected (and didn't receive) correct login
      // credentials. This indicates that
      // 1) the container is up (in a containerized scenario),
      // 2) the database server is up
      // 3) the tns listener is active
      if (errorCode != INVALID_LOGIN_ERROR_CODE) {
        throw new LivenessException(ex);
      }
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          logger.error("Error closing connection");
        }
      }
    }
  }
}
