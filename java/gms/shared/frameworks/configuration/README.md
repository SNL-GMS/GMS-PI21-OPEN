# Purpose

The GMS Processing Configuration Service is used to store and retrieve processing configuration in a PostgreSQL database.  It allows services to reload modified processing configuration without requiring services to recompile. 

# Goals

1. **Uniform abstraction and usage**: interaction with the Processing Configuration Service occurs through well-defined code-level abstractions (Java interfaces).  The caller should not need to deal with underlying details such as transport mechanism (e.g. HTTP) or storage layer (e.g. SQL).  This abstraction also covers the means in which the caller communicates with the service, i.e. the interface is the same even when the transport means varies (such as service communication versus direct database communication).
2. **Centralize location for Processing Configuration Files** All processing configuration files are stored in a common location.  This simplifies loading and updating configuration.  Most importantly, it needs to be external from the client service requesting the configuration, so that changes to the configuration do not require recompile. 
3. **Storage of Processing Configuration in PostgreSQL** Storage in a relational database allows for enforcing business constraints on the data and allows for querying of processing configuration.
4. **Service endpoints for update/retrieval of procesing configuration** Service endpoints allow data to be stored/retreived through well known protocols with need for restarting the service.
5. **Processing Configuration Loader** The processing configuration loader allows users to upload configuration from a local directory, without the need to commit files to the repository and rebuild the image.

# Structure

## `commonobjects`

Defines the Common Object Interface (COI) objects for Processing Configuration.  These objects are what are sent/received in all Processing Configuration operations.

## `api`

Defines the Processing Configuration Service interfaces.  Different classes can extend this interface if the datasource for the processing configuration changes.  (FileSystem, Database, etc...)

## `repository`

Contains implementations of the Processing Configuration interfaces, which use storage technologies such as [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API)/[Hibernate](https://en.wikipedia.org/wiki/Hibernate_(framework)) to store and retrieve COI objects.

The DAO objects that map to the Database as well as converters for transformations from COI - DAO are also stored in this project.

## `service`

Runnable application that executes `frameworks-configuration-repository` as an HTTP service.