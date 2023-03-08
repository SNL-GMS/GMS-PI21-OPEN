package gms.shared.user.preferences.dao;

import gms.shared.user.preferences.coi.AudibleNotification;
import gms.shared.user.preferences.coi.UserPreferences;
import gms.shared.user.preferences.coi.WorkspaceLayout;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_preferences")
public class UserPreferencesDao {

  @Id
  @Column(name = "id")
  private String userId;

  @Column(name = "default_analyst_layout_name")
  private String defaultAnalystLayoutName;

  @Column(name = "default_soh_layout_name")
  private String defaultSohLayoutName;

  @Column(name = "current_theme")
  private String currentTheme;

  @OneToMany(mappedBy = "userPreferences", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkspaceLayoutDao> workspaceLayouts;

  @OneToMany(mappedBy = "userPreferences", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AudibleNotificationDao> audibleNotifications;

  protected UserPreferencesDao() {
    // no arg JPA constructor
  }

  public UserPreferencesDao(UserPreferences userPreferences) {
    this.userId = userPreferences.getUserId();
    this.defaultAnalystLayoutName = userPreferences.getDefaultAnalystLayoutName();
    this.defaultSohLayoutName = userPreferences.getDefaultSohLayoutName();
    this.currentTheme = userPreferences.getCurrentTheme();
    this.workspaceLayouts = userPreferences.getWorkspaceLayouts().stream()
      .map(WorkspaceLayoutDao::new)
      .map(workspaceLayout -> {
        workspaceLayout.setUserPreferences(UserPreferencesDao.this);
        return workspaceLayout;
      })
      .collect(Collectors.toList());
    this.audibleNotifications = userPreferences.getAudibleNotifications().stream()
      .map(AudibleNotificationDao::new)
      .map(audibleNotification -> {
        audibleNotification.setUserPreferences(UserPreferencesDao.this);
        return audibleNotification;
      })
      .collect(Collectors.toList());
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCurrentTheme() {
    return currentTheme;
  }

  public void setCurrentTheme(String currentTheme) {
    this.currentTheme = currentTheme;
  }

  public String getDefaultAnalystLayoutName() {
    return defaultAnalystLayoutName;
  }

  public void setDefaultAnalystLayoutName(String defaultAnalystLayoutName) {
    this.defaultAnalystLayoutName = defaultAnalystLayoutName;
  }

  public String getDefaultSohLayoutName() {
    return defaultSohLayoutName;
  }

  public void setDefaultSohLayoutName(String defaultSohLayoutName) {
    this.defaultSohLayoutName = defaultSohLayoutName;
  }

  public List<WorkspaceLayoutDao> getWorkspaceLayouts() {
    return workspaceLayouts;
  }

  public void setWorkspaceLayouts(List<WorkspaceLayoutDao> workspaceLayouts) {
    this.workspaceLayouts = workspaceLayouts;
  }

  public List<AudibleNotificationDao> getAudibleNotifications() {
    return audibleNotifications;
  }

  public void setAudibleNotifications(
    List<AudibleNotificationDao> audibleNotifications) {
    this.audibleNotifications = audibleNotifications;
  }

  public UserPreferences toCoi() {
    final List<WorkspaceLayout> workspaceLayoutCois = workspaceLayouts.stream()
      .map(WorkspaceLayoutDao::toCoi)
      .collect(Collectors.toList());
    final List<AudibleNotification> audibleNotificationCois = audibleNotifications.stream()
      .map(AudibleNotificationDao::toCoi)
      .collect(Collectors.toList());
    return UserPreferences.from(userId,
      defaultAnalystLayoutName,
      defaultSohLayoutName,
      currentTheme,
      workspaceLayoutCois,
      audibleNotificationCois);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserPreferencesDao that = (UserPreferencesDao) o;
    return userId.equals(that.userId) &&
      defaultAnalystLayoutName.equals(that.defaultAnalystLayoutName) &&
      defaultSohLayoutName.equals(that.defaultSohLayoutName) &&
      currentTheme.equals(that.currentTheme) &&
      workspaceLayouts.equals(that.workspaceLayouts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, defaultAnalystLayoutName, defaultSohLayoutName, currentTheme, workspaceLayouts);
  }
}
