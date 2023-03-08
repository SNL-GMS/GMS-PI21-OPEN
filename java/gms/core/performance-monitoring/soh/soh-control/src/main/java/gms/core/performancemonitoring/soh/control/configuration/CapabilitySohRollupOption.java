package gms.core.performancemonitoring.soh.control.configuration;

/**
 * Just a convenience interface for all Capability configuration *Option objects.
 * <p>
 * This will help prevent cast warnings buy eliminating the need to cast an unknown Option object
 * to what we think it should be.
 */
public interface CapabilitySohRollupOption {

  /**
   * The rollup operator inside this option class. Convenience method used for configu resolution.
   */
  RollupOperator rollupOperator();

}
