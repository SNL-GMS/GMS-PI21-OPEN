package gms.shared.signaldetection.repository.utils;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SignalDetectionIdUtility {

  public static final CacheInfo ARID_SIGNAL_DETECTION_ID_CACHE = new CacheInfo("arid-signal-detection-id-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_ID_ARID_CACHE = new CacheInfo("signal-detection-id-arid-cache",
    CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());

  public static final CacheInfo ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID =
    new CacheInfo("arrival-id-signal-detection-hypothesis-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID =
    new CacheInfo("signal-detection-hypothesis-id-arrival-id-cache", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());

  public static final CacheInfo ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID =
    new CacheInfo("assoc-id-signal-detection-hypothesis-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID =
    new CacheInfo("signal-detection-hypothesis-id-assoc-id-cache", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());

  public static final CacheInfo AMPLITUDE_ID_FEATURE_MEASUREMENT_ID =
    new CacheInfo("amplitude-id-feature-measurement-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo FEATURE_MEASUREMENT_ID_AMPLITUDE_ID =
    new CacheInfo("feature-measurement-id-amplitude-id", CacheMode.REPLICATED,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());

  private IgniteCache<Long, UUID> aridSignalDetectionMap;
  private IgniteCache<UUID, Long> signalDetectionAridMap;
  private IgniteCache<SignalDetectionHypothesisArrivalIdComponents, UUID> arrivalIdComponentsSignalDetectionHypothesisIdMap;
  private IgniteCache<UUID, SignalDetectionHypothesisArrivalIdComponents> signalDetectionHypothesisIdArrivalIdComponentsMap;
  private IgniteCache<SignalDetectionHypothesisAssocIdComponents, UUID> assocIdComponentsSignalDetectionHypothesisIdMap;
  private IgniteCache<UUID, SignalDetectionHypothesisAssocIdComponents> signalDetectionHypothesisIdAssocIdComponentsMap;
  private IgniteCache<AmplitudeIdComponents, FeatureMeasurementIdComponents> amplitudeIdComponentsFeatureMeasurementIdComponentsMap;
  private IgniteCache<FeatureMeasurementIdComponents, AmplitudeIdComponents> featureMeasurementIdComponentsAmplitudeIdComponentsMap;


  @Autowired
  public SignalDetectionIdUtility() {

    this.aridSignalDetectionMap =
      IgniteConnectionManager.getOrCreateCache(ARID_SIGNAL_DETECTION_ID_CACHE);
    this.signalDetectionAridMap =
      IgniteConnectionManager.getOrCreateCache(SIGNAL_DETECTION_ID_ARID_CACHE);
    this.arrivalIdComponentsSignalDetectionHypothesisIdMap =
      IgniteConnectionManager.getOrCreateCache(ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID);
    this.signalDetectionHypothesisIdArrivalIdComponentsMap =
      IgniteConnectionManager.getOrCreateCache(SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID);
    this.assocIdComponentsSignalDetectionHypothesisIdMap =
      IgniteConnectionManager.getOrCreateCache(ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID);
    this.signalDetectionHypothesisIdAssocIdComponentsMap =
      IgniteConnectionManager.getOrCreateCache(SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID);
    this.amplitudeIdComponentsFeatureMeasurementIdComponentsMap =
      IgniteConnectionManager.getOrCreateCache(AMPLITUDE_ID_FEATURE_MEASUREMENT_ID);
    this.featureMeasurementIdComponentsAmplitudeIdComponentsMap =
      IgniteConnectionManager.getOrCreateCache(FEATURE_MEASUREMENT_ID_AMPLITUDE_ID);
  }

  // for testing only
  SignalDetectionIdUtility(IgniteCache<Long, UUID> aridSignalDetectionMap,
    IgniteCache<UUID, Long> signalDetectionAridMap,
    IgniteCache<SignalDetectionHypothesisArrivalIdComponents, UUID> arrivalIdComponentsSignalDetectionHypothesisIdMap,
    IgniteCache<UUID, SignalDetectionHypothesisArrivalIdComponents> signalDetectionHypothesisIdArrivalIdComponentsMap,
    IgniteCache<SignalDetectionHypothesisAssocIdComponents, UUID> assocIdComponentsSignalDetectionHypothesisIdMap,
    IgniteCache<UUID, SignalDetectionHypothesisAssocIdComponents> signalDetectionHypothesisIdAssocIdComponentsMap,
    IgniteCache<AmplitudeIdComponents, FeatureMeasurementIdComponents> amplitudeIdComponentsFeatureMeasurementIdComponentsMap,
    IgniteCache<FeatureMeasurementIdComponents, AmplitudeIdComponents> featureMeasurementIdComponentsAmplitudeIdComponentsMap) {

    this.aridSignalDetectionMap = aridSignalDetectionMap;
    this.signalDetectionAridMap = signalDetectionAridMap;
    this.arrivalIdComponentsSignalDetectionHypothesisIdMap = arrivalIdComponentsSignalDetectionHypothesisIdMap;
    this.signalDetectionHypothesisIdArrivalIdComponentsMap = signalDetectionHypothesisIdArrivalIdComponentsMap;
    this.assocIdComponentsSignalDetectionHypothesisIdMap = assocIdComponentsSignalDetectionHypothesisIdMap;
    this.signalDetectionHypothesisIdAssocIdComponentsMap = signalDetectionHypothesisIdAssocIdComponentsMap;
    this.amplitudeIdComponentsFeatureMeasurementIdComponentsMap = amplitudeIdComponentsFeatureMeasurementIdComponentsMap;
    this.featureMeasurementIdComponentsAmplitudeIdComponentsMap = featureMeasurementIdComponentsAmplitudeIdComponentsMap;
  }

  /**
   * Find the UUID of SignalDetection for a given arid, returns null if no value is found
   *
   * @param arid Long Arrival Id
   * @return UUID
   */
  public UUID getSignalDetectionForArid(long arid) {
    return aridSignalDetectionMap.get(arid);
  }

  /**
   * Find Arrival Id for a given SignalDetection UUID, returns null if no value is found
   *
   * @param uuid SignalDetection UUID
   * @return arid
   */
  public Long getAridForSignalDetectionUUID(UUID uuid) {
    return signalDetectionAridMap.get(uuid);
  }

  /**
   * Find SignalDetectionHypothesis UUID for given Arrival Id and Stage Id, returns null if no value is found
   *
   * @param arid Long Arrival Id
   * @param legacyDatabaseAccountId String legacy db account
   * @return SignalDetectionHypothesis UUID
   */
  public UUID getSignalDetectionHypothesisIdForAridAndStageId(long arid, String legacyDatabaseAccountId) {
    var id = SignalDetectionHypothesisArrivalIdComponents.create(
      legacyDatabaseAccountId, arid);
    return arrivalIdComponentsSignalDetectionHypothesisIdMap.get(id);
  }

  /**
   * Find Arrival Id and Stage Id for a given SignalDetectionHypothesis UUID, returns null if no value is found
   *
   * @param uuid SignalDetectionHypothesis UUID
   * @return SignalDetectionHypothesisArrivalIdComponents class containing arid and stageid
   */
  public SignalDetectionHypothesisArrivalIdComponents getArrivalIdComponentsFromSignalDetectionHypothesisId(UUID uuid) {
    return signalDetectionHypothesisIdArrivalIdComponentsMap.get(uuid);
  }

  /**
   * Add mapping between Signal detection hypothesis UUID and stageId and arid
   *
   * @param arid Long Arrival Id
   * @param legacyDatabaseAccountId String for database account from stage id
   * @param uuid SignalDetectionHypothesis UUID
   */
  public void addAridAndStageIdForSignalDetectionHypothesisUUID(long arid, String legacyDatabaseAccountId, UUID uuid) {
    Preconditions.checkNotNull(legacyDatabaseAccountId);
    Preconditions.checkNotNull(uuid);
    var id = SignalDetectionHypothesisArrivalIdComponents.create(
      legacyDatabaseAccountId, arid);
    arrivalIdComponentsSignalDetectionHypothesisIdMap.put(id, uuid);
    signalDetectionHypothesisIdArrivalIdComponentsMap.put(uuid, id);
  }

  /**
   * Add mapping between Signal detection UUID and arid
   *
   * @param uuid SignalDetectionHypothesis UUID
   * @param arid Long Arrival Id
   */
  public void addAridForSignalDetectionUUID(long arid, UUID uuid) {
    aridSignalDetectionMap.put(arid, uuid);
    signalDetectionAridMap.put(uuid, arid);
  }

  /**
   * Find SignalDetectionHypothesis UUID for a given Arrival Id, Origin Id, and Stage Id,
   * returns null if no value is found
   *
   * @param arid Long Arrival Id
   * @param orid Long Origin Id
   * @param legacyDatabaseAccountId String for legacy db account id from stage id
   * @return SignalDetectionHypothesis UUID
   */
  public UUID getSignalDetectionHypothesisIdForAridOridAndStageId(long arid, long orid,
    String legacyDatabaseAccountId) {
    var id = SignalDetectionHypothesisAssocIdComponents.create(
      legacyDatabaseAccountId, arid, orid);
    return assocIdComponentsSignalDetectionHypothesisIdMap.get(id);
  }

  /**
   * Find Arrival Id, Origin Id, and Stage Id for a given SignalDetectionHypothesis UUID,
   * returns null if no value is found
   *
   * @param uuid SignalDetectionHypothesis UUID
   * @return {@link SignalDetectionHypothesisAssocIdComponents} containing arid, orid, and stage id
   */
  public SignalDetectionHypothesisAssocIdComponents getAssocIdComponentsFromSignalDetectionHypothesisId(UUID uuid) {
    return signalDetectionHypothesisIdAssocIdComponentsMap.get(uuid);
  }

  /**
   * Add mapping between Signal detection hypothesis UUID and stage id and arid and orid
   *
   * @param arid Long Arrival Id
   * @param orid Long Origin Id
   * @param legacyDatabaseAccountId String legacy account db id
   * @param uuid SignalDetectionHypothesis UUID
   */
  public void addAridAndOridAndStageIdForSignalDetectionHypothesisUUID(long arid, long orid,
    String legacyDatabaseAccountId, UUID uuid) {
    Preconditions.checkNotNull(legacyDatabaseAccountId);
    Preconditions.checkNotNull(uuid);
    var id = SignalDetectionHypothesisAssocIdComponents.create(
      legacyDatabaseAccountId, arid, orid);
    assocIdComponentsSignalDetectionHypothesisIdMap.put(id, uuid);
    signalDetectionHypothesisIdAssocIdComponentsMap.put(uuid, id);
  }

  /**
   * Find the UUID of SignalDetection for a given arid, creates and returns a new uuid if no value is found in map
   *
   * @param arid Long Arrival Id
   * @return UUID
   */
  public UUID getOrCreateSignalDetectionIdfromArid(long arid) {
    var uuid = getSignalDetectionForArid(arid);

    if (uuid == null) {
      var aridString = Long.toString(arid);
      uuid = UUID.nameUUIDFromBytes(aridString.getBytes());
    }

    aridSignalDetectionMap.put(arid, uuid);
    signalDetectionAridMap.put(uuid, arid);

    return uuid;
  }

  /**
   * Find SignalDetectionHypothesis UUID for given Arrival Id and Stage Id, creates and returns a new uuid if no value is found in map
   *
   * @param arid Long Arrival Id
   * @param legacyDatabaseAccountId String legacy account db id
   * @return SignalDetectionHypothesis UUID
   */
  public UUID getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(long arid, String legacyDatabaseAccountId) {

    var uuid = getSignalDetectionHypothesisIdForAridAndStageId(arid, legacyDatabaseAccountId);

    if (uuid == null) {
      var aridString = Long.toString(arid);
      uuid = UUID.nameUUIDFromBytes((aridString + legacyDatabaseAccountId).getBytes());
    }

    var id = SignalDetectionHypothesisArrivalIdComponents.create(
      legacyDatabaseAccountId, arid);
    arrivalIdComponentsSignalDetectionHypothesisIdMap.put(id, uuid);
    signalDetectionHypothesisIdArrivalIdComponentsMap.put(uuid, id);

    return uuid;
  }

  /**
   * Find SignalDetectionHypothesis UUID for given Arrival Id, Origin Id, and Stage Id,
   * creates and returns a new uuid if no value is found in map
   *
   * @param arid Long Arrival Id
   * @param orid Long Origin Id
   * @param legacyDatabaseAccountId String legacy account db id
   * @return SignalDetectionHypothesis UUID
   */
  public UUID getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(long arid, long orid,
    String legacyDatabaseAccountId) {

    var uuid = getSignalDetectionHypothesisIdForAridOridAndStageId(arid, orid, legacyDatabaseAccountId);

    if (uuid == null) {
      var aridString = Long.toString(arid);
      var oridString = Long.toString(orid);
      uuid = UUID.nameUUIDFromBytes((aridString + oridString + legacyDatabaseAccountId).getBytes());
    }

    var id = SignalDetectionHypothesisAssocIdComponents.create(
      legacyDatabaseAccountId, arid, orid);
    assocIdComponentsSignalDetectionHypothesisIdMap.put(id, uuid);
    signalDetectionHypothesisIdAssocIdComponentsMap.put(uuid, id);

    return uuid;
  }

  /**
   * Find Amplitude Id and Stage Id for a given SignalDetectionHypothesis UUID and Feature Measurement type
   * returns null if no value is found
   *
   * @param uuid SignalDetectionHypothesis UUID
   * @param featureMeasurementType FeatureMeasurementType
   * @return AmplitudeIdComponents class containing ampid and stageid
   */
  public AmplitudeIdComponents getAmplitudeIdComponentsFromSignalDetectionHypothesisIdAndFeatureMeasurementType(
    UUID uuid,
    FeatureMeasurementType featureMeasurementType) {

    return featureMeasurementIdComponentsAmplitudeIdComponentsMap
      .get(FeatureMeasurementIdComponents.create(uuid, featureMeasurementType));
  }

  /**
   * Find SignalDetectionHypothesis UUID and Feature Measurement type for a given Amplitude Id and Stage Id
   * returns null if no value is found
   *
   * @param ampid long Amplitude Id
   * @param legacyDatabaseAccountId String legacy db account id
   * @return FeatureMeasurementIdComponents class containing SignalDetectionHypothesis UUID and Feature Measurement
   */
  public FeatureMeasurementIdComponents getFeatureMeasurementIdComponentsFromAmpidAndStageId(long ampid,
    String legacyDatabaseAccountId) {

    return amplitudeIdComponentsFeatureMeasurementIdComponentsMap
      .get(AmplitudeIdComponents.create(legacyDatabaseAccountId, ampid));
  }

  /**
   * Add mapping of Signal detection hypothesis UUID and FeatureMeasurementType to and stage id and ampid
   *
   * @param ampid Long Amplitude Id
   * @param legacyDatabaseAccountId String legacy database account id
   * @param uuid SignalDetectionHypothesis UUID
   * @param featureMeasurementType FeatureMeasurementType
   */
  public void addAmpidAndStageIdForSignalDetectionHypothesisUUIDAndFeatureMeasurementType(long ampid,
    String legacyDatabaseAccountId, UUID uuid, FeatureMeasurementType<?> featureMeasurementType) {

    Preconditions.checkNotNull(legacyDatabaseAccountId);
    Preconditions.checkNotNull(uuid);

    var ampidComponents = AmplitudeIdComponents.create(legacyDatabaseAccountId, ampid);
    var featureMeasurementComponents = FeatureMeasurementIdComponents.create(uuid, featureMeasurementType);
    featureMeasurementIdComponentsAmplitudeIdComponentsMap.put(featureMeasurementComponents, ampidComponents);
    amplitudeIdComponentsFeatureMeasurementIdComponentsMap.put(ampidComponents, featureMeasurementComponents);
  }

}