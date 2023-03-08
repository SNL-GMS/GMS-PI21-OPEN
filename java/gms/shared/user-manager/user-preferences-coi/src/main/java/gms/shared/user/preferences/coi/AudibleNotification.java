package gms.shared.user.preferences.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

@AutoValue
public abstract class AudibleNotification {

  public abstract String getFileName();

  public abstract SystemMessageType getNotificationType();

  @JsonCreator
  public static AudibleNotification from(@JsonProperty("fileName") String fileName,
    @JsonProperty("notificationType") SystemMessageType notificationType) {
    notEmpty(fileName, "AudibleNotification requires a non-empty file name");
    notNull(notificationType, "AudibleNotification requires a non-null system message type");

    return new AutoValue_AudibleNotification(fileName, notificationType);
  }

}
