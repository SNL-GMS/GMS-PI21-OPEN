package gms.shared.plugin;


/**
 * Interface common to all plugins in GMS.  Subsequent layers of Plugin functionality will extend
 * this interface and then add the corresponding functions that are required
 */
public interface Plugin {

  /**
   * Gets the name of the plugin, used for looking it up.
   *
   * @return the name of the plugin
   */
  default String getName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Function that allows for a Plugin to do anything specific it needs before being used. Will be
   * called by the class that contains the Registry most likely once the config of active plugins
   * has been read.
   */
  void initialize();

}
