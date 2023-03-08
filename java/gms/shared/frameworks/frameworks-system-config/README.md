# <sup> GMS Frameworks </sup><br>**System Configuration**

The *System Configuration* framework is a **key-value** store for GMS
system configuration values.

* A single **SystemConfig** class provides access to configuration
  *values* based on *key* names.
* The **SystemConfig** is tied to a *component name* on construction.
* **Key-value** pairs may be scoped to the *component name* with
  **key-values** specific to a *component name* overriding generic
  **key-value** definitions.
* Configuration *values* may be returned in a variety of types
  (*String*, *int*, *long*, *double*, *boolean*, *Path*).
* A **MissingResourceException** is thrown if no value can be
  found for a requested key.

Configuration **key-values** are stored in a hierarchy of backend
repositories:

* Configuration values are primarily managed via an [**etcd
  server**](https://etcd.io).
* Configuration values can be overriden for development and testing
  with a local `configuration_overrides.properties` file in your home
  directory.
* Configuration values can also be overriden via select environment 
  variables.

## Usage

* Construct a **SystemConfiguration** client, specifying the *name* of the component
  it is associated with:
  ```java
  SystemConfig systemConfig = SystemConfig().create("component-name");
  ```

* Retrieve a system configuration value:
  ```java
  try {
      String host = systemConfig.getValue(SystemConfig.HOST);
  } catch (MissingResourceException e) {
      logger.error(e.toString());
  }
  ```
  
* Retrieve a system configuration value as an *integer*:
  ```java
  try {
      int port = systemConfig.getValueAsInt(SystemConfig.PORT);
  } catch (MissingResourceException | NumberFormatException e) {
      logger.error(e.toString());
  }
  ```

* Retrieve a system configuration value as an *long*:
  ```java
  try {
      long value = systemConfig.getValueAsInt("key");
  } catch (MissingResourceException | NumberFormatException e) {
      logger.error(e.toString());
  }
  ```
  
* Retrieve a system configuration value as an *double*:
  ```java
  try {
      double value = systemConfig.getValueAsDouble("key");
  } catch (MissingResourceException | NumberFormatException e) {
      logger.error(e.toString());
  }
  ```

* Retrieve a system configuration value as an *boolean*:
  ```java
  try {
      boolean value = systemConfig.getValueAsBoolean("key");
  } catch (MissingResourceException | IllegalFormatException e) {
      logger.error(e.toString());
  }
  ```
  
## Common Configuration Names

Several common configuration names are defined as static strings in
the **SystemConfig** class for reference.

| Name                             | Type   | Description |
|:---------------------------------|:-------|:------------|
|**HOST**                          | String | Service host identifier. |
|**PORT**                          | int    | Port at which a service is provided. |
|**IDLE_TIMEOUT_MILLIS**           | int    | The number of milliseconds before a request should time out. |
|**MIN_THREADS**                   | int    | The minimum number of threads that should be available to satisfy service requests. |
|**MAX_THREADS**                   | int    | The maximum number of threads that should be available to satisfy service requests. |
|**PROCESSING_CONFIGURATION_ROOT** | Path   | File system location of the processing configuration. |

## Scope and Overrides

Key lookup is scoped so that a **key-value** definition specific to a
component name overrides a generic **key-value** definition.

Consider the following key-value pairs:

```properties
 port = 8080
 
 detection.host = detection
 detection.port = 80
  
 correlation.host = correlation
```
In this example:
* The value of *"port"* resolved for the *detection* component would be *80*. 
  * Since a component-specific key `detection.port` was defined, that value overrides
    the value for `port`.
* The value of *"port"* resolved for the *correlation* component would be *8080*.
  * Since `correlation.port` was not defined, the value for `port` is returned. 

In Java:
```java
SystemConfig detectionConfig = SystemConfig().create("detection");
int port = detectionConfig.getValueAsInt("port");  // would return 80

SystemConfig correlationConfig = SystemConfig().create("correlation");
int port = correlationConfig.getValueAsInt("port");  // would return 8080
```

*Key-value* definitions can also be overridden for development by
specifying them in a *configuration_overrides.properties* text file in
your home directory.

Key resolution will follow this scoping, with the key resolving to the first
found definition:

* Component-specific value from **Environment**
  * Component-specific value from **Overrides File**
    * Component-specific value from **Etcd**
      * Generic value from **Environment**
        * Generic value from **Overrides File**
          * Generic value from **Etcd**

## Environment Variable Naming

System configuration keys may contain characters such as `.` or `-`
which are unsupported in environment variable names. 

Environment variable names are based on the configuration name but
with the following modifications:
 * Be prefixed with 'GMS_CONFIG_'
 * Be converted to all capital letters.
 * Dashes will be translated to single-underscores.
 * Dots will be translated to double-underscores.

For example, this configuration value:
```properties
 correlation.secondary-port = 9012
```
Could be overridden via an environment variable:
```bash
  GMS_CONFIG_CORRELATION__SECONDARY_PORT = 9018
```
Take care not to override generic values.  For example, setting the
environment variable `GMS_CONFIG_HOST` would override **all** `.host`
values for all components in the system.

## Etcd

The key value store is managed as an [**etcd**](https://etcd.io)
service. The service endpoint by default will resolve to *etcd:2379*.
In a docker swarm cluster, this will resolve to the local system etcd
container.

During development, you may set an environment variable
**GMS_ETCD_ENDPOINTS** to point to a specific etcd service
endpoint.

* Run `export GMS_ETCD_ENDPOINTS=localhost:2379` to use a local etcd
  server running on your development machine.

