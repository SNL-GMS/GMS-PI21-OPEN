package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;

@PositionalRecord
public class OrigErrRecordP3 extends OrigErrRecord {
  private static final int RECORD_LENGTH = 257;

  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 9)
  public int getOriginId() {
    return originId;
  }

  public void setOriginId(String id) {
    this.originId = Integer.valueOf(id);
  }

  @Override
  @PositionalField(initialPosition = 11, finalPosition = 25)
  public double getSxx() {
    return sxx;
  }

  public void setSxx(String sxx) {
    this.sxx = Double.valueOf(sxx);
  }

  @Override
  @PositionalField(initialPosition = 27, finalPosition = 41)
  public double getSyy() {
    return syy;
  }

  public void setSyy(String syy) {
    this.syy = Double.valueOf(syy);
  }

  @Override
  @PositionalField(initialPosition = 43, finalPosition = 57)
  public double getSzz() {
    return szz;
  }

  public void setSzz(String szz) {
    this.szz = Double.valueOf(szz);
  }

  @Override
  @PositionalField(initialPosition = 59, finalPosition = 73)
  public double getStt() {
    return stt;
  }

  public void setStt(String stt) {
    this.stt = Double.valueOf(stt);
  }

  @Override
  @PositionalField(initialPosition = 75, finalPosition = 89)
  public double getSxy() {
    return sxy;
  }

  public void setSxy(String sxy) {
    this.sxy = Double.valueOf(sxy);
  }

  @Override
  @PositionalField(initialPosition = 91, finalPosition = 105)
  public double getSxz() {
    return sxz;
  }

  public void setSxz(String sxz) {
    this.sxz = Double.valueOf(sxz);
  }

  @Override
  @PositionalField(initialPosition = 107, finalPosition = 121)
  public double getSyz() {
    return syz;
  }

  public void setSyz(String syz) {
    this.syz = Double.valueOf(syz);
  }

  @Override
  @PositionalField(initialPosition = 123, finalPosition = 137)
  public double getStx() {
    return stx;
  }

  public void setStx(String stx) {
    this.stx = Double.valueOf(stx);
  }

  @Override
  @PositionalField(initialPosition = 139, finalPosition = 153)
  public double getSty() {
    return sty;
  }

  public void setSty(String sty) {
    this.sty = Double.valueOf(sty);
  }

  @Override
  @PositionalField(initialPosition = 155, finalPosition = 169)
  public double getStz() {
    return stz;
  }

  public void setStz(String stz) {
    this.stz = Double.valueOf(stz);
  }

  @Override
  @PositionalField(initialPosition = 171, finalPosition = 179)
  public double getSdobs() {
    return sdobs;
  }

  public void setSdobs(String sdobs) {
    this.sdobs = Double.valueOf(sdobs);
  }

  @Override
  @PositionalField(initialPosition = 181, finalPosition = 189)
  public double getSmajax() {
    return smajax;
  }

  public void setSmajax(String smajax) {
    this.smajax = Double.valueOf(smajax);
  }

  @Override
  @PositionalField(initialPosition = 191, finalPosition = 199)
  public double getSminax() {
    return sminax;
  }

  public void setSminax(String sminax) {
    this.sminax = Double.valueOf(sminax);
  }

  @Override
  @PositionalField(initialPosition = 201, finalPosition = 206)
  public double getStrike() {
    return strike;
  }

  public void setStrike(String strike) {
    this.strike = Double.valueOf(strike);
  }

  @Override
  @PositionalField(initialPosition = 208, finalPosition = 216)
  public double getSdepth() {
    return sdepth;
  }

  public void setSdepth(String sdepth) {
    this.sdepth = Double.valueOf(sdepth);
  }

  @Override
  @PositionalField(initialPosition = 218, finalPosition = 223)
  public double getStime() {
    return stime;
  }

  public void setStime(String stime) {
    this.stime = Double.valueOf(stime);
  }

  @Override
  @PositionalField(initialPosition = 225, finalPosition = 229)
  public double getConf() {
    return conf;
  }

  public void setConf(String conf) {
    this.conf = Double.valueOf(conf);
  }

  @Override
  @PositionalField(initialPosition = 231, finalPosition = 239)
  public int getCommentId() {
    return commentId;
  }

  public void setCommentId(String id) {
    this.commentId = Integer.valueOf(id);
  }

  @Override
  @PositionalField(initialPosition = 241, finalPosition = 257)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate).orElse(null);
  }

}

