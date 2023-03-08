package gms.shared.frameworks.test.utils.services;

/**
 * GMS Service types used for component and integration TestContainers.
 */
public enum GmsServiceType {
  ACEI_MERGE_PROCESSOR("acei-merge-processor"),
  BASTION("bastion"),
  CONFIG_LOADER("config-loader"),
  CONNMAN("da-connman"),
  DATAMAN("da-dataman"),
  DATAPROVIDERFILE("cd11-data-provider-file"),
  DATAPROVIDERKAFKA("cd11-data-provider-kafka"),
  ETCD("etcd"),
  INTERACTIVE_ANALYSIS_API_GATEWAY("interactive-analysis-api-gateway"),
  UI_PROCESSING_CONFIGURATION_SERVICE("ui-processing-configuration-service"),
  INTERACTIVE_ANALYSIS_UI("interactive-analysis-ui"),
  KAFKA_ONE("kafka1"),
  OSD_RSDF_KAFKA_CONSUMER("frameworks-osd-rsdf-kafka-consumer"),
  OSD_SERVICE("frameworks-osd-service"),
  OSD_STATION_SOH_KAFKA_CONSUMER("frameworks-osd-station-soh-kafka-consumer"),
  POSTGRES_SERVICE("postgresql-gms"),
  POSTGRES_EXPORTER("postgresql-exporter"),
  PROCESSING_CONFIG_SERVICE("frameworks-configuration-service"),
  RSDF_STREAMS_PROCESSOR("cd11-rsdf-processor"),
  SOH_CONTROL("soh-control"),
  ZOOKEEPER("zoo");

  private final String type;

  GmsServiceType(String type) {
    this.type = type;
  }

  public static GmsServiceType getEnum(String type) {
    for (GmsServiceType serviceType : values()) {
      if (serviceType.toString().equalsIgnoreCase(type)) {
        return serviceType;
      }
    }

    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return this.type;
  }
}

