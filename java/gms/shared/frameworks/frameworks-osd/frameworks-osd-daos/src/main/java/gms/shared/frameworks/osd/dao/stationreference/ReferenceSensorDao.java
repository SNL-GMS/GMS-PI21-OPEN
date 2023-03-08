package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.dao.emerging.provenance.InformationSourceDao;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reference_sensor")
public class ReferenceSensorDao {

  @Id
  @Column(unique = true)
  private UUID id;

  @Column(name = "channel_name")
  private String channelName;

  @Column(name = "instrument_manufacturer")
  private String instrumentManufacturer;

  @Column(name = "instrument_model")
  private String instrumentModel;

  @Column(name = "serial_number")
  private String serialNumber;

  @Column(name = "number_of_components")
  private int numberOfComponents;

  @Column(name = "corner_period")
  private double cornerPeriod;

  @Column(name = "low_passband")
  private double lowPassband;

  @Column(name = "high_passband")
  private double highPassband;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  @Column(name = "comment")
  private String comment;

  @Embedded
  private InformationSourceDao informationSource;

  /**
   * Default constructor for JPA.
   */
  public ReferenceSensorDao() {
  }

  /**
   * Create a DAO from the COI object.
   *
   * @param sensor The ReferenceSensor object.
   */
  public ReferenceSensorDao(ReferenceSensor sensor) throws NullPointerException {
    Validate.notNull(sensor);
    this.id = sensor.getId();
    this.channelName = sensor.getChannelName();
    this.instrumentManufacturer = sensor.getInstrumentManufacturer();
    this.instrumentModel = sensor.getInstrumentModel();
    this.serialNumber = sensor.getSerialNumber();
    this.numberOfComponents = sensor.getNumberOfComponents();
    this.cornerPeriod = sensor.getCornerPeriod();
    this.lowPassband = sensor.getLowPassband();
    this.highPassband = sensor.getHighPassband();
    this.actualTime = sensor.getActualTime();
    this.systemTime = sensor.getSystemTime();
    this.comment = sensor.getComment();
    this.informationSource = new InformationSourceDao(sensor.getInformationSource());
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A ReferenceSensor COI object.
   */
  public ReferenceSensor toCoi() {
    return ReferenceSensor.builder()
      .setChannelName(getChannelName())
      .setInstrumentManufacturer(getInstrumentManufacturer())
      .setInstrumentModel(getInstrumentModel())
      .setSerialNumber(getSerialNumber())
      .setNumberOfComponents(getNumberOfComponents())
      .setCornerPeriod(getCornerPeriod())
      .setLowPassband(getLowPassband())
      .setHighPassband(getHighPassband())
      .setActualTime(getActualTime())
      .setSystemTime(getSystemTime())
      .setInformationSource(getInformationSource().toCoi())
      .setComment(getComment())
      .build();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }

  public String getInstrumentManufacturer() {
    return instrumentManufacturer;
  }

  public void setInstrumentManufacturer(String instrumentManufacturer) {
    this.instrumentManufacturer = instrumentManufacturer;
  }

  public String getInstrumentModel() {
    return instrumentModel;
  }

  public void setInstrumentModel(String instrumentModel) {
    this.instrumentModel = instrumentModel;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public int getNumberOfComponents() {
    return numberOfComponents;
  }

  public void setNumberOfComponents(int numberOfComponents) {
    this.numberOfComponents = numberOfComponents;
  }

  public double getCornerPeriod() {
    return cornerPeriod;
  }

  public void setCornerPeriod(double cornerPeriod) {
    this.cornerPeriod = cornerPeriod;
  }

  public double getLowPassband() {
    return lowPassband;
  }

  public void setLowPassband(double lowPassband) {
    this.lowPassband = lowPassband;
  }

  public double getHighPassband() {
    return highPassband;
  }

  public void setHighPassband(double highPassband) {
    this.highPassband = highPassband;
  }

  public Instant getActualTime() {
    return actualTime;
  }

  public void setActualTime(Instant actualTime) {
    this.actualTime = actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(Instant systemTime) {
    this.systemTime = systemTime;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public InformationSourceDao getInformationSource() {
    return informationSource;
  }

  public void setInformationSource(InformationSourceDao informationSource) {
    this.informationSource = informationSource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceSensorDao sensorDao = (ReferenceSensorDao) o;

    return new EqualsBuilder()
      .appendSuper(super.equals(o))
      .append(numberOfComponents, sensorDao.numberOfComponents)
      .append(cornerPeriod, sensorDao.cornerPeriod)
      .append(lowPassband, sensorDao.lowPassband)
      .append(highPassband, sensorDao.highPassband)
      .append(id, sensorDao.id)
      .append(channelName, sensorDao.channelName)
      .append(instrumentManufacturer, sensorDao.instrumentManufacturer)
      .append(instrumentModel, sensorDao.instrumentModel)
      .append(serialNumber, sensorDao.serialNumber)
      .append(actualTime, sensorDao.actualTime)
      .append(systemTime, sensorDao.systemTime)
      .append(comment, sensorDao.comment)
      .append(informationSource, sensorDao.informationSource)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, channelName, instrumentManufacturer, instrumentModel, serialNumber,
      numberOfComponents, cornerPeriod, lowPassband, highPassband, actualTime, systemTime,
      comment, informationSource);
  }

  @Override
  public String toString() {
    return "ReferenceSensorDao{" +
      "id=" + id +
      ", channelId=" + channelName +
      ", instrumentManufacturer='" + instrumentManufacturer + '\'' +
      ", instrumentModel='" + instrumentModel + '\'' +
      ", serialNumber='" + serialNumber + '\'' +
      ", numberOfComponents=" + numberOfComponents +
      ", cornerPeriod=" + cornerPeriod +
      ", lowPassband=" + lowPassband +
      ", highPassband=" + highPassband +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      ", comment='" + comment + '\'' +
      ", informationSource=" + informationSource +
      '}';
  }
}
