package gms.shared.frameworks.osd.dao.emerging.provenance;

import gms.shared.frameworks.osd.coi.provenance.InformationSource;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class InformationSourceDao {

  @Column(name = "originating_organization", nullable = false)
  private String originatingOrganization;

  @Column(name = "information_time", nullable = false)
  private Instant informationTime;

  @Column(name = "reference", nullable = false)
  private String reference;

  /**
   * Default constructor for use by JPA.
   */
  public InformationSourceDao() {
  }

  public InformationSourceDao(InformationSource informationSource) {
    Objects.requireNonNull(informationSource);
    setOriginatingOrganization(informationSource.getOriginatingOrganization());
    setInformationTime(informationSource.getInformationTime());
    setReference(informationSource.getReference());
  }

  /**
   * Convert this DAO into the associated COI object.
   *
   * @return An instance of the InformationSource object.
   */
  public InformationSource toCoi() {

    return InformationSource.from(getOriginatingOrganization(),
      getInformationTime(), getReference());
  }


  public String getOriginatingOrganization() {
    return originatingOrganization;
  }

  public void setOriginatingOrganization(String originatingOrganization) {
    this.originatingOrganization = originatingOrganization;
  }

  public Instant getInformationTime() {
    return informationTime;
  }

  public void setInformationTime(Instant informationTime) {
    this.informationTime = informationTime;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InformationSourceDao that = (InformationSourceDao) o;

    if (originatingOrganization != null ? !originatingOrganization
      .equals(that.originatingOrganization) : that.originatingOrganization != null) {
      return false;
    }
    if (informationTime != null ? !informationTime.equals(that.informationTime)
      : that.informationTime != null) {
      return false;
    }
    return reference != null ? reference.equals(that.reference) : that.reference == null;
  }
}
