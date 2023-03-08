package gms.shared.frameworks.osd.dao.soh.statuschange;

import gms.shared.frameworks.osd.dao.channel.StationDao;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Entity
@NamedQuery(name = "SohStatusChangeEventDao.checkExistsByStationName", query = "SELECT dao FROM SohStatusChangeEventDao dao WHERE dao.station.name = :stationName")
@Table(name = "soh_status_change_event")
public class SohStatusChangeEventDao {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ElementCollection(targetClass = SohStatusChangeDao.class)
  @CollectionTable(name = "soh_status_change_collection",
    joinColumns = {@JoinColumn(name = "unack_id",
      referencedColumnName = "id")})
  private Collection<SohStatusChangeDao> sohStatusChangeDaos;

  @OneToOne
  @JoinColumn(
    name = "station_name",
    referencedColumnName = "name",
    unique = true,
    nullable = false,
    updatable = false
  )
  private StationDao station;


  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public StationDao getStationDao() {
    return station;
  }

  public void setStationDao(StationDao station) {
    this.station = station;
  }

  public Collection<SohStatusChangeDao> getSohStatusChangeDaos() {
    return sohStatusChangeDaos;
  }

  public void setSohStatusChangeDaos(Collection<SohStatusChangeDao> sohStatusChangeDaos) {
    this.sohStatusChangeDaos = sohStatusChangeDaos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SohStatusChangeEventDao)) {
      return false;
    }
    SohStatusChangeEventDao that = (SohStatusChangeEventDao) o;
    return station.equals(that.station) &&
      Objects.equals(sohStatusChangeDaos, that.sohStatusChangeDaos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, sohStatusChangeDaos);
  }


}
