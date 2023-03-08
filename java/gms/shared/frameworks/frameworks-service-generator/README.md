# A GMS Frameworks library for running an instance of an Object who's class has proper GMS service annotations as a HTTP service.

The usage of this library is just:

```java
ServiceGenerator.runService(anObj, sysConfig);
```

This is typically used as the one-liner of a `main` method.  It is used to run components as a service that is not a 'Control' component (see [frameworks-control](../frameworks-control)).

This library uses reflection to inspect GMS-annotated methods and create HTTP routes based on them.  
It uses `SystemConfig` (see [frameworks-system-config](../frameworks-system-config)) to determine port and other HTTP server parameters (e.g. thread pool configuration).