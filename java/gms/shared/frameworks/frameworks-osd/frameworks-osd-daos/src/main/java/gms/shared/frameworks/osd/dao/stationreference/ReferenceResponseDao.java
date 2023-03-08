package gms.shared.frameworks.osd.dao.stationreference;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.signaldetection.FrequencyAmplitudePhase;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA data access object for {@link ReferenceResponse}
 */
@Entity
@Table(name = "reference_response")
public class ReferenceResponseDao {
  @Id
  @Column(name = "id", nullable = false)
  private UUID referenceResponseId;

  @Column(name = "channel_name", nullable = false)
  private String channelName;

  @Column(name = "actual_time", nullable = false)
  private Instant actualTime;

  @Column(name = "system_time", nullable = false)
  private Instant systemTime;

  @Column(name = "comment")
  private String comment;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinTable(name = "reference_response_reference_source_response",
    joinColumns = {@JoinColumn(name = "reference_response_id", table = "reference_response", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "reference_source_response_id", table = "reference_source_response", referencedColumnName = "id")
    })
  private ReferenceSourceResponseDao sourceResponseDao;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinTable(name = "reference_response_reference_calibrations",
    joinColumns = {@JoinColumn(name = "reference_response_id", table = "reference_response", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "reference_calibration_id", table = "reference_calibration", referencedColumnName = "id")
    })
  private ReferenceCalibrationDao referenceCalibrationDao;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinTable(name = "reference_response_frequency_amplitude_phase",
    joinColumns = {@JoinColumn(name = "reference_response_id", referencedColumnName = "id")},
    inverseJoinColumns = {
      @JoinColumn(name = "reference_frequency_amplitude_phase_id", referencedColumnName = "id")
    })
  protected FrequencyAmplitudePhaseDao frequencyAmplitudePhaseDao;

  public ReferenceResponseDao() {
  }

  /**
   * Create a DAO from a COI
   */
  public ReferenceResponseDao(ReferenceResponse referenceResponse) {

    Preconditions.checkNotNull(referenceResponse,
      "Cannot create ReferenceResponseDao from null ReferenceResponse");

    this.referenceResponseId = referenceResponse.getReferenceResponseId();
    this.channelName = referenceResponse.getChannelName();
    this.actualTime = referenceResponse.getActualTime();
    this.systemTime = referenceResponse.getSystemTime();
    this.comment = referenceResponse.getComment();
    this.sourceResponseDao = referenceResponse.getSourceResponse().map(
      ReferenceSourceResponseDao::from).orElse(null);
    this.referenceCalibrationDao = ReferenceCalibrationDao
      .from(referenceResponse.getReferenceCalibration().getCalibrationInterval(),
        referenceResponse.getReferenceCalibration());

    Optional<FrequencyAmplitudePhase> response = referenceResponse.getFapResponse();
    this.frequencyAmplitudePhaseDao = response.map(FrequencyAmplitudePhaseDao::from).orElse(null);
  }

  /**
   * Create a COI from a DAO
   */
  public ReferenceResponse toCoi() {
    return ReferenceResponse.builder()
      .setReferenceResponseId(this.referenceResponseId)
      .setChannelName(this.channelName)
      .setActualTime(this.actualTime)
      .setSystemTime(this.systemTime)
      .setComment(this.comment)
      .setSourceResponse(sourceResponseDao.toCoi())
      .setReferenceCalibration(referenceCalibrationDao.toCoi())
      .setFapResponse(this.getFrequencyAmplitudePhase().map(FrequencyAmplitudePhaseDao::toCoi))
      .build();
  }

  public UUID getReferenceResponseId() {
    return referenceResponseId;
  }

  public void setReferenceResponseId(UUID referenceResponseId) {
    this.referenceResponseId = referenceResponseId;
  }

  public String getChannelName() {
    return channelName;
  }

  public void setChannelName(String channelName) {
    this.channelName = channelName;
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

  public ReferenceSourceResponseDao getSourceResponseDao() {
    return sourceResponseDao;
  }

  public void setSourceResponseDao(
    ReferenceSourceResponseDao sourceResponse) {
    this.sourceResponseDao = sourceResponse;
  }

  public ReferenceCalibrationDao getReferenceCalibrationDao() {
    return referenceCalibrationDao;
  }

  public void setReferenceCalibrationDao(
    ReferenceCalibrationDao referenceCalibrationDao) {
    this.referenceCalibrationDao = referenceCalibrationDao;
  }

  public Optional<FrequencyAmplitudePhaseDao> getFrequencyAmplitudePhase() {
    return Optional.ofNullable(frequencyAmplitudePhaseDao);
  }

  public void setFrequencyAmplitudePhase(FrequencyAmplitudePhaseDao response) {
    this.frequencyAmplitudePhaseDao = response;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceResponseDao that = (ReferenceResponseDao) o;
    return Objects.equals(referenceResponseId, that.referenceResponseId) &&
      Objects.equals(channelName, that.channelName) &&
      Objects.equals(actualTime, that.actualTime) &&
      Objects.equals(systemTime, that.systemTime) &&
      Objects.equals(comment, that.comment) &&
      Objects.equals(sourceResponseDao, that.sourceResponseDao) &&
      Objects.equals(referenceCalibrationDao, that.referenceCalibrationDao) &&
      Objects.equals(frequencyAmplitudePhaseDao, that.frequencyAmplitudePhaseDao);
  }

  @Override
  public int hashCode() {
    return Objects
      .hash(referenceResponseId, channelName, actualTime, systemTime, comment, sourceResponseDao,
        referenceCalibrationDao, frequencyAmplitudePhaseDao);
  }

  @Override
  public String toString() {
    return "ReferenceResponseDao{" +
      "referenceResponseId=" + referenceResponseId +
      ", channelName='" + channelName + '\'' +
      ", actualTime=" + actualTime +
      ", systemTime=" + systemTime +
      ", comment='" + comment + '\'' +
      ", sourceResponseDao=" + sourceResponseDao +
      ", referenceCalibrationDao=" + referenceCalibrationDao +
      ", frequencyAmplitudePhaseDao=" + frequencyAmplitudePhaseDao +
      '}';
  }
}
