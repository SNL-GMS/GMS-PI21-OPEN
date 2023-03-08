package gms.shared.frameworks.osd.dao.soh;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import java.util.Objects;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@MappedSuperclass
public abstract class SohMonitorValueAndStatusDao extends SohDao {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "smvsSequenceGenerator")
  @SequenceGenerator(name = "smvsSequenceGenerator", sequenceName = "smvs_sequence", allocationSize = 100)
  private int id;
  
  @Convert(converter = SohStatusConverter.class)
  @Column(name = "status", nullable = false)
  private SohStatus status;

  @Convert(converter = SohMonitorTypeConverter.class)
  @Column(name = "monitor_type", nullable = false)
  private SohMonitorType monitorType;

  @Column(name = "duration")
  private Integer duration;

  @Column(name = "percent")
  private Float percent;
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public SohStatus getStatus() {
    return status;
  }

  public void setStatus(SohStatus status) {
    this.status = status;
  }

  public SohMonitorType getMonitorType() {
    return monitorType;
  }

  public void setMonitorType(SohMonitorType monitorType) {
    this.monitorType = monitorType;
  }

  public Integer getDuration() {
    return this.duration;
  }

  public void setDuration(final Integer duration) {
    this.duration = duration;
  }

  public Float getPercent() {
    return this.percent;
  }

  public void setPercent(final Float percent) {
    this.percent = percent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SohMonitorValueAndStatusDao that = (SohMonitorValueAndStatusDao) o;
    return id == that.id &&
      status == that.status &&
      monitorType == that.monitorType && 
      Objects.equals(creationTime, that.creationTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, monitorType, creationTime);
  }
}
