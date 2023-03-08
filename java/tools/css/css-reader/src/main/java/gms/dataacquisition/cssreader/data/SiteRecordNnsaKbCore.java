package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;
import java.util.Optional;

@PositionalRecord
public class SiteRecordNnsaKbCore extends SiteRecord {
  protected static int recordLength = 159;

  public static int getRecordLength() {
    return recordLength;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 6)
  public String getSta() {
    return sta;
  }

  public void setSta(String sta) {
    this.sta = sta.trim();
  }

  @Override
  @PositionalField(initialPosition = 8, finalPosition = 15)
  public Optional<Instant> getOndate() {
    return ondate;
  }

  public void setOndate(String ondate) {
    this.ondate = CssReaderUtility.jdToInstant(ondate);
  }

  @Override
  @PositionalField(initialPosition = 17, finalPosition = 24)
  public Optional<Instant> getOffdate() {
    return offdate;
  }

  public void setOffdate(String offdate) {
    this.offdate = CssReaderUtility.jdToInstant(offdate);
  }

  @Override
  @PositionalField(initialPosition = 26, finalPosition = 36)
  public double getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = Double.valueOf(lat);
  }

  @Override
  @PositionalField(initialPosition = 38, finalPosition = 48)
  public double getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = Double.valueOf(lon);
  }

  @Override
  @PositionalField(initialPosition = 50, finalPosition = 58)
  public double getElev() {
    return elev;
  }

  public void setElev(String elev) {
    this.elev = Double.valueOf(elev);
  }

  @Override
  @PositionalField(initialPosition = 60, finalPosition = 109)
  public String getStaname() {
    return staname;
  }

  public void setStaname(String staname) {
    this.staname = staname.trim();
  }

  @Override
  @PositionalField(initialPosition = 111, finalPosition = 114)
  public String getStatype() {
    return statype;
  }

  public void setStatype(String statype) {
    this.statype = statype.trim();
  }

  @Override
  @PositionalField(initialPosition = 116, finalPosition = 121)
  public String getRefsta() {
    return refsta;
  }

  public void setRefsta(String refsta) {
    this.refsta = refsta.trim();
  }

  @Override
  @PositionalField(initialPosition = 123, finalPosition = 131)
  public double getDnorth() {
    return dnorth;
  }

  public void setDnorth(String dnorth) {
    this.dnorth = Double.valueOf(dnorth);
  }

  @Override
  @PositionalField(initialPosition = 133, finalPosition = 141)
  public double getDeast() {
    return deast;
  }

  public void setDeast(String deast) {
    this.deast = Double.valueOf(deast);
  }

  @Override
  @PositionalField(initialPosition = 143, finalPosition = 159)
  public Optional<Instant> getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate);
  }
}
