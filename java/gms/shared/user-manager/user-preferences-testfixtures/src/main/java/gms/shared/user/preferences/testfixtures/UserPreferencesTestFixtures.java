package gms.shared.user.preferences.testfixtures;

import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import gms.shared.user.preferences.coi.AudibleNotification;
import gms.shared.user.preferences.coi.UserInterfaceMode;
import gms.shared.user.preferences.coi.UserPreferences;
import gms.shared.user.preferences.coi.WorkspaceLayout;

import java.util.ArrayList;
import java.util.List;

public class UserPreferencesTestFixtures {
  private static final String USER_ID = "Test User";
  private static final String DEFAULT_LAYOUT_NAME = "Default Layout";
  private static final String CURRENT_THEME = "Current Theme";
  private static final String LAYOUT_CONFIG_1 = "Test Configuration";
  private static final String LAYOUT_CONFIG_2 = "Test Configuration2";
  private static final String HEY_WAV = "Hey.wav";
  private static final String LISTEN_WAV = "Listen.wav";
  private static final String HUSH_WAV = "Hush.wav";
  private static final String WATCH_OUT_WAV = "WatchOut.wav";

  private static final String REACT_COMPONENT_STRING1 = "%22componentName%22:%22lm-react-component%22,%22isClosable%22:true,";

  private static final String REORDER_ENABLED = "%22reorderEnabled%22:true%7D,%7B%22type%22:%22component%22,";

  private static final List<AudibleNotification> audibleNotifications = List.of(
    AudibleNotification
      .from(HEY_WAV, SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED),
    AudibleNotification
      .from(LISTEN_WAV, SystemMessageType.STATION_NEEDS_ATTENTION));

  private static final List<AudibleNotification> audibleNotificationsUpdate = List.of(
    AudibleNotification
      .from(HEY_WAV, SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED),
    AudibleNotification
      .from(HUSH_WAV, SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED),
    AudibleNotification
      .from(WATCH_OUT_WAV, SystemMessageType.STATION_NEEDS_ATTENTION));


  private UserPreferencesTestFixtures() {
  }

  public static final UserPreferences USER_PREFERENCES_DEFAULT = UserPreferences
    .from(USER_ID,
      DEFAULT_LAYOUT_NAME,
      DEFAULT_LAYOUT_NAME,
      CURRENT_THEME,
      List.of(WorkspaceLayout
        .from(DEFAULT_LAYOUT_NAME, List.of(UserInterfaceMode.ANALYST),
          LAYOUT_CONFIG_1)), new ArrayList<>());

  public static final UserPreferences USER_PREFERENCES_TWO = UserPreferences
    .from(USER_ID,
      DEFAULT_LAYOUT_NAME,
      DEFAULT_LAYOUT_NAME,
      CURRENT_THEME,
      List.of(WorkspaceLayout
        .from(DEFAULT_LAYOUT_NAME, List.of(UserInterfaceMode.ANALYST),
          LAYOUT_CONFIG_2)), new ArrayList<>());

  public static final UserPreferences USER_PREFERENCES_AUDIBLE_DEFAULT = UserPreferences
    .from(USER_ID,
      DEFAULT_LAYOUT_NAME,
      DEFAULT_LAYOUT_NAME,
      CURRENT_THEME,
      List.of(WorkspaceLayout
        .from(DEFAULT_LAYOUT_NAME, List.of(
            UserInterfaceMode.ANALYST),
          LAYOUT_CONFIG_1)), audibleNotifications);

  public static final UserPreferences USER_PREFERENCES_AUDIBLE_UPDATE = UserPreferences
    .from(USER_ID,
      DEFAULT_LAYOUT_NAME,
      DEFAULT_LAYOUT_NAME,
      CURRENT_THEME,
      List.of(WorkspaceLayout
        .from(DEFAULT_LAYOUT_NAME, List.of(
            UserInterfaceMode.ANALYST),
          LAYOUT_CONFIG_1)), audibleNotificationsUpdate);

  public static final UserPreferences USER_PREFERENCES_AUDIBLE_TWO = UserPreferences
    .from(USER_ID,
      DEFAULT_LAYOUT_NAME,
      DEFAULT_LAYOUT_NAME,
      CURRENT_THEME,
      List.of(WorkspaceLayout
        .from(DEFAULT_LAYOUT_NAME, List.of(
            UserInterfaceMode.ANALYST),
          LAYOUT_CONFIG_2)), audibleNotifications);

  public static final String USER_PREFERENCES_JSON = "{\n" +
    "  \"defaultAnalystLayoutName\": \"SOH Layout\",\n" +
    "  \"defaultSohLayoutName\": \"SOH Layout\",\n" +
    "  \"audibleNotifications\": [],\n" +
    "  \"sohLayoutName\": \"SOH Layout\",\n" +
    "  \"userId\": \"defaultUser\",\n" +
    "  \"currentTheme\": \"GMS Dark Theme\",\n" +
    "  \"workspaceLayouts\": [\n" +
    "    {\n" +
    "      \"name\": \"SOH Layout\",\n" +
    "      \"supportedUserInterfaceModes\": [\"SOH\", \"ANALYST\"],\n" +
    "      \"layoutConfiguration\": \"%7B%22settings%22:%7B%22hasHeaders%22:true," +
    "%22constrainDragToContainer%22:true,%22reorderEnabled%22:true," +
    "%22selectionEnabled%22:false,%22popoutWholeStack%22:false," +
    "%22blockedPopoutsThrowError%22:true,%22closePopoutsOnUnload%22:true," +
    "%22showPopoutIcon%22:false,%22showMaximiseIcon%22:true,%22showCloseIcon%22:true," +
    "%22responsiveMode%22:%22onload%22,%22tabOverlapAllowance%22:0," +
    "%22reorderOnTabMenuClick%22:true,%22tabControlOffset%22:10%7D," +
    "%22dimensions%22:%7B%22borderWidth%22:2,%22borderGrabWidth%22:15,%22minItemHeight%22:30," +
    "%22minItemWidth%22:30,%22headerHeight%22:30,%22dragProxyWidth%22:300," +
    "%22dragProxyHeight%22:200%7D,%22labels%22:%7B%22close%22:%22close%22," +
    "%22maximise%22:%22maximise%22,%22minimise%22:%22minimise%22," +
    "%22popout%22:%22open%20in%20new%20window%22,%22popin%22:%22pop%20in%22," +
    "%22tabDropdown%22:%22additional%20tabs%22%7D,%22content%22:%5B%7B%22type%22:%22row%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true,%22title%22:%22%22," +
    "%22content%22:%5B%7B%22type%22:%22stack%22,%22width%22:50,%22isClosable%22:true," +
    "%22reorderEnabled%22:true,%22title%22:%22%22,%22activeItemIndex%22:0," +
    "%22content%22:%5B%7B%22type%22:%22component%22,%22title%22:%22SOH%20Overview%22," +
    "%22component%22:%22soh-overview%22,%22componentName%22:%22lm-react-component%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true%7D%5D%7D,%7B%22type%22:%22stack%22," +
    "%22header%22:%7B%7D,%22isClosable%22:true,%22reorderEnabled%22:true,%22title%22:%22%22," +
    "%22activeItemIndex%22:0,%22width%22:50,%22content%22:%5B%7B%22type%22:%22component%22," +
    "%22title%22:%22SOH%20Details%22,%22component%22:%22soh-details%22," +
    REACT_COMPONENT_STRING1 +
    "%22reorderEnabled%22:true%7D%5D%7D%5D%7D%5D,%22isClosable%22:true," +
    "%22reorderEnabled%22:true,%22title%22:%22%22,%22openPopouts%22:%5B%5D," +
    "%22maximisedItemId%22:null%7D\"    },\n" +
    "    {\n" +
    "      \"name\": \"Analyst Displays Layout\",\n" +
    "      \"supportedUserInterfaceModes\": [\"ANALYST\"],\n" +
    "      \"layoutConfiguration\": \"%7B%22settings%22:%7B%22hasHeaders%22:true," +
    "%22constrainDragToContainer%22:true,%22reorderEnabled%22:true," +
    "%22selectionEnabled%22:false,%22popoutWholeStack%22:false," +
    "%22blockedPopoutsThrowError%22:true,%22closePopoutsOnUnload%22:true," +
    "%22showPopoutIcon%22:false,%22showMaximiseIcon%22:true,%22showCloseIcon%22:true," +
    "%22responsiveMode%22:%22onload%22,%22tabOverlapAllowance%22:0," +
    "%22reorderOnTabMenuClick%22:true,%22tabControlOffset%22:10%7D," +
    "%22dimensions%22:%7B%22borderWidth%22:2,%22borderGrabWidth%22:15,%22minItemHeight%22:30," +
    "%22minItemWidth%22:30,%22headerHeight%22:30,%22dragProxyWidth%22:300," +
    "%22dragProxyHeight%22:200%7D,%22labels%22:%7B%22close%22:%22close%22," +
    "%22maximise%22:%22maximise%22,%22minimise%22:%22minimise%22," +
    "%22popout%22:%22open%20in%20new%20window%22,%22popin%22:%22pop%20in%22," +
    "%22tabDropdown%22:%22additional%20tabs%22%7D,%22content%22:%5B%7B%22type%22:%22row%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true,%22title%22:%22%22," +
    "%22content%22:%5B%7B%22type%22:%22column%22,%22width%22:60,%22isClosable%22:true," +
    "%22reorderEnabled%22:true,%22title%22:%22%22,%22content%22:%5B%7B%22type%22:%22stack%22," +
    "%22height%22:30,%22isClosable%22:true,%22reorderEnabled%22:true,%22title%22:%22%22," +
    "%22activeItemIndex%22:0,%22content%22:%5B%7B%22type%22:%22component%22," +
    "%22title%22:%22Map%22,%22component%22:%22map%22,%22height%22:30," +
    REACT_COMPONENT_STRING1 +
    "%22reorderEnabled%22:true%7D%5D%7D,%7B%22type%22:%22stack%22,%22isClosable%22:true," +
    "%22reorderEnabled%22:true,%22title%22:%22%22,%22height%22:70,%22activeItemIndex%22:4," +
    "%22content%22:%5B%7B%22type%22:%22component%22,%22title%22:%22Events%22," +
    "%22component%22:%22events%22,%22componentName%22:%22lm-react-component%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true%7D,%7B%22type%22:%22component%22," +
    "%22title%22:%22Signal%20Detections%22,%22component%22:%22signal-detections%22," +
    REACT_COMPONENT_STRING1 +
    REORDER_ENABLED +
    "%22title%22:%22Azimuth%20Slowness%22,%22component%22:%22azimuth-slowness%22," +
    REACT_COMPONENT_STRING1 +
    "%22reorderEnabled%22:true%7D,%7B%22type%22:%22component%22,%22title%22:%22Magnitude%22," +
    "%22component%22:%22magnitude%22,%22componentName%22:%22lm-react-component%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true%7D,%7B%22type%22:%22component%22," +
    "%22title%22:%22Location%22,%22component%22:%22location%22," +
    REACT_COMPONENT_STRING1 +
    REORDER_ENABLED +
    "%22title%22:%22Station%20Information%22,%22component%22:%22station-information%22," +
    REACT_COMPONENT_STRING1 +
    REORDER_ENABLED +
    "%22title%22:%22Station%20Configuration%22,%22component%22:%22station-configuration%22," +
    REACT_COMPONENT_STRING1 +
    REORDER_ENABLED +
    "%22title%22:%22SOH%20Overview%22,%22component%22:%22soh-overview%22," +
    REACT_COMPONENT_STRING1 +
    REORDER_ENABLED +
    "%22title%22:%22Transfer%20Gaps%22,%22component%22:%22transfer-gaps%22," +
    REACT_COMPONENT_STRING1 +
    REORDER_ENABLED +
    "%22title%22:%22Configure%20Station%20Groups%22," +
    "%22component%22:%22configure-station-groups%22," +
    REACT_COMPONENT_STRING1 +
    "%22reorderEnabled%22:true%7D%5D%7D%5D%7D,%7B%22type%22:%22column%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true,%22title%22:%22%22,%22width%22:40," +
    "%22content%22:%5B%7B%22type%22:%22stack%22,%22height%22:30,%22isClosable%22:true," +
    "%22reorderEnabled%22:true,%22title%22:%22%22,%22activeItemIndex%22:0," +
    "%22content%22:%5B%7B%22type%22:%22component%22,%22title%22:%22Workflow%22," +
    "%22component%22:%22workflow%22,%22componentName%22:%22lm-react-component%22," +
    "%22isClosable%22:true,%22reorderEnabled%22:true%7D%5D%7D,%7B%22type%22:%22stack%22," +
    "%22height%22:70,%22isClosable%22:true,%22reorderEnabled%22:true,%22title%22:%22%22," +
    "%22activeItemIndex%22:0,%22content%22:%5B%7B%22type%22:%22component%22," +
    "%22title%22:%22Waveforms%22,%22component%22:%22waveform-display%22,%22height%22:70," +
    REACT_COMPONENT_STRING1 +
    "%22reorderEnabled%22:true%7D%5D%7D%5D%7D%5D%7D%5D,%22isClosable%22:true," +
    "%22reorderEnabled%22:true,%22title%22:%22%22,%22openPopouts%22:%5B%5D," +
    "%22maximisedItemId%22:null%7D\"\n" +
    "    }\n" +
    "  ]\n" +
    "}";
}
