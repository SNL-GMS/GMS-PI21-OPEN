package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;
import gms.shared.frameworks.osd.coi.event.MagnitudeType;

import java.time.Instant;

@PositionalRecord
public class NetmagRecordP3 extends NetmagRecord {

  private static final int RECORD_LENGTH = 114;

  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 9)
  public int getMagid() {
    return magid;
  }

  public void setMagid(String val) {
    this.magid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 11, finalPosition = 18)
  public String getNet() {
    return net;
  }

  @Override
  public void setNet(String val) {
    this.net = val;
  }

  @Override
  @PositionalField(initialPosition = 20, finalPosition = 28)
  public int getOrid() {
    return orid;
  }

  public void setOrid(String val) {
    this.orid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 30, finalPosition = 38)
  public int getEvid() {
    return evid;
  }

  public void setEvid(String val) {
    this.evid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 40, finalPosition = 45)
  public MagnitudeType getMagtype() {
    return magtype;
  }

  public void setMagtype(String val) {
    this.magtype = CssReaderUtility.getMagnitudeType(val);
  }

  @Override
  @PositionalField(initialPosition = 47, finalPosition = 54)
  public int getNsta() {
    return nsta;
  }

  public void setNsta(String val) {
    this.nsta = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 56, finalPosition = 62)
  public double getMagnitude() {
    return magnitude;
  }

  public void setMagnitude(String val) {
    this.magnitude = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 64, finalPosition = 70)
  public double getUncertainty() {
    return uncertainty;
  }

  public void setUncertainty(String val) {
    this.uncertainty = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 72, finalPosition = 86)
  public String getAuth() {
    return auth;
  }

  @Override
  public void setAuth(String val) {
    this.auth = val;
  }

  @Override
  @PositionalField(initialPosition = 88, finalPosition = 96)
  public int getCommid() {
    return commid;
  }

  public void setCommid(String val) {
    this.commid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 98, finalPosition = 114)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String val) {
    this.lddate = CssReaderUtility.parseDate(val).orElse(null);
  }
}
