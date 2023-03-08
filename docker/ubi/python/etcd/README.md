GMS etcd Docker Image

The **etcd** Docker image is built to provide a key value service
backing for GMS System configuration.  An instance of etcd with loaded
configuration must be available in given cluster running the GMS system.

The **etcd** image is built automatifcally from this directory as part
of the system builds.

The following command may be run to build a fresh **local** docker
container using your local system configuration values. For example,
this would be useful for testing with system configuration changes not
yet incorporated into the system build.

```bash
local-docker-build.sh
```
## Details

This **Dockerfile** create a GMS-specific **etcd** Docker image based
on the base GMS UBI8 Python 3.7 image with the following
changes applied:

- Install and configure **etcd** 
- Configure **etcd** users and permissions
- Load latest configuration values ($GMS_COMMON/config/system)[../../config/system]

Once the container is up and running, three users exist:
- User `root` is all powerful and can do anything.
- User `gmsadmin` has full readwrite permissions to all keys.
- User `gms` has read-only permissions to all the keys.

The passwords for those etcd users are set by specifying the following environment variables when the container is built:
- `GMS_ETCD_PASSWORD` sets the `gms` user's password.
- `GMS_ETCD_ADMIN_PASSWORD` sets the `gmsadmin` user's password.
- `GMS_ETCD_ROOT_PASSWORD` sets the `root` user's password.

## Building the Container:

The container requires the `gms-sysconfig` Python code to load the
configuration, as well as the configuration values.

```bash
DOCKER_REGISTRY=///
VERSION=///
$ docker-build-prep.sh
$ docker build -t ${DOCKER_REGISTRY}/etcd}:${VERSION} . 
```

## Docker Compose
```
  etcd:
    image: "${CI_DOCKER_REGISTRY}/etcd:${VERSION}"
    deploy:
      restart_policy:
        condition: on-failure
```
