package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;

@PositionalRecord
public class ArrivalRecordP3 extends ArrivalRecord {

  private static final int RECORD_LENGTH = 227;


  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 6)
  public String getSta() {
    return sta;
  }

  public void setSta(String val) {
    this.sta = val;
  }

  @Override
  @PositionalField(initialPosition = 8, finalPosition = 24)
  public Instant getTime() {
    return time;
  }

  public void setTime(String val) {
    this.time = CssReaderUtility.toInstant(val).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 26, finalPosition = 34)
  public int getArid() {
    return arid;
  }

  public void setArid(String val) {
    this.arid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 36, finalPosition = 43)
  public Instant getJdate() {
    return jdate;
  }

  public void setJdate(String val) {
    this.jdate = CssReaderUtility.jdToInstant(val).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 45, finalPosition = 53)
  public int getStassid() {
    return stassid;
  }

  public void setStassid(String val) {
    this.stassid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 55, finalPosition = 62)
  public int getChanid() {
    return chanid;
  }

  public void setChanid(String val) {
    this.chanid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 64, finalPosition = 71)
  public String getChan() {
    return chan;
  }

  public void setChan(String val) {
    this.chan = val;
  }

  @Override
  @PositionalField(initialPosition = 73, finalPosition = 80)
  public String getIphase() {
    return iphase;
  }

  public void setIphase(String val) {
    this.iphase = val;
  }


  @Override
  @PositionalField(initialPosition = 82, finalPosition = 82)
  public String getStype() {
    return stype;
  }

  public void setStype(String val) {
    this.stype = val;
  }

  @Override
  @PositionalField(initialPosition = 84, finalPosition = 89)
  public double getDeltim() {
    return deltim;
  }

  public void setDeltim(String val) {
    this.deltim = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 91, finalPosition = 97)
  public double getAzimuth() {
    return azimuth;
  }

  public void setAzimuth(String val) {
    this.azimuth = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 99, finalPosition = 105)
  public double getDelaz() {
    return delaz;
  }

  public void setDelaz(String val) {
    this.delaz = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 107, finalPosition = 113)
  public double getSlow() {
    return slow;
  }

  public void setSlow(String val) {
    this.slow = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 115, finalPosition = 121)
  public double getDelslo() {
    return delslo;
  }

  public void setDelslo(String val) {
    this.delslo = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 123, finalPosition = 129)
  public double getEma() {
    return ema;
  }

  public void setEma(String val) {
    this.ema = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 131, finalPosition = 137)
  public double getRect() {
    return rect;
  }

  public void setRect(String val) {
    this.rect = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 139, finalPosition = 149)
  public double getAmp() {
    return amp;
  }

  public void setAmp(String val) {
    this.amp = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 151, finalPosition = 157)
  public double getPer() {
    return per;
  }

  public void setPer(String val) {
    this.per = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 159, finalPosition = 165)
  public double getLogat() {
    return logat;
  }

  public void setLogat(String val) {
    this.logat = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 167, finalPosition = 167)
  public String getClip() {
    return clip;
  }

  public void setClip(String val) {
    this.clip = val;
  }

  @Override
  @PositionalField(initialPosition = 169, finalPosition = 170)
  public String getFm() {
    return fm;
  }

  public void setFm(String val) {
    this.fm = val;
  }

  @Override
  @PositionalField(initialPosition = 172, finalPosition = 181)
  public double getSnr() {
    return snr;
  }

  public void setSnr(String val) {
    this.snr = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 183, finalPosition = 183)
  public String getQual() {
    return qual;
  }

  public void setQual(String val) {
    this.qual = val;
  }


  @Override
  @PositionalField(initialPosition = 185, finalPosition = 199)
  public String getAuth() {
    return auth;
  }

  public void setAuth(String val) {
    this.auth = val;
  }

  @Override
  @PositionalField(initialPosition = 201, finalPosition = 209)
  public int getCommid() {
    return commid;
  }

  public void setCommid(String val) {
    this.commid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 211, finalPosition = 227)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String val) {
    this.lddate = CssReaderUtility.parseDate(val).orElse(null);
  }


}
