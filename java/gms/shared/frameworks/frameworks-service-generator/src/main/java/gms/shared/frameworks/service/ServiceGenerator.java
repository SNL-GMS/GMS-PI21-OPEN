package gms.shared.frameworks.service;

import gms.shared.frameworks.systemconfig.SystemConfig;

import javax.ws.rs.Path;
import java.util.Objects;

/**
 * Generates services for objects that have annotations according to GMS communications framework conventions.
 * <p>
 * The requirements to use this generator are:
 * - The class is annotated with {@link Path}, which can contain the empty string (e.g. @Path("")).
 * - Each public method of the class:
 * - Accepts a single input parameter
 * - Is annotated with {@link Path} (not empty string this time) and `@Post`
 */
public final class ServiceGenerator {

  private static ServiceGeneratorWorker worker = new ServiceGeneratorWorker();

  private ServiceGenerator() {
  }

  /**
   * Sets the {@link ServiceGeneratorWorker} that this generator uses.
   * <p>
   * The usage of a worker makes this facade have a few convenience static methods and aids
   * testing of the implementation by testing it's worker (which has non-static methods).
   *
   * @param w the worker to use
   */
  static void setWorker(ServiceGeneratorWorker w) {
    worker = Objects.requireNonNull(w, "ServiceGenerator requires non-null worker");
  }

  /**
   * Runs the given component instance as a service.  The component must be of a class
   * (or e.g. implement an interface) with particular annotations; see the README.
   *
   * @param component an instance of the component to run as a service.  The instance
   * will be inspected (using reflection) to discover appropriate annotations; if they are
   * absent or malformed, exceptions are thrown.
   * @param sysConfig the system configuration which is used to determine
   * server settings.
   */
  public static void runService(Object component, SystemConfig sysConfig) {
    worker.createService(component, sysConfig).start();
  }
}
