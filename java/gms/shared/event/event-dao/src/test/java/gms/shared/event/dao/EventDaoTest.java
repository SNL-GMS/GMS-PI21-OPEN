package gms.shared.event.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventDaoTest {

  private EventDao.Builder happyBuilder;

  @BeforeEach
  void initializeHappyBuilder() {
    happyBuilder = new EventDao.Builder()
      .withEventId(1918)
      .withEventName("You can call me Al")
      .withPreferredOrigin(5)
      .withAuthor("Aldous Huxley")
      .withCommentId(12)
      .withLoadDate(Instant.ofEpochSecond(325345740));
  }

  @Test
  void testBuilderHappy() {

    assertDoesNotThrow(() -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadEventId() {

    happyBuilder
      .withEventId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadEventName() {

    happyBuilder
      .withEventName(null);
    assertThrows(NullPointerException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withEventName("");
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withEventName("123456789*123456789*123456789*123");
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadPreferredOrigin() {

    happyBuilder
      .withPreferredOrigin(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadAuthor() {

    happyBuilder
      .withAuthor("");
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withAuthor("123456789*123456");
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadCommentId() {

    happyBuilder
      .withCommentId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadLoadDate() {

    happyBuilder
      .withLoadDate(null);
    assertThrows(NullPointerException.class, () -> happyBuilder
      .build());
  }
}