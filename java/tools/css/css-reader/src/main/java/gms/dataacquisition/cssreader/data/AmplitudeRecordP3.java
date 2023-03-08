package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Duration;
import java.time.Instant;

@PositionalRecord

public class AmplitudeRecordP3 extends AmplitudeRecord {

  private static final int RECORD_LENGTH = 184;


  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 9)
  public int getAmpid() {
    return ampid;
  }

  public void setAmpid(String val) {
    this.ampid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 11, finalPosition = 19)
  public int getArid() {
    return arid;
  }

  public void setArid(String val) {
    this.arid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 21, finalPosition = 29)
  public int getParid() {
    return parid;
  }

  public void setParid(String val) {
    this.parid = Integer.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 31, finalPosition = 38)
  public String getChan() {
    return chan;
  }

  public void setChan(String val) {
    this.chan = val;
  }

  @Override
  @PositionalField(initialPosition = 40, finalPosition = 50)
  public double getAmp() {
    return amp;
  }

  public void setAmp(String val) {
    this.amp = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 52, finalPosition = 58)
  public Duration getPer() {
    return per;
  }

  public void setPer(String val) {
    this.per = Duration.ofNanos((long) (Double.valueOf(val) * 1e9));
  }

  @Override
  @PositionalField(initialPosition = 60, finalPosition = 69)
  public double getSnr() {
    return snr;
  }

  public void setSnr(String val) {
    this.snr = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 71, finalPosition = 87)
  public Instant getAmptime() {
    return amptime;
  }

  public void setAmptime(String val) {
    this.amptime = CssReaderUtility.toInstant(val).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 89, finalPosition = 105)
  public Instant getTime() {
    return time;
  }

  public void setTime(String val) {
    this.time = CssReaderUtility.toInstant(val).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 107, finalPosition = 113)
  public double getDuration() {
    return duration;
  }

  public void setDuration(String val) {
    this.duration = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 115, finalPosition = 121)
  public double getDeltaf() {
    return deltaf;
  }

  public void setDeltaf(String val) {
    this.deltaf = Double.valueOf(val);
  }

  @Override
  @PositionalField(initialPosition = 123, finalPosition = 130)
  public String getAmptype() {
    return amptype;
  }

  public void setAmptype(String val) {
    this.amptype = val;
  }

  @Override
  @PositionalField(initialPosition = 132, finalPosition = 146)
  public String getUnits() {
    return units;
  }

  public void setUnits(String val) {
    this.units = val;
  }

  @Override
  @PositionalField(initialPosition = 148, finalPosition = 148)
  public String getClip() {
    return clip;
  }

  public void setClip(String val) {
    this.clip = val;
  }

  @Override
  @PositionalField(initialPosition = 150, finalPosition = 150)
  public String getInarrival() {
    return inarrival;
  }

  public void setInarrival(String val) {
    this.inarrival = val;
  }

  @Override
  @PositionalField(initialPosition = 152, finalPosition = 166)
  public String getAuth() {
    return auth;
  }

  public void setAuth(String val) {
    this.auth = val;
  }

  @Override
  @PositionalField(initialPosition = 168, finalPosition = 184)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String val) {
    this.lddate = CssReaderUtility.parseDate(val).orElse(null);
  }

}
