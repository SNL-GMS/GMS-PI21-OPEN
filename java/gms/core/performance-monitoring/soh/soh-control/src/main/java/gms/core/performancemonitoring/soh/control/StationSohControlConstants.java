package gms.core.performancemonitoring.soh.control;

import java.time.Duration;

/**
 * Holds constants used for Soh control.
 */
final class StationSohControlConstants {

  private StationSohControlConstants() {

  }

  static final String APPLICATION_ID = "application-id";
  static final String KAFKA_BOOTSTRAP_SERVERS = "kafka-bootstrap-servers";
  static final String INPUT_TOPIC = "sohInputTopic";
  static final String INPUT_TOPIC_DEFAULT = "soh.extract";
  static final String STATION_SOH_OUTPUT_TOPIC = "stationSohOutputTopic";
  static final String STATION_SOH_OUTPUT_TOPIC_DEFAULT = "soh.station-soh";
  static final String CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC = "capabilitySohRollupOutputTopic";
  static final String CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC_DEFAULT = "soh.capability-rollup";
  static final String MONITOR_LOGGING_PERIOD = "monitorLoggingPeriod";

  // How ofter to output logging stats in the monitor method. This might be made a
  // configurable parameter. But to start, use 10 minutes.
  static final Duration MONITOR_LOGGING_DEFAULT_OUTPUT_PERIOD = Duration.ofMinutes(10);

  static final String MONITOR_LOGGING_FORMAT =
    "MONITOR STATS (period %02d:%02d): %d calls to monitor() of (%.2f, %.2f, %.2f) seconds " +
      "with (%d, %.1f, %d) extracts, " +
      "(%.2f, %.2f, %.2f) msec StationSoh computation time, (%.2f, %.2f, %.2f) msec " +
      "CapabilitySohRollup computation time";

}
