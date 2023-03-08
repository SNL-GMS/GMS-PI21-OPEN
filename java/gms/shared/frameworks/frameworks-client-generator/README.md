# Library for generating client-side implementations of Component interfaces

This library provides a means to instantiate a client side implementation of a 'shared interface' (i.e. the interface of a component class and thus it's service contract as well) used to invoke a remote service.  This library is the complement to how `frameworks-service-generator` generates service wrappers based on a server side implementation of the component interface.

Usage example:

```java
SomeComponentInterface client = ClientGenerator.createClient(SomeComponentInterface.class);
```

`createClient` throws `IllegalArgumentException`'s when it can't instantiate the client, such as because of lack of configuration present or lack of required annotations on the component interface.