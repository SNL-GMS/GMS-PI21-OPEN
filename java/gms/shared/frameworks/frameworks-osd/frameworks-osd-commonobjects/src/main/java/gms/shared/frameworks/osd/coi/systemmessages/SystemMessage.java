package gms.shared.frameworks.osd.coi.systemmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a GMS system message.
 */
@AutoValue
public abstract class SystemMessage {

  /**
   * Id of this object.
   */
  public abstract UUID getId();

  /**
   * Time this system message was generated.
   */
  public abstract Instant getTime();

  /**
   * The content of this system message.
   */
  public abstract String getMessage();

  /**
   * The type of this system message.
   */
  public abstract SystemMessageType getType();

  /**
   * The severity of this system message.
   */
  public abstract SystemMessageSeverity getSeverity();

  /**
   * The category of this system message.
   */
  public abstract SystemMessageCategory getCategory();

  /**
   * The subcategory of this system message.
   */
  public abstract SystemMessageSubCategory getSubCategory();

  /**
   * Additional metadata associated with this system message.
   */
  public abstract Map<String, Object> getMessageTags();

  /**
   * Generates a new system message.
   */
  @JsonCreator
  public static SystemMessage from(
    @JsonProperty("id") UUID id,
    @JsonProperty("time") Instant time,
    @JsonProperty("message") String message,
    @JsonProperty("type") SystemMessageType type,
    @JsonProperty("severity") SystemMessageSeverity severity,
    @JsonProperty("category") SystemMessageCategory category,
    @JsonProperty("subCategory") SystemMessageSubCategory subCategory,
    @JsonProperty("messageTags") Map<String, Object> messageTags) {

    SystemMessageType.validate(message, type, severity, category, subCategory, messageTags);

    return new AutoValue_SystemMessage(
      id, time, message, type, severity, category, subCategory, messageTags
    );
  }

  /**
   * Generates a new system message.
   */
  public static SystemMessage create(
    Instant time,
    String message,
    SystemMessageType type,
    SystemMessageSeverity severity,
    SystemMessageCategory category,
    SystemMessageSubCategory subCategory,
    Map<String, Object> messageTags
  ) {

    SystemMessageType.validate(message, type, severity, category, subCategory, messageTags);

    return new AutoValue_SystemMessage(
      UUID.randomUUID(), time, message, type, severity, category, subCategory, messageTags
    );
  }
}
