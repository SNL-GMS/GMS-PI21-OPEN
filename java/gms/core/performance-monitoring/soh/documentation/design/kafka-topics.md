# Kafka Topics used for SOH Processing

Three Kafka instances are configured in the `docker-compose-swarm-soh.yml` 
file: `kafka1`, `kafka2` and `kafka3`. The configuration is similar for each instance. The topics
are defined in the `KAFKA_CREATE_TOPICS` property. Note we only use 1 partition for each Kafka topic,
this is defined via the `:1:3` shown below with 1 and 3 being the partitions and Kafka instances 
respectively. Currently, there are no message keys being used and a default Kafka retention 
time of 7 days is used.


```
  kafka1:
    image: ${CI_DOCKER_REGISTRY}/gms-common/bitnami-kafka:${VERSION}
    environment:
      SITE_DOMAIN: ${SITE_DOMAIN}
      HOSTNAME_COMMAND: "docker info | grep ^Name: | cut -d' ' -f 2"
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zoo:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT
      KAFKA_LISTENERS: INTERNAL://:9092
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka1:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_CREATE_TOPICS: >
        soh.rsdf:1:3,soh.acei:1:3,soh.extract:1:3,soh.waveform:1:3,
        soh.station-soh:1:3,soh.ack-station-soh:1:3,soh.capability-rollup:1:3,
        soh.quieted-list:1:3,soh.status-change-event:1:3,system.system-messages:1:3,
        malformed.frames:1:3,soh.ui-materialized-view:1:3,soh.quieted-status-change:1:3
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
      KAFKA_LOG_RETENTION_HOURS: 6
      KAFKA_LOGS_DIR: /kafka/kafka_logs
      ENABLE_METRICS: "on"
    deploy:
      labels:
        com.docker.ucp.access.label: ${COLLECTION}
        gms.startup.stage: 1
      restart_policy:
        condition: on-failure
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - kafka1-volume:/kafka
```

There are a number of global Kafka-related properties defined in `gms-system-configuration.properties`. 
Additionally, you can review this file for service-specific Kafka-related properties. These property
values are used in various Java services.

```
# global kafka properties for producers/consumers
kafka-bootstrap-servers = kafka:9092
kafka-compression-type = gzip
kafka-key-serializer = org.apache.kafka.common.serialization.StringSerializer
kafka-value-serializer = org.apache.kafka.common.serialization.StringSerializer
kafka-key-deserializer = org.apache.kafka.common.serialization.StringDeserializer
kafka-value-deserializer = org.apache.kafka.common.serialization.StringDeserializer

# session timeout for consumers (default to 10s measured in ms)
kafka-consumer-session-timeout = 10000

# heartbeat interval measured milliseconds
kafka-consumer-heartbeat-interval = 3000

# kafka properties
verification-attempts = 15
streams-close-timeout-ms = 120000
connection-retry-count = 10
retry-backoff-ms = 1000
```

### `StationSohControl`

##### Kafka Topics

INPUT:

- `soh.extract`
   - This topic contains a collection of `AcquiredStationSohExtract` objects that are processed in 
   the `monitor` method of the `StationSohControl` class.
   
OUTPUT:

- `soh.station-soh`
   - Following the processing of `AcquiredStationSohExtract` data, the service publishes `StationSoh`, 
   one object at a time, to this topic. 
   These messages are consumed by the `StationSohAnalysisManagerControl` service.
- `soh.capability-rollup`
   - Following the processing of `AcquiredStationSohExtract` data, the service publishes `CapabilitySohRollup` 
     objects are published,  one object at time, to this topic. These messages are consumed by the `StationSohAnalysisManagerControl` 
     service.
 
### `StationSohAnalysisManagerControl`

##### Kafka Topics

INPUT:

- `soh.station-soh`
   - The service consumes `StationSoh` objects off this topic. `StationSoh` objects are cached and 
   published to the `soh.ui-materialized-view` topic every processing period.
- `soh.capability-rollup`
   - The service consumes `CapabilitySohRollup` objects off this topic. `CapabilitySohRollup` 
   objects are cached and published to the `soh.ui-materialized-view` topic every processing period.
- `soh.ack-station-soh`
   - The service consumes `AcknowledgedSohStatusChanged` objects off this topic. The service publishes 
   messages to the `soh.ui-materialized-view` if the station exists in the cache. These messages
    represent stations that have been "acknowledged" via the UI.
- `soh.quieted-list`
   - The service consumes `QuietedSohStatusChangeUpdate` objects off this topic. The service publishes 
   to the `soh.ui-materialized-view` if the station exists in the cache. These messages represent
   channels that have been "quieted" for a period of time via the UI.

OUTPUT:

- `soh.ui-materialized-view`
   - The service publishes a List of `UiStationAndStationGroups`, one object at a time, 
   to this topic every processing period, which are consumed by the UI.
- `soh.quieted-status-change`
   - The service publishes a collection of `QuietedSohStatusChange` objects, one at a time, on this 
   topic. These messages represent a change to a channel's "quieted" status and are consumed by the 
   UI and the `QuietedSohStorageConsumer`, which stores `QuietedSohStatusChange` objects in the database.
- `soh.status-change-event`.
   - The service publishes a collection of `UnacknowledgedSohStatusChange` objects, one at a time, 
   on this topic. These messages represent a change to a station's "unacknowledged" status and are 
   consumed by the UI and the `UnacknowledgedSohStatusChangeStorageConsumer`, which stores the 
   `UnacknowledgedSohStatusChange` objects in the database.
- `system.system-messages`
   - The service creates a collection of `SystemMessage` and publishes, one object at a time, 
   to this topic. These messages are then consumed by the UI and the `SystemMessageStorageConsumer`, 
   which stores the `SystemMessage`s in the database. 

### `AceiMergeProcessor`
 
##### Kafka Topic

INPUT:

- `soh.acei`
   - This service consumes `AcquiredChannelEnvironmentIssue` objects off this topic, which may be 
   instances of one of two sub-types: either `AcquiredChannelEnvironmentIssueBoolean` 
   or `AcquiredChannelEnvironmentIssueAnalog`. The `AceiMergeProcessor` merges related 
   `AcquiredChannelEnvironmentIssueBoolean` instances in order to reduce the amount of data stored 
   in the database. There are both `delete` and `insert` operations for this sub-type. 
   `AcquiredChannelEnvironmentIssueAnalog` are not merged at this point and an `insert` operation is 
   always performed for this sub-type. There is no Kafka output topic for this service because the 
   database operations are called directly using endpoints defined in the `StationSohRepositoryInterface`.
 