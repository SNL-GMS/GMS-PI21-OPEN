package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.ParameterValidation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an interval of one or more unprocessed channel segments where there are data quality
 * issues, e.g., missing data, spikes, etc. This interval contains n-number from QC Mask Versions
 * and the processing channel this QcMask has been created for.
 */
@AutoValue
public abstract class QcMask {

  public abstract UUID getId();

  public abstract String getChannelName();

  public abstract List<QcMaskVersion> getQcMaskVersions();

  /**
   * Recreation factory method (sets the QcMask entity identity). Handles parameter validation. Used
   * for deserialization and recreating from persistence.
   *
   * @param id Id for the QcMask.
   * @param channelName name for the Processing Channel this QcMask was created for.
   * @param qcMaskVersions Historical version of this QcMask.
   * @return QcMask representing all the input parameters.
   * @throws IllegalArgumentException if id, channelName, or qcMaskVersions are null
   */
  @JsonCreator
  public static QcMask from(
    @JsonProperty("id") UUID id,
    @JsonProperty("channelName") String channelName,
    @JsonProperty("qcMaskVersions") List<QcMaskVersion> qcMaskVersions) {
    Objects.requireNonNull(id, "Cannot create QcMask from a null id");
    Objects.requireNonNull(channelName,
      "Cannot create QcMask from a null channel name");
    Objects.requireNonNull(qcMaskVersions, "Cannot create QcMask from null QcMaskVersions");

    return new AutoValue_QcMask(id, channelName, new ArrayList<>(qcMaskVersions));
  }

  /**
   * Factory method for creating a brand new QcMask with an initial QcMaskVersion.  Assigns the
   * QcMask entity identity.
   *
   * @param channelName name for the Processing Channel this QcMask was created for, not null
   * @param parents collection of parents for the initial version of this mask, not null
   * @param channelSegmentIdList Collection of Ids for Channel Segments this QcMask was created on,
   * not null
   * @param category Category of the QcMask.
   * @param type Type of the QcMask, not null
   * @param rationale Rationale for creating the QcMask. Empty string is allowed.
   * @param startTime Start of the time range this QcMask is masking.
   * @param endTime End of the time range this QcMask is masking.
   * creation time), not null
   * @return New QcMask with a initial version representing the input.
   */
  public static QcMask create(String channelName,
    Collection<QcMaskVersionDescriptor> parents,
    List<UUID> channelSegmentIdList, QcMaskCategory category, QcMaskType type,
    String rationale, Instant startTime, Instant endTime) {

    ParameterValidation.requireFalse(QcMaskCategory.REJECTED::equals, category,
      "Cannot create QcMask with REJECTED QcMaskCategory");

    return from(UUID.randomUUID(), channelName,
      Collections.singletonList(
        QcMaskVersion.from(0L, parents,
          channelSegmentIdList, category, type, rationale, startTime, endTime)));
  }

  /**
   * Gets the current {@link QcMaskVersion} of this mask
   *
   * @return current QcMaskVersion, not null
   */
  public QcMaskVersion getCurrentQcMaskVersion() {
    return getQcMaskVersions().get(getQcMaskVersions().size() - 1);
  }

  /**
   * Adds a new version instance {@link QcMaskVersion} for this mask. This method assumes the parent
   * version is the current last version of this QcMask (i.e. the last entry in the qcMaskVersions
   * list).
   *
   * @param channelSegmentIds The list of channel segment ids for which this version is created.
   * @param category The QcMask category
   * @param type The QcMask type
   * @param rationale The descriptive reason for creating this version.
   * @param startTime The start time of this version.
   * @param endTime The end time of this version
   * @throws IllegalArgumentException if any from the input parameters are null
   */
  public void addQcMaskVersion(List<UUID> channelSegmentIds, QcMaskCategory category,
    QcMaskType type, String rationale, Instant startTime, Instant endTime) {
    ParameterValidation.requireFalse(QcMaskCategory.REJECTED::equals, category,
      "Cannot add QcMaskVersion with REJECTED QcMaskCategory");

    //validation checks happen in QcMaskVersion creation
    getQcMaskVersions().add(
      QcMaskVersion.builder()
        .setVersion(getCurrentQcMaskVersion().getVersion() + 1)
        .addParentQcMask(getCurrentVersionAsReference())
        .setChannelSegmentIds(channelSegmentIds)
        .setCategory(category)
        .setType(type)
        .setRationale(rationale)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build());
  }

  /**
   * Creates a new rejected {@link QcMaskVersion} for this mask. This method assumes the parent
   * version is the current last version from this QcMask (i.e. the last entry in the qcMaskVersions
   * list).
   *
   * @param rationale The descriptive reason for creating this rejected version.
   * @param channelSegmentIds The list of channel segment ids related to the qc mask rejection
   * @throws IllegalArgumentException if rationale is null
   */
  public void reject(String rationale,
    List<UUID> channelSegmentIds) {
    //null checks happen in QcMaskVersion creation
    getQcMaskVersions().add(
      QcMaskVersion.builder()
        .setVersion(getCurrentQcMaskVersion().getVersion() + 1)
        .addParentQcMask(getCurrentVersionAsReference())
        .setChannelSegmentIds(channelSegmentIds)
        .setCategory(QcMaskCategory.REJECTED)
        .setRationale(rationale)
        .build());
  }

  /**
   * Creates a {@link QcMaskVersionDescriptor} using this QcMask's current version as the parent
   *
   * @return QcMask's current version as a QcMaskVersionDescriptor, not null
   */
  public QcMaskVersionDescriptor getCurrentVersionAsReference() {
    return QcMaskVersionDescriptor.from(this.getId(), getCurrentQcMaskVersion().getVersion());
  }
}