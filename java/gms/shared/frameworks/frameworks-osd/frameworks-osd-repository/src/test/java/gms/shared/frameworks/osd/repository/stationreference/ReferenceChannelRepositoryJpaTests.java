package gms.shared.frameworks.osd.repository.stationreference;

import gms.shared.frameworks.coi.exceptions.DataExistsException;
import gms.shared.frameworks.osd.api.util.ReferenceChannelRequest;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.dao.channel.ReferenceChannelDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.util.TestFixtures;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class ReferenceChannelRepositoryJpaTests extends SohPostgresTest {

  private static ReferenceChannelRepositoryJpa referenceChannelRepositoryJpa;

  private static boolean removeEntries = false;

  @BeforeAll
  static void testSuiteSetup() {
    referenceChannelRepositoryJpa = new ReferenceChannelRepositoryJpa(entityManagerFactory);

    // Load some initial objects.
    referenceChannelRepositoryJpa.storeReferenceChannels(TestFixtures.allReferenceChannels);
  }

  @AfterAll
  public static void tearDown() {
    if (removeEntries) {
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      try {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM " + ReferenceChannelDao.class.getSimpleName())
          .executeUpdate();
        entityManager.getTransaction().commit();
      } finally {
        entityManager.close();
        entityManagerFactory.close();
      }
    }
  }

  @Test
  void storeExistingChannel() {
    // Storing a channel that already exists should throw an exception
    assertThrows(DataExistsException.class, () -> {
      referenceChannelRepositoryJpa
        .storeReferenceChannels(List.of(TestFixtures.allReferenceChannels.get(0)));
    });
  }

  @Test
  void retrieveChannelsTest() {
    // Retrieve all channel data (don't put anything in ReferenceChannelRequest)
    ReferenceChannelRequest requestAllChannels = ReferenceChannelRequest.builder().build();
    List<ReferenceChannel> allReferenceChannels =
      referenceChannelRepositoryJpa.retrieveReferenceChannels(requestAllChannels);
    assertEquals(TestFixtures.allReferenceChannels, allReferenceChannels);

    List<ReferenceChannel> channelsList = List.of(
      TestFixtures.allReferenceChannels.get(0),
      TestFixtures.allReferenceChannels.get(1));

    // Retrieve two channels by name
    List<String> channelNames = channelsList.stream()
      .map(ReferenceChannel::getName).collect(Collectors.toList());
    ReferenceChannelRequest requestByName =
      ReferenceChannelRequest.builder()
        .setChannelNames(channelNames)
        .build();
    List<ReferenceChannel> channelsByName =
      referenceChannelRepositoryJpa.retrieveReferenceChannels(requestByName);
    assertEquals(2, channelsByName.size());
    assertEquals(channelsList, channelsByName);

    // Retrieve two channels by entityIds
    List<UUID> entityIds = channelsList.stream()
      .map(ReferenceChannel::getEntityId).collect(Collectors.toList());
    ReferenceChannelRequest requestByEntityId =
      ReferenceChannelRequest.builder()
        .setEntityIds(entityIds)
        .build();
    List<ReferenceChannel> channelsByEntityId =
      referenceChannelRepositoryJpa.retrieveReferenceChannels(requestByEntityId);
    assertEquals(2, channelsByEntityId.size());
    assertEquals(channelsList, channelsByEntityId);

    // Retrieve two channels by versionIds
    List<UUID> versionIds = channelsList.stream()
      .map(ReferenceChannel::getVersionId).collect(Collectors.toList());
    ReferenceChannelRequest requestByVersionId =
      ReferenceChannelRequest.builder()
        .setVersionIds(versionIds)
        .build();
    List<ReferenceChannel> channelsByVersionId =
      referenceChannelRepositoryJpa.retrieveReferenceChannels(requestByVersionId);
    assertEquals(2, channelsByVersionId.size());
    assertEquals(channelsList, channelsByVersionId);

    // Retrieve two channels by name, versionId, and entityId
    ReferenceChannelRequest requestByAll =
      ReferenceChannelRequest.builder()
        .setChannelNames(List.of(channelNames.get(0)))
        .setVersionIds(List.of(versionIds.get(1)))
        .setEntityIds(List.of(entityIds.get(0)))
        .build();
    List<ReferenceChannel> channelsByAll =
      referenceChannelRepositoryJpa.retrieveReferenceChannels(requestByAll);
    assertEquals(2, channelsByAll.size());
    assertEquals(channelsList, channelsByAll);

  }
}
