package gms.shared.frameworks.osd.coi.systemmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

import java.util.Objects;

@AutoValue
public abstract class SystemMessageDefinition {

  public abstract SystemMessageType getSystemMessageType();

  public abstract SystemMessageCategory getSystemMessageCategory();

  public abstract SystemMessageSubCategory getSystemMessageSubCategory();

  public abstract SystemMessageSeverity getSystemMessageSeverity();

  @Memoized
  public String getTemplate() {
    return getSystemMessageType().getMessageTemplate();
  }

  public static SystemMessageDefinition from(SystemMessageType systemMessageType) {
    Objects.requireNonNull(systemMessageType,
      "SystemMessageDefinition requires non-null SystemMessageType.");
    return SystemMessageDefinition.from(
      systemMessageType,
      systemMessageType.getCategory(),
      systemMessageType.getSubCategory(),
      systemMessageType.getSeverity());
  }

  @JsonCreator
  public static SystemMessageDefinition from(
    @JsonProperty("systemMessageType") SystemMessageType systemMessageType,
    @JsonProperty("systemMessageCategory") SystemMessageCategory systemMessageCategory,
    @JsonProperty("systemMessageSubCategory") SystemMessageSubCategory systemMessageSubCategory,
    @JsonProperty("systemMessageSeverity") SystemMessageSeverity systemMessageSeverity) {
    return new AutoValue_SystemMessageDefinition(
      systemMessageType,
      systemMessageCategory,
      systemMessageSubCategory,
      systemMessageSeverity);
  }

}
