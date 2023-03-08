package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;

@PositionalRecord
public class SensorRecordNnsaKbCore extends SensorRecord {
  protected static int recordLength = 149;

  public static int getRecordLength() {
    return recordLength;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 6)
  public String getSta() {
    return sta;
  }

  @Override
  public void setSta(String sta) {
    this.sta = sta;
  }

  @Override
  @PositionalField(initialPosition = 8, finalPosition = 15)
  public String getChan() {
    return chan;
  }

  @Override
  public void setChan(String chan) {
    this.chan = chan;
  }

  @Override
  @PositionalField(initialPosition = 17, finalPosition = 33)
  public Instant getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = CssReaderUtility.toInstant(time).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 35, finalPosition = 51)
  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = CssReaderUtility.toInstant(endTime).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 53, finalPosition = 60)
  public int getInid() {
    return inid;
  }

  public void setInid(String inid) {
    this.inid = Integer.valueOf(inid);
  }

  @Override
  @PositionalField(initialPosition = 62, finalPosition = 69)
  public int getChanid() {
    return chanid;
  }

  public void setChanid(String chanid) {
    this.chanid = Integer.valueOf(chanid);
  }

  @Override
  @PositionalField(initialPosition = 71, finalPosition = 78)
  public int getJdate() {
    return jdate;
  }

  public void setJdate(String jdate) {
    this.jdate = Integer.valueOf(jdate);
  }

  @Override
  @PositionalField(initialPosition = 80, finalPosition = 95)
  public double getCalratio() {
    return calratio;
  }

  public void setCalratio(String calratio) {
    this.calratio = Double.valueOf(calratio);
  }

  @Override
  @PositionalField(initialPosition = 97, finalPosition = 112)
  public double getCalper() {
    return calper;
  }

  public void setCalper(String calper) {
    this.calper = Double.valueOf(calper);
  }

  @Override
  @PositionalField(initialPosition = 114, finalPosition = 129)
  public double getTshift() {
    return tshift;
  }

  public void setTshift(String tshift) {
    this.tshift = Double.valueOf(tshift);
  }

  @Override
  @PositionalField(initialPosition = 131, finalPosition = 131)
  public char getInstant() {
    return instant;
  }

  public void setInstant(String instant) {
    this.instant = instant.charAt(0);
  }

  @Override
  @PositionalField(initialPosition = 133, finalPosition = 149)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate).orElse(null);
  }


}
