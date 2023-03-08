package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.soh.SohStatus;

/**
 * Represents overall state of health.
 */
public enum UiSohStatus {
  //
  // These are ordered Worst -> Best.
  //
  NONE,
  BAD,
  MARGINAL,
  GOOD;

  public static UiSohStatus from(SohStatus sohStatus) {
    if (sohStatus == null) {
      return UiSohStatus.NONE;
    }
    return UiSohStatus.valueOf(sohStatus.toString());
  }
}