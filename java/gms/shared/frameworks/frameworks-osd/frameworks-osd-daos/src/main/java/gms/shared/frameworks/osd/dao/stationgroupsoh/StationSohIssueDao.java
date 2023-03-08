package gms.shared.frameworks.osd.dao.stationgroupsoh;

import gms.shared.frameworks.osd.coi.soh.StationSohIssue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Instant;

/**
 * JPA data access object for {@link StationSohIssue}
 */
@Entity
@Table(name = "station_soh_issue")
public class StationSohIssueDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_soh_issue_sequence")
  @SequenceGenerator(name = "station_soh_issue_sequence", sequenceName = "station_soh_issue_sequence", allocationSize = 5)
  private long id;

  @Column(name = "requires_acknowledgement", nullable = false)
  private boolean requiresAcknowledgement;

  @Column(name = "acknowledged_at", nullable = true)
  private Instant acknowledgedAt;

  public StationSohIssueDao() {
    // Empty constructor needed for JPA
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public boolean isRequiresAcknowledgement() {
    return requiresAcknowledgement;
  }

  public void setRequiresAcknowledgement(boolean requiresAcknowledgement) {
    this.requiresAcknowledgement = requiresAcknowledgement;
  }

  public Instant getAcknowledgedAt() {
    return acknowledgedAt;
  }

  public void setAcknowledgedAt(Instant acknowledgedAt) {
    this.acknowledgedAt = acknowledgedAt;
  }
}
