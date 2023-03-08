package gms.shared.stationdefinition.repository.util;

import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.repository.BridgedRepositoryUtils;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedRepositoryUtilTest {

  @Mock
  WfdiscDatabaseConnector wfdiscDatabaseConnector;

  @Mock
  SensorDatabaseConnector sensorDatabaseConnector;

  @Mock
  InstrumentDatabaseConnector instrumentDatabaseConnector;

  @Test
  void testGetMinMaxFromSiteDaos() {

    var siteDao1 = DefaultCoiTestFixtures.getDefaultSiteDao();
    var siteDao2 = DefaultCoiTestFixtures.getDefaultSiteDao();

    var t1 = Instant.parse("2008-11-10T17:26:44Z");
    var t2 = Instant.parse("2010-11-10T17:26:44Z");
    var t3 = Instant.parse("2015-11-10T17:26:44Z");

    siteDao1.getId().setOnDate(t1);
    siteDao1.setOffDate(t2);
    siteDao2.getId().setOnDate(t2);
    siteDao2.setOffDate(t3);

    var pairTime = BridgedRepositoryUtils.getMinMaxFromSiteDaos(List.of(siteDao1, siteDao2), t1, t2);

    assertEquals(t1.minusSeconds(1), pairTime.getLeft());
    assertEquals(t3.plusSeconds(1), pairTime.getRight());
  }

  @Test
  void testGetMinMaxFromSiteDaosMaxMinTimes() {

    var siteDao1 = DefaultCoiTestFixtures.getDefaultSiteDao();
    var t1 = Instant.parse("2008-11-10T17:26:44Z");
    var t2 = Instant.parse("2010-11-10T17:26:44Z");

    siteDao1.getId().setOnDate(Instant.MIN);
    siteDao1.setOffDate(Instant.MAX);

    var pairTime = BridgedRepositoryUtils.getMinMaxFromSiteDaos(List.of(siteDao1), t1, t2);

    assertEquals(Instant.MIN, pairTime.getLeft());
    assert(pairTime.getRight().isBefore(Instant.MAX));
  }

  @Test
  void testGetSensorAndWfdiscData(){

    var siteChan1 = DefaultCoiTestFixtures.getDefaultSiteChanDao();
    var siteChan2 = DefaultCoiTestFixtures.getDefaultSiteChanDao();
    var t1 = Instant.parse("2008-11-10T17:26:44Z");
    var t2 = Instant.parse("2010-11-10T17:26:44Z");
    var t3 = Instant.parse("2015-11-10T17:26:44Z");

    siteChan1.getId().setOnDate(t1);
    siteChan1.setOffDate(t2);
    siteChan2.getId().setOnDate(t2);
    siteChan2.setOffDate(t3);

    var sensor1 = DefaultCoiTestFixtures.getDefaultSensorDao();
    var t4 = Instant.parse("2008-04-10T17:26:44Z");
    sensor1.setEndTime(t2);
    sensor1.getSensorKey().setTime(t4);
    var wfdisc = DefaultCoiTestFixtures.getDefaultWfdisc();

    var listOfSiteChanKeys = List.of(siteChan1.getId(), siteChan2.getId());

    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(listOfSiteChanKeys, t1, t3)).thenReturn(List.of(sensor1));
    when(wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(listOfSiteChanKeys, t4, t3)).thenReturn(List.of(wfdisc));

    var dataHolder = new StationDefinitionDataHolder(List.of(), List.of(siteChan1, siteChan2), List.of(), List.of(), List.of());

    var dataHolderReturned =
      BridgedRepositoryUtils.getSensorAndWfdiscData(dataHolder, sensorDatabaseConnector, wfdiscDatabaseConnector);

    assertEquals(List.of(sensor1), dataHolderReturned.getSensorDaos());
    assertEquals(1, dataHolderReturned.getWfdiscVersions().size());
  }

  @Test
  void testGetInstrumentData(){

    var sensor1 = DefaultCoiTestFixtures.getDefaultSensorDao();
    var t2 = Instant.parse("2010-11-10T17:26:44Z");
    var t4 = Instant.parse("2008-04-10T17:26:44Z");
    sensor1.setEndTime(t2);
    sensor1.getSensorKey().setTime(t4);

    var dataHolder = new StationDefinitionDataHolder(List.of(), List.of(), List.of(sensor1), List.of(), List.of());

    var instrument = DefaultCoiTestFixtures.getDefaultInstrumentDao();
    when(instrumentDatabaseConnector.findInstruments(List.of(sensor1.getInstrument().getInstrumentId())))
      .thenReturn(List.of(instrument));

    var insDaos = BridgedRepositoryUtils.getInstrumentData(dataHolder, instrumentDatabaseConnector);
    assertEquals(1, insDaos.size());
  }
}
