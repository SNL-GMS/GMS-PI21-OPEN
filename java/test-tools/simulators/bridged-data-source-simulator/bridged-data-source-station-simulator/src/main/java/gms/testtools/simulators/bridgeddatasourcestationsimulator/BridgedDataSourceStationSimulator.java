package gms.testtools.simulators.bridgeddatasourcestationsimulator;

import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.database.connector.AffiliationDatabaseConnector;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.NetworkDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorAugmentation;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.Site;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.SiteChan;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceStationRepositoryJpa;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Bridged Data Source Station Simulator is responsible for loading station definitions into the
 * simulation database for a specified simulation by copying definitions from the pre-installed
 * seed data set into the simulation data set . The Simulator loads a single version for each
 * station, channel group & channel & response effective (based on ontime) for the operational time
 * period (as defined in the spec).
 */
public class BridgedDataSourceStationSimulator implements BridgedDataSourceDataSimulator, BridgedDataSourceSimulatorAugmentation {

  private static final String VERSION_KEY_TEMPLATE = "%s:%s";
  private final NetworkDatabaseConnector networkDatabaseConnector;
  private final AffiliationDatabaseConnector affiliationDatabaseConnector;
  private final SiteDatabaseConnector siteDatabaseConnector;
  private final SiteChanDatabaseConnector siteChanDatabaseConnector;
  private final SensorDatabaseConnector sensorDatabaseConnector;
  private final InstrumentDatabaseConnector instrumentDatabaseConnector;
  private final BridgedDataSourceStationRepositoryJpa bridgedDataSourceRepository;
  private long maxChanId;

  private static final Logger logger = LoggerFactory.getLogger(BridgedDataSourceStationSimulator.class);

  private BridgedDataSourceStationSimulator(
    NetworkDatabaseConnector networkDatabaseConnector,
    AffiliationDatabaseConnector affiliationDatabaseConnector,
    SiteDatabaseConnector siteDatabaseConnector,
    SiteChanDatabaseConnector siteChanDatabaseConnector,
    SensorDatabaseConnector sensorDatabaseConnector,
    InstrumentDatabaseConnector instrumentDatabaseConnector,
    BridgedDataSourceStationRepositoryJpa bridgedDataSourceRepository) {
    this.networkDatabaseConnector = networkDatabaseConnector;
    this.affiliationDatabaseConnector = affiliationDatabaseConnector;
    this.siteDatabaseConnector = siteDatabaseConnector;
    this.siteChanDatabaseConnector = siteChanDatabaseConnector;
    this.sensorDatabaseConnector = sensorDatabaseConnector;
    this.instrumentDatabaseConnector = instrumentDatabaseConnector;
    this.bridgedDataSourceRepository = bridgedDataSourceRepository;
    this.maxChanId = 0;
  }

  /**
   * Creates a {@link BridgedDataSourceStationSimulator} given the required, non null components.
   *
   * @param networkDatabaseConnector - provides read access to {@link gms.shared.stationdefinition.dao.css.NetworkDao}s
   * in the database's seed schema
   * @param affiliationDatabaseConnector - provides read access to {@link gms.shared.stationdefinition.dao.css.AffiliationDao}s
   * in the database's seed schema
   * @param siteDatabaseConnector - provides read access to {@link gms.shared.stationdefinition.dao.css.SiteDao}s
   * in the database's seed schema
   * @param siteChanDatabaseConnector - provides read access to {@link gms.shared.stationdefinition.dao.css.SiteChanDao}s
   * in the database's seed schema
   * @param sensorDatabaseConnector - provides read access to {@link gms.shared.stationdefinition.dao.css.SensorDao}s
   * in the database's seed schema
   * @param instrumentDatabaseConnector - provides read access to {@link gms.shared.stationdefinition.dao.css.InstrumentDao}s
   * in the database's seed schema
   * @param bridgedDataSourceStationRepositoryJpa - provides write and cleanup access in the database's
   * simulation schema
   * @return {@link BridgedDataSourceStationSimulator}
   */
  public static BridgedDataSourceStationSimulator create(
    NetworkDatabaseConnector networkDatabaseConnector,
    AffiliationDatabaseConnector affiliationDatabaseConnector,
    SiteDatabaseConnector siteDatabaseConnector,
    SiteChanDatabaseConnector siteChanDatabaseConnector,
    SensorDatabaseConnector sensorDatabaseConnector,
    InstrumentDatabaseConnector instrumentDatabaseConnector,
    BridgedDataSourceStationRepositoryJpa bridgedDataSourceStationRepositoryJpa) {
    Validate.notNull(networkDatabaseConnector);
    Validate.notNull(affiliationDatabaseConnector);
    Validate.notNull(siteDatabaseConnector);
    Validate.notNull(siteChanDatabaseConnector);
    Validate.notNull(sensorDatabaseConnector);
    Validate.notNull(instrumentDatabaseConnector);
    Validate.notNull(bridgedDataSourceStationRepositoryJpa);
    return new BridgedDataSourceStationSimulator(
      networkDatabaseConnector,
      affiliationDatabaseConnector,
      siteDatabaseConnector,
      siteChanDatabaseConnector,
      sensorDatabaseConnector,
      instrumentDatabaseConnector,
      bridgedDataSourceStationRepositoryJpa);
  }

  /**
   * Initialize station definitions for the specified simulation based on the provided spec, copying
   * and modifying data from the seed data set.
   *
   * @param bridgeSimulatorSpec - An {@link BridgedDataSourceSimulatorSpec} to provided the
   * simulation specification details.
   */
  @Override
  public void initialize(BridgedDataSourceSimulatorSpec bridgeSimulatorSpec) {
    var start = Instant.now();
    final Instant startTime = bridgeSimulatorSpec.getSeedDataStartTime();
    final Instant endTime = bridgeSimulatorSpec.getSeedDataEndTime();
    final Duration operationalTimePeriod = bridgeSimulatorSpec.getOperationalTimePeriod();
    final Instant operationalStartTime = Instant.now().minus(operationalTimePeriod);

    final Instant onDate = operationalStartTime.minus(1, ChronoUnit.DAYS);
    final Instant offDate = Instant.MAX;

    final List<AffiliationDao> affiliationDaos = initializeAffiliations(startTime, endTime, onDate,
      offDate);
    initializeNetworks(
      affiliationDaos.stream().map(a -> a.getNetworkStationTimeKey().getNetwork())
        .collect(Collectors.toList()),
      onDate);
    initializeSites(startTime, endTime, onDate, offDate);
    final List<SiteChanDao> siteChanDaos = initializeSiteChans(startTime, endTime, onDate, offDate);
    initializeSensors(
      siteChanDaos.stream().map(SiteChanDao::getChannelId).collect(Collectors.toList()),
      startTime, endTime, onDate, offDate);
    var end = Instant.now();
    logger.info("station simulator finished initializing at {} taking {} ms",
      end,
      Duration.between(start, end).toMillis());
  }

  private void initializeNetworks(List<String> networkIds, Instant onDate) {
    final List<NetworkDao> simulationNetworks = new ArrayList<>();
    final Collection<List<NetworkDao>> networkVersions = networkDatabaseConnector
      .findNetworks(networkIds)
      .stream()
      .collect(Collectors.groupingBy(NetworkDao::getNetworkId))
      .values();

    for (List<NetworkDao> networkDaos : networkVersions) {
      var networkDao = networkDaos.stream().max((s1, s2) -> Objects
        .compare(s1.getLdDate(), s2.getLdDate(), Comparator
          .nullsLast(Comparator.naturalOrder()))).orElseThrow();
      networkDao.setLdDate(onDate);
      simulationNetworks.add(networkDao);
    }

    bridgedDataSourceRepository.store(simulationNetworks);
  }

  private List<AffiliationDao> initializeAffiliations(Instant startTime, Instant endTime,
    Instant onDate,
    Instant offDate) {
    final List<AffiliationDao> simulationAffiliations = new ArrayList<>();
    final Collection<List<AffiliationDao>> affiliationVersions = affiliationDatabaseConnector
      .findAffiliationsByTimeRange(startTime, endTime)
      .stream()
      .collect(Collectors.groupingBy(s -> String
        .format(VERSION_KEY_TEMPLATE, s.getNetworkStationTimeKey().getNetwork(),
          s.getNetworkStationTimeKey().getStation())))
      .values();
    for (List<AffiliationDao> affiliationDaos : affiliationVersions) {
      var affiliationDao = affiliationDaos.stream().max((s1, s2) -> Objects
        .compare(s1.getNetworkStationTimeKey().getTime(),
          s2.getNetworkStationTimeKey().getTime(), Comparator
            .nullsLast(Comparator.naturalOrder()))).orElseThrow();
      affiliationDao.setEndTime(offDate);
      affiliationDao.getNetworkStationTimeKey().setTime(onDate);
      simulationAffiliations.add(affiliationDao);
    }
    bridgedDataSourceRepository.store(simulationAffiliations);
    return simulationAffiliations;
  }

  private void initializeSites(Instant startTime, Instant endTime, Instant onDate,
    Instant offDate) {
    final List<SiteDao> simulationSites = new ArrayList<>();
    final Collection<List<SiteDao>> siteVersions = siteDatabaseConnector
      .findSitesByTimeRange(startTime,
        endTime)
      .stream()
      .collect(Collectors.groupingBy(s -> s.getId().getStationCode()))
      .values();
    for (List<SiteDao> siteDaos : siteVersions) {
      var siteDao = siteDaos.stream().max((s1, s2) -> Objects
        .compare(s1.getId().getOnDate(), s2.getId().getOnDate(),
          Comparator.nullsLast(Comparator.naturalOrder()))).orElseThrow();
      siteDao.setOffDate(offDate);
      siteDao.getId().setOnDate(onDate);
      simulationSites.add(siteDao);
    }
    bridgedDataSourceRepository.store(simulationSites);
  }

  private List<SiteChanDao> initializeSiteChans(Instant startTime, Instant endTime, Instant onDate,
    Instant offDate) {

    final List<SiteChanDao> simulationSiteChans = new ArrayList<>();
    final Collection<List<SiteChanDao>> siteChanVersions = siteChanDatabaseConnector
      .findSiteChansByTimeRange(startTime, endTime)
      .stream()
      .collect(Collectors.groupingBy(
        s -> String.format(VERSION_KEY_TEMPLATE, s.getId().getStationCode(),
          s.getId().getChannelCode())))
      .values();
    for (List<SiteChanDao> siteChanDaos : siteChanVersions) {
      var siteChanDao = siteChanDaos.stream().max((s1, s2) -> Objects
        .compare(s1.getId().getStationCode(),
          s2.getId().getChannelCode(), Comparator
            .nullsLast(Comparator.naturalOrder()))).orElseThrow();
      siteChanDao.setOffDate(offDate);
      siteChanDao.getId().setOnDate(onDate);

      maxChanId = Math.max(maxChanId, siteChanDao.getChannelId());
      simulationSiteChans.add(siteChanDao);
    }
    bridgedDataSourceRepository.store(simulationSiteChans);
    return simulationSiteChans;
  }

  private List<SensorDao> initializeSensors(Collection<Long> channelIds,
    Instant startTime, Instant endTime, Instant onDate,
    Instant offDate) {
    final Set<InstrumentDao> observedInstruments = new HashSet<>();
    final List<SensorDao> simulationSensors = new ArrayList<>();
    final Collection<List<SensorDao>> sensorVersions = sensorDatabaseConnector
      .findSensorsByChannelIdAndTimeRange(channelIds, startTime, endTime)
      .stream()
      .collect(Collectors.groupingBy(s -> String
        .format(VERSION_KEY_TEMPLATE, s.getSensorKey().getStation(),
          s.getSensorKey().getChannel())))
      .values();
    for (List<SensorDao> c : sensorVersions) {
      var sensorDao = c.stream().max((s1, s2) -> Objects
        .compare(s1.getSensorKey().getStation(),
          s2.getSensorKey().getChannel(), Comparator
            .nullsLast(Comparator.naturalOrder()))).orElseThrow();
      var currentInstrument = sensorDao.getInstrument();
      observedInstruments.add(currentInstrument);
      sensorDao.getSensorKey().setEndTime(offDate);
      sensorDao.getSensorKey().setTime(onDate);
      simulationSensors.add(sensorDao);
    }

    initializeInstruments(observedInstruments.stream()
      .map(InstrumentDao::getInstrumentId)
      .collect(Collectors.toList()), onDate);

    bridgedDataSourceRepository.store(simulationSensors);
    return simulationSensors;
  }

  private Map<Long, InstrumentDao> initializeInstruments(Collection<Long> instrumentIds, Instant onDate) {
    final Map<Long, InstrumentDao> simulationInstruments = new HashMap<>();
    final Collection<List<InstrumentDao>> instrumentVersions = instrumentDatabaseConnector
      .findInstruments(instrumentIds)
      .stream()
      .collect(Collectors.groupingBy(InstrumentDao::getInstrumentId))
      .values();
    for (List<InstrumentDao> c : instrumentVersions) {
      var instrumentDao = c.stream().max((s1, s2) -> Objects
        .compare(s1.getLoadDate(),
          s2.getLoadDate(), Comparator
            .nullsLast(Comparator.naturalOrder()))).orElseThrow();

      instrumentDao.setLoadDate(onDate);
      simulationInstruments.put(instrumentDao.getInstrumentId(), instrumentDao);
    }
    bridgedDataSourceRepository.store(Arrays.asList(simulationInstruments.values().toArray()));
    return simulationInstruments;
  }

  @Override
  public void load(String placeholder) {
//    function not needed as it is handled by the controller but due to interface inheritance is
//    required to be in this class as well
  }

  /**
   * This operation does not currently implement any behavior.
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be
   * ignored.
   */
  @Override
  public void start(String placeholder) {
    //this method is intentionally a no-op.
  }

  /**
   * This operation does not currently implement any behavior.
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be
   * ignored.
   */
  @Override
  public void stop(String placeholder) {
    //this method is intentionally a no-op.
  }

  /**
   * Deletes the station records from the simulation data set.
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be
   * ignored.
   */
  @Override
  public void cleanup(String placeholder) {
    bridgedDataSourceRepository.cleanupData();
  }

  /**
   * Stores all sites that are new versions of existing sites in db, updates relevant sites in db with new endtime.
   *
   * @param sites - collections of site cois
   */
  @Override
  public void storeNewSiteVersions(Collection<Site> sites) {

    Map<String, Instant> staRefStaToOnDateMap = new HashMap<>();

    List<SiteDao> siteDaosToStore = sites.stream()
      .map(site -> {

        var onDateInstant = staRefStaToOnDateMap.put(mapSiteToString(site), site.getOnDate());

        if (onDateInstant != null) {
          logger.warn("Site with sta {}, refsta {} and onDate {} will not be stored " +
              "as a site with the same station and channel code already exists in the provided list.",
            site.getStationCode(), site.getReferenceStation(), site.getOnDate());
          return Optional.<SiteDao>empty();
        }

        var siteKey = new SiteKey(site.getStationCode(), site.getOnDate());
        var siteDao = new SiteDao();
        siteDao.setId(siteKey);
        siteDao.setOffDate(site.getOffDate());
        siteDao.setLatitude(site.getLatitude());
        siteDao.setLongitude(site.getLongitude());
        siteDao.setElevation(site.getElevation());
        siteDao.setStationName(site.getStationName());
        siteDao.setStaType(site.getStationType());
        siteDao.setReferenceStation(site.getReferenceStation());
        siteDao.setDegreesNorth(site.getDegreesNorth());
        siteDao.setDegreesEast(site.getDegreesEast());
        siteDao.setLoadDate(Instant.now());

        return Optional.of(siteDao);

      }).filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());

    bridgedDataSourceRepository.updateAndStoreSites(siteDaosToStore);

  }

  /**
   * Stores all sitechans that are new versions of existing sitechans in db, updates relevant sitechans in db with new endtime.
   *
   * @param channels - collections of sitechan cois
   */
  @Override
  public void storeNewChannelVersions(Collection<SiteChan> channels) {

    Map<String, Instant> stationChannelCodeToOnDateMap = new HashMap<>();

    List<SiteChanDao> siteChansVersionPairsToStore = channels.stream()
      .map(siteChan -> {

        Instant ondate = stationChannelCodeToOnDateMap.put(
          mapStationChannelCodesToString(siteChan.getStationCode(), siteChan.getChannelCode()), siteChan.getOnDate());
        if (ondate != null) {
          logger.warn("SiteChan with sta {}, chan {} and onDate {} will not be stored " +
              "as a SiteChan with the same station and channel code already exists in the provided list.",
            siteChan.getStationCode(), siteChan.getChannelCode(), siteChan.getOnDate());
          return Optional.<SiteChanDao>empty();
        }


        var siteChanKey = new SiteChanKey(siteChan.getStationCode(), siteChan.getChannelCode(), siteChan.getOnDate());
        var siteChanDao = new SiteChanDao();
        siteChanDao.setId(siteChanKey);
        //assumption is that only this class will be writing SiteChans to db
        siteChanDao.setChannelId(++maxChanId);
        siteChanDao.setOffDate(siteChan.getOffDate());
        siteChanDao.setLoadDate(Instant.now());
        siteChanDao.setChannelDescription(siteChan.getChannelDescription());
        siteChanDao.setChannelType(siteChan.getChannelType());
        siteChanDao.setEmplacementDepth(siteChan.getEmplacementDepth());
        siteChanDao.setVerticalAngle(siteChan.getVerticalAngle());
        siteChanDao.setHorizontalAngle(siteChan.getHorizontalAngle());

        return Optional.of(siteChanDao);
      }).filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());


    bridgedDataSourceRepository.updateAndStoreSiteChans(siteChansVersionPairsToStore);
  }

  private String mapStationChannelCodesToString(String stationCode, String channelCode) {
    return stationCode + channelCode;
  }

  private String mapSiteToString(Site site) {
    return site.getStationCode() + site.getReferenceStation();
  }
}
