package gms.shared.signaldetection.dao.css;

import gms.shared.signaldetection.dao.css.converter.SignalTypeConverter;
import gms.shared.signaldetection.dao.css.enums.SignalType;
import gms.shared.stationdefinition.dao.css.StationChannelTimeKey;
import gms.shared.utilities.bridge.database.converter.ClipFlagConverter;
import gms.shared.utilities.bridge.database.converter.JulianDateConverterNegativeNa;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "arrival")
public class ArrivalDao {

  private StationChannelTimeKey arrivalKey;
  private long arid;
  private Instant jDate;
  private int singleStationOriginId;
  private int channelId;
  private String phase;
  private SignalType signalType;
  private int commid;
  private double timeUncertainty;
  private double azimuth;
  private double azimuthUncertainty;
  private double slowness;
  private double slownessUncertainty;
  private double emergenceAngle;
  private double rectilinearity;
  private double amplitude;
  private double period;
  private double logAmpliterPeriod;
  private ClipFlag clipped;
  private String firstMotion;
  private double snr;
  private String signalOnsetQuality;
  private String author;
  private Instant loadDate;

  public ArrivalDao() {
    // JPA constructor
  }

  /**
   * Create a deep copy of the given {@link ArrivalDao}
   *
   * @param arrivalDaoCopy ArrivalDao to copy
   * @return {@link ArrivalDao}
   */
  public ArrivalDao(ArrivalDao arrivalDaoCopy) {

    Validate.notNull(arrivalDaoCopy);
    Validate.notNull(arrivalDaoCopy.getArrivalKey());

    this.arid = arrivalDaoCopy.arid;
    this.jDate = arrivalDaoCopy.jDate;
    this.singleStationOriginId = arrivalDaoCopy.singleStationOriginId;
    this.channelId = arrivalDaoCopy.channelId;
    this.phase = arrivalDaoCopy.phase;
    this.signalType = arrivalDaoCopy.signalType;
    this.commid = arrivalDaoCopy.commid;
    this.timeUncertainty = arrivalDaoCopy.timeUncertainty;
    this.azimuth = arrivalDaoCopy.azimuth;
    this.azimuthUncertainty = arrivalDaoCopy.azimuthUncertainty;
    this.slowness = arrivalDaoCopy.slowness;
    this.slownessUncertainty = arrivalDaoCopy.slownessUncertainty;
    this.emergenceAngle = arrivalDaoCopy.emergenceAngle;
    this.rectilinearity = arrivalDaoCopy.rectilinearity;
    this.amplitude = arrivalDaoCopy.amplitude;
    this.period = arrivalDaoCopy.period;
    this.logAmpliterPeriod = arrivalDaoCopy.logAmpliterPeriod;
    this.clipped = arrivalDaoCopy.clipped;
    this.firstMotion = arrivalDaoCopy.firstMotion;
    this.snr = arrivalDaoCopy.snr;
    this.signalOnsetQuality = arrivalDaoCopy.signalOnsetQuality;
    this.author = arrivalDaoCopy.author;
    this.loadDate = arrivalDaoCopy.loadDate;
    this.arrivalKey = new StationChannelTimeKey();

    StationChannelTimeKey arrivalKeyOld = arrivalDaoCopy.getArrivalKey();

    this.arrivalKey.setChannelCode(arrivalKeyOld.getChannelCode());
    this.arrivalKey.setStationCode(arrivalKeyOld.getStationCode());
    this.arrivalKey.setTime(arrivalKeyOld.getTime());
  }

  @EmbeddedId
  public StationChannelTimeKey getArrivalKey() {
    return arrivalKey;
  }

  public void setArrivalKey(StationChannelTimeKey arrivalKey) {
    this.arrivalKey = arrivalKey;
  }

  @Column(name = "arid", nullable = false)
  public long getId() {
    return arid;
  }

  public void setId(long id) {
    this.arid = id;
  }

  @Column(name = "jdate")
  @Convert(converter = JulianDateConverterNegativeNa.class)
  public Instant getjDate() {
    return jDate;
  }

  public void setjDate(Instant jDate) {
    this.jDate = jDate;
  }

  @Column(name = "stassid")
  public int getSingleStationOriginId() {
    return singleStationOriginId;
  }

  public void setSingleStationOriginId(int singleStationOriginId) {
    this.singleStationOriginId = singleStationOriginId;
  }

  @Column(name = "chanid")
  public int getChannelId() {
    return channelId;
  }

  public void setChannelId(int channelId) {
    this.channelId = channelId;
  }

  @Column(name = "iphase")
  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  @Column(name = "stype")
  @Convert(converter = SignalTypeConverter.class)
  public SignalType getSignalType() {
    return signalType;
  }

  public void setSignalType(SignalType signalType) {
    this.signalType = signalType;
  }

  @Column(name = "deltim")
  public double getTimeUncertainty() {
    return timeUncertainty;
  }

  public void setTimeUncertainty(double timeUncertainty) {
    this.timeUncertainty = timeUncertainty;
  }

  @Column(name = "azimuth")
  public double getAzimuth() {
    return azimuth;
  }

  public void setAzimuth(double azimuth) {
    this.azimuth = azimuth;
  }

  @Column(name = "delaz")
  public double getAzimuthUncertainty() {
    return azimuthUncertainty;
  }

  public void setAzimuthUncertainty(double azimuthUncertainty) {
    this.azimuthUncertainty = azimuthUncertainty;
  }

  @Column(name = "slow")
  public double getSlowness() {
    return slowness;
  }

  public void setSlowness(double slowness) {
    this.slowness = slowness;
  }

  @Column(name = "delslo")
  public double getSlownessUncertainty() {
    return slownessUncertainty;
  }

  public void setSlownessUncertainty(double slownessUncertainty) {
    this.slownessUncertainty = slownessUncertainty;
  }

  @Column(name = "ema")
  public double getEmergenceAngle() {
    return emergenceAngle;
  }

  public void setEmergenceAngle(double emergenceAngle) {
    this.emergenceAngle = emergenceAngle;
  }

  @Column(name = "rect")
  public double getRectilinearity() {
    return rectilinearity;
  }

  public void setRectilinearity(double rectilinearity) {
    this.rectilinearity = rectilinearity;
  }

  @Column(name = "amp")
  public double getAmplitude() {
    return amplitude;
  }

  public void setAmplitude(double amplitude) {
    this.amplitude = amplitude;
  }

  @Column(name = "per")
  public double getPeriod() {
    return period;
  }

  public void setPeriod(double period) {
    this.period = period;
  }

  @Column(name = "logat")
  public double getLogAmpliterPeriod() {
    return logAmpliterPeriod;
  }

  public void setLogAmpliterPeriod(double logAmpliterPeriod) {
    this.logAmpliterPeriod = logAmpliterPeriod;
  }

  @Column(name = "clip")
  @Convert(converter = ClipFlagConverter.class)
  public ClipFlag getClipped() {
    return clipped;
  }

  public void setClipped(ClipFlag clipped) {
    this.clipped = clipped;
  }

  @Column(name = "fm")
  public String getFirstMotion() {
    return firstMotion;
  }

  public void setFirstMotion(String firstMotion) {
    this.firstMotion = firstMotion;
  }

  @Column(name = "snr")
  public double getSnr() {
    return snr;
  }

  public void setSnr(double snr) {
    this.snr = snr;
  }

  @Column(name = "qual")
  public String getSignalOnsetQuality() {
    return signalOnsetQuality;
  }

  public void setSignalOnsetQuality(String signalOnsetQuality) {
    this.signalOnsetQuality = signalOnsetQuality;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "commid")
  public int getCommid() {
    return commid;
  }

  public void setCommid(int commid) {
    this.commid = commid;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  @Override
  public String toString() {
    return "ArrivalDao{" +
      "arrivalKey=" + arrivalKey +
      ", arid=" + arid +
      ", jDate=" + jDate +
      ", singleStationOriginId=" + singleStationOriginId +
      ", channelId=" + channelId +
      ", phase='" + phase + '\'' +
      ", signalType=" + signalType +
      ", commid=" + commid +
      ", timeUncertainty=" + timeUncertainty +
      ", azimuth=" + azimuth +
      ", azimuthUncertainty=" + azimuthUncertainty +
      ", slowness=" + slowness +
      ", slownessUncertainty=" + slownessUncertainty +
      ", emergenceAngle=" + emergenceAngle +
      ", rectilinearity=" + rectilinearity +
      ", amplitude=" + amplitude +
      ", period=" + period +
      ", logAmpliterPeriod=" + logAmpliterPeriod +
      ", clipped=" + clipped +
      ", firstMotion='" + firstMotion + '\'' +
      ", snr=" + snr +
      ", signalOnsetQuality='" + signalOnsetQuality + '\'' +
      ", author='" + author + '\'' +
      ", loadDate=" + loadDate +
      '}';
  }
}
