package gms.shared.signaldetection.repository.utils;

import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.test.utils.containers.ZookeeperTest;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
  //zookeeper has been removed 8/5/2021
class SignalDetectionIdUtilityComponentTest extends ZookeeperTest {

  @BeforeAll
  static void setIgniteHome() {
    try {
      Path tempIgniteDirectory = Files.createTempDirectory("ignite-work");
      System.setProperty("IGNITE_HOME", tempIgniteDirectory.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static final CacheInfo ARID_SIGNAL_DETECTION_ID_CACHE = new CacheInfo("arid-signal-detection-id-cache",
    CacheMode.LOCAL, CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_ID_ARID_CACHE = new CacheInfo("signal-detection-id-arid-cache",
    CacheMode.LOCAL, CacheAtomicityMode.ATOMIC, true, Optional.empty());

  public static final CacheInfo ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID =
    new CacheInfo("arrival-id-signal-detection-hypothesis-id", CacheMode.LOCAL,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID =
    new CacheInfo("signal-detection-hypothesis-id-arrival-id-cache", CacheMode.LOCAL,
      CacheAtomicityMode.ATOMIC, true, Optional.empty());


  @Test
  void exerciseCaches() {
    IgniteConnectionManager.initialize(systemConfig, List.of(ARID_SIGNAL_DETECTION_ID_CACHE, SIGNAL_DETECTION_ID_ARID_CACHE, ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID, SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID));
    SignalDetectionIdUtility signalDetectionIdUtility = new SignalDetectionIdUtility();

    final String stage = "stage";
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from(stage);
    final long arid1 = 1L;
    final long arid2 = 2L;
    final UUID uuid1 = UUID.randomUUID();
    final UUID uuid2 = UUID.randomUUID();
    final SignalDetectionHypothesisArrivalIdComponents id = SignalDetectionHypothesisArrivalIdComponents.create(
      stageId.getName(), arid2);

    signalDetectionIdUtility.addAridForSignalDetectionUUID(arid1, uuid1);
    signalDetectionIdUtility.addAridAndStageIdForSignalDetectionHypothesisUUID(arid2, stageId.getName(), uuid2);

    assertEquals(uuid1, signalDetectionIdUtility.getSignalDetectionForArid(arid1));
    assertEquals(uuid1, signalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(arid1));

    assertEquals(arid1, signalDetectionIdUtility.getAridForSignalDetectionUUID(uuid1));

    assertEquals(id, signalDetectionIdUtility.getArrivalIdComponentsFromSignalDetectionHypothesisId(uuid2));

    assertEquals(uuid2, signalDetectionIdUtility.getSignalDetectionHypothesisIdForAridAndStageId(arid2, stageId.getName()));
    assertEquals(uuid2, signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(arid2, stageId.getName()));
  }
}
