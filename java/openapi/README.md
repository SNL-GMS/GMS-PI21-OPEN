# OpenAPI Swagger Server

The purpose of this service is to host and serve API definition files generated
by the `gradle alljavadoc resolve` command in the `/java` directory of GMS

## Building all service 

To build, run:

```
cd ../java
gradle alljavadoc resolve
# Prep for openapi doc
mkdir spec
cp $(find . -regex '.*build/swagger/.*\.json') spec
cd ../openapi
make
```
## Building single service
The steps are nearly the same as above except you can first cd into the api directory.
For example, to build the station definition service swagger docs:
```
cd ~/gms-common/java/gms/shared/station-definition/station-definition-api/
gradle alljavadoc resolve
# Prep for openapi doc
cd ~/gms-common/java
mkdir spec
cp $(find . -regex '.*build/swagger/.*\.json') spec
cd ../openapi
make
```
This invokes Gradle which generates the necessary json files and then copies
them into the scope of the openapi Dockerfile. Once these are in the correct
location, the docker image is built.

Run the previously built image with the `docker run` command. Then open http://localhost in your browser, and you should be greeted with an
interface to navigate the API definitions.
