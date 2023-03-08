package gms.shared.stationdefinition.dao.css;

import gms.shared.stationdefinition.dao.css.converter.DataTypeConverter;
import gms.shared.stationdefinition.dao.css.converter.SegTypeConverter;
import gms.shared.stationdefinition.dao.css.enums.DataType;
import gms.shared.stationdefinition.dao.css.enums.SegType;
import gms.shared.utilities.bridge.database.converter.ClipFlagConverter;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;
import gms.shared.utilities.bridge.database.converter.JulianDateConverterNegativeNa;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Entity
@Table(name = "wfdisc")
public class WfdiscDao {

  private long wfid;
  private String stationCode;
  private String channelCode;
  private Instant time;
  private long channelId;
  private Instant jDate;
  private Instant endTime;
  private int nsamp;
  private double sampRate;
  private double calib;
  private double calper;
  private String insType;
  private SegType segType;
  private DataType dataType;
  private ClipFlag clip;
  private String dir;
  private String dfile;
  private long foff;
  private long commid;
  private Instant loadDate;


  public WfdiscDao() {
    // JPA constructor
  }

  //copy constructor
  public WfdiscDao(WfdiscDao wfdiscDao) {

    Validate.notNull(wfdiscDao);

    this.wfid = wfdiscDao.wfid;
    this.stationCode = wfdiscDao.stationCode;
    this.channelCode = wfdiscDao.channelCode;
    this.time = wfdiscDao.time;
    this.channelId = wfdiscDao.channelId;
    this.jDate = wfdiscDao.jDate;
    this.endTime = wfdiscDao.endTime;
    this.nsamp = wfdiscDao.nsamp;
    this.sampRate = wfdiscDao.sampRate;
    this.calib = wfdiscDao.calib;
    this.calper = wfdiscDao.calper;
    this.insType = wfdiscDao.insType;
    this.segType = wfdiscDao.segType;
    this.dataType = wfdiscDao.dataType;
    this.clip = wfdiscDao.clip;
    this.dir = wfdiscDao.dir;
    this.dfile = wfdiscDao.dfile;
    this.foff = wfdiscDao.foff;
    this.commid = wfdiscDao.commid;
    this.loadDate = wfdiscDao.loadDate;
  }

  @Id
  @Column(name = "wfid", nullable = false)
  public long getId() {
    return wfid;
  }

  public void setId(long id) {
    this.wfid = id;
  }

  @Column(name = "sta", nullable = false)
  public String getStationCode() {
    return stationCode;
  }

  public void setStationCode(String stationCode) {
    this.stationCode = stationCode;
  }

  @Column(name = "chan", nullable = false)
  public String getChannelCode() {
    return channelCode;
  }

  public void setChannelCode(String channelCode) {
    this.channelCode = channelCode;
  }

  @Column(name = "time", nullable = false)
  @Convert(converter = InstantToDoubleConverterNegativeNa.class)
  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  @Column(name = "chanid")
  public long getChannelId() {
    return channelId;
  }

  public void setChannelId(long channelId) {
    this.channelId = channelId;
  }

  @Column(name = "jdate")
  @Convert(converter = JulianDateConverterNegativeNa.class)
  public Instant getjDate() {
    return jDate;
  }

  public void setjDate(Instant jDate) {
    this.jDate = jDate;
  }

  @Column(name = "endtime")
  @Convert(converter = InstantToDoubleConverterPositiveNa.class)
  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  @Column(name = "nsamp")
  public int getNsamp() {
    return nsamp;
  }

  public void setNsamp(int nsamp) {
    this.nsamp = nsamp;
  }

  @Column(name = "samprate")
  public double getSampRate() {
    return sampRate;
  }

  public void setSampRate(double sampRate) {
    this.sampRate = sampRate;
  }

  @Column(name = "calib")
  public double getCalib() {
    return calib;
  }

  public void setCalib(double calib) {
    this.calib = calib;
  }

  @Column(name = "calper")
  public double getCalper() {
    return calper;
  }

  public void setCalper(double calper) {
    this.calper = calper;
  }

  @Column(name = "instype")
  public String getInsType() {
    return insType;
  }

  public void setInsType(String insType) {
    this.insType = insType;
  }

  @Column(name = "segtype")
  @Convert(converter = SegTypeConverter.class)
  public SegType getSegType() {
    return segType;
  }

  public void setSegType(SegType segType) {
    this.segType = segType;
  }

  @Column(name = "datatype")
  @Convert(converter = DataTypeConverter.class)
  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  @Column(name = "clip")
  @Convert(converter = ClipFlagConverter.class)
  public ClipFlag getClip() {
    return clip;
  }

  public void setClip(ClipFlag clip) {
    this.clip = clip;
  }

  @Column(name = "dir")
  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  @Column(name = "dfile")
  public String getDfile() {
    return dfile;
  }

  public void setDfile(String dfile) {
    this.dfile = dfile;
  }

  @Column(name = "foff")
  public long getFoff() {
    return foff;
  }

  public void setFoff(long foff) {
    this.foff = foff;
  }

  @Column(name = "commid")
  public long getCommid() {
    return commid;
  }

  public void setCommid(long commid) {
    this.commid = commid;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  @Transient
  public int getVersionAttributeHash() {
    return Objects.hash(this.getStationCode(), this.getChannelCode(), this.getCalib(), this.getCalper());
  }

  @Transient
  public int getVersionTimeHash() {
    return Objects.hash(this.getStationCode(), this.getChannelCode(), this.getTime());
  }

  @Transient
  public Optional<Double> getSampRateAsOptional() {
    return Optional.of(sampRate);
  }
}
