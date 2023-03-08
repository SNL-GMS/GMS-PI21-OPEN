package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Optional;

@PositionalRecord
public class SiteChannelRecord {
  protected static int recordLength = 140;

  protected String sta;
  protected String chan;
  protected Optional<Instant> ondate;
  protected int chanid;
  protected Optional<Instant> offdate;
  protected String ctype;
  protected double edepth;
  protected double hang;
  protected double vang;
  protected String descrip;
  protected Optional<Instant> lddate;

  public static int getRecordLength() {
    return recordLength;
  }

  @Override
  public String toString() {
    return "SiteChannelRecord{" +
      "recordLength=" + recordLength +
      ", sta='" + sta + '\'' +
      ", chan='" + chan + '\'' +
      ", ondate=" + ondate +
      ", chanid=" + chanid +
      ", offdate=" + offdate +
      ", ctype='" + ctype + '\'' +
      ", edepth=" + edepth +
      ", hang=" + hang +
      ", vang=" + vang +
      ", descrip='" + descrip + '\'' +
      ", lddate=" + lddate +
      '}';
  }

  @PositionalField(initialPosition = 1, finalPosition = 6)
  public String getSta() {
    return sta;
  }

  public void setSta(String sta) {
    this.sta = sta.trim();
  }

  @PositionalField(initialPosition = 8, finalPosition = 15)
  public String getChan() {
    return chan;
  }

  public void setChan(String chan) {
    this.chan = chan.trim();
  }

  @PositionalField(initialPosition = 17, finalPosition = 24)
  public Optional<Instant> getOndate() {
    return ondate;
  }

  public void setOndate(String ondate) {
    this.ondate = Optional.ofNullable(CssReaderUtility.jdToInstant(ondate)).orElse(null);
  }

  @PositionalField(initialPosition = 26, finalPosition = 33)
  public int getChanid() {
    return chanid;
  }

  public void setChanid(String chanid) {
    this.chanid = Integer.valueOf(chanid);
  }

  @PositionalField(initialPosition = 35, finalPosition = 42)
  public Optional<Instant> getOffdate() {
    return offdate;
  }

  public void setOffdate(String offdate) {
    this.offdate = CssReaderUtility.jdToInstant(offdate);
  }

  @PositionalField(initialPosition = 44, finalPosition = 47)
  public String getCtype() {
    return ctype;
  }

  public void setCtype(String ctype) {
    this.ctype = ctype.trim();
  }

  @PositionalField(initialPosition = 49, finalPosition = 57)
  public double getEdepth() {
    return edepth;
  }

  public void setEdepth(String edepth) {
    this.edepth = Double.valueOf(edepth);
  }

  @PositionalField(initialPosition = 59, finalPosition = 64)
  public double getHang() {
    return hang;
  }

  public void setHang(String hang) {
    this.hang = Double.valueOf(hang);
  }

  @PositionalField(initialPosition = 66, finalPosition = 71)
  public double getVang() {
    return vang;
  }

  public void setVang(String vang) {
    this.vang = Double.valueOf(vang);
  }

  @PositionalField(initialPosition = 73, finalPosition = 122)
  public String getDescrip() {
    return descrip;
  }

  public void setDescrip(String descrip) {
    this.descrip = descrip.trim();
  }

  @PositionalField(initialPosition = 124, finalPosition = 140)
  public Optional<Instant> getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate);
  }

  public void validate() {
    Validate.notEmpty(getSta(), "Station name is empty");
    Validate.notEmpty(getChan(), "Channel name is empty");
    Validate.notNull(getOndate(), "On date is null");
    Validate.notNaN(getChanid(), "Chanid is Nan");
    Validate.notNull(getOffdate(), "Off date is null");
    Validate.notEmpty(getCtype(), "Channel type is empty");
    Validate.notNaN(getEdepth(), "Depth is NaN");
    Validate.notNaN(getHang(), "Horizontal angle is NaN");
    Validate.notNaN(getVang(), "Vertical angle is NaN");
    Validate.notEmpty(getDescrip(), "Description is empty");
    Validate.notNull(getLddate(), "Load date is null");
  }
}
