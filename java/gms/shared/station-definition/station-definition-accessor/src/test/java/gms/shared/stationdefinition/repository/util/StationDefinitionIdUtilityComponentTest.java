package gms.shared.stationdefinition.repository.util;

import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.test.utils.containers.ZookeeperTest;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.CHANNEL_RECORD_ID_WFID_CACHE;
import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.RECORD_ID_WFID_CHANNEL_CACHE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
//zookeeper has been removed 8/5/2021
class StationDefinitionIdUtilityComponentTest extends ZookeeperTest {
  private static final Logger logger = LoggerFactory.getLogger(StationDefinitionIdUtilityComponentTest.class);


  @BeforeAll
  static void setIgniteHome() {
    try {
      Path tempIgniteDirectory = Files.createTempDirectory("ignite-work");
      System.setProperty("IGNITE_HOME", tempIgniteDirectory.toString());
    } catch (IOException e) {
      fail(e);
    }
    try {
      IgniteConnectionManager.initialize(systemConfig, List.of(RECORD_ID_WFID_CHANNEL_CACHE, CHANNEL_RECORD_ID_WFID_CACHE));
    } catch (IllegalStateException e) {
      logger.info("IgniteCache already initialized.");
    }
  }

  @Test
  void testIgniteCaches() {

    StationDefinitionIdUtility idUtility = new StationDefinitionIdUtility(systemConfig);

    long wfid = 3141;
    long recordId = 2718;
    TagName tagName = TagName.ARID;
    DerivedChannelIdComponents testIdComponents =
      DerivedChannelIdComponents.create(tagName, recordId, wfid);

    Channel expectedChannel = Channel.createVersionReference(CHANNEL.getName(), CHANNEL.getEffectiveAt().orElseThrow());

    Channel testChannel = CHANNEL;

    Channel nullChannel = idUtility.getDerivedChannelForWfidRecordId(tagName, recordId, wfid);
    assertNull(nullChannel);

    DerivedChannelIdComponents nullIdComponents = idUtility.getWfidRecordIdFromChannel(testChannel);
    assertNull(nullIdComponents);

    idUtility.storeWfidRecordIdChannelMapping(tagName, recordId, wfid, testChannel);
    Channel channel = idUtility.getDerivedChannelForWfidRecordId(tagName, recordId, wfid);
    assertEquals(expectedChannel, channel);
    DerivedChannelIdComponents idComponents = idUtility.getWfidRecordIdFromChannel(expectedChannel);
    assertEquals(testIdComponents, idComponents);

    IgniteConnectionManager.close();
  }

}
