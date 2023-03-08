package gms.shared.stationdefinition.dao.css;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "beam")
public class BeamDao {

  private long wfId;
  private int filterId;
  private double azimuth;
  private double slowness;
  private String description;
  private Instant loadDate;

  public BeamDao() {
  }

  /**
   * Create a deep copy of the given {@link BeamDao}
   *
   * @param beamDao BeamDao to copy
   * @return {@link BeamDao}
   */
  public BeamDao(BeamDao beamDao) {

    this.wfId = beamDao.wfId;
    this.filterId = beamDao.filterId;
    this.azimuth = beamDao.azimuth;
    this.slowness = beamDao.slowness;
    this.description = beamDao.description;
    this.loadDate = beamDao.loadDate;
  }

  @Id
  @Column(name = "wfid")
  public long getWfId() {
    return wfId;
  }

  public void setWfId(long wfId) {
    this.wfId = wfId;
  }

  @Column(name = "filterid")
  public int getFilterId() {
    return filterId;
  }

  public void setFilterId(int filterId) {
    this.filterId = filterId;
  }

  @Column(name = "azimuth", nullable = false)
  public double getAzimuth() {
    return azimuth;
  }

  public void setAzimuth(double azimuth) {
    this.azimuth = azimuth;
  }

  @Column(name = "slow", nullable = false)
  public double getSlowness() {
    return slowness;
  }

  public void setSlowness(double slowness) {
    this.slowness = slowness;
  }

  @Column(name = "descrip", nullable = false)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Column(name = "lddate", nullable = false)
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }
}
