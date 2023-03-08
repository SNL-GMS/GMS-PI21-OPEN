package gms.shared.stationdefinition.converter.util.assemblers;

import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters;
import gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFDISC_ARGS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_1_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.INSTRUMENT_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_2;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_4;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_5;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createWfdiscDao;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.END_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE4;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.SENSOR_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.START_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFDISC_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_3;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StationDefinitionVersionUtilityTest {

  @Test
  void getWfDiscsWithNextVersion_nextWfdiscSameVersion() {
    WfdiscDao wfdiscChangeCalper = createWfdiscDao(STA1, CHAN1, ONDATE3, ONDATE4, WFID_3, CHANID_3, WFDISC_PARAM_MAP);
    List<WfdiscDao> updatedWfdisc = StationDefinitionVersionUtility.getWfDiscsWithNextVersion(
      List.of(WFDISC_TEST_DAO_5, wfdiscChangeCalper));
    assertEquals(1, updatedWfdisc.size());
    assertEquals(CssDaoAndCoiParameters.ONDATE, updatedWfdisc.get(0).getTime());
    assertEquals(CssDaoAndCoiParameters.ONDATE4, updatedWfdisc.get(0).getEndTime());
  }

  @Test
  void getWfDiscsWithNextVersion_nextWfdiscNewVersion() {
    Map<WFDISC_ARGS, Number> WFDISC_PARAM_MAP_CHANGE_CALIBPER = Map.ofEntries(
      Map.entry(WFDISC_ARGS.CALIB, 0.061607), Map.entry(WFDISC_ARGS.CALIBPER, 2.0),
      Map.entry(WFDISC_ARGS.NSAMP, 1), Map.entry(WFDISC_ARGS.SAMPLERATE, 40.0),
      Map.entry(WFDISC_ARGS.FOFF, 20L), Map.entry(WFDISC_ARGS.COMMID, 21L));
    WfdiscDao wfdiscChangeCalper = createWfdiscDao(STA1, CHAN1, ONDATE3, ONDATE4, WFID_3, CHANID_3, WFDISC_PARAM_MAP_CHANGE_CALIBPER);
    List<WfdiscDao> updatedWfdisc = StationDefinitionVersionUtility.getWfDiscsWithNextVersion(
      List.of(WFDISC_TEST_DAO_5, wfdiscChangeCalper));
    assertEquals(1, updatedWfdisc.size());
    assertEquals(CssDaoAndCoiParameters.ONDATE, updatedWfdisc.get(0).getTime());
    assertEquals(CssDaoAndCoiParameters.ONDATE3, updatedWfdisc.get(0).getEndTime());
  }

  @Test
  void getWfDiscsWithNextVersion_emptyList() {
    List<WfdiscDao> updatedWfdisc = StationDefinitionVersionUtility.getWfDiscsWithNextVersion(List.of());
    assertEquals(0, updatedWfdisc.size());
  }

  @Test
  void getWfDiscsWithNextVersion_null() {
    Assertions.assertThrows(NullPointerException.class, () ->
      StationDefinitionVersionUtility.getWfDiscsWithNextVersion(null));
  }

  @Test
  void getSensorsWithVersionEndTime() {
    SensorDao sensorDao = CSSDaoTestFixtures.createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_2, SENSOR_PARAM_MAP,
      START_TIME.plusSeconds(30000), END_TIME);
    SensorDao sensorDao2 = CSSDaoTestFixtures.createSensorDao(STA1, CHAN1, CHANID_1, INSTRUMENT_DAO_1_1, SENSOR_PARAM_MAP,
      START_TIME, START_TIME.plusSeconds(30000));
    List<SensorDao> updatedSensor = StationDefinitionVersionUtility.getSensorsWithVersionEndTime(
      List.of(sensorDao, sensorDao2));
    assertEquals(1, updatedSensor.size());
    assertEquals(CssDaoAndCoiParameters.START_TIME, updatedSensor.get(0).getSensorKey().getTime());
    assertEquals(CssDaoAndCoiParameters.END_TIME, updatedSensor.get(0).getSensorKey().getEndTime());
  }

  @Test
  void getSensorsWithVersionEndTime_emptyList() {
    List<SensorDao> updatedSensor = StationDefinitionVersionUtility.getSensorsWithVersionEndTime(List.of());
    assertEquals(0, updatedSensor.size());
  }

  @Test
  void getSensorsWithVersionEndTime_null() {
    Assertions.assertThrows(NullPointerException.class, () ->
      StationDefinitionVersionUtility.getSensorsWithVersionEndTime(null));
  }

  @Test
  void getVersionMapAsIntWithSameVersionAttributeHash() {
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_4);

    Map<Integer, List<WfdiscDao>> wfdiscDaoByVersionMap = StationDefinitionVersionUtility.getVersionMapAsInt(
      wfdiscDaos, WfdiscDao::getVersionAttributeHash, WfdiscDao::getVersionTimeHash);

    assertEquals(1, wfdiscDaoByVersionMap.keySet().size());
    assertEquals(2, wfdiscDaoByVersionMap.get(WFDISC_TEST_DAO_1.getVersionTimeHash()).size());
  }

  @Test
  void getVersionMapAsIntWithDifferentVersionAttributeHash() {
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_2);

    Map<Integer, List<WfdiscDao>> wfdiscDaoByVersionMap = StationDefinitionVersionUtility.getVersionMapAsInt(
      wfdiscDaos, WfdiscDao::getVersionAttributeHash, WfdiscDao::getVersionTimeHash);

    assertEquals(2, wfdiscDaoByVersionMap.keySet().size());
  }

  @Test
  void getVersionMapWithSameVersionAttributeHash() {
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_4);

    Map<Instant, List<WfdiscDao>> wfdiscDaoByVersionMap = StationDefinitionVersionUtility.getVersionMapAsDouble(
      wfdiscDaos, WfdiscDao::getSampRateAsOptional, WfdiscDao::getTime);

    assertEquals(1, wfdiscDaoByVersionMap.keySet().size());
    assertEquals(2, wfdiscDaoByVersionMap.get(WFDISC_TEST_DAO_1.getTime()).size());
  }

  @Test
  void getVersionMapWithDifferentVersionAttributeHash() {
    List<WfdiscDao> wfdiscDaos = List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_2);

    Map<Integer, List<WfdiscDao>> wfdiscDaoByVersionMap = StationDefinitionVersionUtility.getVersionMapAsInt(
      wfdiscDaos, WfdiscDao::getVersionAttributeHash, WfdiscDao::getVersionTimeHash);

    assertEquals(2, wfdiscDaoByVersionMap.keySet().size());
  }
}