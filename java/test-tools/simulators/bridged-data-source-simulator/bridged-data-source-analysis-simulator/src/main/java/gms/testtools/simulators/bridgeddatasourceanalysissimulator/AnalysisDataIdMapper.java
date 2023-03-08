package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums.AnalysisIdTag;

import java.util.EnumMap;
import java.util.Map;

public class AnalysisDataIdMapper {


  private final Map<AnalysisIdTag, IdentifierMapping> idMappings;

  public AnalysisDataIdMapper() {
    idMappings = new EnumMap<>(AnalysisIdTag.class);
    var aridMapping = new IdentifierMapping(1L);
    var wfidMapping = new IdentifierMapping(1L);
    var ampidMapping = new IdentifierMapping(1L);
    var oridMapping = new IdentifierMapping(1L);
    var evidMapping = new IdentifierMapping(1L);
    var mbidMapping = new IdentifierMapping(1L);
    var msidMapping = new IdentifierMapping(1L);
    var mlidMapping = new IdentifierMapping(1L);
    idMappings.put(AnalysisIdTag.ARID, aridMapping);
    idMappings.put(AnalysisIdTag.WFID, wfidMapping);
    idMappings.put(AnalysisIdTag.AMPID, ampidMapping);
    idMappings.put(AnalysisIdTag.ORID, oridMapping);
    idMappings.put(AnalysisIdTag.EVID, evidMapping);
    idMappings.put(AnalysisIdTag.MBID, mbidMapping);
    idMappings.put(AnalysisIdTag.MSID, msidMapping);
    idMappings.put(AnalysisIdTag.MLID, mlidMapping);
  }


  /**
   * Gets the new long id generated for analysis record of given type and old long id
   *
   * @param identifierType - type of record (e.g. Arrival, Wfdisc,etc.)
   * @param seedIdentifier - a {@link Long} corresponding to an analysis's record's id
   * @return {@link Long} the new id of the analysis record
   */
  protected long getOrGenerate(AnalysisIdTag identifierType, long seedIdentifier,
    long seedNAValue) {
    if (seedIdentifier == seedNAValue) {
      return seedIdentifier;
    } else {
      return idMappings.get(identifierType).get(seedIdentifier);
    }
  }


  /**
   * Clear all identifier mappings after the generated data has been stored
   */
  protected void clear() {
    idMappings.forEach((idType, mapping) -> mapping.clear());
  }
}
