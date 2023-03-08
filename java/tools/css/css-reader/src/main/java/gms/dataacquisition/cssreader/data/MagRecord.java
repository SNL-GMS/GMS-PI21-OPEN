package gms.dataacquisition.cssreader.data;

import java.time.Instant;

public class MagRecord {

  protected String auth;
  protected int commid;
  protected Instant lddate;

  /**
   * No default implementations for MagRecord outside of package
   */
  protected MagRecord() {
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth = auth;
  }

  public int getCommid() {
    return commid;
  }

  public void setCommid(int commid) {
    this.commid = commid;
  }

  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(Instant lddate) {
    this.lddate = lddate;
  }


}
