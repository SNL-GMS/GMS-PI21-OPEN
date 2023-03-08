package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;
import java.util.Optional;

@PositionalRecord
public class SiteRecordCss30 extends SiteRecord {
  protected static int recordLength = 155;

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
    this.ondate = Optional.ofNullable(CssReaderUtility.jdToInstant(ondate)).orElse(null);
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
  @PositionalField(initialPosition = 26, finalPosition = 34)
  public double getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = Double.valueOf(lat);
  }

  @Override
  @PositionalField(initialPosition = 36, finalPosition = 44)
  public double getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = Double.valueOf(lon);
  }

  @Override
  @PositionalField(initialPosition = 46, finalPosition = 54)
  public double getElev() {
    return elev;
  }

  public void setElev(String elev) {
    this.elev = Double.valueOf(elev);
  }

  @Override
  @PositionalField(initialPosition = 56, finalPosition = 105)
  public String getStaname() {
    return staname;
  }

  public void setStaname(String staname) {
    this.staname = staname.trim();
  }

  @Override
  @PositionalField(initialPosition = 107, finalPosition = 110)
  public String getStatype() {
    return statype;
  }

  public void setStatype(String statype) {
    this.statype = statype.trim();
  }

  @Override
  @PositionalField(initialPosition = 112, finalPosition = 117)
  public String getRefsta() {
    return refsta;
  }

  public void setRefsta(String refsta) {
    this.refsta = refsta.trim();
  }

  @Override
  @PositionalField(initialPosition = 119, finalPosition = 127)
  public double getDnorth() {
    return dnorth;
  }

  public void setDnorth(String dnorth) {
    this.dnorth = Double.valueOf(dnorth);
  }

  @Override
  @PositionalField(initialPosition = 129, finalPosition = 137)
  public double getDeast() {
    return deast;
  }

  public void setDeast(String deast) {
    this.deast = Double.valueOf(deast);
  }

  @Override
  @PositionalField(initialPosition = 139, finalPosition = 155)
  public Optional<Instant> getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate);
  }
}
