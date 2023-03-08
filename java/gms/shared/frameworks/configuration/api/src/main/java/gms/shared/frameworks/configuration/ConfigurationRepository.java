package gms.shared.frameworks.configuration;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.common.annotations.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.Optional;

/**
 * Interface providing retrieval and storage operations for {@link Configuration}s.
 */
@Component("processing-cfg")
@Path("/frameworks-configuration-service/processing-cfg")
public interface ConfigurationRepository {

  /**
   * Obtains the most recent {@link Configuration} with the provided key.  The returned {@link Optional} is empty if the key does not
   * resolve to a Configuration.
   *
   * @param key key to a single configuration value, not null
   * @return Optional Configuration, not null
   * @throws NullPointerException if key is null
   * @throws IllegalArgumentException if the key resolves to more than a single Configuration
   */
  @Path("/get")
  @POST
  @Produces(ContentType.JSON_NAME)
  @Consumes(ContentType.JSON_NAME)
  Optional<Configuration> get(String key);

  /**
   * Obtains all of the {@link Configuration}s which have keys beginning with the provided keyPrefix.  The returned Collection is empty if
   * no Configurations have keys matching the prefix.
   *
   * @param keyPrefix key prefix to one or more configuration values, not null
   * @return Collection of Configuration, not null
   * @throws NullPointerException if keyPrefix is null
   */
  @Path("/range")
  @POST
  @Produces(ContentType.JSON_NAME)
  @Consumes(ContentType.JSON_NAME)
  Collection<Configuration> getKeyRange(String keyPrefix);

  /**
   * Creates and stores a new {@link Configuration}.
   *
   * @param configuration ConfigurationItem to upload, not null
   * @return {@link Optional} new Configuration on success or emptry optional if ut method fails.
   * @throws NullPointerException if Configuration is null
   */
  @Path("/put")
  @POST
  @Produces(ContentType.JSON_NAME)
  @Consumes(ContentType.JSON_NAME)
  Optional<Configuration> put(Configuration configuration);

  /**
   * Creates and stores a list of new {@link Configuration}.
   *
   * @param configurations ConfigurationItems to upload, not null
   * @return {@link Optional} list of new Configuration on success or empty optional if put-all method fails.
   * @throws NullPointerException if Configuration List is null
   */
  @Path("/put-all")
  @POST
  @Produces(ContentType.JSON_NAME)
  @Consumes(ContentType.JSON_NAME)
  Collection<Configuration> putAll(Collection<Configuration> configurations);
  /*
  tpf - 120419 - verify we should remove this or what it will be used for
  // TODO: need to figure out what watch() methods should look like.
  @FunctionalInterface
  interface GmsConfigurationValueChange {

    // TODO: also provide previous value?
    void update(Configuration updatedConfiguration);
  }

  // TODO: watch until certain time?  only provide updates at fixed intervals?
  // These seem like details if we aren't going to initially focus on watches
  void watch(String key, GmsConfigurationValueChange callback);

  void watchRange(String keyRange, GmsConfigurationValueChange callback);
  */
}
