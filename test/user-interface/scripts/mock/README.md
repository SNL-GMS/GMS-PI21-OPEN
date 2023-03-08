# Mock Data Servers

The mock data servers can be run by using Docker Compose.

## Using Mocks

The preferred way to use mocks is to use yarn, as described in the [user-interface README](../../../../typescript/user-interface/README.md). This page describes the mocks in more detail, should you want to run them manually.

## Version

By default, the mocks use `develop` images. If you want to use a different image, you can set the `VERSION` environment variable.

## SOH Mock

To start or stop the SOH mock producer using Docker images from the `develop` branch, use

```sh
GMS_UI_MODE=soh yarn mock boot

yarn mock stop
```

This will launch the prerequisites for the frameworks data injector service...

- etcd, for service configuration
- Kafka
- Zookeeper
- the JSON mock server, for user preferences, system messages and other assorted data

... and build and run the frameworks data injector service.

## IAN Mock

To start or stop the IAN mock producer using Docker images from the `develop` branch, run

```sh
docker-compose -f ian-mock-compose.yaml up

docker-compose -f ian-mock-compose.yaml down
```

The IAN mock server includes

- etcd, for the waveform service configuration
- the JSON mock server, for user preferences, system messages and other assorted data
- the waveform mock service, for waveform mocks
