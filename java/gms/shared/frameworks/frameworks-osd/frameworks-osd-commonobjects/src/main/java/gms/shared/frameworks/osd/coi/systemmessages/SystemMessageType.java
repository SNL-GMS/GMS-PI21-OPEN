package gms.shared.frameworks.osd.coi.systemmessages;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.CHANNEL;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.MONITOR_TYPE;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.STATION;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.STATION_GROUP;


/**
 * Defines the type of the message.
 */
public enum SystemMessageType {

  // Station <station> needs attention
  STATION_NEEDS_ATTENTION("Station %s needs attention", SystemMessageSeverity.CRITICAL,
    SystemMessageCategory.SOH, SystemMessageSubCategory.STATION,
    Set.of(STATION)),

  // Station <station> SOH status changed from <previous_status> to <current_status>
  STATION_SOH_STATUS_CHANGED("Station %s SOH status changed from %s to %s",
    SystemMessageSeverity.INFO, SystemMessageCategory.SOH, SystemMessageSubCategory.STATION,
    Set.of(STATION)),

  /*
   * Station <station> capability status for Station Group <group> changed
   * from <previous_status> to <current_status>
   */
  STATION_CAPABILITY_STATUS_CHANGED(
    "Station %s capability status for Station Group %s changed from %s to %s",
    SystemMessageSeverity.WARNING, SystemMessageCategory.SOH,
    SystemMessageSubCategory.CAPABILITY,
    Set.of(STATION, STATION_GROUP)),

  // Station Group <group> capability status changed from <previous_status> to <current_status>
  STATION_GROUP_CAPABILITY_STATUS_CHANGED(
    "Station Group %s capability status changed from %s to %s", SystemMessageSeverity.WARNING,
    SystemMessageCategory.SOH, SystemMessageSubCategory.CAPABILITY,
    Set.of(STATION_GROUP)),

  /*
   * Station <station> Channel <channel> <monitor_type> status changed from
   * <value> <units>(<previous_status>) to <value> <units>(<current_status>)
   */
  CHANNEL_MONITOR_TYPE_STATUS_CHANGED(
    "Station %s Channel %s %s status changed from %s (%s) to %s (%s)",
    SystemMessageSeverity.INFO, SystemMessageCategory.SOH, SystemMessageSubCategory.STATION,
    Set.of(STATION, CHANNEL, MONITOR_TYPE)),

  /*
   * Without comment:
   * Station <station> Channel <channel> <monitor_type> status change acknowledged by user <username>
   *
   * With comment:
   * Station <station> Channel <channel> <monitor_type> status change acknowledged by
   * user <username> with comment '<comment>'
   *
   * Split on pipe '|' to get appropriate message template.
   */
  CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED(
    "Station %s Channel %s %s status change acknowledged by user %s"
      + "|Station %s Channel %s %s status change acknowledged by user %s with comment '%s'",
    SystemMessageSeverity.INFO, SystemMessageCategory.SOH, SystemMessageSubCategory.USER,
    Set.of(STATION, CHANNEL, MONITOR_TYPE)),

  /*
   * Without comment:
   * Station <station> Channel <channel> <monitor_type> quieted for <time interval> by user <username>
   *
   * With comment:
   * Station <station> Channel <channel> <monitor_type> quieted for <time interval> by user <username>
   * with comment '<comment>'
   *
   * Split on pipe '|' to get appropriate message template.
   */
  CHANNEL_MONITOR_TYPE_QUIETED("Station %s Channel %s %s quieted for %s by user %s"
    + "|Station %s Channel %s %s quieted for %s by user %s with comment '%s'",
    SystemMessageSeverity.WARNING, SystemMessageCategory.SOH, SystemMessageSubCategory.USER,
    Set.of(STATION, CHANNEL, MONITOR_TYPE)),

  // Station <station> Channel <channel> <monitor_type> quiet period canceled by user <username>
  CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED(
    "Station %s Channel %s %s quiet period canceled by user %s", SystemMessageSeverity.INFO,
    SystemMessageCategory.SOH, SystemMessageSubCategory.USER,
    Set.of(STATION, CHANNEL, MONITOR_TYPE)),

  // Station <station> Channel <channel> <monitor_type> quiet period expired
  CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED("Station %s Channel %s %s quiet period expired",
    SystemMessageSeverity.INFO, SystemMessageCategory.SOH, SystemMessageSubCategory.STATION,
    Set.of(STATION, CHANNEL, MONITOR_TYPE));

  private final String messageTemplate;
  private final SystemMessageSeverity severity;
  private final SystemMessageCategory category;
  private final SystemMessageSubCategory subCategory;
  private final Set<SystemMessageTagNames> tags;

  SystemMessageType(String messageTemplate, SystemMessageSeverity severity,
    SystemMessageCategory category, SystemMessageSubCategory subCategory,
    Set<SystemMessageTagNames> tags) {

    this.messageTemplate = messageTemplate;
    this.severity = severity;
    this.category = category;
    this.subCategory = subCategory;
    this.tags = tags;
  }

  public String getMessageTemplate() {
    return messageTemplate;
  }

  public SystemMessageSeverity getSeverity() {
    return severity;
  }

  public SystemMessageCategory getCategory() {
    return category;
  }

  public SystemMessageSubCategory getSubCategory() {
    return subCategory;
  }

  public Set<SystemMessageTagNames> getTags() {
    return tags;
  }

  public static void validate(String message, SystemMessageType type,
    SystemMessageSeverity severity, SystemMessageCategory category,
    SystemMessageSubCategory subCategory, Map<String, Object> messageTags) {

    Objects.requireNonNull(type, "System message type may not be null");
    Objects.requireNonNull(message, "System message template may not be null");

    if (!Objects.equals(severity, type.getSeverity())) {
      throw new IllegalArgumentException("Incorrect system message severity found");
    }

    if (!Objects.equals(category, type.getCategory())) {
      throw new IllegalArgumentException("Incorrect system message category found");
    }

    if (!Objects.equals(subCategory, type.getSubCategory())) {
      throw new IllegalArgumentException("Incorrect system message subcategory found");
    }

    if (messageTags.keySet().size() != type.getTags().size()) {
      throw new IllegalArgumentException("Incorrect system message tags found");
    }
  }
}
