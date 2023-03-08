package gms.shared.frameworks.osd.dao.stationreference;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSourceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ResponseTypes;
import gms.shared.frameworks.osd.dao.emerging.provenance.InformationSourceDao;
import org.apache.commons.lang3.Validate;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA data access object for {@link ReferenceSourceResponse}
 */
@Entity
@Table(name = "reference_source_response")
public class ReferenceSourceResponseDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reference_source_response_sequence")
  @SequenceGenerator(name = "reference_source_response_sequence", sequenceName = "reference_source_response_sequence", allocationSize = 5)
  private long id;

  @Column(name = "source_response_data", nullable = false)
  private byte[] sourceResponseData;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_response_units", nullable = false)
  private Units sourceResponseUnits;

  @Column(name = "source_response_type", nullable = false)
  private ResponseTypes sourceResponseType;

  @ElementCollection
  @CollectionTable(name = "reference_source_response_information_sources",
    joinColumns = @JoinColumn(name = "reference_source_response_id"))
  protected List<InformationSourceDao> sourceResponseInformationSource;

  protected ReferenceSourceResponseDao() {
  }

  /**
   * Create a DAO from a COI
   */
  public static ReferenceSourceResponseDao from(ReferenceSourceResponse referenceSourceResponse) {
    Preconditions.checkNotNull(referenceSourceResponse,
      "Cannot create ReferenceSourceResponseDao from null ReferenceSourceResponse");
    return new ReferenceSourceResponseDao(referenceSourceResponse);
  }

  private ReferenceSourceResponseDao(ReferenceSourceResponse referenceSourceResponse) {
    List<InformationSourceDao> informationSourceDaoList = new ArrayList<>();
    for (InformationSource entry : referenceSourceResponse.getInformationSources()) {
      informationSourceDaoList.add(new InformationSourceDao(entry));
      Validate.notEmpty(informationSourceDaoList,
        "InformationSourceDaoList in ReferenceSourceResponseDao is empty");
    }
    this.sourceResponseData = referenceSourceResponse.getSourceResponseData();
    this.sourceResponseUnits = referenceSourceResponse.getSourceResponseUnits();
    this.sourceResponseType = referenceSourceResponse.getSourceResponseTypes();
    this.sourceResponseInformationSource = informationSourceDaoList;
  }

  /**
   * Create a COI from a DAO
   */
  public ReferenceSourceResponse toCoi() {
    List<InformationSource> informationSourceList = new ArrayList<>();
    for (InformationSourceDao entry : this.sourceResponseInformationSource) {
      informationSourceList.add(entry.toCoi());
    }

    return ReferenceSourceResponse.builder()
      .setSourceResponseData(this.sourceResponseData)
      .setSourceResponseUnits(this.sourceResponseUnits)
      .setSourceResponseTypes(this.sourceResponseType)
      .setInformationSources(informationSourceList)
      .build();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public byte[] getSourceResponseData() {
    return sourceResponseData;
  }

  public void setSourceResponseData(byte[] sourceResponseData) {
    this.sourceResponseData = sourceResponseData;
  }

  public Units getSourceResponseUnits() {
    return sourceResponseUnits;
  }

  public void setSourceResponseUnits(
    Units sourceResponseUnits) {
    this.sourceResponseUnits = sourceResponseUnits;
  }

  public ResponseTypes getSourceResponseType() {
    return sourceResponseType;
  }

  public void setSourceResponseType(ResponseTypes sourceResponseType) {
    this.sourceResponseType = sourceResponseType;
  }

  public List<InformationSourceDao> getSourceResponseInformationSource() {
    return sourceResponseInformationSource;
  }

  public void setSourceResponseInformationSource(
    List<InformationSourceDao> sourceResponseInformationSource) {
    this.sourceResponseInformationSource = sourceResponseInformationSource;
  }
}
