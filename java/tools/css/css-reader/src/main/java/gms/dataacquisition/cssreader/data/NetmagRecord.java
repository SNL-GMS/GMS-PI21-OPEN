package gms.dataacquisition.cssreader.data;

import gms.shared.frameworks.osd.coi.event.MagnitudeType;
import org.apache.commons.lang3.Validate;


/**
 * Represents the 'Netmag' table in CSS
 */
public class NetmagRecord extends MagRecord {

  protected static int recordLength;

  int magid;
  String net;
  int orid;
  int evid;
  MagnitudeType magtype;
  int nsta;
  double magnitude;
  double uncertainty;
  private static final double UNCERTAINTY_NA_VALUE = -1;

  public void validate() {
    Validate.notNaN(getMagid(), "magid is NaN");
    Validate.notNull(getNet(), "net is null");
    Validate.notNaN(getOrid(), "orid is NaN");
    Validate.notNaN(getEvid(), "evid is NaN");
    Validate.notNull(getMagtype(), "magtype is null");
    Validate.notNaN(getNsta(), "nsta is NaN");
    Validate.notNaN(getMagnitude(), "magnitude is NaN");
    Validate.notNaN(getUncertainty(), "uncertainty is NaN");
    Validate.notNull(getAuth(), "auth is null");
    Validate.notNaN(getCommid(), "commid is NaN");
    Validate.notNull(getLddate(), "lddate is null");
  }

  public int getMagid() {
    return magid;
  }

  public void setMagid(int magid) {
    this.magid = magid;
  }

  public String getNet() {
    return net;
  }

  public void setNet(String net) {
    this.net = net;
  }

  public int getOrid() {
    return orid;
  }

  public void setOrid(int orid) {
    this.orid = orid;
  }

  public int getEvid() {
    return evid;
  }

  public void setEvid(int evid) {
    this.evid = evid;
  }

  public MagnitudeType getMagtype() {
    return magtype;
  }

  public void setMagtype(MagnitudeType magtype) {
    this.magtype = magtype;
  }

  public int getNsta() {
    return nsta;
  }

  public void setNsta(int nsta) {
    this.nsta = nsta;
  }

  public double getMagnitude() {
    return magnitude;
  }

  public void setMagnitude(double magnitude) {
    this.magnitude = magnitude;
  }

  public double getUncertainty() {
    return uncertainty;
  }

  public void setUncertainty(double uncertainty) {
    this.uncertainty = uncertainty;
  }

  public static double getUncertaintyNaValue() {
    return UNCERTAINTY_NA_VALUE;
  }

  @Override
  public String toString() {
    return "NetmagRecord{" +
      "magid=" + magid +
      ", net='" + net + '\'' +
      ", orid=" + orid +
      ", evid=" + evid +
      ", magtype=" + magtype +
      ", nsta=" + nsta +
      ", magnitude=" + magnitude +
      ", uncertainty=" + uncertainty +
      ", auth='" + auth + '\'' +
      ", commid=" + commid +
      ", lddate=" + lddate +
      '}';
  }
}
