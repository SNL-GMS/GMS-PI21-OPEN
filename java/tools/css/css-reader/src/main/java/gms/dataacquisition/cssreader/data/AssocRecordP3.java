package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;

import java.time.Instant;

@PositionalRecord
public class AssocRecordP3 extends AssocRecord {

  private static final int RECORD_LENGTH = 156;

  public static int getRecordLength() {
    return RECORD_LENGTH;
  }

  @Override
  @PositionalField(initialPosition = 1, finalPosition = 9)
  public int getArrivalId() {
    return arrivalId;
  }

  public void setArrivalId(String id) {
    this.arrivalId = Integer.valueOf(id);
  }

  @Override
  @PositionalField(initialPosition = 11, finalPosition = 19)
  public int getOriginId() {
    return originId;
  }

  public void setOriginId(String id) {
    this.originId = Integer.valueOf(id);
  }

  @Override
  @PositionalField(initialPosition = 21, finalPosition = 26)
  public String getStationName() {
    return stationName;
  }

  public void setStationName(String name) {
    this.stationName = name;
  }

  @Override
  @PositionalField(initialPosition = 28, finalPosition = 35)
  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  @Override
  @PositionalField(initialPosition = 37, finalPosition = 40)
  public double getBelief() {
    return belief;
  }

  public void setBelief(String belief) {
    this.belief = Double.valueOf(belief);
  }

  @Override
  @PositionalField(initialPosition = 42, finalPosition = 49)
  public double getDelta() {
    return delta;
  }

  public void setDelta(String delta) {
    this.delta = Double.valueOf(delta);
  }

  @Override
  @PositionalField(initialPosition = 51, finalPosition = 57)
  public double getSeaz() {
    return seaz;
  }

  public void setSeaz(String seaz) {
    this.seaz = Double.valueOf(seaz);
  }

  @Override
  @PositionalField(initialPosition = 59, finalPosition = 65)
  public double getEsaz() {
    return esaz;
  }

  public void setEsaz(String esaz) {
    this.esaz = Double.valueOf(esaz);
  }

  @Override
  @PositionalField(initialPosition = 67, finalPosition = 74)
  public double getTimeres() {
    return timeres;
  }

  public void setTimeres(String timeres) {
    this.timeres = Double.valueOf(timeres);
  }

  @Override
  @PositionalField(initialPosition = 76, finalPosition = 76)
  public boolean getTimedef() {
    return timedef;
  }

  public void setTimedef(String timedef) {
    this.timedef = setIsDefining(timedef);
  }

  @Override
  @PositionalField(initialPosition = 78, finalPosition = 84)
  public double getAzres() {
    return azres;
  }

  public void setAzres(String azres) {
    this.azres = Double.valueOf(azres);
  }

  @Override
  @PositionalField(initialPosition = 86, finalPosition = 86)
  public boolean getAzdef() {
    return azdef;
  }

  public void setAzdef(String azdef) {
    this.azdef = setIsDefining(azdef);
  }

  @Override
  @PositionalField(initialPosition = 88, finalPosition = 94)
  public double getSlores() {
    return slores;
  }

  public void setSlores(String slores) {
    this.slores = Double.valueOf(slores);
  }

  @Override
  @PositionalField(initialPosition = 96, finalPosition = 96)
  public boolean getSlodef() {
    return slodef;
  }

  public void setSlodef(String slodef) {
    this.slodef = setIsDefining(slodef);
  }

  @Override
  @PositionalField(initialPosition = 98, finalPosition = 104)
  public double getEmares() {
    return emares;
  }

  public void setEmares(String emares) {
    this.emares = Double.valueOf(emares);
  }

  @Override
  @PositionalField(initialPosition = 106, finalPosition = 111)
  public double getWgt() {
    return wgt;
  }

  public void setWgt(String wgt) {
    this.wgt = Double.valueOf(wgt);
  }

  @Override
  @PositionalField(initialPosition = 113, finalPosition = 127)
  public String getVmodel() {
    return vmodel;
  }

  public void setVmodel(String vmodel) {
    this.vmodel = vmodel;
  }

  @Override
  @PositionalField(initialPosition = 129, finalPosition = 137)
  public int getCommentId() {
    return commentId;
  }

  public void setCommentId(String id) {
    this.commentId = Integer.valueOf(id);
  }

  @Override
  @PositionalField(initialPosition = 139, finalPosition = 155)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = CssReaderUtility.parseDate(lddate).orElse(null);
  }

  public boolean setIsDefining(String str) {
    return "d".equals(str);
  }

}
