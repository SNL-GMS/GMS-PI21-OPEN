package gms.shared.user.preferences.coi;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPreferencesTest {

  @Test
  void testSerialization() throws IOException {
    final var audibleNotifications = List.of(
      AudibleNotification
        .from("Hey.wav", SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED),
      AudibleNotification
        .from("Listen.wav", SystemMessageType.STATION_NEEDS_ATTENTION));
    UserPreferences preferences = buildUserPreferences(audibleNotifications);

    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(preferences, mapper.readValue(mapper.writeValueAsString(preferences),
      UserPreferences.class));
  }

  @Test
  void testSerialization_emptyNotificationList() throws IOException {
    final List<AudibleNotification> audibleNotifications = Collections.emptyList();
    UserPreferences preferences = buildUserPreferences(audibleNotifications);

    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(preferences, mapper.readValue(mapper.writeValueAsString(preferences),
      UserPreferences.class));
  }

  @Test
  void testNullNotificationListValidation() {
    assertThrows(IllegalArgumentException.class, this::buildUserPreferencesWithDuplicateMessageType);
  }

  @Test
  void testDuplicateMessageTypeValidation() {
    assertThrows(IllegalArgumentException.class, this::buildUserPreferencesWithNullNotificationList
    );
  }

  private UserPreferences buildUserPreferencesWithDuplicateMessageType() {
    return buildUserPreferences(null);
  }

  private UserPreferences buildUserPreferencesWithNullNotificationList() {
    final var audibleNotifications = List.of(
      AudibleNotification
        .from("Hey.wav", SystemMessageType.STATION_NEEDS_ATTENTION),
      AudibleNotification
        .from("Listen.wav", SystemMessageType.STATION_NEEDS_ATTENTION));
    return buildUserPreferences(audibleNotifications);
  }

  private UserPreferences buildUserPreferences(List<AudibleNotification> audibleNotifications) {
    return UserPreferences.from(
      "Test Id",
      "Test Layout",
      "Test SoH Layout",
      "Test Theme",
      List.of(WorkspaceLayout.from("Test Layout",
        List.of(UserInterfaceMode.ANALYST),
        "Test Layout")),
      audibleNotifications);
  }
}