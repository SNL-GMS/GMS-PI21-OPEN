package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.EventIdOriginIdKey;
import gms.shared.event.dao.EventIdOriginIdKey.Builder;
import gms.shared.event.dao.LatLonDepthTimeKey;
import gms.shared.event.dao.MagnitudeIdAmplitudeIdStationNameKey;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.dao.OriginIdArrivalIdKey;
import gms.shared.event.dao.StaMagDao;
import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums.AnalysisIdTag;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The OriginDataSimulator is responsible for loading Origin, Event, EventControl, Assoc, ArInfo, NetMag, StaMag
 * records into the simulation database and removing these records from the simulation database.
 */
public class OriginDataSimulator implements AnalysisDataSimulator {

  private final Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap;
  private final Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap;
  private final Map<String, EventDatabaseConnector> eventDatabaseConnectorMap;
  private final Map<String, BridgedDataSourceRepository> bridgedDataSourceRepositoryMap;
  private final AnalysisDataIdMapper analysisDataIdMapper;

  private OriginDataSimulator(
    Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap,
    Map<String, EventDatabaseConnector> eventDatabaseConnectorMap,
    Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap,
    Map<String, BridgedDataSourceRepository> bridgedDataSourceRepositoryMap,
    AnalysisDataIdMapper analysisDataIdMapper
  ) {
    this.originSimulatorDatabaseConnectorMap = originSimulatorDatabaseConnectorMap;
    this.bridgedDataSourceRepositoryMap = bridgedDataSourceRepositoryMap;
    this.analysisDataIdMapper = analysisDataIdMapper;
    this.eventDatabaseConnectorMap = eventDatabaseConnectorMap;
    this.originErrDatabaseConnectorMap = originErrDatabaseConnectorMap;
  }

  public static OriginDataSimulator create(
    Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap,
    Map<String, EventDatabaseConnector> eventDatabaseConnectorMap,
    Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap,
    Map<String, BridgedDataSourceRepository> bridgedDataSourceRepositoryMap,
    AnalysisDataIdMapper analysisDataIdMapper
  ) {
    return new OriginDataSimulator(
      originSimulatorDatabaseConnectorMap,
      eventDatabaseConnectorMap,
      originErrDatabaseConnectorMap,
      bridgedDataSourceRepositoryMap,
      analysisDataIdMapper
    );
  }

  /**
   * Load ORIGIN records, as well as there associated records, into the simulation database.
   *
   * @param stageId - stageId string for querying particular stage
   * @param seedDataStartTime - start time of seed data to retrieve from bridged database
   * @param seedDataEndTime - end time of seed data to retrieve from bridged database
   * @param copiedDataTimeShift - amount of time to shift data by
   */
  @Override
  public void loadData(String stageId, Instant seedDataStartTime, Instant seedDataEndTime,
    Duration copiedDataTimeShift) {

    // initialize stage and db connectors
    var originSimulatorDatabaseConnector = originSimulatorDatabaseConnectorMap.get(stageId);
    var eventDatabaseConnector = eventDatabaseConnectorMap.get(stageId);
    var originErrDatabaseConnector = originErrDatabaseConnectorMap.get(stageId);

    // find origin seed data using current stage simulator db connector
    var seedData = originSimulatorDatabaseConnector.findOriginDaosByPreciseTime(
      seedDataStartTime, seedDataEndTime
    );

    //
    // Get a flux from the seed data, then use cache() to allow it to be subscribed to multiple
    // times.
    //
    var originDaoFlux = getOriginDaoFlux(seedData, copiedDataTimeShift).cache();

    // Get, shift, and store OriginDao
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getNewOriginDaoTransformer(analysisDataIdMapper)
      ).collectList().flux()
    );

    // Get, shift, and store EventDao
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getOriginDaoEventDaoTransformer(
          analysisDataIdMapper, eventDatabaseConnector
        ))
    );

    // Get, shift, and store OrigerrDao
    subscribeAndStore(stageId,
      originDaoFlux
        .map(OriginDao::getOriginId)
        .collectList()
        .filter(l -> !l.isEmpty())
        .map(originErrDatabaseConnector::findByIds)
        .map(origerr -> origerr.stream()
          .map(dao -> {
            OrigerrDao.Builder newDao = OrigerrDao.Builder
              .initializeFromInstance(dao);
            newDao.withOriginId(
              analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ORID, dao.getOriginId(), -1));
            return newDao.build();
          })
          .collect(Collectors.toList()))
        .flux()
    );

    // Get, shift, and store EventControlDaos
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getOriginDaoEventControlDaoTransformer(
          analysisDataIdMapper, originSimulatorDatabaseConnector
        )
      ).filter(Optional::isPresent)
        .map(Optional::get)
        .map(List::of)
    );

    // Get, shift, and store ASSOC
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getOriginDaoAssocDaoTransformer(
          analysisDataIdMapper, originSimulatorDatabaseConnector
        ))
    );

    // Get, shift, and store ArInfo
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getOriginDaoArInfoDaoTransformer(
          analysisDataIdMapper, originSimulatorDatabaseConnector
        ))
    );

    // Get, shift, and store NetMag
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getOriginDaoNetMagDaoTransformer(
          analysisDataIdMapper, originSimulatorDatabaseConnector
        ))
    );


    // Get, shift, and store StaMag
    subscribeAndStore(stageId,
      transformOriginDaoFlux(
        originDaoFlux,
        getOriginDaoStaMagDaoTransformer(
          analysisDataIdMapper, originSimulatorDatabaseConnector
        ))
    );
  }

  /**
   * Cleanup ORIGIN (and related) records
   */
  @Override
  public void cleanup() {
    bridgedDataSourceRepositoryMap.values().forEach(BridgedDataSourceRepository::cleanupData);
  }

  /**
   * Create a flux of OriginDao that can be subsribed to (ie, to store or test) which shifts the
   * time (and jdate) fields by the given duration, and adjust IDs with analysisDataIdMapper
   *
   * @param originDaoList Original list of OriginDao
   * @param copiedDataTimeShift Amount to shift time field
   * @return Flux of OriginDao
   */
  static Flux<OriginDao> getOriginDaoFlux(
    List<OriginDao> originDaoList,
    Duration copiedDataTimeShift
  ) {
    return Flux.fromIterable(originDaoList)
      .map(originDao -> {

        double timeShiftSeconds = copiedDataTimeShift.toMillis() / 1000.0;
        double timeAsFloat = originDao.getEpoch() + timeShiftSeconds;

        var julianDate = Long.parseLong(DateTimeFormatter.ISO_ORDINAL_DATE
          .withZone(ZoneId.of("UTC"))
          .format(Instant.ofEpochMilli((long) (timeAsFloat * 1000L)))
          .replace("-", "")
          .replace("Z", "")
        );

        return OriginDao.Builder.initializeFromInstance(originDao)
          .withLatLonDepthTimeKey(
            new LatLonDepthTimeKey.Builder()
              .withLatitude(originDao.getLatitude())
              .withLongitude(originDao.getLongitude())
              .withDepth(originDao.getDepth())
              .withTime(timeAsFloat)
              .build()
          )
          .withJulianDate(julianDate)
          .build();
      });
  }

  /**
   * Subscribe to the give Flux of Lists with a subscriber that stores the lists.
   *
   * @param fluxOfLists the Flux of Lists
   * @param <T> the type stored in the lists.
   */
  private <T> void subscribeAndStore(String stageId, Flux<List<T>> fluxOfLists) {
    fluxOfLists
      .filter(list -> !list.isEmpty())
      .subscribe(list -> bridgedDataSourceRepositoryMap.get(stageId).store(list));
  }

  //
  // Below are methods that transform an OriginDao into something else, using the OriginDao
  // and some connector that retrieves a "template" of the thing to transform to.
  //

  private static Function<OriginDao, OriginDao> getNewOriginDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper
  ) {
    return originDao -> {
      OriginDao.Builder newDao = OriginDao.Builder.initializeFromInstance(originDao);
      newDao
        .withOriginId(analysisDataIdMapper.getOrGenerate(
          AnalysisIdTag.ORID, originDao.getOriginId(), -1
        ))
        .withEventId(analysisDataIdMapper.getOrGenerate(
          AnalysisIdTag.EVID, originDao.getEventId(), -1
        ))
        .withBodyWaveMagId(analysisDataIdMapper.getOrGenerate(
          AnalysisIdTag.MBID, originDao.getBodyWaveMagId(), -1
        ))
        .withSurfaceWaveMagId(analysisDataIdMapper.getOrGenerate(
          AnalysisIdTag.MSID, originDao.getSurfaceWaveMagId(), -1
        ))
        .withLocalMagId(analysisDataIdMapper.getOrGenerate(
          AnalysisIdTag.MLID, originDao.getLocalMagId(), -1
        ));

      return newDao.build();
    };
  }

  private static Function<OriginDao, List<EventDao>> getOriginDaoEventDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper,
    EventDatabaseConnector eventDatabaseConnector
  ) {
    return originDao -> {
      Optional<EventDao> eventDaoSeed = eventDatabaseConnector.findEventById(
        originDao.getEventId());

      EventDao.Builder newDao = EventDao.Builder.initializeFromInstance(eventDaoSeed.get());
      newDao.withEventId(analysisDataIdMapper
        .getOrGenerate(AnalysisIdTag.EVID, originDao.getEventId(), -1));
      newDao.withPreferredOrigin(analysisDataIdMapper
        .getOrGenerate(AnalysisIdTag.ORID, originDao.getOriginId(), -1));

      return List.of(newDao.build());
    };
  }

  private static Function<OriginDao, Optional<EventControlDao>> getOriginDaoEventControlDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper,
    OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector
  ) {

    return originDao -> {
      Optional<EventControlDao> optionalEventControlDao = originSimulatorDatabaseConnector
        .retrieveEventControlDaoByEventIdAndOriginId(new Builder()
          .withEventId(originDao.getEventId())
          .withOriginId(originDao.getOriginId())
          .build());

      return optionalEventControlDao.map(
        eventControlDao -> {
          var newDaoBuilder = EventControlDao.Builder.initializeFromInstance(
            optionalEventControlDao.get());

          newDaoBuilder.withEventIdOriginIdKey(new EventIdOriginIdKey.Builder()
            .withEventId(
              analysisDataIdMapper.getOrGenerate(AnalysisIdTag.EVID, originDao.getEventId(), -1))
            .withOriginId(
              analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ORID, originDao.getOriginId(), -1))
            .build());

          return newDaoBuilder.build();
        }
      );
    };
  }

  private static Function<OriginDao, List<AssocDao>> getOriginDaoAssocDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper,
    OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector
  ) {

    return originDao -> originSimulatorDatabaseConnector
      .retrieveAssocDaoListFromOriginId(originDao.getOriginId())
      .stream()
      .map(assocDao -> {
        AssocDao.Builder newDao = AssocDao.Builder.initializeFromInstance(assocDao);
        var oldArid = assocDao.getId().getArrivalId();
        var newArid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldArid, -1);

        newDao.withId(new AridOridKey.Builder()
          .withOriginId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.ORID, assocDao.getId().getOriginId(), -1))
          .withArrivalId(newArid)
          .build());

        return newDao.build();
      })
      .collect(Collectors.toList());
  }

  private static Function<OriginDao, List<ArInfoDao>> getOriginDaoArInfoDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper,
    OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector
  ) {

    return originDao -> originSimulatorDatabaseConnector
      .retrieveArInfoDaoListForOriginId(originDao.getOriginId())
      .stream()
      .map(arInfoDao -> {
        ArInfoDao.Builder newDao = ArInfoDao.Builder.initializeFromInstance(arInfoDao);
        var oldArid = arInfoDao.getArrivalId();
        var newArid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldArid, -1);

        newDao.withOriginIdArrivalIdKey(new OriginIdArrivalIdKey.Builder()
          .withOriginId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.ORID, arInfoDao.getOriginId(), -1))
          .withArrivalId(newArid)
          .build());

        return newDao.build();
      })
      .collect(Collectors.toList());
  }

  private static Function<OriginDao, List<NetMagDao>> getOriginDaoNetMagDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper,
    OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector
  ) {
    return originDao -> originSimulatorDatabaseConnector
      .retrieveNetMagDaoListForOriginId(originDao.getOriginId())
      .stream()
      .map(netMagDao -> {
        NetMagDao.Builder newDao = NetMagDao.Builder.initializeFromInstance(netMagDao);
        return newDao
          .withOriginId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.ORID, netMagDao.getOriginId(), -1))
          .withEventId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.EVID, netMagDao.getEventId(), -1))
          .withMagnitudeId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.MBID, netMagDao.getMagnitudeId(), -1))
          .build();
      })
      .collect(Collectors.toList());
  }

  private static Function<OriginDao, List<StaMagDao>> getOriginDaoStaMagDaoTransformer(
    AnalysisDataIdMapper analysisDataIdMapper,
    OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector
  ) {
    return originDao -> originSimulatorDatabaseConnector
      .retrieveStamagDaoListForOriginId(originDao.getOriginId())
      .stream()
      .map(staMagDao -> {

        var oldArid = staMagDao.getArrivalId();
        var newArid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldArid, -1);

        StaMagDao.Builder newDao = StaMagDao.Builder.initializeFromInstance(staMagDao);
        return newDao
          .withMagnitudeIdAmplitudeIdStationNameKey(
            new MagnitudeIdAmplitudeIdStationNameKey.Builder()
              .withAmplitudeId(analysisDataIdMapper
                .getOrGenerate(AnalysisIdTag.AMPID, staMagDao.getAmplitudeId(), -1))
              .withMagnitudeId(analysisDataIdMapper
                .getOrGenerate(AnalysisIdTag.MBID, staMagDao.getMagnitudeId(), -1))
              .withStationName(staMagDao.getStationName())
              .build())
          .withOriginId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.ORID, staMagDao.getOriginId(), -1))
          .withEventId(analysisDataIdMapper
            .getOrGenerate(AnalysisIdTag.EVID, staMagDao.getEventId(), -1))
          .withArrivalId(newArid)
          .build();
      })
      .collect(Collectors.toList());
  }

  ///// End of transformation methods

  private static <T> Flux<T> transformOriginDaoFlux(
    Flux<OriginDao> originDaoFlux,
    Function<OriginDao, T> transformer
  ) {
    return originDaoFlux.map(transformer);
  }
}
