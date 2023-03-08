package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;
import gms.shared.frameworks.osd.coi.event.DepthRestraintType;

import java.time.Instant;

@PositionalRecord
public class OriginRecordP3 extends OriginRecord {

  private static final int RECORD_LENGTH = 247;

  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 11)
  public double getLat() {
    return lat;
  }

  public void setLat(String val) {
    this.lat = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 13, finalPosition = 23)
  public double getLon() {
    return lon;
  }

  public void setLon(String val) {
    this.lon = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 25, finalPosition = 33)
  public double getDepth() {
    return depth;
  }

  public void setDepth(String val) {
    this.depth = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 35, finalPosition = 51)
  public Instant getTime() {
    return time;
  }

  public void setTime(String val) {
    this.time = CssReaderUtility.toInstant(val).orElse(null);
  }

  @Override
  @PositionalField(initialPosition = 53, finalPosition = 61)
  public int getOrid() {
    return orid;
  }

  public void setOrid(String val) {
    this.orid = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 63, finalPosition = 71)
  public int getEvid() {
    return evid;
  }

  public void setEvid(String val) {
    this.evid = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 73, finalPosition = 80)
  public int getJdate() {
    return jdate;
  }

  public void setJdate(String val) {
    this.jdate = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 82, finalPosition = 85)
  public int getNass() {
    return nass;
  }

  public void setNass(String val) {
    this.nass = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 87, finalPosition = 90)
  public int getNdef() {
    return ndef;
  }

  public void setNdef(String val) {
    this.ndef = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 92, finalPosition = 95)
  public int getNdp() {
    return ndp;
  }

  public void setNdp(String val) {
    this.ndp = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 97, finalPosition = 104)
  public int getGrn() {
    return grn;
  }

  public void setGrn(String val) {
    this.grn = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 106, finalPosition = 113)
  public int getSrn() {
    return srn;
  }

  public void setSrn(String val) {
    this.srn = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 115, finalPosition = 121)
  public String getEtype() {
    return etype;
  }

  public void setEtype(String val) {
    this.etype = val;
  }

  @Override
  @PositionalField(initialPosition = 123, finalPosition = 131)
  public double getDepdp() {
    return depdp;
  }

  public void setDepdp(String val) {
    this.depdp = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 133, finalPosition = 133)
  public DepthRestraintType getDtype() {
    return dtype;
  }

  public void setDtype(String val) {
    if ("d".equals(val)) {
      this.dtype = DepthRestraintType.FIXED_AT_DEPTH;
    } else if ("r".equals(val) || "g".equals(val)) {
      this.dtype = DepthRestraintType.FIXED_AT_SURFACE;
    } else {
      this.dtype = DepthRestraintType.UNRESTRAINED;
    }
  }

  @Override
  @PositionalField(initialPosition = 135, finalPosition = 141)
  public double getMb() {
    return mb;
  }

  public void setMb(String val) {
    this.mb = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 143, finalPosition = 151)
  public int getMbid() {
    return mbid;
  }

  public void setMbid(String val) {
    this.mbid = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 153, finalPosition = 159)
  public double getMs() {
    return ms;
  }

  public void setMs(String val) {
    this.ms = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 161, finalPosition = 169)
  public int getMsid() {
    return msid;
  }

  public void setMsid(String val) {
    this.msid = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 171, finalPosition = 177)
  public double getMl() {
    return ml;
  }

  public void setMl(String val) {
    this.ml = Double.parseDouble(val);
  }

  @Override
  @PositionalField(initialPosition = 179, finalPosition = 187)
  public int getMlid() {
    return mlid;
  }

  public void setMlid(String val) {
    this.mlid = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 189, finalPosition = 203)
  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String val) {
    this.algorithm = val;
  }

  @Override
  @PositionalField(initialPosition = 205, finalPosition = 219)
  public String getAuth() {
    return auth;
  }

  public void setAuth(String val) {
    this.auth = val;
  }

  @Override
  @PositionalField(initialPosition = 221, finalPosition = 229)
  public int getCommid() {
    return commid;
  }

  public void setCommid(String val) {
    this.commid = Integer.parseInt(val);
  }

  @Override
  @PositionalField(initialPosition = 231, finalPosition = 247)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String val) {
    this.lddate = CssReaderUtility.parseDate(val).orElse(null);
  }

}
