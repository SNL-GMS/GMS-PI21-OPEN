package gms.shared.signaldetection.dao.css;

import gms.shared.signaldetection.dao.css.converter.AmplitudeUnitsConverter;
import gms.shared.signaldetection.dao.css.converter.DurationToDoubleConverter;
import gms.shared.signaldetection.dao.css.enums.AmplitudeUnits;
import gms.shared.utilities.bridge.database.converter.BooleanYNConverter;
import gms.shared.utilities.bridge.database.converter.ClipFlagConverter;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "amplitude")
public class AmplitudeDao {

  private long id;
  private long arrivalId;
  private long predictedArrivalId;
  private String channelCode;
  private double amplitude;
  private double period;
  private double snr;
  private Instant amplitudeTime;
  private Instant time;
  private Duration duration;
  private double sampleIntervalWidth;
  private String amplitudeType;
  private AmplitudeUnits units;
  private ClipFlag clip;
  private boolean inArrival;
  private String author;
  private Instant loadDate;

  public AmplitudeDao() {

  }

  /**
   * Create a deep copy of the given {@link AmplitudeDao}
   *
   * @param amplitudeCopy AmplitudeDao to copy
   * @return {@link AmplitudeDao}
   */
  public AmplitudeDao(AmplitudeDao amplitudeCopy) {

    Validate.notNull(amplitudeCopy);

    this.id = amplitudeCopy.id;
    this.arrivalId = amplitudeCopy.arrivalId;
    this.predictedArrivalId = amplitudeCopy.predictedArrivalId;
    this.channelCode = amplitudeCopy.channelCode;
    this.amplitude = amplitudeCopy.amplitude;
    this.period = amplitudeCopy.period;
    this.snr = amplitudeCopy.snr;
    this.amplitudeTime = amplitudeCopy.amplitudeTime;
    this.time = amplitudeCopy.time;
    this.duration = amplitudeCopy.duration;
    this.sampleIntervalWidth = amplitudeCopy.sampleIntervalWidth;
    this.amplitudeType = amplitudeCopy.amplitudeType;
    this.units = amplitudeCopy.units;
    this.clip = amplitudeCopy.clip;
    this.inArrival = amplitudeCopy.inArrival;
    this.author = amplitudeCopy.author;
    this.loadDate = amplitudeCopy.loadDate;
  }

  public AmplitudeDao(Builder builder) {
    this.id = builder.id;
    this.arrivalId = builder.arrivalId;
    this.predictedArrivalId = builder.predictedArrivalId;
    this.channelCode = builder.channelCode;
    this.amplitude = builder.amplitude;
    this.period = builder.period;
    this.snr = builder.snr;
    this.amplitudeTime = builder.amplitudeTime;
    this.time = builder.time;
    this.duration = builder.duration;
    this.sampleIntervalWidth = builder.sampleIntervalWidth;
    this.amplitudeType = builder.amplitudeType;
    this.units = builder.units;
    this.clip = builder.clip;
    this.inArrival = builder.inArrival;
    this.author = builder.author;
    this.loadDate = builder.loadDate;
  }

  @Id
  @Column(name = "ampid")
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Column(name = "arid", nullable = false)
  public long getArrivalId() {
    return arrivalId;
  }

  public void setArrivalId(long arrivalId) {
    this.arrivalId = arrivalId;
  }

  @Column(name = "parid", nullable = false)
  public long getPredictedArrivalId() {
    return predictedArrivalId;
  }

  public void setPredictedArrivalId(long predictedArrivalId) {
    this.predictedArrivalId = predictedArrivalId;
  }

  @Column(name = "chan", nullable = false)
  public String getChannelCode() {
    return channelCode;
  }

  public void setChannelCode(String channelCode) {
    this.channelCode = channelCode;
  }

  @Column(name = "amp", nullable = false)
  public double getAmplitude() {
    return amplitude;
  }

  public void setAmplitude(double amplitude) {
    this.amplitude = amplitude;
  }

  @Column(name = "per", nullable = false)
  public double getPeriod() {
    return period;
  }

  public void setPeriod(double period) {
    this.period = period;
  }

  @Column(name = "snr", nullable = false)
  public double getSnr() {
    return snr;
  }

  public void setSnr(double snr) {
    this.snr = snr;
  }

  @Column(name = "amptime", nullable = false)
  @Convert(converter = InstantToDoubleConverterNegativeNa.class)
  public Instant getAmplitudeTime() {
    return amplitudeTime;
  }

  public void setAmplitudeTime(Instant amplitudeTime) {
    this.amplitudeTime = amplitudeTime;
  }

  @Column(name = "time", nullable = false)
  @Convert(converter = InstantToDoubleConverterNegativeNa.class)
  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  @Column
  @Convert(converter = DurationToDoubleConverter.class)
  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  @Column(name = "deltaf", nullable = false)
  public double getSampleIntervalWidth() {
    return sampleIntervalWidth;
  }

  public void setSampleIntervalWidth(double sampleIntervalWidth) {
    this.sampleIntervalWidth = sampleIntervalWidth;
  }

  @Column(name = "amptype", nullable = false)
  public String getAmplitudeType() {
    return amplitudeType;
  }

  public void setAmplitudeType(String amplitudeType) {
    this.amplitudeType = amplitudeType;
  }

  @Column(name = "units", nullable = false)
  @Convert(converter = AmplitudeUnitsConverter.class)
  public AmplitudeUnits getUnits() {
    return units;
  }

  public void setUnits(AmplitudeUnits units) {
    this.units = units;
  }

  @Column(name = "clip", nullable = false)
  @Convert(converter = ClipFlagConverter.class)
  public ClipFlag getClip() {
    return clip;
  }

  public void setClip(ClipFlag clip) {
    this.clip = clip;
  }

  @Column(name = "inarrival", nullable = false)
  @Convert(converter = BooleanYNConverter.class)
  public boolean isInArrival() {
    return inArrival;
  }

  public void setInArrival(boolean inArrival) {
    this.inArrival = inArrival;
  }

  @Column(name = "auth")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Column(name = "lddate", nullable = false)
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  public static class Builder {
    private long id;
    private long arrivalId;
    private long predictedArrivalId;
    private String channelCode;
    private double amplitude;
    private double period;
    private double snr;
    private Instant amplitudeTime;
    private Instant time;
    private Duration duration;
    private double sampleIntervalWidth;
    private String amplitudeType;
    private AmplitudeUnits units;
    private ClipFlag clip;
    private boolean inArrival;
    private String author;
    private Instant loadDate;

    public Builder withId(long id) {
      this.id = id;
      return this;
    }

    public Builder withArrivalId(long arrivalId) {
      this.arrivalId = arrivalId;
      return this;
    }

    public Builder withPredictedArrivalId(long predictedArrivalId) {
      this.predictedArrivalId = predictedArrivalId;
      return this;
    }

    public Builder withChannelCode(String channelCode) {
      this.channelCode = channelCode;
      return this;
    }

    public Builder withAmplitude(double amplitude) {
      this.amplitude = amplitude;
      return this;
    }

    public Builder withPeriod(double period) {
      this.period = period;
      return this;
    }

    public Builder withSnr(double snr) {
      this.snr = snr;
      return this;
    }

    public Builder withAmplitudeTime(Instant amplitudeTime) {
      this.amplitudeTime = amplitudeTime;
      return this;
    }

    public Builder withTime(Instant time) {
      this.time = time;
      return this;
    }

    public Builder withDuration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public Builder withSampleIntervalWidth(double sampleIntervalWidth) {
      this.sampleIntervalWidth = sampleIntervalWidth;
      return this;
    }

    public Builder withAmplitudeType(String amplitudeType) {
      this.amplitudeType = amplitudeType;
      return this;
    }

    public Builder withUnits(AmplitudeUnits units) {
      this.units = units;
      return this;
    }

    public Builder withClip(ClipFlag clip) {
      this.clip = clip;
      return this;
    }

    public Builder withInArrival(boolean inArrival) {
      this.inArrival = inArrival;
      return this;
    }

    public Builder withAuthor(String author) {
      this.author = author;
      return this;
    }

    public Builder withLoadDate(Instant loadDate) {
      this.loadDate = loadDate;
      return this;
    }

    public AmplitudeDao createAmplitudeDao() {
      return new AmplitudeDao(this);
    }
  }
}
