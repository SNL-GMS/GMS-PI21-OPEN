package gms.core.performancemonitoring.ssam.control;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.MissingResourceException;

/**
 * Configuration keys for all of the various Kafka topics that we produce/consume.
 */
public enum KafkaTopicConfigurationKeys {

  STATION_SOH_INPUT_TOPIC_KEY("soh_station_input_topic", "soh.station-soh"),
  ACKNOWLEDGED_SOH_STATUS_CHANGE_INPUT_TOPIC_KEY("status_change_input_topic",
    "soh.ack-station-soh"),
  QUIETED_SOH_STATUS_CHANGE_INPUT_TOPIC_KEY("quieted_list_input_topic", "soh.quieted-list"),
  CAPABILITY_SOH_ROLLUP_INPUT_TOPIC_KEY("capability_rollup_input_topic", "soh.capability-rollup"),
  SOH_SYSTEM_MESSAGE_UI_OUTPUT_TOPIC_KEY("system_message_ui_output_topic", "system-event"),
  SOH_SYSTEM_MESSAGE_OUTPUT_TOPIC_KEY("system_message_soh_output_topic", "system.system-messages"),
  STATION_SOH_ANALYSIS_VIEW_OUTPUT_TOPIC_KEY("materialized_view_output_topic",
    "system-event"),
  STATION_SOH_QUIETED_OUTPUT_TOPIC_KEY("quieted_status_change_output_topic",
    "soh.quieted-status-change"),
  STATION_SOH_STATUS_CHANGE_OUTPUT_TOPIC_KEY("status_change_output_topic",
    "soh.status-change-event"),

  STATION_SOH_ANALYSIS_MAX_RETRY_ATTEMPTS_KEY("retry-max-attempts",
    "10"),

  STATION_SOH_ANALYSIS_RETRY_BACKOFF_MS_KEY("retry-backoff-ms",
    "1000");

  private static final Logger logger = LoggerFactory.getLogger(KafkaTopicConfigurationKeys.class);

  private final String configKeyString;
  private final String defaultValue;

  public String getConfigKeyString() {
    return configKeyString;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  KafkaTopicConfigurationKeys(
    String configKeyString, String defaultValue
  ) {
    this.configKeyString = configKeyString;
    this.defaultValue = defaultValue;
  }

  /**
   * Get a value from the system config, returning a default value if not defined.
   */
  String getSystemConfigValue(
    SystemConfig systemConfig
  ) {

    String value = this.defaultValue;
    try {
      value = systemConfig.getValue(this.configKeyString);
    } catch (MissingResourceException e) {
      logger.warn("{} is not defined in SystemConfig, using default value: {}",
        this.configKeyString, this.defaultValue);
    }
    return value;
  }
}
