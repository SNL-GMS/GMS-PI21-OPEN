package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;
import gms.shared.frameworks.osd.coi.event.MagnitudeModel;
import gms.shared.frameworks.osd.coi.event.MagnitudeType;

import java.time.Instant;

@PositionalRecord
public class StamagRecordP3 extends StamagRecord {

  private static final int RECORD_LENGTH = 167;

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
  @PositionalField(initialPosition = 11, finalPosition = 19)
  public int getAmpid() {
    return ampid;
  }

  public void setAmpid(String val) {
    this.ampid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 21, finalPosition = 26)
  public String getSta() {
    return sta;
  }

  @Override
  public void setSta(String val) {
    this.sta = val;
  }

  @Override
  @PositionalField(initialPosition = 28, finalPosition = 36)
  public int getArid() {
    return arid;
  }

  public void setArid(String val) {
    this.arid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 38, finalPosition = 46)
  public int getOrid() {
    return orid;
  }

  public void setOrid(String val) {
    this.orid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 48, finalPosition = 56)
  public int getEvid() {
    return evid;
  }

  public void setEvid(String val) {
    this.evid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 58, finalPosition = 65)
  public String getPhase() {
    return phase;
  }

  @Override
  public void setPhase(String val) {
    this.phase = val;
  }

  @Override
  @PositionalField(initialPosition = 67, finalPosition = 74)
  public double getDelta() {
    return delta;
  }

  public void setDelta(String val) {
    this.delta = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 76, finalPosition = 81)
  public MagnitudeType getMagtype() {
    return magtype;
  }

  public void setMagtype(String val) {
    this.magtype = CssReaderUtility.getMagnitudeType(val);
  }

  @Override
  @PositionalField(initialPosition = 83, finalPosition = 89)
  public double getMagnitude() {
    return magnitude;
  }

  public void setMagnitude(String val) {
    this.magnitude = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 91, finalPosition = 97)
  public double getUncertainty() {
    return uncertainty;
  }

  public void setUncertainty(String val) {
    this.uncertainty = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 99, finalPosition = 105)
  public double getMagres() {
    return magres;
  }

  public void setMagres(String val) {
    this.magres = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 107, finalPosition = 107)
  public boolean getMagdef() {
    return magdef;
  }

  public void setMagdef(String val) {
    this.magdef = "d".equals(val);
  }

  @Override
  @PositionalField(initialPosition = 109, finalPosition = 123)
  public MagnitudeModel getMmodel() {
    return mmodel;
  }

  public void setMmodel(String val) {
    this.mmodel = CssReaderUtility.getMagnitudeModel(val);
  }

  @Override
  @PositionalField(initialPosition = 125, finalPosition = 139)
  public String getAuth() {
    return auth;
  }

  @Override
  public void setAuth(String val) {
    this.auth = val;
  }

  @Override
  @PositionalField(initialPosition = 141, finalPosition = 149)
  public int getCommid() {
    return commid;
  }

  public void setCommid(String val) {
    this.commid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 151, finalPosition = 167)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String val) {
    this.lddate = CssReaderUtility.parseDate(val).orElse(null);
  }
}
