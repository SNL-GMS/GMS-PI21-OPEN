package gms.shared.user.preferences.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.util.List;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class UserPreferences {

  public abstract String getUserId();

  public abstract String getDefaultAnalystLayoutName();

  public abstract String getDefaultSohLayoutName();

  public abstract String getCurrentTheme();

  public abstract List<WorkspaceLayout> getWorkspaceLayouts();

  public abstract List<AudibleNotification> getAudibleNotifications();

  @JsonCreator
  public static UserPreferences from(@JsonProperty("userId") String userId,
    @JsonProperty("defaultAnalystLayoutName") String defaultAnalystLayoutName,
    @JsonProperty("defaultSohLayoutName") String defaultSohLayoutName,
    @JsonProperty("currentTheme") String currentTheme,
    @JsonProperty("workspaceLayouts") List<WorkspaceLayout> workspaceLayouts,
    @JsonProperty("audibleNotifications") List<AudibleNotification> audibleNotifications) {
    Validate.isTrue(workspaceLayouts != null && !workspaceLayouts.isEmpty(),
      "User Preferences must contain at least 1 WorkspaceLayout");
    Validate.isTrue(audibleNotifications != null,
      "User Preferences may not have an empty AudibleNotifications List");
    Validate.isTrue(audibleNotifications.size() == audibleNotifications.stream()
        .map(AudibleNotification::getNotificationType).distinct().count(),
      "User Preferences may not have duplicate Message type entries in the AudibleNotifications List");
    return new AutoValue_UserPreferences(userId,
      defaultAnalystLayoutName,
      defaultSohLayoutName,
      currentTheme,
      workspaceLayouts,
      audibleNotifications);
  }
}
