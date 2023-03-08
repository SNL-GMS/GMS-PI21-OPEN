package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums.AnalysisIdTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdMapperTest {

  private AnalysisDataIdMapper idMapper;

  @BeforeEach
  void testSetup() {
    idMapper = new AnalysisDataIdMapper();
  }

  @Test
  void testGetOrGenerate() {
    var i = 0;
    var arid = 0L;
    var wfid = 0L;
    while (i < 100) {
      arid = idMapper.getOrGenerate(AnalysisIdTag.ARID, i, -1);
      wfid = idMapper.getOrGenerate(AnalysisIdTag.WFID, i, -1);
      i++;
    }

    assertEquals(100, arid);
    assertEquals(100, wfid);
  }

  @Test
  void testClear() {
    var firstArid = idMapper.getOrGenerate(AnalysisIdTag.ARID, 123, -1);
    var firstAridSecondCall = idMapper.getOrGenerate(AnalysisIdTag.ARID, 123, -1);
    var firstWfid = idMapper.getOrGenerate(AnalysisIdTag.WFID, 123, -1);
    var firstWfidSecondCall = idMapper.getOrGenerate(AnalysisIdTag.WFID, 123, -1);
    var secondArid = idMapper.getOrGenerate(AnalysisIdTag.ARID, 124, -1);
    var secondWfid = idMapper.getOrGenerate(AnalysisIdTag.WFID, 124, -1);

    assertEquals(1, firstArid);
    assertEquals(firstArid, firstAridSecondCall);
    assertEquals(1, firstWfid);
    assertEquals(firstArid, firstWfidSecondCall);
    assertEquals(2, secondArid);
    assertEquals(2, secondWfid);

    idMapper.clear();

    var thirdArid = idMapper.getOrGenerate(AnalysisIdTag.ARID, 123, -1);
    var thirdWfid = idMapper.getOrGenerate(AnalysisIdTag.WFID, 123, -1);
    var fourthArid = idMapper.getOrGenerate(AnalysisIdTag.ARID, 125, -1);
    var fourthWfid = idMapper.getOrGenerate(AnalysisIdTag.WFID, 125, -1);

    assertEquals(3, thirdArid);
    assertEquals(3, thirdWfid);
    assertEquals(4, fourthArid);
    assertEquals(4, fourthWfid);
  }
}
