import type { SystemMessageType } from '../system-message/types';

/**
 * The enum for the user mode. Matches OSD enum and environment variables
 * used in build/start scripts.
 */
export enum UserMode {
  LEGACY = 'LEGACY',
  IAN = 'IAN',
  SOH = 'SOH'
}

/**
 * Possible Analyst Layout Names
 */
export enum DefaultLayoutNames {
  ANALYST_LAYOUT = 'ANALYST_LAYOUT',
  SOH_LAYOUT = 'SOH_LAYOUT'
}

/**
 * Arguments for the user profile query
 */
export interface UserProfileQueryArguments {
  defaultLayoutName: DefaultLayoutNames;
}

/**
 * User preferences COI object
 */
export interface UserProfileCOI {
  readonly userId: string;
  // just for storing default for Analyst mode
  readonly defaultAnalystLayoutName: string;
  /**
   * The name (unique) of the UI theme that should be applied to GMS. The theme with this name
   * will be looked up in processing config. If not found, GMS will fall back to the default theme.
   */
  readonly currentTheme: string;
  // just for storing default for SOH mode
  readonly defaultSohLayoutName: string;
  readonly workspaceLayouts: UserLayout[];
  readonly audibleNotifications: AudibleNotification[];
}

/**
 * User preferences UI version
 */
export interface UserProfile extends UserProfileCOI {
  // defaultLayoutName is the name of the currently set default layout (could be SOH or Analyst depending on mode)
  // Note that it is *not* readonly, as it is added by the UI and removed before saving.
  defaultLayoutName?: string;
}

/**
 * User layout. LayoutConfiguration is a URI encoded json representation of a golden layout.
 * Also includes metadata like supported user interface modes and the name of the layout.
 */
export interface UserLayout {
  name: string;
  supportedUserInterfaceModes: UserMode[];
  layoutConfiguration: string;
}

/**
 * The union of all allowed audible notification types.
 */
export type AudibleNotificationType = SystemMessageType;

/**
 * A pair of a notification enum type (for example, a SystemMessageType)
 * and a file, which is found in the /sounds/ directory
 */
export interface AudibleNotification {
  notificationType: AudibleNotificationType;
  /**
   * The layout name, used for updating the query cache.
   */
  defaultLayoutName?: DefaultLayoutNames;

  fileName: string;
}

/**
 * The audible notification mutation input, an array of audible notifications.
 */
export interface SetAudibleNotificationsInput {
  audibleNotificationsInput: AudibleNotification[];
}
