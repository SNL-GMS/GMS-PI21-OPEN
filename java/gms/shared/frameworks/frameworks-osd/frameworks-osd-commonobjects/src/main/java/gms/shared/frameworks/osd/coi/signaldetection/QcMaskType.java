package gms.shared.frameworks.osd.coi.signaldetection;

/**
 * Enumeration for types of{@link QcMask}
 */
public enum QcMaskType {
  SENSOR_PROBLEM,
  STATION_PROBLEM,
  CALIBRATION,
  STATION_SECURITY,
  TIMING,
  REPAIRABLE_GAP,
  REPEATED_ADJACENT_AMPLITUDE_VALUE,
  LONG_GAP,
  SPIKE
}
