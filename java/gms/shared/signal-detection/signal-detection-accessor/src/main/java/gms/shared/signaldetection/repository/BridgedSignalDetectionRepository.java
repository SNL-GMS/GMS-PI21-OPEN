package gms.shared.signaldetection.repository;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import gms.shared.signaldetection.api.SignalDetectionRepositoryInterface;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisConverterId;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.signaldetection.converter.detection.SignalDetectionConverterInterface;
import gms.shared.signaldetection.converter.detection.SignalDetectionHypothesisConverterInterface;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.signaldetection.database.connector.AssocDatabaseConnector;
import gms.shared.signaldetection.database.connector.SignalDetectionBridgeDatabaseConnectors;
import gms.shared.signaldetection.database.connector.config.SignalDetectionBridgeDefinition;
import gms.shared.signaldetection.repository.utils.SignalDetectionComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisArrivalIdComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisAssocIdComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.spring.utilities.aspect.Timing;
import gms.shared.stationdefinition.api.channel.ChannelRepositoryInterface;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.StationChannelTimeKey;
import gms.shared.stationdefinition.dao.css.WfTagDao;
import gms.shared.stationdefinition.dao.css.WfTagKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.AMPLITUDE_CONNECTOR_TYPE;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.ARRIVAL_CONNECTOR_TYPE;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.ASSOC_CONNECTOR_TYPE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Bridged signal detection repository for querying legacy objects from previous and current stages
 * in order to build signal detection objects
 */
@Component
@Qualifier("bridgedSignalDetectionRepository")
public class BridgedSignalDetectionRepository implements SignalDetectionRepositoryInterface {
  private static final Logger logger = LoggerFactory.getLogger(BridgedSignalDetectionRepository.class);

  private final SignalDetectionBridgeDatabaseConnectors signalDetectionBridgeDatabaseConnectors;
  private final SiteDatabaseConnector siteDatabaseConnector;
  private final WfdiscDatabaseConnector wfdiscDatabaseConnector;
  private final WftagDatabaseConnector wftagDatabaseConnector;
  private final SignalDetectionBridgeDefinition signalDetectionBridgeDefinition;
  private final ChannelRepositoryInterface bridgedChannelRepository;
  private final SignalDetectionConverterInterface signalDetectionConverter;
  private final SignalDetectionHypothesisConverterInterface signalDetectionHypothesisConverter;
  private final SignalDetectionIdUtility signalDetectionIdUtility;
  private final IgniteCache<ChannelSegmentDescriptor, Long> channelSegmentDescriptorWfidCache;

  private static final String STAGEID_DOES_NOT_EXIST = "Requested Stage ID {} not in definition. Returning empty signal detections.";

  @Autowired
  public BridgedSignalDetectionRepository(
    SignalDetectionBridgeDatabaseConnectors signalDetectionBridgeDatabaseConnectors,
    SiteDatabaseConnector siteDatabaseConnector,
    WfdiscDatabaseConnector wfdiscDatabaseConnector,
    WftagDatabaseConnector wftagDatabaseConnector,
    SignalDetectionBridgeDefinition signalDetectionBridgeDefinition,
    @Qualifier("bridgedChannelRepository") ChannelRepositoryInterface bridgedChannelRepository,
    SignalDetectionIdUtility signalDetectionIdUtility,
    SignalDetectionHypothesisConverterInterface signalDetectionHypothesisConverter,
    SignalDetectionConverterInterface signalDetectionConverter,
    IgniteCache<ChannelSegmentDescriptor, Long> channelSegmentDescriptorWfidCache) {

    // set the database connectors for signal detection
    this.signalDetectionBridgeDatabaseConnectors = signalDetectionBridgeDatabaseConnectors;
    this.siteDatabaseConnector = siteDatabaseConnector;
    this.wfdiscDatabaseConnector = wfdiscDatabaseConnector;
    this.wftagDatabaseConnector = wftagDatabaseConnector;

    // set the signal detection bridge definition, repository and id utility
    this.signalDetectionBridgeDefinition = signalDetectionBridgeDefinition;
    this.bridgedChannelRepository = bridgedChannelRepository;
    this.signalDetectionIdUtility = signalDetectionIdUtility;

    // create signal detection converter using feature measurement and hypothesis converters
    this.signalDetectionHypothesisConverter = signalDetectionHypothesisConverter;
    this.signalDetectionConverter = signalDetectionConverter;

    // create the channel segment and wfid ignite cache
    this.channelSegmentDescriptorWfidCache = channelSegmentDescriptorWfidCache;
  }

  @Override
  @Timing
  public List<SignalDetection> findByIds(List<UUID> ids, WorkflowDefinitionId stageId) {
    checkNotNull(ids, "List of ids cannot be null");
    checkNotNull(stageId, "Stage cannot be null");

    if (!signalDetectionBridgeDefinition.getOrderedStages().contains(stageId)) {
      logger.warn(STAGEID_DOES_NOT_EXIST, stageId.getName());
      return List.of();
    }

    var stageName = stageId.getName();
    Optional<WorkflowDefinitionId> previousStageOptional = getPreviousStage(stageId);

    // get the current connectors for arrival and assoc
    var arrivalDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ARRIVAL_CONNECTOR_TYPE);
    var assocDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ASSOC_CONNECTOR_TYPE);

    // create arids using from id utility and signal detection objects
    Collection<Long> arids = ids.stream()
      .map(signalDetectionIdUtility::getAridForSignalDetectionUUID)
      .collect(Collectors.toList());

    // create map of current stage arrivals using arids as keys
    Map<Long, ArrivalDao> currentStageArrivals = findCurrentStageArrivals(arrivalDatabaseConnector, arids);
    // get the key sets of current stage arrivals used to query assocs
    var currentArids = new ArrayList<>(currentStageArrivals.keySet());
    // create map of current stage arrivals using arids as keys
    SetMultimap<Long, AssocDao> currentStageAssocs = findCurrentStageAssocs(assocDatabaseConnector, currentArids);

    // check if the previous connectors exists for arrival and assoc
    var prevArrivalDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ARRIVAL_CONNECTOR_TYPE);
    var prevAssocDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ASSOC_CONNECTOR_TYPE);

    // if previous stage connector exists, create map of previous stage arrivals using arids as keys
    Map<Long, ArrivalDao> previousStageArrivals = prevArrivalDatabaseConnectorExists ?
      findPreviousStageArrivals(stageId, arids) : Map.of();
    var previousArids = new ArrayList<>(previousStageArrivals.keySet());

    // if previous arids exists and previous stage database connector exists query for previous assocs
    SetMultimap<Long, AssocDao> previousStageAssocs = (!previousArids.isEmpty() && prevAssocDatabaseConnectorExists) ?
      findPreviousStageAssocs(stageId, previousArids) : HashMultimap.create();

    var signalDetectionList =
      findByIdsHelper(currentStageArrivals, currentStageAssocs, previousStageArrivals, previousStageAssocs, stageId);

    previousArids.removeAll(currentArids);

    //get signal detections for any remaining arids
    if (!previousArids.isEmpty() && previousStageOptional.isPresent()) {

      signalDetectionList.addAll(
        findByIdsHelper(previousStageArrivals, previousStageAssocs, previousStageOptional.get()));
    }

    return signalDetectionList;
  }


  private List<SignalDetection> findByIdsHelper(Map<Long, ArrivalDao> currentStageArrivals,
    SetMultimap<Long, AssocDao> currentStageAssocs,
    WorkflowDefinitionId currentStage) {

    var stageName = currentStage.getName();
    var currentArids = new ArrayList<>(currentStageArrivals.keySet());

    // check if the previous connectors exists for arrival and assoc
    var prevArrivalDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ARRIVAL_CONNECTOR_TYPE);
    var prevAssocDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ASSOC_CONNECTOR_TYPE);

    // if previous stage connector exists, create map of previous stage arrivals using arids as keys
    Map<Long, ArrivalDao> previousStageArrivals = prevArrivalDatabaseConnectorExists ?
      findPreviousStageArrivals(currentStage, currentArids) : Map.of();
    var previousArids = new ArrayList<>(previousStageArrivals.keySet());

    // if previous arids exists and previous stage database connector exists query for previous assocs
    SetMultimap<Long, AssocDao> previousStageAssocs = (!previousArids.isEmpty() && prevAssocDatabaseConnectorExists) ?
      findPreviousStageAssocs(currentStage, previousArids) : HashMultimap.create();

    return findByIdsHelper(currentStageArrivals, currentStageAssocs, previousStageArrivals, previousStageAssocs, currentStage);
  }

  private List<SignalDetection> findByIdsHelper(Map<Long, ArrivalDao> currentStageArrivals,
    SetMultimap<Long, AssocDao> currentStageAssocs, Map<Long, ArrivalDao> previousStageArrivals,
    SetMultimap<Long, AssocDao> previousStageAssocs, WorkflowDefinitionId currentStage) {

    var currentArids = new ArrayList<>(currentStageArrivals.keySet());
    var stageName = currentStage.getName();
    Optional<WorkflowDefinitionId> previousStageOptional = getPreviousStage(currentStage);

    var amplitudeDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, AMPLITUDE_CONNECTOR_TYPE);
    // get amplitude from current set of arids
    SetMultimap<Long, AmplitudeDao> amplitudeDaos = findCurrentStageAmplitudes(amplitudeDatabaseConnector, currentArids);

    return currentStageArrivals.entrySet().stream()
      .map(entry -> {
        var station = StationDefinitionIdUtility
          .getStationEntityForSta(entry.getValue().getArrivalKey().getStationCode());

        Optional<ArrivalDao> previousArrival =
          previousStageArrivals.containsKey(entry.getKey()) ? Optional.of(previousStageArrivals.get(entry.getKey())) :
            Optional.empty();

        var components = SignalDetectionComponents.builder()
          .setCurrentStage(currentStage)
          .setPreviousStage(previousStageOptional)
          .setCurrentArrival(entry.getValue())
          .setPreviousArrival(previousArrival)
          .setCurrentAssocs(currentStageAssocs.get(entry.getKey()))
          .setPreviousAssocs(previousStageAssocs.get(entry.getKey()))
          .setAmplitudeDaos(amplitudeDaos.get(entry.getKey()))
          .setStation(station)
          .setMonitoringOrganization(signalDetectionBridgeDefinition.getMonitoringOrganization())
          .build();

        return signalDetectionConverter.convert(components);
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  @Override
  @Timing
  public List<SignalDetectionHypothesis> findHypothesesByIds(List<SignalDetectionHypothesisId> ids) {
    checkNotNull(ids, "List of ids cannot be null");

    // create list of sdh arrival id components
    List<SignalDetectionHypothesisArrivalIdComponents> arrivalIdComponents = ids.stream()
      .map(hypId ->
        signalDetectionIdUtility.getArrivalIdComponentsFromSignalDetectionHypothesisId(hypId.getId())
      )
      .collect(toList());

    // create list of sdh assoc components
    List<SignalDetectionHypothesisAssocIdComponents> sdhAssocIdComponents = ids.stream()
      .map(hypId ->
        signalDetectionIdUtility.getAssocIdComponentsFromSignalDetectionHypothesisId(hypId.getId())
      )
      .collect(Collectors.toList());

    // using the given list of arrival id components, query for signal detection hypotheses
    List<SignalDetectionHypothesis> hypotheses = arrivalIdComponents.stream()
      .filter(Objects::nonNull)
      .collect(
        groupingBy(SignalDetectionHypothesisArrivalIdComponents::getLegacyDatabaseAccountId,
          Collectors.mapping(SignalDetectionHypothesisArrivalIdComponents::getArid, Collectors.toList()))
      )
      .entrySet()
      .stream()
      .map(entry -> {
        Optional<WorkflowDefinitionId> stageId = findStageIdFromDatabaseAccountByStage(entry.getKey());
        return stageId.isPresent() ? buildHypothesesFromStageIdAndArids(stageId.get(), entry.getValue())
          : Collections.<SignalDetectionHypothesis>emptyList();
      })
      .flatMap(List::stream)
      .collect(Collectors.toList());

    if (!sdhAssocIdComponents.isEmpty()) {
      // get the stage ids, arids and orids from the hypothesis ids
      Map<String, List<Pair<Long, Long>>> aridOridMap = sdhAssocIdComponents.stream()
        .filter(Objects::nonNull)
        .collect(groupingBy(SignalDetectionHypothesisAssocIdComponents::getLegacyDatabaseAccountId,
          collectingAndThen(toList(), list ->
            // create list of pairs of arid/orid values
            list.stream()
              .map(idComp -> Pair.of(idComp.getArid(), idComp.getOrid()))
              .collect(toList())
          )));

      // using the arid/orid map build signal detection hypotheses accordingly
      hypotheses.addAll(aridOridMap.entrySet()
        .stream()
        .map(entry -> {
          Optional<WorkflowDefinitionId> stageId = findStageIdFromDatabaseAccountByStage(entry.getKey());
          List<Pair<Long, Long>> aridOridList = entry.getValue();

          // need to build hypotheses with arids/orids
          return stageId.isPresent() ? buildHypothesesFromStageIdAridsAndOrids(stageId.get(), aridOridList)
            : Collections.<SignalDetectionHypothesis>emptyList();
        })
        .flatMap(List::stream)
        .collect(toList()));
    }

    return hypotheses;
  }

  @Override
  @Timing
  public List<SignalDetection> findByStationsAndTime(List<Station> stations,
    Instant startTime, Instant endTime, WorkflowDefinitionId stageId, List<SignalDetection> excludedSignalDetections) {
    checkNotNull(stations, "Stations cannot be null");
    checkNotNull(startTime, "Start time cannot be null");
    checkNotNull(endTime, "End time cannot be null");
    checkNotNull(stageId, "Stage id cannot be null");
    checkNotNull(excludedSignalDetections, "Excluded detections cannot be null");

    if (!signalDetectionBridgeDefinition.getOrderedStages().contains(stageId)) {
      logger.warn(STAGEID_DOES_NOT_EXIST, stageId.getName());
      return List.of();
    }

    var stageName = stageId.getName();
    // get the current connectors for amplitude, arrival and assoc
    var amplitudeDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, AMPLITUDE_CONNECTOR_TYPE);
    var arrivalDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ARRIVAL_CONNECTOR_TYPE);
    var assocDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ASSOC_CONNECTOR_TYPE);

    // check if the previous connectors exists for arrival and assoc
    var prevArrivalDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ARRIVAL_CONNECTOR_TYPE);
    var prevAssocDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ASSOC_CONNECTOR_TYPE);

    // get lead/lag duration from the bridge definition
    var leadDuration = signalDetectionBridgeDefinition.getMeasuredWaveformLeadDuration();
    var lagDuration = signalDetectionBridgeDefinition.getMeasuredWaveformLagDuration();

    // create excluded arids using from id utility and signal detection objects
    Collection<Long> excludedArids = excludedSignalDetections.stream()
      .map(sd -> signalDetectionIdUtility.getAridForSignalDetectionUUID(sd.getId()))
      .collect(Collectors.toList());

    SetMultimap<String, String> channelGroupNames = findChannelGroupNamesFromStationsAndTimeRange(stations,
      startTime, endTime);

    return stations.stream()
      .map(station -> {
        Map<Long, ArrivalDao> currentStageArrivals =
          arrivalDatabaseConnector.findArrivals(
              new ArrayList<>(channelGroupNames.get(station.getName())),
              excludedArids,
              startTime,
              endTime,
              leadDuration,
              lagDuration).stream()
            .collect(Collectors.toMap(ArrivalDao::getId, Functions.identity()));

        var currentArids = new ArrayList<>(currentStageArrivals.keySet());

        // query for previous stage arrivals using current stage and arids
        Map<Long, ArrivalDao> previousStageArrivals = prevArrivalDatabaseConnectorExists ?
          findPreviousStageArrivals(stageId, currentArids) : Map.of();
        var previousArids = previousStageArrivals.keySet();

        // query current stage assocs using current arids
        SetMultimap<Long, AssocDao> currentStageAssocs = findCurrentStageAssocs(assocDatabaseConnector, currentArids);

        // if previous arids exists and previous stage database connector exists querry for previous assocs
        SetMultimap<Long, AssocDao> previousStageAssocs = (!previousArids.isEmpty() && prevAssocDatabaseConnectorExists) ?
          findPreviousStageAssocs(stageId, previousArids) : HashMultimap.create();

        SetMultimap<Long, AmplitudeDao> amplitudeDaos = findCurrentStageAmplitudes(amplitudeDatabaseConnector, currentArids);

        Optional<WorkflowDefinitionId> previousStageOptional = getPreviousStage(stageId);

        return currentStageArrivals.entrySet().stream()
          .map(entry -> {

            Optional<ArrivalDao> previousArrival = previousStageArrivals.containsKey(entry.getKey()) ?
              Optional.of(previousStageArrivals.get(entry.getKey())) : Optional.empty();

            var components = SignalDetectionComponents.builder()
              .setCurrentStage(stageId)
              .setPreviousStage(previousStageOptional)
              .setCurrentArrival(entry.getValue())
              .setPreviousArrival(previousArrival)
              .setCurrentAssocs(currentStageAssocs.get(entry.getKey()))
              .setPreviousAssocs(previousStageAssocs.get(entry.getKey()))
              .setAmplitudeDaos(amplitudeDaos.get(entry.getKey()))
              .setStation(station)
              .setMonitoringOrganization(signalDetectionBridgeDefinition.getMonitoringOrganization())
              .build();

            return signalDetectionConverter.convert(components);
          })
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
      })
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  private SetMultimap<String, String> findChannelGroupNamesFromStationsAndTimeRange(List<Station> stations,
    Instant start, Instant end) {

    List<String> stationNames = stations.stream().map(Station::getName)
      .collect(Collectors.toList());

    List<SiteDao> sites = siteDatabaseConnector.findSitesByReferenceStationAndTimeRange(stationNames, start, end);

    return sites.stream()
      .collect(HashMultimap::create, (mapper, site) -> mapper.put(site.getReferenceStation(),
        site.getId().getStationCode()), Multimap::putAll);
  }

  /**
   * Build hypotheses from stage id and arid from hypothesis id components
   *
   * @param stageId {@link WorkflowDefinitionId} for stage
   * @param arids long id representing arrival
   * @return list of {@link SignalDetectionHypothesis}
   */
  private List<SignalDetectionHypothesis> buildHypothesesFromStageIdAndArids(WorkflowDefinitionId stageId,
    List<Long> arids) {

    var stageName = stageId.getName();
    List<WorkflowDefinitionId> orderedStages = signalDetectionBridgeDefinition.getOrderedStages();
    int stageIndex = orderedStages.indexOf(stageId);
    Preconditions.checkState(stageIndex >= 0,
      "Requested stage does not exist: %s", stageId);

    // get the current connectors for amplitude, arrival and assoc
    var amplitudeDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, AMPLITUDE_CONNECTOR_TYPE);
    var arrivalDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ARRIVAL_CONNECTOR_TYPE);
    var assocDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ASSOC_CONNECTOR_TYPE);

    // check if the previous connectors exists for arrival and assoc
    var prevArrivalDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ARRIVAL_CONNECTOR_TYPE);

    // current and previous stage arrivals
    List<ArrivalDao> currentStageArrivals = arrivalDatabaseConnector.findArrivalsByArids(arids);

    // if prevous stage connector exists, create map of previous stage arrivals using arids as keys
    Map<Long, ArrivalDao> previousStageArrivals = prevArrivalDatabaseConnectorExists ?
      findPreviousStageArrivals(stageId, arids) : Map.of();

    //get map of arids to assocs in same stage
    //this map is only used to check if assocs exist for given arid,
    // so ignoring the extra assocs associationed with an arid is intended
    Map<Long, AssocDao> currentStageAssocs = assocDatabaseConnector.findAssocsByArids(arids).stream()
      .collect(Collectors.toMap(Functions.compose(AridOridKey::getArrivalId, AssocDao::getId),
        Function.identity(), (a, b) -> a));

    // current stage amplitude daos using arids
    SetMultimap<Long, AmplitudeDao> currentStageAmplitudes = findCurrentStageAmplitudes(amplitudeDatabaseConnector, arids);

    //mapping of arids to appropriate wfdiscDaos
    Map<Long, WfdiscDao> aridWfdiscDaoMap = getAridWfdiscDaoMap(currentStageArrivals, arids);

    // get the previous stage workflow id if it exists
    WorkflowDefinitionId previousStage = getPreviousStage(stageId).orElse(null);

    return signalDetectionHypothesesFromArrivals(aridWfdiscDaoMap, currentStageArrivals,
      currentStageAssocs, currentStageAmplitudes, previousStageArrivals, stageId, previousStage);
  }

  /**
   * Return the associated {@link WorkflowDefinitionId} for the given legacy database account id string
   *
   * @param legacyDatabaseAccountId String for legacy database account id
   * @return Optional of {@link WorkflowDefinitionId} stage id
   */
  private Optional<WorkflowDefinitionId> findStageIdFromDatabaseAccountByStage(String legacyDatabaseAccountId) {
    ImmutableMap<WorkflowDefinitionId, String> databaseAccountByStage = signalDetectionBridgeDefinition
      .getDatabaseAccountByStage();

    // first check that the account by stage map contains the given legacy db account id string
    if (databaseAccountByStage.containsValue(legacyDatabaseAccountId)) {
      for (Map.Entry<WorkflowDefinitionId, String> entry : databaseAccountByStage.entrySet()) {
        if (Objects.equals(entry.getValue(), legacyDatabaseAccountId)) {
          return Optional.of(entry.getKey());
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Find current stage amplitudes using collection of arids
   *
   * @param amplitudeDatabaseConnector {@link AmplitudeDatabaseConnector}
   * @param arids collection of arids
   */
  private SetMultimap<Long, AmplitudeDao> findCurrentStageAmplitudes(
    AmplitudeDatabaseConnector amplitudeDatabaseConnector, Collection<Long> arids) {
    return amplitudeDatabaseConnector.findAmplitudesByArids(arids).stream()
      .collect(HashMultimap::create, (m, i) -> m.put(i.getArrivalId(), i), Multimap::putAll);
  }

  /**
   * Find current stage arrivals using collection of arids
   *
   * @param arrivalDatabaseConnector {@link ArrivalDatabaseConnector}
   * @param arids collection of arids
   */
  private Map<Long, ArrivalDao> findCurrentStageArrivals(ArrivalDatabaseConnector arrivalDatabaseConnector,
    Collection<Long> arids) {
    // create map of current stage arrivals using arids as keys
    return arrivalDatabaseConnector.findArrivalsByArids(arids).stream()
      .collect(Collectors.toMap(ArrivalDao::getId, Functions.identity()));
  }

  /**
   * Find current stage assocs using collection of arids
   *
   * @param assocDatabaseConnector {@link AssocDatabaseConnector}
   * @param arids collection of arids
   * @return multimap of long and {@link AssocDao}
   */
  private SetMultimap<Long, AssocDao> findCurrentStageAssocs(AssocDatabaseConnector assocDatabaseConnector,
    Collection<Long> arids) {
    // create map of current stage arrivals using arids as keys
    return assocDatabaseConnector.findAssocsByArids(arids).stream()
      .collect(HashMultimap::create, (m, i) -> m.put(i.getId().getArrivalId(), i), Multimap::putAll);
  }

  /**
   * Find previous stage {@link ArrivalDao}s using list of arids and {@link WorkflowDefinitionId}
   *
   * @param stageId {@link WorkflowDefinitionId} stage id
   * @param arids list of arids to query
   * @return Map of long to {@link ArrivalDao}
   */
  private Map<Long, ArrivalDao> findPreviousStageArrivals(WorkflowDefinitionId stageId, Collection<Long> arids) {

    return signalDetectionBridgeDatabaseConnectors.getConnectorForPreviousStageOrThrow(stageId.getName(),
        ARRIVAL_CONNECTOR_TYPE).findArrivalsByArids(arids).stream()
      .collect(Collectors.toMap(ArrivalDao::getId, Functions.identity()));
  }

  /**
   * Find previous stage {@link AssocDao}s using list of arids and {@link WorkflowDefinitionId}
   *
   * @param stageId {@link WorkflowDefinitionId} stage id
   * @param arids list of arids to query
   * @return Map of long to {@link AssocDao}
   */
  private SetMultimap<Long, AssocDao> findPreviousStageAssocs(WorkflowDefinitionId stageId, Collection<Long> arids) {
    return signalDetectionBridgeDatabaseConnectors.getConnectorForPreviousStageOrThrow(stageId.getName(),
        ASSOC_CONNECTOR_TYPE).findAssocsByArids(arids).stream()
      .collect(HashMultimap::create, (m, i) -> m.put(i.getId().getArrivalId(), i), Multimap::putAll);
  }

  /**
   * Create arid to {@link WfdiscDao} hash map using current stage arrivals and
   * corresponding list of arids
   *
   * @param currentStageArrivals list of current stage {@link ArrivalDao}s
   * @param arids list of ArrivalDao arids
   * @return map of arid to {@link WfdiscDao}
   */
  Map<Long, WfdiscDao> getAridWfdiscDaoMap(List<ArrivalDao> currentStageArrivals, List<Long> arids) {

    // check if wftag record for an arrival exists and create hypotheses accordingly
    List<WfTagDao> wfTagDaos = wftagDatabaseConnector.findWftagsByTagIds(arids);
    Map<Long, Long> aridWfidMap = wfTagDaos.stream()
      .collect(Collectors.toMap(Functions.compose(WfTagKey::getId, WfTagDao::getWfTagKey),
        Functions.compose(WfTagKey::getWfId, WfTagDao::getWfTagKey)));

    // create the wftag arrival daos and remaining arrival daos by matching tagids and arids
    List<ArrivalDao> wftagArrivalDaos = currentStageArrivals.stream()
      .filter(dao -> aridWfidMap.containsKey(dao.getId()))
      .collect(Collectors.toList());
    List<ArrivalDao> remainingArrivalDaos = currentStageArrivals.stream()
      .filter(dao -> !aridWfidMap.containsKey(dao.getId()))
      .collect(Collectors.toList());

    HashMap<Long, WfdiscDao> aridWfdiscDaoMap = new HashMap<>();

    if (!wftagArrivalDaos.isEmpty()) {

      Map<Long, WfdiscDao> wfdiscDaoMap = wfdiscDatabaseConnector
        .findWfdiscsByWfids(new ArrayList<>(aridWfidMap.values())).stream()
        .collect(Collectors.toMap(WfdiscDao::getId, Functions.identity()));

      for (Map.Entry<Long, Long> aridWfid : aridWfidMap.entrySet()) {
        if (wfdiscDaoMap.containsKey(aridWfid.getValue())) {
          aridWfdiscDaoMap.put(aridWfid.getKey(), wfdiscDaoMap.get(aridWfid.getValue()));
        }
      }
    }

    if (!remainingArrivalDaos.isEmpty()) {

      // wftags are empty use arrival records using sta, chan and time attr
      var siteChanKeys = remainingArrivalDaos.stream()
        .map(arrivalDao -> {
          StationChannelTimeKey key = arrivalDao.getArrivalKey();
          return new SiteChanKey(key.getStationCode(), key.getChannelCode(), key.getTime());
        })
        .collect(Collectors.toList());

      // create map of arids to station/channel keys
      Map<Long, String> staChansByArid = remainingArrivalDaos.stream()
        .map(arrivalDao -> {
          StationChannelTimeKey key = arrivalDao.getArrivalKey();
          String staChanKey = key.getStationCode() + key.getChannelCode();
          return new AbstractMap.SimpleImmutableEntry<>(arrivalDao.getId(), staChanKey);
        })
        .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey,
          AbstractMap.SimpleImmutableEntry::getValue));

      // query for wfdiscs using the sitechan keys and create map of station/channel keys to wfdisc dao
      Map<String, WfdiscDao> wfdiscsByStaChan = wfdiscDatabaseConnector
        .findWfDiscVersionAfterEffectiveTime(siteChanKeys).stream()
        .map(wfdiscDao -> {
          String staChanKey = wfdiscDao.getStationCode() + wfdiscDao.getChannelCode();
          return new AbstractMap.SimpleImmutableEntry<>(staChanKey, wfdiscDao);
        })
        .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey,
          AbstractMap.SimpleImmutableEntry::getValue));

      for (Map.Entry<Long, String> aridStaChan : staChansByArid.entrySet()) {

        if (wfdiscsByStaChan.containsKey(aridStaChan.getValue())) {
          aridWfdiscDaoMap.put(aridStaChan.getKey(), wfdiscsByStaChan.get(aridStaChan.getValue()));
        }
      }
    }
    return aridWfdiscDaoMap;
  }

  /**
   * Method for getting the previous {@link WorkflowDefinitionId} given the current stage id
   *
   * @param stageId current stage {@link WorkflowDefinitionId}
   * @return optional of previous {@link WorkflowDefinitionId}
   */
  private Optional<WorkflowDefinitionId> getPreviousStage(WorkflowDefinitionId stageId) {
    var currStageIndex = signalDetectionBridgeDefinition.getOrderedStages().indexOf(stageId);
    if (currStageIndex <= 0) {
      return Optional.empty();
    }
    return Optional.of(signalDetectionBridgeDefinition.getOrderedStages().get(currStageIndex - 1));
  }

  /**
   * Build hypotheses from stage id and arid from hypothesis id components
   *
   * @param stageId {@link WorkflowDefinitionId} for stage
   * @param aridOridList list of pairs of airds and orids
   * @return list of {@link SignalDetectionHypothesis}
   */
  private List<SignalDetectionHypothesis> buildHypothesesFromStageIdAridsAndOrids(WorkflowDefinitionId stageId,
    List<Pair<Long, Long>> aridOridList) {

    if (!signalDetectionBridgeDefinition.getOrderedStages().contains(stageId)) {
      logger.warn(STAGEID_DOES_NOT_EXIST, stageId.getName());
      return List.of();
    }

    var stageName = stageId.getName();
    // get the current connectors for amplitude, arrival and assoc
    var amplitudeDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, AMPLITUDE_CONNECTOR_TYPE);
    var arrivalDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ARRIVAL_CONNECTOR_TYPE);
    var assocDatabaseConnector = signalDetectionBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ASSOC_CONNECTOR_TYPE);

    // check if the previous connectors exists for arrival and assoc
    var prevArrivalDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ARRIVAL_CONNECTOR_TYPE);
    var prevAssocDatabaseConnectorExists = signalDetectionBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ASSOC_CONNECTOR_TYPE);

    List<Long> arids = aridOridList.stream().map(Pair::getLeft).collect(toList());

    // current and previous stage arrivals
    List<ArrivalDao> currentStageArrivals = arrivalDatabaseConnector.findArrivalsByArids(arids);

    // if prevous stage connector exists, create map of previous stage arrivals using arids as keys
    Map<Long, ArrivalDao> previousStageArrivals = prevArrivalDatabaseConnectorExists ?
      findPreviousStageArrivals(stageId, arids) : Map.of();

    // create list and maps of current and previous stage assocs using arids and orids as keys
    List<AssocDao> currentStageAssocs = assocDatabaseConnector.findAssocsByAridsAndOrids(aridOridList);

    // if previous arids exists and previous stage database connector exists querry for previous assocs
    List<AssocDao> previousStageAssocs = prevAssocDatabaseConnectorExists ?
      signalDetectionBridgeDatabaseConnectors.getConnectorForPreviousStageOrThrow(stageName, ASSOC_CONNECTOR_TYPE)
        .findAssocsByAridsAndOrids(aridOridList) : List.of();

    // get amplitude from current set of arids
    SetMultimap<Long, AmplitudeDao> currentStageAmplitudes = findCurrentStageAmplitudes(amplitudeDatabaseConnector, arids);

    //mapping of arids to appropriate wfdiscDaos
    Map<Long, WfdiscDao> aridWfdiscDaoMap = getAridWfdiscDaoMap(currentStageArrivals, arids);

    // get the previous stage workflow id if it exists
    WorkflowDefinitionId previousStage = getPreviousStage(stageId).orElse(null);

    return signalDetectionHypothesesFromAssocs(aridWfdiscDaoMap, currentStageArrivals, currentStageAssocs,
      previousStageArrivals, previousStageAssocs, currentStageAmplitudes, Pair.of(stageId, previousStage));
  }

  /**
   * Create list of {@link SignalDetectionHypothesis} given the following data:
   *
   * @param aridWfdiscDaoMap a map of arids to @link WfdiscDao}s.
   * @param arrivalDaos list of {@link ArrivalDao}s from current stage
   * @param aridAssocMap a map of arids to {@link AssocDao}s from current stage
   * used for determining existence of assoc daos associated with arid
   * @param aridAmplitudeMap map of arids to {@link AmplitudeDao}s from current stage
   * @param aridPreviousStageArrivalsMap map of arids to previous stage {@link ArrivalDao}s
   * @param previousStage previous stage {@link WorkflowDefinitionId}
   * @param stageId current stage {@link WorkflowDefinitionId}
   * @return list of {@link SignalDetectionHypothesis}
   **/

  private List<SignalDetectionHypothesis> signalDetectionHypothesesFromArrivals(
    Map<Long, WfdiscDao> aridWfdiscDaoMap,
    List<ArrivalDao> arrivalDaos,
    Map<Long, AssocDao> aridAssocMap,
    Multimap<Long, AmplitudeDao> aridAmplitudeMap,
    Map<Long, ArrivalDao> aridPreviousStageArrivalsMap,
    WorkflowDefinitionId stageId,
    WorkflowDefinitionId previousStage) {

    return arrivalDaos.stream()
      .map(arrival -> {

        long arid = arrival.getId();

        /*determine whether to make SDH from arrival:
        SDH created from arrival if it's in the first stage, or it has no associated assocs in the same stage,
        or there was no arrival in previous stage with the same id, or the phase changed */
        if (previousStage == null || aridAssocMap.get(arid) == null || aridPreviousStageArrivalsMap.get(arid) == null ||
          !aridPreviousStageArrivalsMap.get(arid).getPhase().equals(arrival.getPhase())) {

          Optional<UUID> parentId = createParentId(arid, aridPreviousStageArrivalsMap, previousStage);

          if (aridWfdiscDaoMap.containsKey(arid)) {
            var wfdisc = aridWfdiscDaoMap.get(arid);
            Collection<AmplitudeDao> amplitudeDaos = aridAmplitudeMap.get(arid);
            // create the signal detection hypothesis from arrival and wfdisc
            //parent id will be empty for first stage, SDH of previous stage with same arid for subsequent stages
            return createSignalDetectionHypothesis(arrival, wfdisc, arid, stageId, Optional.empty(),
              amplitudeDaos, parentId);
          }
        }
        return Optional.<SignalDetectionHypothesis>empty();
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  /**
   * Create list of {@link SignalDetectionHypothesis} given the following data:
   *
   * @param aridWfdiscDaoMap a map of arids to @link WfdiscDao}s.
   * @param arrivalDaos list of {@link ArrivalDao}s from current stage
   * @param currentStageAssocs list of {@link ArrivalDao}s from previous stage
   * @param aridPreviousStageArrivalsMap map of arids to previous stage {@link ArrivalDao}s
   * @param previousStageAssocs list of {@link AssocDao}s from previous stage
   * @param aridAmplitudeMap map of arids to {@link AmplitudeDao}s from current stage
   * @param currentPreviousStagePair pair of current and previous stage {@link WorkflowDefinitionId}
   * @return list of {@link SignalDetectionHypothesis}
   **/
  private List<SignalDetectionHypothesis> signalDetectionHypothesesFromAssocs(
    Map<Long, WfdiscDao> aridWfdiscDaoMap,
    List<ArrivalDao> arrivalDaos,
    List<AssocDao> currentStageAssocs,
    Map<Long, ArrivalDao> aridPreviousStageArrivalsMap,
    List<AssocDao> previousStageAssocs,
    Multimap<Long, AmplitudeDao> aridAmplitudeMap,
    Pair<WorkflowDefinitionId, WorkflowDefinitionId> currentPreviousStagePair) {

    var stageId = currentPreviousStagePair.getLeft();
    var previousStage = currentPreviousStagePair.getRight();

    // map of arids to arrival daos
    Map<Long, ArrivalDao> aridArrivalMap = arrivalDaos.stream()
      .collect(Collectors.toMap(ArrivalDao::getId, Functions.identity()));

    //map of aridoridkey to previous assocs
    Map<AridOridKey, AssocDao> keyAssocDaoMap = previousStageAssocs.stream()
      .collect(Collectors.toMap(AssocDao::getId, Functions.identity()));

    return currentStageAssocs.stream().map(assocDao -> {

        var arid = assocDao.getId().getArrivalId();

        //determine parent SDH
        Optional<UUID> parentIdAssoc =
          findAssocParent(arid, assocDao, aridArrivalMap, keyAssocDaoMap, aridPreviousStageArrivalsMap,
            stageId, previousStage);

        ArrivalDao potentialArrival = aridArrivalMap.get(arid);
        //get amplitude dao from current stage associated with arid
        Collection<AmplitudeDao> amplitudeDaos = aridAmplitudeMap.get(arid);

        if (aridWfdiscDaoMap.containsKey(arid)) {

          var wfdisc = aridWfdiscDaoMap.get(arid);
          return createSignalDetectionHypothesis(potentialArrival, wfdisc, arid, stageId, Optional.of(assocDao),
            amplitudeDaos, parentIdAssoc);
        }

        return Optional.<SignalDetectionHypothesis>empty();
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  Optional<UUID> findAssocParent(long arid,
    AssocDao assocDao,
    Map<Long, ArrivalDao> aridArrivalMap,
    Map<AridOridKey, AssocDao> previousStageAssocs,
    Map<Long, ArrivalDao> previousStageArrivals,
    WorkflowDefinitionId stageId,
    WorkflowDefinitionId previousStage) {

    // for first stage parent is SDH of arrival
    if (previousStage == null) {
      return createParentId(arid, stageId);
    }

    //if previous stage had an assoc with same id, that assoc is the parent
    if (previousStageAssocs.get(assocDao.getId()) != null) {
      return createParentId(assocDao, previousStage);
    }

    ArrivalDao currArrival = aridArrivalMap.get(arid);
    ArrivalDao previousArrival = previousStageArrivals.get(arid);

    //determine if SDH was created from current stage arrival
    if (currArrival != null && (previousArrival == null ||
      !currArrival.getPhase().equals(previousArrival.getPhase()))) {

      return createParentId(arid, stageId);
    }

    return createParentId(arid, previousStageArrivals, previousStage);
  }

  /**
   * Create signal detection hypothesis for the give {@link ArrivalDao}, {@link WfdiscDao},
   * {@link WorkflowDefinitionId} and the arrival parent UUID if it exists
   *
   * @param arrival {@link ArrivalDao}
   * @param wfdisc {@link WfdiscDao}
   * @param arid ArrivalDao id
   * @param stageId stage id of the query
   * @param assoc AssocDao associated with SDH if it exists
   * @param amplitude AmplitudeDao associated with SDH if it exists
   * @param parentId parent id of arrivals if it exists
   * @return optional of {@link SignalDetectionHypothesis}
   */
  private Optional<SignalDetectionHypothesis> createSignalDetectionHypothesis(ArrivalDao arrival,
    WfdiscDao wfdisc,
    long arid,
    WorkflowDefinitionId stageId,
    Optional<AssocDao> assoc,
    Collection<AmplitudeDao> amplitude,
    Optional<UUID> parentId) {

    // load channel from wfdisc using wfdisc id
    Channel channel = null;

    try {
      channel = bridgedChannelRepository.loadChannelFromWfdisc(List.of(wfdisc.getId()),
        Optional.of(TagName.ARID),
        Optional.of(arid),
        Optional.empty(),
        wfdisc.getTime(),
        wfdisc.getEndTime());
    } catch (Exception e) {
      logger.warn("Failed to retrieve channel for signal detection hypothesis," +
        " no signal detection hypothesis will be returned", e);
    }

    if (channel == null) {
      return Optional.empty();
    }

    // create channel segment description from channel
    var descriptor = ChannelSegmentDescriptor.from(channel.toBuilder()
        .setData(Optional.empty())
        .build(),
      wfdisc.getTime(),
      wfdisc.getEndTime(),
      wfdisc.getTime());

    // cache the wfdisc id using the channel segment descriptor
    channelSegmentDescriptorWfidCache.put(descriptor, wfdisc.getId());
    String legacyDatabaseAccountId = signalDetectionBridgeDefinition.getDatabaseAccountByStage().get(stageId);
    var converterId = SignalDetectionHypothesisConverterId.from(legacyDatabaseAccountId,
      signalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(arid),
      parentId);

    return signalDetectionHypothesisConverter.convert(converterId,
      arrival,
      assoc,
      amplitude,
      signalDetectionBridgeDefinition.getMonitoringOrganization(),
      StationDefinitionIdUtility.getStationEntityForSta(arrival.getArrivalKey().getStationCode()),
      channel,
      ChannelSegment.builder().setId(descriptor).build());
  }

  /**
   * Create the parent id for the given {@link ArrivalDao}s and stage id
   *
   * @param arid {@link ArrivalDao} id
   * @param previousStageArrivals map of arids to previous stage {@link ArrivalDao}
   * @param previousStage {@link WorkflowDefinitionId}
   * @return optional of UUID
   */
  private Optional<UUID> createParentId(long arid, Map<Long, ArrivalDao> previousStageArrivals,
    WorkflowDefinitionId previousStage) {

    if (previousStageArrivals.containsKey(arid)) {
      String legacyDatabaseAccountId = signalDetectionBridgeDefinition
        .getDatabaseAccountByStage().get(previousStage);
      return Optional.of(signalDetectionIdUtility
        .getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(arid, legacyDatabaseAccountId));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Create the parent id for the given {@link ArrivalDao}s and stage id
   *
   * @param arid {@link ArrivalDao} id
   * @param currentStage {@link WorkflowDefinitionId}
   * @return optional of UUID
   */
  private Optional<UUID> createParentId(long arid, WorkflowDefinitionId currentStage) {

    String legacyDatabaseAccountId = signalDetectionBridgeDefinition.getDatabaseAccountByStage().get(currentStage);
    return Optional.of(signalDetectionIdUtility
      .getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(arid, legacyDatabaseAccountId));
  }

  /**
   * Create the parent id for the given parent {@link AssocDao} and stage id
   *
   * @param parentAssocDao {@link AssocDao} the parent assoc dao
   * @param previousStage {@link WorkflowDefinitionId}
   * @return optional of UUID
   */
  private Optional<UUID> createParentId(AssocDao parentAssocDao, WorkflowDefinitionId previousStage) {
    String legacyDatabaseAccountId = signalDetectionBridgeDefinition.getDatabaseAccountByStage().get(previousStage);
    return Optional.of(signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(
      parentAssocDao.getId().getArrivalId(), parentAssocDao.getId().getOriginId(), legacyDatabaseAccountId));
  }
}