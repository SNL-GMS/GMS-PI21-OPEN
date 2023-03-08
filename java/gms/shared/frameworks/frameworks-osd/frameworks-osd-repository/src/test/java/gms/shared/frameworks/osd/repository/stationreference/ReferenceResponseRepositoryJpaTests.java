package gms.shared.frameworks.osd.repository.stationreference;

import gms.shared.frameworks.coi.exceptions.DataExistsException;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.util.TestFixtures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class ReferenceResponseRepositoryJpaTests extends SohPostgresTest {

  private static ReferenceResponseRepositoryJpa referenceResponseRepositoryJpa;

  @BeforeAll
  static void testSuiteSetup() {
    referenceResponseRepositoryJpa = new ReferenceResponseRepositoryJpa(entityManagerFactory);

    // Load some initial objects.
    referenceResponseRepositoryJpa.storeReferenceResponses(TestFixtures.ALL_REFERENCE_RESPONSES);
  }

  @Test
  void testRetrieval() {
    List<String> channelNames = TestFixtures.ALL_REFERENCE_RESPONSES
      .stream()
      .map(ReferenceResponse::getChannelName)
      .collect(Collectors.toList());
    List<ReferenceResponse> responses =
      referenceResponseRepositoryJpa.retrieveReferenceResponses(channelNames);

    assertEquals(3, responses.size());
  }

  @Test
  void storeExistingResponse() {
    // Storing a channel that already exists should throw an exception
    assertThrows(DataExistsException.class, () -> {
      referenceResponseRepositoryJpa.storeReferenceResponses(TestFixtures.ALL_REFERENCE_RESPONSES);
    });
  }
}
