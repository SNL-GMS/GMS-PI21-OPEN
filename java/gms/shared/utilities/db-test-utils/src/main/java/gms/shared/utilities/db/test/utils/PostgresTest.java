package gms.shared.utilities.db.test.utils;

import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Testcontainers
@Tag("component")
@Tag("postgres")
public abstract class PostgresTest {

  protected static final int POSTGRES_PORT = 5432;
  protected static final String CI_DOCKER_REGISTRY_ENV_VAR_NAME = "CI_DOCKER_REGISTRY";
  protected static final String DOCKER_IMAGE_TAG_ENV_VAR_NAME = "DOCKER_IMAGE_TAG";
  protected static final String GMS_DB_USER = "gms_test";
  protected static final String GMS_DB_PASSFAKE = "test";
  protected static final String POSTGRES_DB = "gms";
  protected static final String IMAGE_NAME = String.format("%s/gms-common/postgres:%s",
    System.getenv(CI_DOCKER_REGISTRY_ENV_VAR_NAME),
    System.getenv(DOCKER_IMAGE_TAG_ENV_VAR_NAME));

  protected static final DockerImageName dockerImageName = DockerImageName.parse(IMAGE_NAME)
    .asCompatibleSubstituteFor("postgres");

  @Container
  protected static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(dockerImageName)
    .withDatabaseName(POSTGRES_DB)
    .withUsername(GMS_DB_USER)
    .withPassword(GMS_DB_PASSFAKE)
    .withEnv(Map.of(
      "POSTGRES_INITDB_ARGS",
      "--data-checksums -A --auth=scram-sha-256 --auth-host=scram-sha-256 --auth-local=scram-sha-256",
      "POSTGRES_HOST_AUTH_METHOD", "scram-sha-256"
    ))
    .withImagePullPolicy(PullPolicy.defaultPolicy())
    .withExposedPorts(POSTGRES_PORT);

  protected PostgresTest() {
  }
}
