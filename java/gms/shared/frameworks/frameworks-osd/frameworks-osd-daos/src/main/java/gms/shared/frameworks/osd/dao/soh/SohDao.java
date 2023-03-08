package gms.shared.frameworks.osd.dao.soh;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SohDao {
  
  @Column(name = "creation_time", nullable = false)
  Instant creationTime;

  public Instant getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }
  
}
