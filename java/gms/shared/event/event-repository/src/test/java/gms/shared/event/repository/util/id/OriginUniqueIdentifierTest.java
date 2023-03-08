package gms.shared.event.repository.util.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OriginUniqueIdentifierTest {

  @Test
  void testOriginUniqueIdentifierPreconditions() {
    assertThrows(IllegalArgumentException.class, () -> OriginUniqueIdentifier.create(-1, "stage"));
    assertThrows(IllegalArgumentException.class, () -> OriginUniqueIdentifier.create(1L, ""));
    assertThrows(NullPointerException.class, () -> OriginUniqueIdentifier.create(1L, null));
  }
}
