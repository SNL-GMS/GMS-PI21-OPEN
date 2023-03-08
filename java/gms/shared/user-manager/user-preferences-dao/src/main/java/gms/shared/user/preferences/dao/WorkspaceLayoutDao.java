package gms.shared.user.preferences.dao;

import converter.UserInterfaceModeConverter;
import gms.shared.user.preferences.coi.UserInterfaceMode;
import gms.shared.user.preferences.coi.WorkspaceLayout;
import org.hibernate.annotations.Type;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "workspace_layout")
public class WorkspaceLayoutDao {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "name")
  private String name;

  @ElementCollection
  @CollectionTable(name = "workspace_layout_supported_ui_modes",
    joinColumns = {@JoinColumn(name = "workspace_layout_id")})
  @Convert(converter = UserInterfaceModeConverter.class)
  @Column(name = "supported_user_interface_mode")
  private List<UserInterfaceMode> supportedUserInterfaceModes;

  @Lob
  @Type(type = "org.hibernate.type.StringType")
  @Column(name = "layout_configuration")
  private String layoutConfiguration;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_preferences_id", referencedColumnName = "id")
  private UserPreferencesDao userPreferences;

  protected WorkspaceLayoutDao() {

  }

  public WorkspaceLayoutDao(WorkspaceLayout workspaceLayout) {
    this.id = UUID.randomUUID();
    this.name = workspaceLayout.getName();
    this.supportedUserInterfaceModes = workspaceLayout.getSupportedUserInterfaceModes();
    this.layoutConfiguration = workspaceLayout.getLayoutConfiguration();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<UserInterfaceMode> getSupportedUserInterfaceModes() {
    return supportedUserInterfaceModes;
  }

  public void setSupportedUserInterfaceModes(List<UserInterfaceMode> supportedUserInterfaceModes) {
    this.supportedUserInterfaceModes = supportedUserInterfaceModes;
  }

  public String getLayoutConfiguration() {
    return layoutConfiguration;
  }

  public void setLayoutConfiguration(String layoutConfiguration) {
    this.layoutConfiguration = layoutConfiguration;
  }

  public UserPreferencesDao getUserPreferences() {
    return userPreferences;
  }

  public void setUserPreferences(UserPreferencesDao userPreferences) {
    this.userPreferences = userPreferences;
  }

  public WorkspaceLayout toCoi() {
    return WorkspaceLayout.from(name, supportedUserInterfaceModes, layoutConfiguration);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkspaceLayoutDao that = (WorkspaceLayoutDao) o;
    return id == that.id &&
      Objects.equals(name, that.name) &&
      Objects.equals(supportedUserInterfaceModes, that.supportedUserInterfaceModes) &&
      Objects.equals(layoutConfiguration, that.layoutConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, supportedUserInterfaceModes, layoutConfiguration);
  }
}
