package gms.dataacquisition.css.stationrefconverter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gms.dataacquisition.cssreader.data.InstrumentRecord;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import gms.dataacquisition.cssreader.utilities.CssReaderUtility;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.NetworkOrganization;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.ResponseTypes;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CssReferenceReaderTest {

  @BeforeAll
  static void setup() throws Exception {
    final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    CssReferenceReader.process(classLoader.getResource("data").getPath(),
      "test_config.network");
  }

  @Test
  void testExpectedNetworks() {
    // assert there are X networks present.
    final Collection<ReferenceNetwork> networks = CssReferenceReader.getReferenceNetworksByName().values();
    assertNotNull(networks);
    // assert that the list has only unique elements
    assertEquals(networks.size(), new HashSet<>(networks).size());
    assertEquals(5, networks.size());
    // assert expected properties of networks
    assertAllElementsMatched(networks, List.of(
      networkMatcher("test", "network of all stations in the test network"),
      networkMatcher("IMS_PRI", "IMS Primary Seismic Stations"),
      networkMatcher("IMS_AUX", "IMS Auxiliary Seismic Stations"),
      networkMatcher("IMS_HYD", "IMS Hydroacoustic Stations"),
      networkMatcher("IMS_INF", "IMS Infrasound Stations")));
  }

  @Test
  void testExpectedStations() {
    // assert there are X stations present.
    final Collection<ReferenceStation> stations = CssReferenceReader.getReferenceStationsByName().values();
    assertNotNull(stations);
    // assert that the list has only unique elements
    assertEquals(stations.size(), new HashSet<>(stations).size());
    assertEquals(7, stations.size());

    // assert that some particular stations are present - one each of the different kinds
    final List<Consumer<Collection<ReferenceStation>>> stationMatchers = List.of(
      // mkar
      stationMatcher(
        "MKAR", "Makanchi_Array,_Kazakhstan,_IMS",
        46.79368, 82.29057, 0.618, CssReaderUtility.jdToInstant("2000092").orElseThrow()),
      // kdak
      stationMatcher(
        "KDAK", "Kodiak_Island,_Alaska_USA",
        57.78280, -152.58350, 0.152, CssReaderUtility.jdToInstant("1997160").orElseThrow()),
      // H06E
      stationMatcher(
        "H06E", "Socorro,_Mexico",
        18.7805, -110.9253, 0.3160, CssReaderUtility.jdToInstant("2005333").orElseThrow()),
      // I51GB
      stationMatcher("I51GB", "Bermuda,_United_Kingdom",
        32.36154, -64.69874, -0.035, CssReaderUtility.jdToInstant("2008315").orElseThrow()));
    assertAllElementsMatched(stations, stationMatchers);
  }

  @Test
  void testExpectedSites() {
    // assert there are X sites present.
    final Collection<ReferenceSite> sites = CssReferenceReader.getReferenceSitesByName().values();
    assertNotNull(sites);
    // assert that the list has only unique elements
    assertEquals(sites.size(), new HashSet<>(sites).size());
    assertEquals(26, sites.size());
    final Location mkarLocation = Location.from(46.79368, 82.29057, 0, 0.618);
    final List<Consumer<Collection<ReferenceSite>>> siteMatchers = List.of(
      // MK01
      siteMatcher("MK01", "Makanchi_Array,_Kazakhstan,_IMS",
        46.76897, 82.30066, 0.608, CssReaderUtility.jdToInstant("2000092").orElseThrow(),
        RelativePosition.from(-2.749, 0.771, 0)),
      // MK32
      siteMatcher("MK32", "Makanchi_Array,_Kazakhstan,_IMS",
        46.79369, 82.29060, 0.617, CssReaderUtility.jdToInstant("2000254").orElseThrow(),
        RelativePosition.from(0.000, 0.002, 0)),
      // KDAK
      siteMatcher("KDAK", "Kodiak_Island,_Alaska_USA",
        57.78280, -152.58350, 0.152, CssReaderUtility.jdToInstant("1997160").orElseThrow(),
        RelativePosition.from(0, 0, 0)));
    assertAllElementsMatched(sites, siteMatchers);
  }

  @Test
  void testExpectedChannels() {
    // assert there are X channels present.
    final Collection<ReferenceChannel> channels = CssReferenceReader.getReferenceChannelsByName().values();
    assertNotNull(channels);
    // assert that the list has only unique elements
    assertEquals(channels.size(), new HashSet<>(channels).size());
    final double mk01Lat = 46.76897, mk01Lon = 82.30066, mk01Elev = 0.608;
    final double kdakLat = 57.78280, kdakLon = -152.58350, kdakElev = 0.152;
    assertEquals(91, channels.size());
    final List<Consumer<Collection<ReferenceChannel>>> channelMatchers = List.of(
      // MK01/sz
      channelMatcher("MK01.sz", mk01Lat, mk01Lon, mk01Elev, 0.029,
        -1, 0, 40, CssReaderUtility.jdToInstant("2000092").orElseThrow()),
      // MK01/SHZ
      channelMatcher("MK01.SHZ", mk01Lat, mk01Lon, mk01Elev, 0.03,
        -1, 0, 40, CssReaderUtility.jdToInstant("2004289").orElseThrow()),
      // KDAK/BH1
      channelMatcher("KDAK.BH1", kdakLat, kdakLon, kdakElev, 0.088,
        0, 90, 20, CssReaderUtility.jdToInstant("2014243").orElseThrow()),
      // KDAK/BH2
      channelMatcher("KDAK.BH2", kdakLat, kdakLon, kdakElev, 0.088,
        90, 90, 20, CssReaderUtility.jdToInstant("2014243").orElseThrow()));
    assertAllElementsMatched(channels, channelMatchers);
  }

  @Test
  void testExpectedNetworkMemberships() {
    final Set<ReferenceNetworkMembership> networkMemberships =
      CssReferenceReader.getReferenceNetworkMemberships();
    assertNotNull(networkMemberships);
    assertEquals(14, networkMemberships.size());
    assertNoDuplicatesByComparator(networkMemberships,
      CssReferenceReaderTest::isNetworkMembershipDuplicate);
    final UUID testNetworkId = getNetworkEntityId("test");
    final UUID imsAuxNetworkId = getNetworkEntityId("IMS_AUX");
    final UUID mkarStationId = getStationEntityId("MKAR");
    final UUID kdakStationId = getStationEntityId("KDAK");
    final List<Consumer<Collection<ReferenceNetworkMembership>>> membershipMatchers = List.of(
      // network 'test' to station 'MKAR'
      networkMembershipMatcher(testNetworkId, mkarStationId, Instant.ofEpochSecond(954547200)),
      // network 'IMS_AUX' to station 'KDAK'
      networkMembershipMatcher(imsAuxNetworkId, kdakStationId, Instant.ofEpochSecond(865814400))
    );
    assertAllElementsMatched(networkMemberships, membershipMatchers);
  }

  @Test
  void testExpectedStationMemberships() {
    final Set<ReferenceStationMembership> stationMemberships = CssReferenceReader.getReferenceStationMemberships();
    assertNotNull(stationMemberships);
    assertEquals(26, stationMemberships.size());
    assertNoDuplicatesByComparator(stationMemberships,
      CssReferenceReaderTest::isStationMembershipDuplicate);
    final UUID mkarStationId = getStationEntityId("MKAR");
    final UUID kdakStationId = getStationEntityId("KDAK");
    final UUID mk01SiteId = getSiteEntityId("MK01");
    final UUID mk32SiteId = getSiteEntityId("MK32");
    final UUID kdakSiteId = getSiteEntityId("KDAK");
    final List<Consumer<Collection<ReferenceStationMembership>>> membershipMatchers = List.of(
      stationMembershipMatcher(mkarStationId, mk01SiteId, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("2000092").orElseThrow()),
      stationMembershipMatcher(mkarStationId, mk32SiteId, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("2000254").orElseThrow()),
      stationMembershipMatcher(kdakStationId, kdakSiteId, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("1997160").orElseThrow())
    );
    assertAllElementsMatched(stationMemberships, membershipMatchers);
  }

  @Test
  void testExpectedSiteMemberships() {
    final Set<ReferenceSiteMembership> siteMemberships = CssReferenceReader.getReferenceSiteMemberships();
    assertNotNull(siteMemberships);
    // TODO: correct with input from SME's on right number
    assertEquals(91, siteMemberships.size());
    assertNoDuplicatesByComparator(siteMemberships,
      CssReferenceReaderTest::isSiteMembershipDuplicate);
    final UUID mk01SiteId = getSiteEntityId("MK01");
    final UUID kdakSiteId = getSiteEntityId("KDAK");
    final String mk01_sz_channelName = "MK01.sz";
    final String mk01_SHZ_channelName = "MK01.SHZ";
    final String kdak_BH1_channelName = "KDAK.BH1";
    final String kdak_sz_channelName = "KDAK.sz";
    final List<Consumer<Collection<ReferenceSiteMembership>>> membershipMatchers = List.of(
      // MK01 associated to channel sz
      siteMembershipMatcher(mk01SiteId, mk01_sz_channelName, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("2000092").orElseThrow()),
      // MK01 unassociated to channel sz
      siteMembershipMatcher(mk01SiteId, mk01_sz_channelName, StatusType.INACTIVE,
        CssReaderUtility.jdToInstant("2004288").orElseThrow()),
      // MK01 associated to channel SHZ
      siteMembershipMatcher(mk01SiteId, mk01_SHZ_channelName, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("2004289").orElseThrow()),
      // kdak associated to channel sz
      siteMembershipMatcher(kdakSiteId, kdak_sz_channelName, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("1997160").orElseThrow()),
      // kdak unassociated to channel sz
      siteMembershipMatcher(kdakSiteId, kdak_sz_channelName, StatusType.INACTIVE,
        CssReaderUtility.jdToInstant("2003013").orElseThrow()),
      // kdak associated to channel BH1
      siteMembershipMatcher(kdakSiteId, kdak_BH1_channelName, StatusType.ACTIVE,
        CssReaderUtility.jdToInstant("2014243").orElseThrow()));
    assertAllElementsMatched(siteMemberships, membershipMatchers);
  }

  @Test
  void testExpectedSensors() {
    final Collection<ReferenceSensor> sensors = CssReferenceReader.getReferenceSensors();
    Map<String, List<ReferenceSensor>> sensorsBystation = sensors.stream()
      .collect(Collectors.groupingBy(sensor -> {
        if (sensor.getChannelName().startsWith("M")) {
          return "MKAR";
        } else {
          return sensor.getChannelName().substring(0, 4);
        }
      }));

    assertNotNull(sensors);
    assertEquals(76, sensors.size());
    final List<Consumer<Collection<ReferenceSensor>>> sensorMatchers = List.of(
      sensorMatcher("MK01.SHZ", "Geotech_GS-21", "GS-21",
        Instant.ofEpochSecond(1101772800)),
      sensorMatcher("MK32.sz", "Geotech_GS-13", "GS-13",
        Instant.ofEpochSecond(968544000)),
      sensorMatcher("KDAK.BH1", "Geotech_KS-54000_Borehole_Seismometer",
        "KS5400", Instant.ofEpochSecond(1409443200))
    );
    assertAllElementsMatched(sensors, sensorMatchers);
  }

  @Test
  void testExpectedReferenceResponses() {
    final Collection<ReferenceResponse> referenceResponses = CssReferenceReader.getReferenceResponses();
    assertNotNull(referenceResponses);
    assertEquals(76, referenceResponses.size());

    // This is a little (a lot?) gnarly since ReferenceResponse has so much in it. But, because it's
    // all linked together, it's hard to break things out more cleanly.
    final List<Consumer<Collection<ReferenceResponse>>> responseMatchers = List.of(
      responseMatcher("MK01.SHZ", Instant.parse("2004-11-30T00:00:00Z"),
        Instant.parse("2004-11-30T00:00:00Z"), ResponseTypes.FAP, 365,
        1.0, 0,
        DoubleValue.from(0.012083, 0.0, Units.COUNTS_PER_NANOMETER),
        199, 0.1,
        DoubleValue.from(0.001543476, 0.0, Units.COUNTS_PER_NANOMETER),
        DoubleValue.from(261.33333, 0.0, Units.DEGREES)),
      responseMatcher("MK32.sz", Instant.parse("2000-09-10T00:00:00Z"),
        Instant.parse("2000-09-10T00:00:00Z"), ResponseTypes.FAP, 365,
        1.0, 0,
        DoubleValue.from(0.015, 0.0, Units.COUNTS_PER_NANOMETER),
        69, 0.01,
        DoubleValue.from(1.499E-6, 0.0, Units.COUNTS_PER_NANOMETER),
        DoubleValue.from(269.140541823, 0.0, Units.DEGREES)),
      responseMatcher("I51H1.BDF", Instant.parse("2008-11-10T17:26:44Z"),
        Instant.parse("2008-11-10T17:26:44Z"), ResponseTypes.FAP, 365,
        10.0, 0,
        DoubleValue.from(1.0E-4, 0.0, Units.COUNTS_PER_PASCAL),
        2048, 0.01,
        DoubleValue.from(0.710845318, 0.0, Units.COUNTS_PER_PASCAL),
        DoubleValue.from(44.948123, 0.0, Units.DEGREES))
    );

    assertAllElementsMatched(referenceResponses, responseMatchers);
  }

  @Test
  void testBuildReferenceChannelInvalidChannelNameReturnsEmpty() {
    Multimap<Integer, SensorRecord> mockSensorRecordsByChannelId = HashMultimap.create();
    Map<Integer, InstrumentRecord> mockInstrumentRecordsByInid = new HashMap<>();
    SiteChannelRecord mockRecord = Mockito.mock(SiteChannelRecord.class);

    Mockito.when(mockRecord.getChan()).thenReturn("BH100");
    Mockito.when(mockRecord.getOndate()).thenReturn(Optional.of(Instant.EPOCH));
    Mockito.when(mockRecord.getOffdate()).thenReturn(Optional.of(Instant.EPOCH));

    ReferenceSite mockSite = Mockito.mock(ReferenceSite.class);

    List<ReferenceChannel> referenceChannels = CssReferenceReader
      .buildReferenceChannel(mockSensorRecordsByChannelId, mockInstrumentRecordsByInid,
        mockRecord, mockSite);

    assertTrue(referenceChannels.isEmpty());
  }

  private static boolean isNetworkMembershipDuplicate(ReferenceNetworkMembership member1,
    ReferenceNetworkMembership member2) {

    return member1.getStationId().equals(member2.getStationId())
      && member1.getNetworkId().equals(member2.getNetworkId())
      && member1.getActualChangeTime().equals(member2.getActualChangeTime())
      && member1.getSystemChangeTime().equals(member2.getSystemChangeTime())
      && member1.getStatus().equals(member2.getStatus());
  }

  private static boolean isStationMembershipDuplicate(ReferenceStationMembership member1,
    ReferenceStationMembership member2) {

    return member1.getStationId().equals(member2.getStationId())
      && member1.getSiteId().equals(member2.getSiteId())
      && member1.getActualChangeTime().equals(member2.getActualChangeTime())
      && member1.getSystemChangeTime().equals(member2.getSystemChangeTime())
      && member1.getStatus().equals(member2.getStatus());
  }

  private static boolean isSiteMembershipDuplicate(ReferenceSiteMembership member1,
    ReferenceSiteMembership member2) {

    return member1.getChannelName().equals(member2.getChannelName())
      && member1.getSiteId().equals(member2.getSiteId())
      && member1.getActualChangeTime().equals(member2.getActualChangeTime())
      && member1.getSystemChangeTime().equals(member2.getSystemChangeTime())
      && member1.getStatus().equals(member2.getStatus());
  }

  private static <T> void assertNoDuplicatesByComparator(Collection<T> elems,
    BiPredicate<T, T> comparisonFunction) {

    for (T t : elems) {
      List<T> duplicateElems = elems.stream()
        .filter(e -> comparisonFunction.test(t, e))
        .collect(Collectors.toList());
      assertEquals(List.of(t), duplicateElems);
    }
  }

  private static <T> boolean containsElementMatchingPredicate(Collection<T> elems,
    Predicate<T> pred) {
    return elems.stream().anyMatch(pred);
  }

  private static <T> void assertAllElementsMatched(Collection<T> elems,
    Collection<Consumer<Collection<T>>> matchers) {
    matchers.forEach(m -> m.accept(elems));
  }

  private static Consumer<Collection<ReferenceNetwork>> networkMatcher(String name,
    String description) {
    final Instant expectedTime = Instant.EPOCH;
    final NetworkOrganization monitoringOrg = NetworkOrganization.CTBTO;
    final Predicate<ReferenceNetwork> predicate = n -> n.getName().equals(name) &&
      n.getDescription().equals(description) && n.getOrganization().equals(monitoringOrg) &&
      n.getActualChangeTime().equals(expectedTime) && n.getSystemChangeTime()
      .equals(Instant.EPOCH);
    return networks -> assertTrue(containsElementMatchingPredicate(networks, predicate),
      String.format("Expected to find network with name=%s and description=%s and organization=%s"
          + " and actual/systemChangeTime=%s in collection: %s",
        name, description, monitoringOrg, expectedTime, networks));
  }

  private static Consumer<Collection<ReferenceStation>> stationMatcher(String name,
    String description,
    double lat, double lon, double elev, Instant changeTime) {

    final Predicate<ReferenceStation> predicate = s -> s.getName().equals(name) && s
      .getDescription().equals(description) &&
      equal(s.getLatitude(), lat) && equal(s.getLongitude(), lon) && equal(s.getElevation(), elev)
      &&
      s.getActualChangeTime().equals(changeTime) && s.getSystemChangeTime().equals(changeTime);
    return stations -> assertTrue(containsElementMatchingPredicate(stations, predicate),
      String.format("Expected to find station with name=%s, description=%s, "
          + "lat=%f, lon=%f, elev=%f, actual/systemChangeTime=%s in collection: " + stations,
        name, description, lat, lon, elev, changeTime));
  }

  private static Consumer<Collection<ReferenceSite>> siteMatcher(String name, String description,
    double lat, double lon, double elev, Instant changeTime, RelativePosition relativePos) {

    final Predicate<ReferenceSite> predicate = s -> s.getName().equals(name) && s.getDescription()
      .equals(description) &&
      equal(s.getLatitude(), lat) && equal(s.getLongitude(), lon) && equal(s.getElevation(), elev)
      &&
      s.getActualChangeTime().equals(changeTime) && s.getSystemChangeTime().equals(changeTime) &&
      s.getPosition().equals(relativePos);
    return sites -> assertTrue(
      containsElementMatchingPredicate(sites, predicate),
      String.format("Expected to find site with name=%s, description=%s, "
          + "lat=%f, lon=%f, elev=%f, actual/systemChangeTime=%s, relativePosition=%s in collection: "
          + sites,
        name, description, lat, lon, elev, changeTime, relativePos));
  }

  private static Consumer<Collection<ReferenceChannel>> channelMatcher(String name, double lat,
    double lon, double elev, double depth, double horizAngle, double vertAngle, double sampleRate,
    Instant changeTime) {

    final Predicate<ReferenceChannel> predicate = c -> c.getName().equals(name) &&
      equal(c.getLatitude(), lat) && equal(c.getLongitude(), lon) && equal(c.getElevation(), elev)
      &&
      equal(c.getDepth(), depth) && equal(c.getVerticalAngle(), vertAngle) && equal(
      c.getHorizontalAngle(), horizAngle) &&
      equal(c.getNominalSampleRate(), sampleRate) && c.getActualTime().equals(changeTime) && c
      .getSystemTime().equals(changeTime);

    return channels -> assertTrue(
      containsElementMatchingPredicate(channels, predicate),
      String.format(
        "Expected to find channel with name=%s, lat=%f, lon=%f, elev=%f, depth=%f, verticalAngle=%f, "
          + "horizontalAngle=%f, nominalSampleRate=%f, actual/systemChangeTime=%s",
        name, lat, lon, elev, depth, vertAngle, horizAngle, sampleRate, changeTime));
  }

  private static Consumer<Collection<ReferenceNetworkMembership>> networkMembershipMatcher(
    UUID networkId, UUID stationId, Instant changeTime) {
    final StatusType status = StatusType.ACTIVE;
    final Predicate<ReferenceNetworkMembership> predicate = m -> m.getNetworkId().equals(networkId)
      &&
      m.getStationId().equals(stationId) && m.getStatus().equals(status) &&
      m.getActualChangeTime().equals(changeTime) && m.getSystemChangeTime().equals(changeTime);
    return memberships ->
      assertTrue(
        containsElementMatchingPredicate(memberships, predicate),
        String.format("Expected to find network membership with networkId=%s, stationId=%s, "
            + "status=%s, actual/systemChangeTime=%s", networkId, stationId, status,
          changeTime));
  }

  private static Consumer<Collection<ReferenceStationMembership>> stationMembershipMatcher(
    UUID stationId, UUID siteId, StatusType status, Instant changeTime) {

    final Predicate<ReferenceStationMembership> predicate = m -> m.getStationId().equals(stationId)
      &&
      m.getSiteId().equals(siteId) && m.getStatus().equals(status) &&
      m.getActualChangeTime().equals(changeTime) && m.getSystemChangeTime().equals(changeTime);
    return memberships -> assertTrue(
      containsElementMatchingPredicate(memberships, predicate),
      String.format("Expected to find station membership with stationId=%s, siteId=%s, "
        + "status=%s, actual/systemChangeTime=%s", stationId, siteId, status, changeTime));
  }

  private static Consumer<Collection<ReferenceSiteMembership>> siteMembershipMatcher(
    UUID siteId, String channelName, StatusType status, Instant changeTime) {

    final Predicate<ReferenceSiteMembership> predicate = m -> m.getSiteId().equals(siteId) &&
      m.getChannelName().equals(channelName) && m.getStatus().equals(status) &&
      m.getActualChangeTime().equals(changeTime) && m.getSystemChangeTime().equals(changeTime);
    return memberships -> assertTrue(
      containsElementMatchingPredicate(memberships, predicate),
      String.format("Expected to find site membership with siteId=%s, channelName=%s"
        + "status=%s, actual/systemChangeTime=%s", siteId, channelName, status, changeTime));
  }

  private Consumer<Collection<ReferenceSensor>> sensorMatcher(
    String channelName, String manufacturer, String model, Instant changeTime) {
    final Predicate<ReferenceSensor> predicate = s -> s.getChannelName().equals(channelName) &&
      s.getInstrumentManufacturer().equals(manufacturer) &&
      s.getInstrumentModel().equals(model) &&
      s.getActualTime().equals(changeTime) &&
      s.getSystemTime().equals(changeTime);
    return sensors -> assertTrue(
      containsElementMatchingPredicate(sensors, predicate),
      String.format("Expected to find sensor with channelId=%s, manufacturer=%s, "
          + "model=%s, actual/system change time=%s",
        channelName, manufacturer, model, changeTime));
  }

  private Consumer<Collection<ReferenceResponse>> responseMatcher(
    String channelName, Instant actualTime, Instant systemTime, ResponseTypes sourceResponseType,
    long calibrationIntervalDays, double calibrationPeriodSeconds,
    long calibrationTimeShiftSeconds, DoubleValue calibrationFactor,
    int responsesSize, Double firstResponseFrequency,
    DoubleValue firstResponseAmplitude, DoubleValue firstResponsePhase) {

    final Predicate<ReferenceResponse> predicate = r ->
      r.getActualTime().equals(actualTime) &&
        r.getSystemTime().equals(systemTime) &&
        (r.getSourceResponse().equals(Optional.empty()) ||
          r.getSourceResponse().orElseThrow(NoSuchElementException::new)
            .getSourceResponseTypes()
            .equals(sourceResponseType)) &&
        r.getReferenceCalibration().getCalibrationInterval().toDays() ==
          calibrationIntervalDays &&
        equal(r.getReferenceCalibration().getCalibration().getCalibrationPeriodSec(),
          calibrationPeriodSeconds) &&
        r.getReferenceCalibration().getCalibration().getCalibrationTimeShift().toSeconds() ==
          calibrationTimeShiftSeconds &&
        r.getReferenceCalibration().getCalibration().getCalibrationFactor()
          .equals(calibrationFactor) &&
        equal(r.getFapResponse().get().getFrequencies()[0], firstResponseFrequency) &&
        r.getFapResponse().get().getResponseAtFrequency(firstResponseFrequency).getAmplitude()
          .equals(firstResponseAmplitude) &&
        r.getFapResponse().get().getResponseAtFrequency(firstResponseFrequency).getPhase()
          .equals(firstResponsePhase);
    return responses -> assertTrue(
      containsElementMatchingPredicate(responses, predicate),
      String
        .format("Expected to find responses with channelName=%s, actualTime=%s, "
            + "systemTime=%s, sourceResponseType=%s, calibrationInterval(days):%d, "
            + "calibrationPeriod(seconds):%f, calibrationTimeShift(seconds):%d, "
            + "calibrationFactor=%s, responsesSize=%d, firstResponseAmplitude=%s, "
            + "firstResponsePhase=%s",
          channelName, actualTime.toString(), systemTime.toString(),
          sourceResponseType, calibrationIntervalDays, calibrationPeriodSeconds,
          calibrationTimeShiftSeconds, calibrationFactor.toString(), responsesSize,
          firstResponseAmplitude.toString(), firstResponsePhase.toString()));
  }

  private static boolean equal(double d1, double d2) {
    return Double.compare(d1, d2) == 0;
  }

  private UUID getNetworkEntityId(String name) {
    return getIdByName(CssReferenceReader.getReferenceNetworksByName().values(), name,
      ReferenceNetwork::getName, ReferenceNetwork::getEntityId);
  }

  private UUID getStationEntityId(String name) {
    return getIdByName(CssReferenceReader.getReferenceStationsByName().values(), name,
      ReferenceStation::getName, ReferenceStation::getEntityId);
  }

  private UUID getSiteEntityId(String name) {
    return getIdByName(CssReferenceReader.getReferenceSitesByName().values(), name,
      ReferenceSite::getName, ReferenceSite::getEntityId);
  }

  private static <T> UUID getIdByName(Collection<T> elems, String name,
    Function<T, String> nameExtractor, Function<T, UUID> idExtractor) {

    return idExtractor.apply(elems.stream()
      .filter(n -> nameExtractor.apply(n).equals(name))
      .findAny().orElseThrow(IllegalStateException::new));
  }

  private static Set<String> allChannelNames(Collection<StationGroup> groups) {
    return groups.stream()
      .map(StationGroup::getStations)
      .flatMap(Set::stream)
      .map(Station::getChannelGroups)
      .flatMap(Set::stream)
      .map(ChannelGroup::getChannels)
      .flatMap(Set::stream)
      .map(Channel::getName)
      .collect(toSet());
  }
}
