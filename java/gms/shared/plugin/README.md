## Plugin Registry

Operation-specific **Plugin** interfaces should extend the base
**Plugin** interface.
* Each **Plugin** must implement a `getName()` method that returns a
  string identifying the plugin.

* Additional methods specific to the processing operation should be
  defined in this interface and implemented by every class that
  implements the interface.

Plugins implemented are automatically discovered and can be looked up
via the **Plugin Registry**.

* **Plugin Registry** lookup by name:
  ```java
  PluginRegistry registry = context.getPluginRegistry();
  MyPluginInterface plugin = registry.get("specific-name", MyPluginInterface.class);
  ```

* **Plugin Registry** lookup, given a list of names:
  ```java
  PluginRegistry registry = context.getPluginRegistry();
  Collection<MyPluginInterface> plugins = registry.get(Set.of("one-name", "another-name"), MyPluginInterface.class);
  ```

**Plugin Registry** retrieval operations throw
`IllegalArgumentException` if a plugin is not found by the provided
name or it is not of the required class (i.e. can't be casted to that
type).
