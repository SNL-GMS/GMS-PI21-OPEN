package gms.shared.frameworks.osd.coi.soh;

/**
 * Represents overall state of health.
 */
public enum SohStatus {
  /**
   * These are ordered Worst to Best.
   * The dbId maps to id values in the DB...if you change these, they DB must be updated as well
   */
  BAD((short) 0),
  MARGINAL((short) 1),
  GOOD((short) 2);

  private short dbId;

  public short getDbId() {
    return this.dbId;
  }

  SohStatus(short dbId) {
    this.dbId = dbId;
  }

}
