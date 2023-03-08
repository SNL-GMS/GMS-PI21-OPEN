package gms.shared.spring.utilities.framework;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AliveCheckTest {

  @Test
  void aliveJSON_Valid() {
    var instantStr = Instant.now().toString();
    var aliveBuilder = AliveCheck.builder().setAliveAt(instantStr);
    assertEquals(instantStr, aliveBuilder.build().getAliveAt());
  }

  @Test
  void aliveJSON_NullTest() {
    var aliveCheckBuilder = AliveCheck.builder();
    assertThrows(NullPointerException.class, () -> aliveCheckBuilder.setAliveAt(null));
  }

  @Test
  void aliveJSON_EmptyStringTest() {
    var aliveBuilder = AliveCheck.builder().setAliveAt("");
    assertThrows(IllegalStateException.class, aliveBuilder::build);
  }

  @Test
  void aliveJSON_WhitespaceStringTest() {
    var aliveBuilder = AliveCheck.builder().setAliveAt("         ");
    assertThrows(IllegalStateException.class, aliveBuilder::build);
  }

  @Test
  void aliveJSON_MissingPropertiesTest() {
    assertThrows(IllegalStateException.class, AliveCheck.builder()::autobuild);
  }

}
