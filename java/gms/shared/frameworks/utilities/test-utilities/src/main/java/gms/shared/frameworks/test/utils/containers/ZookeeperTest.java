package gms.shared.frameworks.test.utils.containers;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.test.utils.config.PropertyConfigRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Properties;

@Testcontainers
@Tag("component")
@Tag("zookeeper")
public abstract class ZookeeperTest {
  protected static final String CI_DOCKER_REGISTRY_ENV_VAR_NAME = "CI_DOCKER_REGISTRY";
  protected static final String DOCKER_IMAGE_TAG_ENV_VAR_NAME = "DOCKER_IMAGE_TAG";
  protected static SystemConfig systemConfig;

  protected static final String IMAGE_NAME = String.format("%s/gms-common/bitnami-zookeeper:%s",
    System.getenv(CI_DOCKER_REGISTRY_ENV_VAR_NAME),
    System.getenv(DOCKER_IMAGE_TAG_ENV_VAR_NAME));

  protected ZookeeperTest() {
  }

  @Container
  protected static GenericContainer<?> container = new GenericContainer<>(IMAGE_NAME)
    .withEnv("ALLOW_ANONYMOUS_LOGIN", "yes")
    .withExposedPorts(2181)
    .withTmpFs(Map.of("/bitnami", "rw"));

  @BeforeAll
  protected static void setUpContainer() {
    var properties = new Properties();
    properties.setProperty("ignite-zookeeper-address",
      container.getContainerIpAddress() + ":" + container.getMappedPort(2181));
    properties.setProperty("ignite-instance-name", "gms-cache");
    var configRepository = PropertyConfigRepository.builder()
      .setProperties(properties)
      .build();
    systemConfig = SystemConfig.create("zookeeper-component-test", configRepository);
  }
}
