package gms.shared.frameworks.service;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.ServiceReflectionUtilities;

import javax.ws.rs.Path;
import java.util.Objects;

/**
 * 'worker' class for the ServiceGenerator facade.  This class exists
 * to simplify the methods ServiceGenerator exposes and to aid in testing the functionality.
 */
class ServiceGeneratorWorker {

  /**
   * Creates a service.
   *
   * @param component an instance of the component to run as a service.  The instance
   * * will be inspected (using reflection) to discover appropriate annotations; if they are
   * * absent or malformed, exceptions are thrown.
   * @param sysConfig the system configuration which is used to determine
   * * server settings.
   * @return a {@link HttpService} which is not null and has not been started
   * @throws IllegalArgumentException if a service cannot be created for the given
   * component.  This typically means it is not conformant to the GMS comms framework,
   * i.e. is missing annotations or has methods with incorrect signatures.  See
   * {@link ServiceGenerator} for more info on those requirements.
   */
  HttpService createService(Object component, SystemConfig sysConfig) {
    return new HttpService(createServiceDefinition(component, sysConfig));
  }

  /**
   * Obtains a {@link ServiceDefinition} for the provided component's {@link Path} operations.
   *
   * @param component instance providing {@link Path} annotated handler operations
   * @return {@link ServiceDefinition}, not null
   * @throws IllegalArgumentException if a service definition cannot be created for the given
   * component.  This typically means it is not conformant to the GMS comms framework,
   * i.e. is missing annotations or has methods with incorrect signatures.  See
   * {@link ServiceGenerator} for more info on those requirements.
   */
  ServiceDefinition createServiceDefinition(Object component, SystemConfig sysConfig) {
    Objects.requireNonNull(component, "Cannot create service for null component instance");
    Objects.requireNonNull(sysConfig, "Cannot create service with null system config");
    try {
      return ServiceDefinition.builder(sysConfig.getServerConfig(),
          ServiceReflectionUtilities.getContextRoot(component.getClass()))
        .setRoutes(RouteGenerator.generate(component)).build();
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Could not create a service definition for the provided object.", e);
    }
  }
}
