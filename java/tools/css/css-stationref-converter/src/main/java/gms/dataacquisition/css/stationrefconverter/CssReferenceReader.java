package gms.dataacquisition.css.stationrefconverter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import gms.dataacquisition.cssreader.data.AffiliationRecord;
import gms.dataacquisition.cssreader.data.FapRecord;
import gms.dataacquisition.cssreader.data.InstrumentRecord;
import gms.dataacquisition.cssreader.data.NetworkRecord;
import gms.dataacquisition.cssreader.data.SensorRecord;
import gms.dataacquisition.cssreader.data.SiteChannelRecord;
import gms.dataacquisition.cssreader.data.SiteRecord;
import gms.dataacquisition.cssreader.flatfilereaders.FapFileReader;
import gms.dataacquisition.cssreader.utilities.ReaderUtility;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import gms.shared.frameworks.osd.coi.signaldetection.Calibration;
import gms.shared.frameworks.osd.coi.signaldetection.FrequencyAmplitudePhase;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.NetworkOrganization;
import gms.shared.frameworks.osd.coi.stationreference.NetworkRegion;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceCalibration;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSourceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.ResponseTypes;
import gms.shared.frameworks.osd.coi.stationreference.StationType;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Read flat files and convert their contents to Reference COI objects.
 */
public class CssReferenceReader {

  private static final Logger logger = LoggerFactory.getLogger(CssReferenceReader.class);

  private static final String EXTERNAL = "External";
  private static final String LOADCSS = "Loaded from CSS file";
  private static final String OFFDATE = "OffDate:";
  private static final String TRIPLESTRING = "%1$s %2$s, %3$s";
  private static final String WARN = "WARN";
  private static final String INFO = "INFO";
  private static final String ERR = "ERROR";
  private static final boolean IS_ERR = logger.isErrorEnabled();
  private static final boolean IS_WARN = logger.isWarnEnabled();
  private static final boolean IS_INFO = logger.isInfoEnabled();
  private static final boolean IS_DEBUG = logger.isDebugEnabled();

  private static final Map<Integer, InstrumentRecord> instrumentRecordsById = new HashMap<>();
  private static final SetMultimap<String, ReferenceStation> referenceStationsByName =
    HashMultimap.create();
  private static final Set<ReferenceStationMembership> referenceStationMemberships =
    new HashSet<>();
  private static final SetMultimap<String, ReferenceSite> referenceSitesByName =
    HashMultimap.create();
  private static final Set<ReferenceNetworkMembership> referenceNetworkMemberships =
    new HashSet<>();
  private static final List<AffiliationRecord> networkAffiliationRecords = new ArrayList<>();
  private static final Set<ReferenceSiteMembership> referenceSiteMemberships = new HashSet<>();
  private static final Set<ReferenceSensor> referenceSensors = new HashSet<>();
  private static final Set<ReferenceResponse> referenceResponses = new HashSet<>();
  private static final Multimap<String, ReferenceChannel> referenceChannelsByName =
    HashMultimap.create();
  private static final SetMultimap<String, ReferenceNetwork> referenceNetworksByName =
    HashMultimap.create();
  private static Set<StationGroup> stationGroups;
  private static Set<Response> responseSet;

  private CssReferenceReader() {

  }

  public static void process(String metadataDir, String networkFile) throws IOException {
    var metadata = new File(metadataDir);

    logger.info("metadata dir: {}", metadataDir);
    logger.info("network file: {}", networkFile);

    Preconditions.checkState(metadata.exists(),
      "Cannot process CSS reference data: data directory does not exist");
    Preconditions.checkState(metadata.isDirectory(),
      "Cannot process CSS reference data: data directory is not a directory");

    var network = new File(metadataDir, networkFile);
    Preconditions.checkState(network.exists(),
      "Cannot process CSS reference data: network file does not exist " + network
        .getAbsolutePath());
    Preconditions.checkState(!network.isDirectory(),
      "Cannot process CSS reference datat: network file is a directory");

    for (File stationDir : metadata.listFiles()) {
      if (stationDir.isDirectory()) {
        var instrumentFilePath =
          Path.of(stationDir.getAbsolutePath(), "instrument.dat").toString();
        instrumentRecordsById
          .putAll(ReaderUtility.readInstrumentRecordsIntoMapByInid(instrumentFilePath));

        // Process site file
        Collection<SiteRecord> siteRecords =
          ReaderUtility
            .readSiteRecords(Path.of(stationDir.getAbsolutePath(), "site.dat").toString());

        // Build reference stations and station by name app
        processSiteRecordsToMakeStations(siteRecords)
          .forEach(referenceStation -> referenceStationsByName.put(referenceStation.getName(),
            referenceStation));

        // Build reference sites and sites by name
        Pair<Collection<ReferenceSite>, Set<ReferenceStationMembership>> siteMembershipPair =
          processSiteRecordsToMakeSites(siteRecords, referenceStationsByName);
        siteMembershipPair.getKey()
          .forEach(site -> referenceSitesByName.put(site.getName(), site));

        referenceStationMemberships.addAll(siteMembershipPair.getValue());

        // add the affiliations - we need to wait until we have the networks to build the
        // NetworkMemberships
        networkAffiliationRecords.addAll(ReaderUtility
          .readAffiliations(Path.of(stationDir.getAbsolutePath(), "affiliation.dat").toString()));

        // Process sensor
        Multimap<Integer, SensorRecord> sensorRecordsByChannelId =
          ReaderUtility.readSensorRecordsIntoMultimapByChannelId(
            Path.of(stationDir.getAbsolutePath(), "sensor.dat").toString());
        Pair<Multimap<String, ReferenceChannel>, Set<ReferenceSiteMembership>> siteChanResults =
          processSiteChannelRecords(ReaderUtility.readSitechanRecords(
              Path.of(stationDir.getAbsolutePath(), "sitechan.dat").toString()),
            referenceSitesByName,
            sensorRecordsByChannelId,
            instrumentRecordsById);

        // Build site to reference channel and channels by name map - switch to table?
        referenceChannelsByName.putAll(siteChanResults.getKey());
        referenceSiteMemberships.addAll(siteChanResults.getValue());

        processSensorRecords2(sensorRecordsByChannelId.values(), stationDir)
          .forEach(pair -> {
            referenceSensors.add(pair.getKey());
            referenceResponses.add(pair.getValue());
          });
      }
    }

    // build networks
    processNetworkRecords(
      ReaderUtility.readNetworkRecords(new File(metadata, networkFile).getAbsolutePath()))
      .forEach(referenceNetwork -> referenceNetworksByName.put(referenceNetwork.getName(),
        referenceNetwork));

    // Create the network memberships
    referenceNetworkMemberships.addAll(processAffiliationRecords(networkAffiliationRecords,
      referenceNetworksByName, referenceStationsByName));

    var stationGroupBuilder = new StationGroupBuilder(referenceNetworkMemberships,
      referenceStationMemberships,
      referenceSiteMemberships,
      referenceNetworksByName.values(),
      referenceStationsByName.values(),
      referenceSitesByName.values(),
      referenceChannelsByName.values(),
      referenceResponses);

    Pair<Set<StationGroup>, Set<Response>> stationGroupsAndResponses =
      stationGroupBuilder.createStationGroupsAndResponses();
    stationGroups = stationGroupsAndResponses.getKey();
    responseSet = stationGroupsAndResponses.getValue();
  }

  public static Map<Integer, InstrumentRecord> getInstrumentRecordsById() {
    return instrumentRecordsById;
  }

  public static SetMultimap<String, ReferenceStation> getReferenceStationsByName() {
    return referenceStationsByName;
  }

  public static Set<ReferenceStationMembership> getReferenceStationMemberships() {
    return referenceStationMemberships;
  }

  public static SetMultimap<String, ReferenceSite> getReferenceSitesByName() {
    return referenceSitesByName;
  }

  public static Set<ReferenceNetworkMembership> getReferenceNetworkMemberships() {
    return referenceNetworkMemberships;
  }

  public static Set<ReferenceSiteMembership> getReferenceSiteMemberships() {
    return referenceSiteMemberships;
  }

  public static Set<ReferenceSensor> getReferenceSensors() {
    return referenceSensors;
  }

  public static Set<ReferenceResponse> getReferenceResponses() {
    return referenceResponses;
  }

  public static Multimap<String, ReferenceChannel> getReferenceChannelsByName() {
    return referenceChannelsByName;
  }

  public static SetMultimap<String, ReferenceNetwork> getReferenceNetworksByName() {
    return referenceNetworksByName;
  }

  public static Set<StationGroup> getStationGroups() {
    return stationGroups;
  }

  public static Set<Response> getResponseSet() {
    return responseSet;
  }

  /**
   * Process all affiliation records and map the stations to networks.  This table also may indicate
   * a relationship between stations and sites.
   */
  private static Set<ReferenceNetworkMembership> processAffiliationRecords(
    Collection<AffiliationRecord> records,
    Multimap<String, ReferenceNetwork> networksByName,
    Multimap<String, ReferenceStation> stationsByName) {

    final Set<ReferenceNetworkMembership> results = new HashSet<>();
    final List<AffiliationRecord> sortedRecords = records.stream().sorted(
        Comparator.comparing(AffiliationRecord::getNet)
          .thenComparing(AffiliationRecord::getSta)
          .thenComparing(AffiliationRecord::getEndtime,
            Comparator.comparing(optional -> optional.orElse(null),
              Comparator.nullsLast(Instant::compareTo))))
      .collect(toList());
    // Loop over all the records in the affiliation file.
    for (var i = 0; i < sortedRecords.size(); i++) {
      final var affiliationRecord = sortedRecords.get(i);
      final String netName = affiliationRecord.getNet();
      final String staName = affiliationRecord.getSta();
      final Optional<Instant> end = affiliationRecord.getEndtime();

      final Optional<ReferenceNetwork> network = getFirst(networksByName, affiliationRecord.getNet());
      final Optional<ReferenceStation> station = getFirst(stationsByName, staName);
      if (network.isEmpty() || station.isEmpty()) {
        logMissingNetworkOrStation(netName, staName, network.isPresent(), station.isPresent());
        continue;
      }

      final Instant actualDate = affiliationRecord.getTime().orElse(Instant.EPOCH);
      final String comment = "Relationship for network " + netName + " and station " + staName;
      final UUID networkId = network.get().getEntityId();
      final UUID stationId = station.get().getEntityId();

      final var membership = ReferenceNetworkMembership.create(
        comment, actualDate, actualDate, networkId, stationId, StatusType.ACTIVE);
      if (isNetworkMembershipUnique(results, membership)) {
        results.add(membership);
      }
      // add inactive membership if required - end will be present if inactiveMembershipRequired
      // returns true.
      if (inactiveMembershipRequired(affiliationRecord, i, sortedRecords) && end.isPresent()) {
        final Instant enddate = end.get();
        final var inactiveMembership = ReferenceNetworkMembership.create(
          comment, enddate, enddate, networkId, stationId, StatusType.INACTIVE);
        if (isNetworkMembershipUnique(results, inactiveMembership)) {
          results.add(inactiveMembership);
        }
      }
    }
    return Collections.unmodifiableSet(results);
  }

  private static Set<Pair<ReferenceSensor, ReferenceResponse>> processSensorRecords2(
    Collection<SensorRecord> sensorRecords,
    File stationDirectory) {

    return sensorRecords.stream()
      .map(sensorRecord -> processSensorRecord(sensorRecord, stationDirectory))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }

  private static Optional<Pair<ReferenceSensor, ReferenceResponse>> processSensorRecord(
    SensorRecord sensorRecord,
    File stationDirectory) {
    Instant offDate = sensorRecord.getEndTime();
    int inId = sensorRecord.getInid();

    String channelName = makeChannelName(sensorRecord.getSta(), sensorRecord.getChan());
    Optional<ReferenceChannel> possibleReferenceChannel = getFirst(referenceChannelsByName,
      channelName);

    if (!instrumentRecordsById.containsKey(inId) || possibleReferenceChannel.isEmpty()) {
      logMissingInstrumentRecordOrChannel2(sensorRecord, inId, channelName,
        possibleReferenceChannel.isPresent());
      return Optional.empty();
    }

    var referenceChannel = possibleReferenceChannel.get();

    var instrumentRecord = instrumentRecordsById.get(inId);
    var source = InformationSource.from(EXTERNAL,
      sensorRecord.getLddate(),
      String.format(TRIPLESTRING, OFFDATE, offDate, LOADCSS));

    String referenceChannelName = referenceChannel.getName();

    var referenceSensor = ReferenceSensor.builder()
      .setChannelName(referenceChannelName)
      .setInstrumentManufacturer(instrumentRecord.getInsname())
      .setInstrumentModel(instrumentRecord.getInstype())
      .setSerialNumber("SNxxxx")
      .setNumberOfComponents(1)
      .setCornerPeriod(1)
      .setLowPassband(0)
      .setHighPassband(0)
      .setActualTime(sensorRecord.getTime())
      .setSystemTime(sensorRecord.getTime())
      .setInformationSource(source)
      .setComment("Sensor is associated with channel " + referenceChannelName)
      .build();

    var calibration = Calibration.from(sensorRecord.getCalper(),
      Duration.ofSeconds(Math.round(sensorRecord.getTshift())),
      DoubleValue.from(instrumentRecord.getNcalib(),
        0,
        UnitsUtility.determineUnits(referenceChannel.getDataType())));

    var referenceResponseBuilder = ReferenceResponse.builder()
      .setChannelName(channelName)
      .setActualTime(sensorRecord.getTime())
      .setSystemTime(sensorRecord.getTime())
      .setComment(String.format("Response associated with channel %s", referenceChannelName))
      .setReferenceCalibration(ReferenceCalibration.from(Duration.ofDays(365), calibration));

    // load response data
    var responseDir = new File(stationDirectory, "responses");
    var responseFile = new File(responseDir, instrumentRecord.getDfile());

    try {
      byte[] responseData = Files.readAllBytes(responseFile.toPath());
      FrequencyAmplitudePhase fap = processResponseFiles(responseFile.getAbsolutePath(),
        referenceChannel.getDataType());

      var referenceSourceResponse = ReferenceSourceResponse.builder()
        .setSourceResponseData(responseData)
        .setSourceResponseUnits(Units.UNITLESS)
        .setSourceResponseTypes(
          ResponseTypes.valueOf(instrumentRecord.getRsptype().toUpperCase(Locale.ENGLISH)))
        .setInformationSources(List.of(source))
        .build();

      referenceResponseBuilder.setSourceResponse(referenceSourceResponse)
        .setFapResponse(fap);
    } catch (IOException ex) {
      logByLevel(WARN,
        String.format("Failed to read response file at path %s : %s", responseFile.getPath(),
          ex));
    }

    return Optional.of(Pair.of(referenceSensor, referenceResponseBuilder.build()));
  }

  /**
   * Process all network records and convert into COI objects.
   */
  private static Collection<ReferenceNetwork> processNetworkRecords(
    Collection<NetworkRecord> records) {
    final NetworkOrganization org = NetworkOrganization.CTBTO;
    final Collection<ReferenceNetwork> result = new ArrayList<>();
    for (NetworkRecord networkRecord : records) {
      final NetworkRegion region;
      switch (networkRecord.getType().toUpperCase(Locale.ENGLISH)) {
        case "WW":
          region = NetworkRegion.GLOBAL;
          break;
        case "AR":
          region = NetworkRegion.REGIONAL;
          break;
        case "LO":
          region = NetworkRegion.LOCAL;
          break;
        default:
          logByLevel(WARN, String.format(
            "processNetworkRecordsUnknown() - network region detected: %1$s", networkRecord.getType()));
          region = NetworkRegion.GLOBAL;
      }
      final var source = InformationSource.from(EXTERNAL,
        networkRecord.getLddate(), LOADCSS);
      ReferenceNetwork network = ReferenceNetwork.builder()
        .setName(networkRecord.getName())
        .setDescription(networkRecord.getDesc())
        .setOrganization(org)
        .setRegion(region)
        .setSource(source)
        .setComment("Loaded from CSS network file")
        .setActualChangeTime(Instant.EPOCH)
        .setSystemChangeTime(Instant.EPOCH)
        .setActive(true)
        .build();
      result.add(network);
    }
    return Collections.unmodifiableCollection(result);
  }

  private static void logMissingNetworkOrStation(String netName, String staName,
    boolean networkPresent, boolean stationPresent) {
    if (!networkPresent) {
      logByLevel(WARN,
        String.format("Could not find network %1$s referenced in affiliation file", netName));
    }
    if (!stationPresent) {
      logByLevel(WARN,
        String.format("Could not find station %1$s referenced in affiliation file", staName));
    }
  }

  /**
   * Process all site-channel records and convert into COI objects.
   */
  private static Pair<Multimap<String, ReferenceChannel>, Set<ReferenceSiteMembership>> processSiteChannelRecords(
    List<SiteChannelRecord> records,
    Multimap<String, ReferenceSite> sitesByName,
    Multimap<Integer, SensorRecord> sensorRecordsByChannelId,
    Map<Integer, InstrumentRecord> instrumentRecordsByInid) {
    final Multimap<String, ReferenceChannel> refChannelsByName = ArrayListMultimap.create();
    final Set<ReferenceSiteMembership> siteMemberships = new HashSet<>();
    final List<SiteChannelRecord> sortedRecords = sortByStaChanOndate(records);

    // Loop over all the SiteChannelRecords read from the flat file.
    for (var i = 0; i < sortedRecords.size(); i++) {
      final var siteChannelRecord = sortedRecords.get(i);
      // Get the station or site name.  Could be either!
      final String entityName = siteChannelRecord.getSta();
      final Instant onDate = siteChannelRecord.getOndate().orElse(Instant.now());

      final Optional<ReferenceSite> associatedSite = findSiteActiveAt(sitesByName.get(entityName)
        , onDate);

      int finalI = i;
      associatedSite.ifPresentOrElse(site -> {
        List<ReferenceChannel> parsedReferenceChannels = buildReferenceChannel(
          sensorRecordsByChannelId,
          instrumentRecordsByInid, siteChannelRecord, site);

        if (parsedReferenceChannels.isEmpty()) {
          logByLevel(WARN, String.format(
            "processSiteChannelRecords() - reference channels could not be created for record " +
              "with channel: %s",
            siteChannelRecord.getChan()));
        } else {
          for (ReferenceChannel refChannel : parsedReferenceChannels) {
            if (!isChannelDuplicate(refChannelsByName.values(), refChannel)) {
              refChannelsByName.put(refChannel.getName(), refChannel);
            } else {
              logByLevel(WARN, String.format(
                "processSiteChannelRecords() - found duplicate channel: %1$s", refChannel));
            }
            addSiteMemberships(sortedRecords, finalI, siteMemberships, site, refChannel);
          }
        }
      }, () ->
        logByLevel(WARN,
          String.format("Could not find site associated to channel record: %1$s", siteChannelRecord)));
    }
    return Pair.of(refChannelsByName, siteMemberships);
  }

  private static void addSiteMemberships(
    List<SiteChannelRecord> sitechanRecords, int index,
    Collection<ReferenceSiteMembership> memberships,
    ReferenceSite refSite, ReferenceChannel refChan) {
    final SiteChannelRecord sitechan = sitechanRecords.get(index);
    final Instant ondate = sitechan.getOndate().orElse(Instant.now());
    // create site membership, associating the site and channel.
    final var activeMember = ReferenceSiteMembership.create(
      "Channel " + refChan.getName() + " is associated with site " + sitechan.getSta(),
      ondate, ondate, refSite.getEntityId(),
      refChan.getName(), StatusType.ACTIVE);
    if (isSiteMembershipUnique(memberships, activeMember)) {
      memberships.add(activeMember);
    }
    // add inactive membership if required
    if (inactiveMembershipRequired(sitechan, index, sitechanRecords)) {
      Instant offdate = sitechan.getOffdate().orElse(null);
      final var inactiveMember = ReferenceSiteMembership.create(
        "Channel " + refChan.getName() + " is un-associated with site " + sitechan.getSta(),
        offdate, offdate, refSite.getEntityId(),
        refChan.getName(), StatusType.INACTIVE);
      if (isSiteMembershipUnique(memberships, inactiveMember)) {
        memberships.add(inactiveMember);
      }
    }
  }

  private static Optional<ReferenceSite> findSiteActiveAt(Collection<ReferenceSite> sites,
    Instant time) {
    return sites.stream().filter(s -> !s.getActualChangeTime().isAfter(time))
      .max(Comparator.comparing(ReferenceSite::getActualChangeTime));
  }

  private static List<SiteChannelRecord> sortByStaChanOndate(
    Collection<SiteChannelRecord> records) {
    return records.stream().sorted(
        Comparator.comparing(SiteChannelRecord::getSta)
          .thenComparing(SiteChannelRecord::getChan)
          .thenComparing(SiteChannelRecord::getOndate,
            Comparator.comparing(optional -> optional.orElse(null),
              Comparator.nullsLast(Instant::compareTo))))
      .collect(toList());
  }

  static List<ReferenceChannel> buildReferenceChannel(
    Multimap<Integer, SensorRecord> sensorRecordsByChannelId,
    Map<Integer, InstrumentRecord> instrumentRecordsByInid, SiteChannelRecord siteChannelRecord,
    ReferenceSite site) {

    Instant onDate = siteChannelRecord.getOndate().orElse(Instant.now());

    // Create a information source object.
    var source = InformationSource.from(EXTERNAL,
      onDate, String.format(TRIPLESTRING, OFFDATE, siteChannelRecord.getOffdate(), LOADCSS));

    AtomicReference<Double> sampleRateRef = new AtomicReference<>(0.0);

    var sensorRecordsOptional = getMany(sensorRecordsByChannelId, siteChannelRecord.getChanid());

    // It is possible there are many matches in the sensor records for the same Chanid
    // therefore we need to check them all since its possible they all do not exist in the instrument file
    // and we must make sure we have a valid matching sample rate
    sensorRecordsOptional
      .ifPresent(sensorRecordsCollection -> sensorRecordsCollection.forEach(sensorRecord -> {
        var instrumentRecord =
          Optional.ofNullable(instrumentRecordsByInid.get(sensorRecord.getInid()));
        instrumentRecord
          .ifPresent(instrumentRecord1 -> sampleRateRef.set(instrumentRecord1.getSamprate()));
      }));

    // create channel
    var position = RelativePosition.from(
      0, 0, 0);

    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(siteChannelRecord.getChan());
    if (channelTypesOptional.isEmpty()) {
      logger.warn("Could not parse ChannelTypes for record {}", siteChannelRecord.getChan());
      return List.of();
    }

    var channelTypes = channelTypesOptional.get();
    List<ReferenceChannel> referenceChannels = new ArrayList<>();
    ReferenceChannel channel = ReferenceChannel.builder()
      .setName(makeChannelName(siteChannelRecord.getSta(), siteChannelRecord.getChan()))
      .setDataType(channelTypes.getDataType())
      .setBandType(channelTypes.getBandType())
      .setInstrumentType(channelTypes.getInstrumentType())
      .setOrientationType(channelTypes.getOrientationType())
      .setOrientationCode(channelTypes.getOrientationCode())
      // Location code can now be found within the Site Names
      // CRs incoming to change COI objects accordingly
      .setLocationCode("")
      .setLatitude(site.getLatitude())
      .setLongitude(site.getLongitude())
      .setElevation(site.getElevation())
      .setDepth(siteChannelRecord.getEdepth())
      .setVerticalAngle(siteChannelRecord.getVang())
      .setHorizontalAngle(siteChannelRecord.getHang())
      .setUnits(UnitsUtility.determineUnits(channelTypes.getDataType()))
      .setNominalSampleRate(sampleRateRef.get())
      .setActualTime(onDate)
      .setSystemTime(onDate)
      .setActive(true)
      .setInformationSource(source)
      .setComment("Channel is associated with site " + siteChannelRecord.getSta())
      .setPosition(position)
      .setAliases(Collections.emptyList())
      .build();

    referenceChannels.add(channel);

    siteChannelRecord.getOffdate().ifPresent(offdate -> {
      if (offdate.isBefore(Instant.now())) {
        ReferenceChannel inactive = channel.toBuilder()
          .setActualTime(offdate)
          .setSystemTime(offdate)
          .setActive(false)
          .build();
        referenceChannels.add(inactive);
      }
    });

    return referenceChannels;
  }

  private static Collection<ReferenceStation> processSiteRecordsToMakeStations(
    Collection<SiteRecord> records) {
    final Collection<ReferenceStation> results = new ArrayList<>();
    final Collection<SiteRecord> siteRecordsThatMakeStations = records
      .stream().filter(CssReferenceReader::recordRepresentsStation)
      .collect(toList());
    for (SiteRecord siteRecord : siteRecordsThatMakeStations) {

      Optional<Instant> offdateOptional = siteRecord.getOffdate();
      Optional<Instant> lddateOptional = siteRecord.getLddate();
      Optional<Instant> ondateOptional = siteRecord.getOndate();
      var offdate = "-1";
      Instant lddate = lddateOptional.orElse(Instant.now());
      Instant ondate = ondateOptional.orElse(Instant.now());
      if (offdateOptional.isPresent()) {
        offdate = offdateOptional.get().toString();
      }
      final var source = InformationSource.from(
        EXTERNAL, lddate,
        String.format(TRIPLESTRING, OFFDATE, offdate, LOADCSS));

      // TODO: this mapping may not be correct.
      StationType type = StationType.UNKNOWN;
      switch (siteRecord.getStatype().toUpperCase(Locale.ENGLISH)) {
        case "SS":
          type = StationType.SEISMIC_3_COMPONENT;
          break;
        case "AR":
          type = StationType.SEISMIC_ARRAY;
          break;
        default:
          logByLevel(WARN, String.format(
            "processSiteRecordsToMakeSites() - Unknown site type detected: %1$s for record: %2$s",
            siteRecord.getStatype(), siteRecord));
      }
      final String name = siteRecord.getSta();

      // Basic record - there's always 1 active ReferenceStation created from a SiteRecord
      ReferenceStation station = ReferenceStation.builder()
        .setName(name)
        .setDescription(siteRecord.getStaname())
        .setStationType(type)
        .setSource(source)
        .setComment("Loaded from site file.")
        .setLatitude(siteRecord.getLat())
        .setLongitude(siteRecord.getLon())
        .setElevation(siteRecord.getElev())
        .setActualChangeTime(ondate)
        .setSystemChangeTime(ondate)
        .setActive(true)
        .setAliases(List.of())
        .build();

      if (isStationUnique(results, station)) {
        results.add(station);
      } else {
        logByLevel(WARN, String
          .format("processSiteRecordsToMakeSites() - found duplicate station: %1$s", station));
      }

      // Possible second record - if the ReferenceStation was turned off
      siteRecord.getOffdate().ifPresent(offDate -> addInactiveStation(results, station, offDate));
    }
    return Collections.unmodifiableCollection(results);
  }

  private static void addInactiveStation(Collection<ReferenceStation> results,
    ReferenceStation station, Instant offDate) {
    if (offDate.isBefore(Instant.now())) {
      ReferenceStation inactive = station.toBuilder()
        .setActualChangeTime(offDate)
        .setSystemChangeTime(offDate)
        .setActive(false)
        .build();

      if (isStationUnique(results, inactive)) {
        results.add(inactive);
      } else {
        logByLevel(WARN, String
          .format("processSiteRecordsToMakeSites() - found duplicate station: %1$s",
            inactive));
      }
    }
  }

  /**
   * Process all site records and convert into COI objects.
   */
  private static Pair<Collection<ReferenceSite>, Set<ReferenceStationMembership>> processSiteRecordsToMakeSites(
    Collection<SiteRecord> records,
    Multimap<String, ReferenceStation> stationsByName) {
    final Collection<ReferenceSite> sites = new ArrayList<>();
    final Set<ReferenceStationMembership> memberships = new HashSet<>();
    final List<SiteRecord> siteRecordsThatMakeSites = records
      .stream().filter(CssReferenceReader::recordRepresentsSite)
      // sort by sta and then ondate
      .sorted(Comparator.comparing(SiteRecord::getSta).thenComparing(SiteRecord::getOndate,
        Comparator.comparing(optional -> optional.orElse(null),
          Comparator.nullsLast(Instant::compareTo))))
      .collect(toList());

    for (var i = 0; i < siteRecordsThatMakeSites.size(); i++) {
      final var siteRecord = siteRecordsThatMakeSites.get(i);
      Instant offdate = siteRecord.getOffdate().orElse(null);
      var offdateString = "-1";
      if (offdate != null) {
        offdateString = offdate.toString();
      }
      Instant lddate = siteRecord.getLddate().orElse(null);
      final var source = InformationSource.from(EXTERNAL,
        lddate,
        String.format(TRIPLESTRING, OFFDATE, offdateString, LOADCSS));

      final String siteName = siteRecord.getSta();
      final String stationName = siteRecord.getRefsta();
      final Optional<ReferenceStation> parentStation = getFirst(stationsByName, stationName);
      if (parentStation.isEmpty()) {
        logByLevel(ERR, String.format(
          "Could not find parent station for site %1$s; ref sta is %2$s", siteName, stationName));
        continue;
      }
      Optional<Instant> ondateValue = siteRecord.getOndate();
      //If, for some reason, there is no ondate value in Optional, we get Instant.now
      final Instant ondate = ondateValue.orElse(Instant.now());

      final ReferenceStation station = parentStation.get();
      final var relativePosition = RelativePosition.from(
        siteRecord.getDnorth(), siteRecord.getDeast(), 0);
      // make the base site
      final ReferenceSite site = ReferenceSite.builder()
        .setName(siteRecord.getSta())
        .setDescription(siteRecord.getStaname())
        .setSource(source)
        .setComment("Site is associated with station " + siteRecord.getRefsta())
        .setLatitude(siteRecord.getLat())
        .setLongitude(siteRecord.getLon())
        .setElevation(siteRecord.getElev())
        .setActualChangeTime(ondate)
        .setSystemChangeTime(ondate)
        .setActive(true)
        .setPosition(relativePosition)
        .setAliases(List.of())
        .build();

      // Save the site in various structures for later processing.  But first make sure this
      // isn't a duplicate entry in the input file.
      if (isSiteUnique(sites, site)) {
        sites.add(site);
      }

      siteRecord.getOffdate().ifPresent(offDate -> addInactiveSite(sites, site, offDate));

      // make the station membership (relates site and it's station)
      final var member = ReferenceStationMembership.create(
        "Relationship for station "
          + station.getName() + " and site " + site.getName(),
        ondate, ondate,
        station.getEntityId(),
        site.getEntityId(),
        StatusType.ACTIVE);
      if (isStationMembershipUnique(memberships, member)) {
        memberships.add(member);
      }
      if (inactiveMembershipRequired(siteRecord, i, siteRecordsThatMakeSites)) {
        final var inactiveMembership = ReferenceStationMembership.create(
          "Relationship for station "
            + station.getName() + " and site " + site.getName(),
          offdate, offdate,
          station.getEntityId(), site.getEntityId(), StatusType.INACTIVE);
        if (isStationMembershipUnique(memberships, inactiveMembership)) {
          memberships.add(inactiveMembership);
        }
      }
    }
    return Pair
      .of(Collections.unmodifiableCollection(sites), Collections.unmodifiableSet(memberships));
  }

  private static void addInactiveSite(Collection<ReferenceSite> sites, ReferenceSite site,
    Instant offDate) {
    if (offDate.isBefore(Instant.now())) {
      ReferenceSite inactive = site.toBuilder()
        .setActive(false)
        .setActualChangeTime(offDate)
        .setSystemChangeTime(offDate)
        .build();

      // Save the site in various structures for later processing.  But first make sure this
      // isn't a duplicate entry in the input file.
      if (isSiteUnique(sites, inactive)) {
        sites.add(inactive);
      } else {
        logByLevel(WARN,
          String.format("processSiteRecordsToMakeSites() - found duplicate site: %1$s",
            site));
      }
    }
  }

  /**
   * Logger method to keep log check from incrementing complexity.
   *
   * @param level The level of log activity
   * @param message The message to be logged
   */
  private static void logByLevel(String level, String message) {
    if (level.equals(WARN) && IS_WARN) {
      logger.warn(message);
    } else if (level.equals(ERR) && IS_ERR) {
      logger.error(message);
    } else if (level.equals(INFO) && IS_INFO) {
      logger.info(message);
    } else if (IS_DEBUG) {
      logger.debug(message);
    }
  }

  private static void logMissingInstrumentRecordOrChannel2(SensorRecord sensorRecord,
    int inId,
    String channelName,
    boolean channelPresent) {
    if (!instrumentRecordsById.containsKey(inId)) {
      logByLevel(ERR, String.format(
        "Could not find instrument record with inid %1$s referenced in sensor record %2$s",
        inId, sensorRecord));
    }
    if (!channelPresent) {
      logByLevel(WARN,
        String.format("Could not find channel for sensor record by name %1$s", channelName));
    }
  }

  /**
   * Read response files and parse them in FrequencyAmplitudePhase object
   *
   * @param responsePath path to response files
   * @param referenceChannelDataType channel data type for the reference channel for this response
   * data
   * @return FrequencyAmplitudePhase object created from data in the response file
   */
  private static FrequencyAmplitudePhase processResponseFiles(
    String responsePath, ChannelDataType referenceChannelDataType) {

    List<Double> frequencies = new ArrayList<>();
    var amplitudeUnits = UnitsUtility.determineUnits(referenceChannelDataType);
    List<Double> amplitudeReponse = new ArrayList<>();
    List<Double> amplitudeReponseStdDev = new ArrayList<>();
    var phaseUnits = Units.DEGREES;
    List<Double> phaseResponse = new ArrayList<>();
    List<Double> phaseResponseStdDev = new ArrayList<>();

    for (FapRecord fapRecord : FapFileReader.readFapFile(responsePath)) {
      frequencies.add(fapRecord.getFrequency());
      amplitudeReponse.add(fapRecord.getAmplitude());
      amplitudeReponseStdDev.add(fapRecord.getAmplitudeError());
      phaseResponse.add(fapRecord.getPhase());
      phaseResponseStdDev.add(fapRecord.getPhaseError());
    }
    return FrequencyAmplitudePhase.builder()
      .setFrequencies(toDoubleArray(frequencies))
      .setAmplitudeResponseUnits(amplitudeUnits)
      .setAmplitudeResponse(toDoubleArray(amplitudeReponse))
      .setAmplitudeResponseStdDev(toDoubleArray(amplitudeReponseStdDev))
      .setPhaseResponseUnits(phaseUnits)
      .setPhaseResponse(toDoubleArray(phaseResponse))
      .setPhaseResponseStdDev(toDoubleArray(phaseResponseStdDev))
      .build();
  }

  private static double[] toDoubleArray(List<Double> doubleList) {
    return doubleList.stream().mapToDouble(Double::doubleValue).toArray();
  }

  private static <K, T> Optional<T> getFirst(Multimap<K, T> m, K key) {
    if (!m.containsKey(key)) {
      return Optional.empty();
    }
    Collection<T> l = m.get(key);

    return l.isEmpty() ? Optional.empty() : Optional.of(l.iterator().next());
  }

  private static <K, T> Optional<Collection<T>> getMany(Multimap<K, T> m, K key) {

    if (!m.containsKey(key)) {
      return Optional.empty();
    }
    Collection<T> l = m.get(key);

    return l.isEmpty() ? Optional.empty() : Optional.of(l);

  }

  private static boolean isNetworkMembershipUnique(
    Collection<ReferenceNetworkMembership> memberships,
    ReferenceNetworkMembership member) {
    for (ReferenceNetworkMembership item : memberships) {
      if (member.getStationId().equals(item.getStationId())
        && member.getNetworkId().equals(item.getNetworkId())
        && member.getActualChangeTime().equals(item.getActualChangeTime())
        && member.getSystemChangeTime().equals(item.getSystemChangeTime())
        && member.getStatus().equals(item.getStatus())
      ) {
        return false;
      }
    }
    return true;
  }

  private static boolean isStationMembershipUnique(
    Collection<ReferenceStationMembership> memberships,
    ReferenceStationMembership member) {
    for (ReferenceStationMembership item : memberships) {
      if (member.getStationId().equals(item.getStationId())
        && member.getSiteId().equals(item.getSiteId())
        && member.getActualChangeTime().equals(item.getActualChangeTime())
        && member.getSystemChangeTime().equals(item.getSystemChangeTime())
        && member.getStatus().equals(item.getStatus())
      ) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSiteMembershipUnique
    (Collection<ReferenceSiteMembership> memberships,
      ReferenceSiteMembership member) {
    for (ReferenceSiteMembership item : memberships) {
      if (member.getChannelName().equals(item.getChannelName())
        && member.getSiteId().equals(item.getSiteId())
        && member.getActualChangeTime().equals(item.getActualChangeTime())
        && member.getSystemChangeTime().equals(item.getSystemChangeTime())
        && member.getStatus().equals(item.getStatus())
      ) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSiteUnique(Collection<ReferenceSite> sites, ReferenceSite site) {
    for (ReferenceSite item : sites) {
      if (site.getName().equals(item.getName())
        && site.getActualChangeTime().equals(item.getActualChangeTime())
        && site.getElevation() == item.getElevation()
        && site.getLatitude() == item.getLatitude()
        && site.getLongitude() == item.getLongitude()
      ) {
        logByLevel(WARN,
          String.format("processSiteRecordsToMakeSites() - found duplicate site: %1$s", site));
        return false;
      }

    }
    return true;
  }

  private static boolean isStationUnique(Collection<ReferenceStation> stations,
    ReferenceStation station) {
    for (ReferenceStation item : stations) {
      if (station.getName().equals(item.getName())
        && station.getActualChangeTime().equals(item.getActualChangeTime())
        && station.getElevation() == item.getElevation()
        && station.getLatitude() == item.getLatitude()
        && station.getLongitude() == item.getLongitude()
        && station.getStationType().equals(item.getStationType())
        && station.isActive() == item.isActive()
      ) {
        return false;
      }

    }
    return true;
  }

  private static boolean isChannelDuplicate(Collection<ReferenceChannel> channels,
    ReferenceChannel channel) {
    for (ReferenceChannel item : channels) {
      if (channel.getName().equals(item.getName())
        && channel.getActualTime().equals(item.getActualTime())
        && channel.getElevation() == item.getElevation()
        && channel.getLatitude() == item.getLatitude()
        && channel.getLongitude() == item.getLongitude()
        && channel.getDepth() == item.getDepth()
        && channel.getVerticalAngle() == item.getVerticalAngle()
        && channel.getHorizontalAngle() == item.getHorizontalAngle()
        && channel.getNominalSampleRate() == item.getNominalSampleRate()
        && channel.getDataType().equals(item.getDataType())
        && channel.getBandType().equals(item.getBandType())
        && channel.getInstrumentType().equals(item.getInstrumentType())
        && channel.getOrientationType().equals(item.getOrientationType())
        && channel.getOrientationCode() == item.getOrientationCode()
        && channel.getComment().equals(item.getComment())
      ) {
        return true;
      }

    }
    return false;
  }

  private static boolean recordRepresentsStation(SiteRecord siteRecord) {
    return siteRecord.getSta().equals(siteRecord.getRefsta());
  }

  private static boolean recordRepresentsSite(SiteRecord siteRecord) {
    return !siteRecord.getSta().equals(siteRecord.getRefsta()) ||
      (siteRecord.getSta().equals(siteRecord.getRefsta()) && "ss".equals(siteRecord.getStatype()));
  }

  private static final BiPredicate<SiteRecord, SiteRecord> SITE_REC_EQUALITY_FUNC = (r1, r2) ->
    r1.getSta().equals(r2.getSta()) && r1.getRefsta().equals(r2.getRefsta());

  private static final BiPredicate<SiteChannelRecord, SiteChannelRecord> SITECHAN_REC_EQUALITY_FUNC = (r1, r2) ->
    r1.getSta().equals(r2.getSta()) && r1.getChan().equals(r2.getChan());

  private static final BiPredicate<AffiliationRecord, AffiliationRecord> AFFILIATION_EQUALITY_FUNC = (r1, r2) ->
    r1.getNet().equals(r2.getNet()) && r1.getSta().equals(r2.getSta());

  private static boolean inactiveMembershipRequired(
    SiteRecord siteRecord, int index, List<SiteRecord> records) {
    return inactiveMembershipRequired(siteRecord, index, records,
      SiteRecord::getOndate, SiteRecord::getOffdate, SITE_REC_EQUALITY_FUNC);
  }

  private static boolean inactiveMembershipRequired(
    SiteChannelRecord siteChannelRecord, int index, List<SiteChannelRecord> records) {

    return inactiveMembershipRequired(siteChannelRecord, index, records,
      SiteChannelRecord::getOndate, SiteChannelRecord::getOffdate,
      SITECHAN_REC_EQUALITY_FUNC);
  }

  private static boolean inactiveMembershipRequired(
    AffiliationRecord affiliationRecord, int index, List<AffiliationRecord> records) {

    return inactiveMembershipRequired(affiliationRecord, index, records,
      AffiliationRecord::getTime, AffiliationRecord::getEndtime,
      AFFILIATION_EQUALITY_FUNC);
  }

  private static <R> boolean inactiveMembershipRequired(
    R r, int index, List<R> records,
    Function<R, Optional<Instant>> ondateExtractor,
    Function<R, Optional<Instant>> offdateExtractor,
    BiPredicate<R, R> equalityFunc) {

    // if offdate is null, then return false. It has no offdate and is therefore active.
    if (offdateExtractor.apply(r).isEmpty()) {
      return false;
    }

    // if last record, return true.
    if ((index + 1) >= records.size()) {
      return true;
    }

    // Check if the next record has ondate = to this records' offdate.
    // If not, create an inactive membership record.
    final var nextRecord = records.get(index + 1);
    final boolean nextRecordIsSameEntity = equalityFunc.test(r, nextRecord);
    final boolean nextRecordStartsOnSameDay = offdateExtractor.apply(r).equals(
      ondateExtractor.apply(nextRecord));
    return !nextRecordIsSameEntity || !nextRecordStartsOnSameDay;
  }

  // populate name of channel with convention '{siteName}.{channelName}'
  private static String makeChannelName(String sta, String chan) {
    return sta + "." + chan;
  }

}
