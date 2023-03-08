package gms.dataacquisition.cssreader.data;


import gms.shared.frameworks.osd.coi.event.MagnitudeModel;
import gms.shared.frameworks.osd.coi.event.MagnitudeType;
import org.apache.commons.lang3.Validate;

public class StamagRecord extends MagRecord {

  int magid;
  int ampid;
  protected String sta;
  protected int arid;
  int orid;
  int evid;
  protected String phase;
  double delta;
  MagnitudeType magtype;
  double magnitude;
  double uncertainty;
  double magres;
  boolean magdef;
  MagnitudeModel mmodel;


  private static final double MAGNITUDE_NA_VALUE = -999;
  private static final double UNCERTAINTY_NA_VALUE = -1;
  private static final double RES_NA_VALUE = -999.0;

  public void validate() {
    Validate.notNaN(getMagid(), "magid is NaN");
    Validate.notNaN(getAmpid(), "ampid is NaN");
    Validate.notNull(getSta(), "sta is null");
    Validate.notNaN(getArid(), "arid is NaN");
    Validate.notNaN(getOrid(), "orid is NaN");
    Validate.notNaN(getEvid(), "evid is NaN");
    Validate.notNull(getPhase(), "phase is null");
    Validate.notNaN(getDelta(), "delta is NaN");
    Validate.notNull(getMagtype(), "magtype is null");
    Validate.notNaN(getMagnitude(), "magnitude is NaN");
    Validate.notNaN(getUncertainty(), "uncertainty is NaN");
    Validate.notNaN(getMagres(), "magres is NaN");
    Validate.notNull(getMagdef(), "magdef is null");
    Validate.notNull(getMmodel(), "mmodel is null");
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

  public int getAmpid() {
    return ampid;
  }

  public void setAmpid(int ampid) {
    this.ampid = ampid;
  }

  public String getSta() {
    return sta;
  }

  public void setSta(String sta) {
    this.sta = sta;
  }

  public int getArid() {
    return arid;
  }

  public void setArid(int arid) {
    this.arid = arid;
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

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public double getDelta() {
    return delta;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }

  public MagnitudeType getMagtype() {
    return magtype;
  }

  public void setMagtype(MagnitudeType magtype) {
    this.magtype = magtype;
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

  public double getMagres() {
    return magres;
  }

  public void setMagres(double magres) {
    this.magres = magres;
  }

  public boolean getMagdef() {
    return magdef;
  }

  public void setMagdef(boolean magdef) {
    this.magdef = magdef;
  }

  public MagnitudeModel getMmodel() {
    return mmodel;
  }

  public void setMmodel(MagnitudeModel mmodel) {
    this.mmodel = mmodel;
  }

  public static double getMagnitudeNaValue() {
    return MAGNITUDE_NA_VALUE;
  }

  public static double getUncertaintyNaValue() {
    return UNCERTAINTY_NA_VALUE;
  }

  public static double getResNaValue() {
    return RES_NA_VALUE;
  }

  @Override
  public String toString() {
    return "StamagRecord{" +
      "magid=" + magid +
      ", ampid=" + ampid +
      ", sta='" + sta + '\'' +
      ", arid=" + arid +
      ", orid=" + orid +
      ", evid=" + evid +
      ", phase='" + phase + '\'' +
      ", delta=" + delta +
      ", magtype=" + magtype +
      ", magnitude=" + magnitude +
      ", uncertainty=" + uncertainty +
      ", magres=" + magres +
      ", magdef=" + magdef +
      ", mmodel=" + mmodel +
      ", auth='" + auth + '\'' +
      ", commid=" + commid +
      ", lddate=" + lddate +
      '}';
  }
}
