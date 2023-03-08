package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.stationreference.ReferenceDigitizer;
import gms.shared.frameworks.osd.dao.emerging.provenance.InformationSourceDao;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reference_digitizer")
public class ReferenceDigitizerDao {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reference_digitizer_sequence")
  @SequenceGenerator(name = "reference_digitizer_sequence", sequenceName = "reference_digitizer_sequence", allocationSize = 5)
  private long id;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "version_id", unique = true)
  private UUID versionId;

  @Column(name = "name")
  private String name;

  @Column(name = "manufacturer")
  private String manufacturer;

  @Column(name = "model")
  private String model;

  @Column(name = "serial_number")
  private String serialNumber;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  @Column(name = "description")
  private String description;

  @Column(name = "comment")
  private String comment;

  @Embedded
  private InformationSourceDao informationSource;

  /**
   * Default constructor for JPA.
   */
  public ReferenceDigitizerDao() {
  }

  /**
   * Create a DAO from the corresponding COI object.
   *
   * @param digitizer The ReferenceDigitizer object.
   * @throws NullPointerException
   */
  public ReferenceDigitizerDao(ReferenceDigitizer digitizer) throws NullPointerException {
    Objects.requireNonNull(digitizer);
    this.entityId = digitizer.getEntityId();
    this.versionId = digitizer.getVersionId();
    this.name = digitizer.getName();
    this.manufacturer = digitizer.getManufacturer();
    this.model = digitizer.getModel();
    this.serialNumber = digitizer.getSerialNumber();
    this.actualTime = digitizer.getActualChangeTime();
    this.systemTime = digitizer.getSystemChangeTime();
    this.informationSource = new InformationSourceDao(digitizer.getInformationSource());
    this.comment = digitizer.getComment();
    this.description = digitizer.getDescription();
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A ReferenceDigitizer COI object.
   */
  public ReferenceDigitizer toCoi() {
    return ReferenceDigitizer.builder().setEntityId(getEntityId())
      .setVersionId(getVersionId())
      .setName(getName())
      .setManufacturer(getManufacturer())
      .setModel(getModel())
      .setSerialNumber(getSerialNumber())
      .setActualChangeTime(getActualTime())
      .setSystemChangeTime(getSystemTime())
      .setInformationSource(getInformationSource().toCoi())
      .setComment(getComment())
      .setDescription(getDescription())
      .build();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public void setEntityId(UUID id) {
    this.entityId = id;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public void setVersionId(UUID id) {
    this.versionId = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(
    String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getModel() {
    return model;
  }

  public void setModel(
    String model) {
    this.model = model;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String desc) {
    this.description = desc;
  }

  public InformationSourceDao getInformationSource() {
    return informationSource;
  }

  public void setInformationSource(
    InformationSourceDao informationSource) {
    this.informationSource = informationSource;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) { return false; }
    if (o == this) { return true; }
    if (o.getClass() != getClass()) {
      return false;
    }
    ReferenceDigitizerDao rhs = (ReferenceDigitizerDao) o;
    return new EqualsBuilder()
      .appendSuper(super.equals(o))
      .append(id, rhs.id)
      .append(entityId, rhs.entityId)
      .append(versionId, rhs.versionId)
      .append(name, rhs.name)
      .append(manufacturer, rhs.manufacturer)
      .append(model, rhs.model)
      .append(serialNumber, rhs.serialNumber)
      .append(actualTime, rhs.actualTime)
      .append(systemTime, rhs.systemTime)
      .append(description, rhs.description)
      .append(comment, rhs.comment)
      .append(informationSource, rhs.informationSource)
      .isEquals();


  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (manufacturer != null ? manufacturer.hashCode() : 0);
    result = 31 * result + (model != null ? model.hashCode() : 0);
    result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (informationSource != null ? informationSource.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceDigitizerDao{" +
      "id=" + id +
      ", entityId=" + entityId +
      ", versionId=" + versionId +
      ", name='" + name + '\'' +
      ", manufacturer='" + manufacturer + '\'' +
      ", model='" + model + '\'' +
      ", serialNumber='" + serialNumber + '\'' +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      ", description='" + description + '\'' +
      ", comment='" + comment + '\'' +
      ", informationSource=" + informationSource +
      '}';
  }
}
