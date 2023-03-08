package gms.shared.frameworks.osd.repository.rawstationdataframe;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryQueryInterface;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import gms.shared.frameworks.osd.dao.transferredfile.RawStationDataFrameDao;
import gms.shared.frameworks.osd.dao.transferredfile.WaveformSummaryDao;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RawStationDataFrameRepositoryQueryViewJpa implements RawStationDataFrameRepositoryQueryInterface {

  public static final String RECEPTION_TIME = "receptionTime";
  public static final String PAYLOAD_DATA_END_TIME = "payloadDataEndTime";
  public static final String PAYLOAD_DATA_START_TIME = "payloadDataStartTime";
  public static final String STATION_NAME = "stationName";
  public static final String CHANNEL_NAMES = "channelNames";
  public static final String PAYLOAD_FORMAT = "payloadFormat";
  public static final String AUTHENTICATION_STATUS = "authenticationStatus";
  public static final String SUMMARIES = "waveformSummaries";

  public static final String CHANNEL_NAME = "channelName";

  public static final String END_TIME = "endTime";


  private EntityManagerFactory entityManagerFactory;

  public RawStationDataFrameRepositoryQueryViewJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Retrieves all {@link RawStationDataFrameMetadata}that have data in the specified time range.
   *
   * @param request The {@link StationTimeRangeRequest}
   * @return
   */
  @Override
  public List<RawStationDataFrameMetadata> retrieveRawStationDataFrameMetadataByStationAndTime(
    StationTimeRangeRequest request) {
    Objects.requireNonNull(request);

    var entityManager = entityManagerFactory.createEntityManager();
    var builder = entityManager.getCriteriaBuilder();

    // We only need a subset of the RSDF, but since it's not stored as a separate object, we will
    // use a projection.
    CriteriaQuery<Tuple> metadataQuery = builder.createTupleQuery();
    Root<RawStationDataFrameDao> fromRsdf = metadataQuery.from(RawStationDataFrameDao.class);
    // When using a projection and the Criteria API, any collections (element, one-to-many, etc)
    // need to be pulled in explicitly using joins. This results in an interesting result set
    // when the query is executed, where each row contains the data from the root table
    // (raw_station_data_frame) and one entry from each join present. The end result is (if any
    // collection has more than one entry) is multiple rows with the same data from the root table,
    // and one entry from each join table, until all the join table is present. As such, we need
    // to handle re-assembling this data specially (see below) to ensure we do not create duplicate
    // items.
    metadataQuery.multiselect(fromRsdf.get(STATION_NAME).alias(STATION_NAME),
        fromRsdf.join(CHANNEL_NAMES).alias(CHANNEL_NAMES),
        fromRsdf.get(PAYLOAD_DATA_START_TIME).alias(PAYLOAD_DATA_START_TIME),
        fromRsdf.get(PAYLOAD_DATA_END_TIME).alias(PAYLOAD_DATA_END_TIME),
        fromRsdf.get(PAYLOAD_FORMAT).alias(PAYLOAD_FORMAT),
        fromRsdf.get(AUTHENTICATION_STATUS).alias(AUTHENTICATION_STATUS),
        fromRsdf.get(RECEPTION_TIME).alias(RECEPTION_TIME),
        fromRsdf.join(SUMMARIES).alias(SUMMARIES))
      .where(
        builder.and(
          builder.between(
            fromRsdf.get(PAYLOAD_DATA_END_TIME),
            request.getTimeRange().getStartTime(),
            request.getTimeRange().getEndTime()),
          builder.equal(fromRsdf.get(STATION_NAME), request.getStationName())
        )
      );

    try {
      // Processing the tuple results into an RSDFMetadata requires assembling all the collection
      // items (ChannelNames and WaveformSammaries) and grouping them together with the RSDFMetadata
      // they belong to, without duplicating that RSDFMetadata.  We do this by building a map of the
      // RSDFMetadata (with empty channel names and waveform summaries) to the lists that will be
      // to populate this.  We don't want to build that list as we go because we need equality of
      // the RSDFMetadata to be able topair up the keys using the .equals and .hashcode methods on
      // RSDFMetadata.
      return entityManager.createQuery(metadataQuery).getResultStream()
        .map(tuple ->
          // Convert each row in the result set to a Triple of an RSDFMetadata (with empty
          // channel names and waveform summaries), the channel name in that row, and the
          // waveform summary from that row.
          Triple.of(RawStationDataFrameMetadata.builder()
              .setStationName(tuple.get(STATION_NAME, String.class))
              .setPayloadStartTime(tuple.get(PAYLOAD_DATA_START_TIME, Instant.class))
              .setPayloadEndTime(tuple.get(PAYLOAD_DATA_END_TIME, Instant.class))
              .setPayloadFormat(tuple.get(PAYLOAD_FORMAT,
                RawStationDataFramePayloadFormat.class))
              .setAuthenticationStatus(tuple.get(AUTHENTICATION_STATUS,
                RawStationDataFrame.AuthenticationStatus.class))
              .setReceptionTime(tuple.get(RECEPTION_TIME, Instant.class))
              .setChannelNames(List.of())
              .setWaveformSummaries(Map.of())
              .build(),
            tuple.get(CHANNEL_NAMES, String.class),
            tuple.get(SUMMARIES, WaveformSummaryDao.class).toCoi()))
        // Collect the triples into a map of the rsdfMetadata to the set of channel names (to
        // remove duplicates from how the tuple had to be built during the join functions and
        // map of waveform summaries from all rows that have that RSDF Metadata
        .collect(Collector.of
          // the supplier - creates the empty map
            ((Supplier<HashMap<RawStationDataFrameMetadata, Pair<Set<String>, Map<String, WaveformSummary>>>>) HashMap::new,
              // the accumulator - adds a triple to the  map. If the RSDFMetadata is not
              // in the map, it creates a new Pair in the map.
              // Once that pair exists in the map (if it needed to be created), add the
              // channel name and waveform summary from this triple and put them in the
              // appropriate collections
              (map, triple) -> {
                Pair<Set<String>, Map<String, WaveformSummary>> value =
                  map.computeIfAbsent(triple.getLeft(),
                    metadata -> Pair.of(new HashSet<>(), new HashMap<>()));
                value.getKey().add(triple.getMiddle());
                value.getValue().put(triple.getRight().getChannelName(), triple.getRight());
              },
              // the merge function to combine two maps. Loop through one map, and either
              // add the entry from one, or add the values from the collections in the value
              // of the map entry to the value for the key in the other map.
              (map1, map2) -> {
                map2.forEach((metadataBuilder, pair) -> map1.merge(metadataBuilder, pair,
                  (pair1, pair2) -> {
                    pair1.getKey().addAll(pair2.getKey());
                    pair1.getValue().putAll(pair2.getValue());
                    return pair1;
                  }));
                return map1;
              }))
        // Stream through the map build above, to rebuild the RSDFMetadata with the channel name
        // and waveform summary collections, now that they have been built and associated  with
        // the appropriate RSDFMetadata
        .entrySet().stream()
        // Convert the RSDFMetadata back to a builder, set the collections, and rebuild it
        .map(rsdfMetadataPair -> rsdfMetadataPair.getKey().toBuilder()
          .setChannelNames(rsdfMetadataPair.getValue().getKey())
          .setWaveformSummaries(rsdfMetadataPair.getValue().getValue())
          .build())
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  @Override
  public Map<String, Instant> retrieveLatestSampleTimeByChannel(List<String> channelNames) {
    Objects.requireNonNull(channelNames);
    Preconditions.checkState(!channelNames.isEmpty());

    var entityManager = entityManagerFactory.createEntityManager();
    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<Tuple> latestSampleTimeQuery = builder.createTupleQuery();
      Root<WaveformSummaryDao> fromWaveformSummary = latestSampleTimeQuery.from(WaveformSummaryDao.class);
      latestSampleTimeQuery.multiselect(fromWaveformSummary.get(CHANNEL_NAME).alias(CHANNEL_NAME),
          builder.max(fromWaveformSummary.get(END_TIME)).alias(END_TIME))
        .where(fromWaveformSummary.get(CHANNEL_NAME).in(channelNames))
        .groupBy(fromWaveformSummary.get(CHANNEL_NAME));

      return entityManager.createQuery(latestSampleTimeQuery).getResultStream()
        .map(tuple -> Pair.of(tuple.get(CHANNEL_NAME, String.class), tuple.get(END_TIME, Instant.class)))
        .distinct()
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    } finally {
      entityManager.close();
    }
  }
}
