package gms.shared.frameworks.osd.dao.stationreference;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceCalibration;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Duration;

/**
 * JPA data access object for {@link ReferenceCalibration}
 */
@Entity
@Table(name = "reference_calibration")
public class ReferenceCalibrationDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reference_calibration_sequence")
  @SequenceGenerator(name = "reference_calibration_sequence", sequenceName = "reference_calibration_sequence", allocationSize = 5)
  private long id;

  @Column(name = "calibration_interval")
  private Duration calibrationInterval;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinTable(name = "reference_calibration_calibrations",
    joinColumns = {@JoinColumn(name = "reference_calibration_id", table = "reference_calibration", referencedColumnName = "id", unique = true)},
    inverseJoinColumns = {@JoinColumn(name = "calibration_id", table = "calibrations", referencedColumnName = "id", unique = true)
    })
  private CalibrationDao calibrationDao;

  public ReferenceCalibrationDao() {
  }

  private ReferenceCalibrationDao(
    Duration calibrationInterval,
    CalibrationDao calibration) {
    this.calibrationInterval = calibrationInterval;
    this.calibrationDao = calibration;
  }

  /**
   * Create a DAO from a COI
   */
  public static ReferenceCalibrationDao from(ReferenceCalibration referenceCalibration) {
    Preconditions.checkNotNull(referenceCalibration,
      "Cannot create ReferenceCalibrationDao from null ReferenceCalibration");

    // calibration interval information is optional since it is not always known. set to zero if not known.
    Duration calibrationIntervalZero = Duration.ofSeconds(0);

    return new ReferenceCalibrationDao(calibrationIntervalZero,
      CalibrationDao.from(referenceCalibration.getCalibration()));
  }

  public static ReferenceCalibrationDao from(Duration calibrationInterval,
    ReferenceCalibration referenceCalibration) {
    Preconditions.checkNotNull(calibrationInterval,
      "Cannot create ReferenceCalibrationDao from null calibrationInterval");
    Preconditions.checkNotNull(referenceCalibration,
      "Cannot create ReferenceCalibrationDao from null ReferenceCalibration");

    return new ReferenceCalibrationDao(calibrationInterval,
      CalibrationDao.from(referenceCalibration.getCalibration()));
  }


  /**
   * Create a COI from a DAO
   */
  public ReferenceCalibration toCoi() {
    return ReferenceCalibration
      .from(this.calibrationInterval, this.calibrationDao.toCoi());
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Duration getCalibrationInterval() {
    return calibrationInterval;
  }

  public void setCalibrationInterval(Duration calibrationInterval) {
    this.calibrationInterval = calibrationInterval;
  }

  public CalibrationDao getCalibration() {
    return calibrationDao;
  }

  public void setCalibration(
    CalibrationDao calibration) {
    this.calibrationDao = calibration;
  }

}
