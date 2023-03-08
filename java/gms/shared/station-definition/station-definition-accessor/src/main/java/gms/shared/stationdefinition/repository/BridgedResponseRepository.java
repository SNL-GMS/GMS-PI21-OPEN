package gms.shared.stationdefinition.repository;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.api.channel.ResponseRepositoryInterface;
import gms.shared.stationdefinition.coi.channel.Calibration;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.converter.util.assemblers.AssemblerUtils;
import gms.shared.stationdefinition.converter.util.assemblers.ResponseAssembler;
import gms.shared.stationdefinition.converter.util.assemblers.StationDefinitionVersionUtility;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SensorKey;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("bridgedResponseRepository")
public class BridgedResponseRepository implements ResponseRepositoryInterface {

  private final WfdiscDatabaseConnector wfdiscDatabaseConnector;
  private final SensorDatabaseConnector sensorDatabaseConnector;
  private final InstrumentDatabaseConnector instrumentDatabaseConnector;
  private final StationDefinitionIdUtility stationDefinitionIdUtility;
  private final ResponseAssembler responseAssembler;

  public BridgedResponseRepository(WfdiscDatabaseConnector wfdiscDatabaseConnector,
    SensorDatabaseConnector sensorDatabaseConnector,
    InstrumentDatabaseConnector instrumentDatabaseConnector, StationDefinitionIdUtility stationDefinitionIdUtility,
    ResponseAssembler responseAssembler) {
    this.wfdiscDatabaseConnector = wfdiscDatabaseConnector;
    this.sensorDatabaseConnector = sensorDatabaseConnector;
    this.instrumentDatabaseConnector = instrumentDatabaseConnector;
    this.stationDefinitionIdUtility = stationDefinitionIdUtility;
    this.responseAssembler = responseAssembler;
  }

  @Override
  public List<Response> findResponsesById(Collection<UUID> responseIds, Instant effectiveTime) {
    Objects.requireNonNull(responseIds);
    Objects.requireNonNull(effectiveTime);

    List<SiteChanKey> siteChanKeys = responseIds.stream()
      .map(stationDefinitionIdUtility::getChannelForResponseId)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(StationDefinitionIdUtility::getCssKeyFromName)
      .collect(Collectors.toList());

    return findResponsesBySiteChanKeys(siteChanKeys, effectiveTime, true);
  }

  public List<Response> findResponsesBySiteChanKeys(List<SiteChanKey> siteChanKeys, Instant effectiveTime,
    boolean shouldFilter) {
    Objects.requireNonNull(siteChanKeys);
    Objects.requireNonNull(effectiveTime);

    StationDefinitionDataHolder data = getResponseRepositoryDataForTime(siteChanKeys, effectiveTime);
    List<Response> responses = responseAssembler.buildAllForTime(effectiveTime,
      data.getWfdiscVersions(), data.getSensorDaos(), data.getInstrumentDaos(), Optional.empty());

    //if the REST endpoint is called, we only want to return responses within the valid range.
    //However, this can be called from the ChannelRepository, whcih wants all responses to set on the channels, and the channels will be filtered by validRange
    if (shouldFilter) {
      return responses.stream()
        .filter(response -> !response.getEffectiveUntil().isPresent()
        || !effectiveTime.isAfter(response.getEffectiveUntil().get()))
        .collect(Collectors.toList());
    }
    return responses;
  }

  @Override
  public List<Response> findResponsesByIdAndTimeRange(Collection<UUID> responseIds, Instant startTime,
    Instant endTime) {
    Objects.requireNonNull(responseIds);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Preconditions.checkState(!endTime.isBefore(startTime), "End time must not be before start time");

    List<SiteChanKey> siteChanKeys = responseIds.stream()
      .map(stationDefinitionIdUtility::getChannelForResponseId)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(StationDefinitionIdUtility::getCssKeyFromName)
      .collect(Collectors.toList());

    return findResponsesBySiteChanKeysAndTimeRange(siteChanKeys, startTime, endTime);
  }

  public List<Response> findResponsesBySiteChanKeysAndTimeRange(List<SiteChanKey> siteChanKeys, Instant startTime,
    Instant endTime) {
    Objects.requireNonNull(siteChanKeys);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Preconditions.checkState(!endTime.isBefore(startTime), "End time must not be before start time");

    StationDefinitionDataHolder data = getResponseRepositoryDataForTimeRange(siteChanKeys, startTime, endTime);
    return responseAssembler.buildAllForTimeRange(startTime,
      endTime,
      data.getWfdiscVersions(),
      data.getSensorDaos(),
      data.getInstrumentDaos(),
      Optional.empty());

  }

  public List<Response> findResponsesGivenSensorAndWfdisc(StationDefinitionDataHolder stationDefinitionDataHolder,
    Instant startTime, Instant endTime) {

    Objects.requireNonNull(stationDefinitionDataHolder.getSensorDaos());
    Objects.requireNonNull(stationDefinitionDataHolder.getWfdiscVersions());

    var instrumentDaos = BridgedRepositoryUtils.getInstrumentData(stationDefinitionDataHolder, instrumentDatabaseConnector);

    return responseAssembler.buildAllForTimeRange(startTime,
      endTime,
      stationDefinitionDataHolder.getWfdiscVersions(),
      stationDefinitionDataHolder.getSensorDaos(),
      instrumentDaos,
      Optional.empty());
  }

  private StationDefinitionDataHolder getResponseRepositoryDataForTimeRange(List<SiteChanKey> siteChanKeys,
    Instant startTime, Instant endTime) {

    List<SensorDao> sensors = sensorDatabaseConnector.findSensorsByKeyAndTimeRange(siteChanKeys, startTime, endTime);

    Map<Integer, List<SensorDao>> sensorDaoByVersionMap = StationDefinitionVersionUtility.getVersionMapAsInt(
      sensors, SensorDao::getVersionAttributeHash, SensorDao::getVersionTimeHash);

    List<SensorDao> sensorVersions = sensorDaoByVersionMap.values().stream()
      .map(StationDefinitionVersionUtility::getSensorsWithVersionEndTime)
      .flatMap(List::stream)
      .collect(Collectors.toList());

    return getResponseRepositoryData(siteChanKeys, sensorVersions, startTime, endTime);
  }

  private StationDefinitionDataHolder getResponseRepositoryDataForTime(List<SiteChanKey> siteChanKeys,
    Instant effectiveTime) {

    List<SensorDao> sensors = StationDefinitionVersionUtility.getSensorsWithVersionEndTime(
      sensorDatabaseConnector.findSensorVersionsByNameAndTime(siteChanKeys, effectiveTime));

    return getResponseRepositoryData(siteChanKeys, sensors, effectiveTime, effectiveTime);
  }

  private StationDefinitionDataHolder getResponseRepositoryData(List<SiteChanKey> siteChanKeys, List<SensorDao> sensors,
    Instant startTime, Instant endTime) {
    List<Long> instrumentIds = sensors.stream()
      .map(SensorDao::getInstrument)
      .map(InstrumentDao::getInstrumentId)
      .collect(Collectors.toList());
    List<InstrumentDao> instruments = instrumentDatabaseConnector.findInstruments(instrumentIds);

    //we only care about wfdiscs if we have a response, whcih requires a sensor
    Instant wfdiscQueryMin = Stream.of(
      sensors.stream()
        .map(Functions.compose(SensorKey::getTime, SensorDao::getSensorKey))
        .min(Instant::compareTo).orElse(Instant.now()),
      startTime)
      .min(Instant::compareTo).orElseThrow();

    Instant wfdiscQueryMax = Stream.of(
      sensors.stream()
        .map(Functions.compose(SensorKey::getEndTime, SensorDao::getSensorKey))
        .max(Instant::compareTo).orElse(Instant.now()),
      endTime)
      .max(Instant::compareTo).orElseThrow();

    //the sensor may have large endTime, don't need to query past today
    wfdiscQueryMax = Stream.of(wfdiscQueryMax, Instant.now())
      .min(Instant::compareTo).orElse(Instant.now());

    List<WfdiscDao> wfdiscs = getWfdiscVersions(
      wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(siteChanKeys, wfdiscQueryMin, wfdiscQueryMax));

    return new StationDefinitionDataHolder(null, null, sensors, instruments, wfdiscs);
  }

  private List<WfdiscDao> getWfdiscVersions(List<WfdiscDao> wfdiscs) {
    Table<String, String, NavigableMap<Instant, WfdiscDao>> wfdiscVersionsByStaChan
      = AssemblerUtils.buildVersionTable(WfdiscDao::getStationCode,
        WfdiscDao::getChannelCode,
        WfdiscDao::getTime,
        wfdiscs);

    //each list contains the map of versions for a sta-chan
    List<Map<Integer, List<WfdiscDao>>> wfdiscDaoByVersionList = wfdiscVersionsByStaChan.cellSet().stream()
      .map(cell -> StationDefinitionVersionUtility.getVersionMapAsInt(
      cell.getValue().values(), WfdiscDao::getVersionAttributeHash, WfdiscDao::getVersionTimeHash))
      .collect(Collectors.toList());

    for (Map<Integer, List<WfdiscDao>> wfdiscDaoByVersionMap : wfdiscDaoByVersionList) {
      List<WfdiscDao> prevWfdiscs = null;
      for (List<WfdiscDao> wfdiscEntries : wfdiscDaoByVersionMap.values()) {
        if (prevWfdiscs == null) {
          prevWfdiscs = wfdiscEntries;
          continue;
        }
        prevWfdiscs.add(wfdiscEntries.get(0));
      }
    }
    return wfdiscDaoByVersionList.stream()
      .map(list -> list.values().stream()
      .map(StationDefinitionVersionUtility::getWfDiscsWithNextVersion)
      .flatMap(List::stream)
      .collect(Collectors.toList()))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  @Override
  public Response loadResponseFromWfdisc(long wfdiscRecord) {

    var wfdiscs = wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(wfdiscRecord));

    if (wfdiscs.isEmpty()) {
      throw new IllegalStateException("Unable to retrieve wfdiscs for ID "
        + wfdiscRecord + " from which to load Response");
    }

    Optional<WfdiscDao> firstWfdisc = wfdiscs.stream().findFirst();
    var wfdisc = firstWfdisc.orElseThrow(() -> new IllegalStateException("No wfdisc from which to load Response"));
    var startTime = wfdisc.getTime();
    var endTime = wfdisc.getEndTime();
    var siteChanKey = new SiteChanKey(wfdisc.getStationCode(), wfdisc.getChannelCode(), startTime);

    var sensors = sensorDatabaseConnector
      .findSensorsByKeyAndTimeRange(List.of(siteChanKey), startTime, endTime);
    var instrumentIds = sensors.stream()
      .map(SensorDao::getInstrument)
      .map(InstrumentDao::getInstrumentId)
      .collect(Collectors.toList());
    var instruments = instrumentDatabaseConnector.findInstruments(instrumentIds);

    var id = wfdisc.getStationCode() + wfdisc.getChannelCode();
    var responseId = UUID.nameUUIDFromBytes(id.getBytes());
    var channelName = stationDefinitionIdUtility.getChannelForResponseId(responseId);

    var responses = responseAssembler.buildAllForTimeRange(startTime,
      endTime,
      List.of(wfdisc),
      sensors,
      instruments,
      channelName);

    var response = responseAssembler.buildResponseEntity(wfdisc);

    var responseBuilder = Response.builder();
    var responseDataBuilder = Response.Data.builder();

    if (!responses.isEmpty()) {

      var updatedCalibration = Calibration.from(wfdisc.getCalper(),
        Duration.ZERO,
        DoubleValue.from(wfdisc.getCalib(),
          Optional.empty(), Units.UNITLESS));
      response = responses.get(0);
      responseDataBuilder
        .setFapResponse(response.getFapResponse())
        .setCalibration(updatedCalibration)
        .setEffectiveUntil(response.getEffectiveUntil());
      responseBuilder.setEffectiveAt(response.getEffectiveAt());
    }

    responseBuilder.setId(response.getId());

    var updatedResponse = responseBuilder
      .setData(
        responseDataBuilder
          .build())
      .build();

    stationDefinitionIdUtility.storeWfidResponseMapping(wfdiscRecord, updatedResponse);

    return updatedResponse;
  }
}
