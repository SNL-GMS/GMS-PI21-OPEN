package gms.shared.user.preferences.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.util.List;

@AutoValue
public abstract class WorkspaceLayout {

  public abstract String getName();

  public abstract List<UserInterfaceMode> getSupportedUserInterfaceModes();

  public abstract String getLayoutConfiguration();

  @JsonCreator
  public static WorkspaceLayout from(@JsonProperty("name") String name,
    @JsonProperty("supportedUserInterfaceModes") List<UserInterfaceMode> supportedUserInterfaceModes,
    @JsonProperty("layoutConfiguration") String layoutConfiguration) {
    Validate.notEmpty(name, "WorkspaceLayout requires a non-empty name");
    Validate.notEmpty(supportedUserInterfaceModes, "WorkspaceLayout requires a non-empty list of supported user interface modes");
    Validate.notEmpty(layoutConfiguration, "WorkspaceLayout requires a non-empty layout configuration");

    return new AutoValue_WorkspaceLayout(name, supportedUserInterfaceModes, layoutConfiguration);
  }

}
