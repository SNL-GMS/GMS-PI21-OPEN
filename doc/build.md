# GMS-Common Build Instructions

Follow these instructions on your local machine to build and run the
SOH deployment for **gms-common**.  The build process results in a
series of tagged Docker container images.

By default, containers are built locally but may optionally be pushed
to a remote repository.

## Build tools

There are two scripts in the repository root directory that will 
facilitate the build:

* **install-gms-prereqs**<BR>
  This script installs the following external dependencies required
  to build GMS

  | Dependency                              | 
  |:----------------------------------------|
  | [**Docker**](https://www.docker.com)    |
  
  **Note**: Additional dependent software libraries are downloaded by the 
  Docker, Gradle, and Python tools in the process of building.

* **build-gms <all|docker|java|python|cpp|wasm|typescript>**<BR>
  This script is used to build all or part of gms.  The script will set all
  required build enviornment variables if they are not already set or overriden.

## Environment

These instructions assume a bash shell.

1. Rename the directory where you cloned the **GMS-PI16-OPEN** repository 
   to **gms-common**
2. Source the `gms-common/.bash_env` file
3. Add the sourcing of `gms-common/.bash_env` to your **~/.bashrc** file

Depending on your configuration, you will also need to set one or more
of the following environment variables.  If these variables are NOT set,
the **build-gms** script will use default values.

* **CI_DOCKER_REGISTRY**<br>
  This should be set to the name of a remote docker registry used when
  building.  If not using a remote registry, set this to `local`.  This
  is the location where all docker images created during the build process
  are saved. <br>
  Example: *gms-docker-registry.example.com* or *local*

* **CI_REMOTE_REPOSITORY_URL**<br>
  If a repository mirror is available and configured (such as
  Artifactory or Nexus) this should be set to the URL of the remote
  repository. If this variable is **NOT set**, external dependencies
  required for the build will be gathered from the open internet.<br>
  Example: *https://artifactory.example.com/artifactory*

* **CI_THIRD_PARTY_DOCKER_REGISTRY**<br>
  For base images (such as *centos*, *zookeeper*, or *traefik*) you
  may specify a repository from which those base images should be
  obtained. If not specified, base images will be obtained from
  the registry specified by **CI_DOCKER_REGISTRY**. 
  Example: *unset* or *registry-1.docker.io/library* or *docker.io*

* **CI_USE_PROXY**<br>
  If building behind a simple HTTP proxy server, setting CI_USE_PROXY=1
  will enable the use of the system proxy variables `http_proxy`, 
  `https_proxy`, and `no_proxy`. Note that this will not work if your
  proxy server does SSL interception.

## Install dependencies
* **Run the Provided Script to Install Dependencies**<br>
  **Note**:  ** GMS_COMMON_HOME** refers the the directory name where
  you cloned the repository (this is likely gms-common if you renamed
  it as suggested in the **Environment** section above.)

  ```bash
  $ cd ${GMS_COMMON_HOME}
  $ ./install-gms-prereqs
  ```

## Building

* **Build Base Containers**
  ```bash
  $ cd ${GMS_COMMON_HOME}/docker
  $ build-gms docker
  ```

* **Build Java**
  ```bash
  $ cd ${GMS_COMMON_HOME}
  $ build-gms java
  ```

* **Build C++**
  ```bash
  $ cd ${GMS_COMMON_HOME}
  $ build-gms cpp
  ```

* **Build WASM**
  ```bash
  $ cd ${GMS_COMMON_HOME}
  $ build-gms wasm
  ```

* **Build Typescript**
  ```bash
  $ cd ${GMS_COMMON_HOME}
  $ build-gms typescript
  ```
  
* **Build Python**
  ```bash
  $ cd ${GMS_COMMON_HOME}
  $ build-gms python
  ```

* **Build All/Everything**
  ```bash
  $ cd ${GMS_COMMON_HOME}
  build-gms all
  ```
## Verify/Push Images
* **Verify Where Images are Saved**
  ```bash
  $ echo $CI_DOCKER_REGISTRY
  ```
  If the value of this variable is **local**, check the local registry:
  ```bash
  $ docker images | grep opensource
  ```
  and you should see all of the docker images the build process created.

* **Push Images to registry**
  ```bash
  $ build-gms push_docker
  ```
