package gms.shared.frameworks.osd.dao.stationreference;

import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.StationType;
import gms.shared.frameworks.osd.dao.emerging.provenance.InformationSourceDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.ALL;

/**
 * Define a Data Access Object to allow read and write access to the relational database.
 */
@Entity
@Table(name = "reference_station")
public class ReferenceStationDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reference_station_sequence")
  @SequenceGenerator(name = "reference_station_sequence", sequenceName = "reference_station_sequence", allocationSize = 5)
  private long id;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "version_id", unique = true)
  private UUID versionId;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "station_type")
  private StationType stationType;

  @Column(name = "latitude")
  private double latitude;

  @Column(name = "longitude")
  private double longitude;

  @Column(name = "elevation")
  private double elevation;

  @Column(name = "comment")
  private String comment;

  @Embedded
  private InformationSourceDao source;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  @Column(name = "active")
  private boolean active;

  @OneToMany(cascade = ALL)
  @JoinTable(
    name = "reference_station_aliases",
    joinColumns = {@JoinColumn(name = "reference_station", table = "reference_station",
      referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "reference_alias", table = "reference_alias",
      referencedColumnName = "id")}
  )
  private List<ReferenceAliasDao> aliases;

  /**
   * Default constructor for JPA.
   */
  public ReferenceStationDao() {
  }

  /**
   * Create a DAO from the COI object.
   *
   * @param station The ReferenceStation object.
   * @throws NullPointerException
   */
  public ReferenceStationDao(ReferenceStation station) throws NullPointerException {
    Validate.notNull(station);
    this.entityId = station.getEntityId();
    this.versionId = station.getVersionId();
    this.name = station.getName();
    this.description = station.getDescription();
    this.stationType = station.getStationType();
    this.source = new InformationSourceDao(station.getSource());
    this.comment = station.getComment();
    this.latitude = station.getLatitude();
    this.longitude = station.getLongitude();
    this.elevation = station.getElevation();
    this.actualTime = station.getActualChangeTime();
    this.systemTime = station.getSystemChangeTime();
    this.active = station.isActive();
    this.aliases = station.getAliases().stream()
      .map(ReferenceAliasDao::new)
      .collect(Collectors.toList());
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A ReferenceStation COI object.
   */
  public ReferenceStation toCoi() {
    return ReferenceStation.builder()
      .setName(getName())
      .setDescription(getDescription())
      .setStationType(getStationType())
      .setSource(getSource())
      .setComment(getComment())
      .setLatitude(getLatitude())
      .setLongitude(getLongitude())
      .setElevation(getElevation())
      .setActualChangeTime(getActualTime())
      .setSystemChangeTime(getSystemTime())
      .setActive(active)
      .setAliases(getAliases().stream().map(ReferenceAliasDao::toCoi)
        .collect(Collectors.toList()))
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public StationType getStationType() {
    return stationType;
  }

  public void setStationType(
    StationType stationType) {
    this.stationType = stationType;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
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

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setSource(InformationSource source) {
    this.source = new InformationSourceDao(source);
  }

  public InformationSource getSource() {

    return source.toCoi();
  }

  public List<ReferenceAliasDao> getAliases() {
    return this.aliases;
  }

  public void setAliases(List<ReferenceAliasDao> aliases) {
    this.aliases = aliases;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReferenceStationDao)) {
      return false;
    }
    ReferenceStationDao that = (ReferenceStationDao) o;
    return id == that.id &&
      Double.compare(that.latitude, latitude) == 0 &&
      Double.compare(that.longitude, longitude) == 0 &&
      Double.compare(that.elevation, elevation) == 0 &&
      active == that.active &&
      entityId.equals(that.entityId) &&
      versionId.equals(that.versionId) &&
      name.equals(that.name) &&
      description.equals(that.description) &&
      stationType == that.stationType &&
      comment.equals(that.comment) &&
      source.equals(that.source) &&
      actualTime.equals(that.actualTime) &&
      systemTime.equals(that.systemTime) &&
      aliases.equals(that.aliases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, entityId, versionId, name, description, stationType, latitude,
      longitude, elevation, comment, source, actualTime, systemTime, active, aliases);
  }

  @Override
  public String toString() {
    return "ReferenceStationDao{" +
      "id=" + id +
      ", entityId=" + entityId +
      ", versionId=" + versionId +
      ", name='" + name + '\'' +
      ", description='" + description + '\'' +
      ", stationType=" + stationType +
      ", latitude=" + latitude +
      ", longitude=" + longitude +
      ", elevation=" + elevation +
      ", comment='" + comment + '\'' +
      ", source=" + source +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      ", active=" + active +
      ", aliases=" + aliases +
      '}';
  }
}
