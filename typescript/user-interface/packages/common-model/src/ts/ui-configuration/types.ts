import type { CommonTypes } from '../common-model';
import type { WaveformFilter } from '../waveform/types';

/**
 * A visual theme for the UI (including colors, typography, spacing, etc...).
 * These are loaded in from processing config, and the user profile defines the active theme.
 */
export interface UITheme {
  /**
   * Theme name must be unique, and is what is used to indicate which theme
   * a user is configured to use.
   */
  name: string;
  /**
   * The colors for this theme.
   */
  colors: ColorTheme;
  /**
   * Visual configurations for opacity and brightness
   */
  display: {
    // Determines the much less visible edge events are shown
    edgeEventOpacity: number;
    edgeSDOpacity: number;
    predictionSDOpacity: number;
  };
  /**
   * Used to determine if we're in a dark mode or a light mode. This indicates if we should
   * tell our component library (Blueprint) to be in dark or light mode.
   */
  isDarkMode?: boolean;
}

/**
 * A definition of configurable colors for a @interface UITheme.
 * Color strings may be any valid css color.
 */
export interface ColorTheme {
  gmsMain: string;
  gmsMainInverted: string;
  gmsBackground: string;
  gmsSelection: string;
  gmsTableSelection: string;
  mapStationDefault: string;
  mapVisibleStation: string;
  unassociatedSDColor: string;
  openEventSDColor: string;
  completeEventSDColor: string;
  otherEventSDColor: string;
  predictionSDColor: string;
  waveformDimPercent: number;
  waveformRaw: string;
  waveformFilterLabel: string;
}

/**
 * The configuration for a single keyboard shortcut, including information
 * for display in the keyboards shortcuts dialog, as well as the actual hotkeys themselves.
 */
export interface KeyboardShortcut {
  /** Human readable, short description of what the keyboard shortcut does */
  description: string;

  /** If provided, gets displayed in an info popover */
  helpText?: string;

  /** The actual hotkey combo(s) that trigger this event */
  hotkeys: string;

  /** A list of search terms that should be considered matches */
  tags?: string[];

  /** Groups like hotkeys. If it is scoped to a display, use the display name */
  category?: string;
}

/**
 * The configuration object describing the keyboard shortcuts.
 */
export interface KeyboardShortcutConfig {
  [key: string]: KeyboardShortcut;
}

/**
 * Interface for the UI Processing Configuration
 */
export interface ProcessingAnalystConfiguration {
  readonly defaultNetwork: string;
  readonly defaultInteractiveAnalysisStationGroup: string;
  readonly defaultFilters: WaveformFilter[];
  readonly currentIntervalEndTime: number;
  readonly currentIntervalDuration: number;
  readonly maximumOpenAnythingDuration: number;
  readonly fixedAmplitudeScaleValues: number[];
  readonly leadBufferDuration: number;
  readonly lagBufferDuration: number;
  readonly uiThemes: UITheme[];
  readonly priorityPhases: string[];
  readonly zasDefaultAlignmentPhase: CommonTypes.PhaseType;
  readonly zasZoomInterval: number;
  readonly unassociatedSignalDetectionLengthMeters: number;
  readonly minimumRequestDuration: number;
  readonly waveformPanningBoundaryDuration: number;
  readonly waveformPanRatio: number;
  readonly workflow: {
    readonly panSingleArrow: number;
    readonly panDoubleArrow: number;
  };
  readonly endpointConfigurations: {
    readonly maxParallelRequests: number;
    readonly getEventsWithDetectionsAndSegmentsByTime: {
      readonly maxTimeRangeRequestInSeconds: number;
    };
  };
  readonly keyboardShortcuts: KeyboardShortcutConfig;
}

/**
 * Common configuration
 */
export interface ProcessingCommonConfiguration {
  readonly systemMessageLimit: number;
}

/**
 * Interface for the Operational Time Period Configuration
 */
export interface OperationalTimePeriodConfiguration {
  readonly operationalPeriodStart: number;
  readonly operationalPeriodEnd: number;
}

/**
 * Interface for the Station Group Names Configuration
 */
export interface StationGroupNamesConfiguration {
  readonly stationGroupNames: string[];
}

/**
 * Soh specific configuration from the Java backend endpoint
 */
export interface SohConfiguration {
  stationSohControlConfiguration: {
    readonly reprocessingPeriod: string;
    readonly displayedStationGroups: string[];
    readonly rollupStationSohTimeTolerance: string;
  };
  stationSohMonitoringDisplayParameters: {
    readonly redisplayPeriod: string;
    readonly acknowledgementQuietDuration: string;
    readonly availableQuietDurations: string[];
    readonly sohStationStaleDuration: string;
    readonly sohHistoricalDurations: string[];
    readonly samplesPerChannel: number;
    readonly maxQueryIntervalSize: number;
  };
}

/**
 * UI Soh specific configuration converted from SohConfiguration
 */
export interface UiSohConfiguration {
  readonly reprocessingPeriodSecs: number;
  readonly displayedStationGroups: string[];
  readonly rollupStationSohTimeToleranceMs: number;
  readonly redisplayPeriodMs: number;
  readonly acknowledgementQuietMs: number;
  readonly availableQuietTimesMs: number[];
  readonly sohStationStaleMs: number;
  readonly sohHistoricalTimesMs: number[];
  readonly historicalSamplesPerChannel: number;
  readonly maxHistoricalQueryIntervalSizeMs: number;
}

/**
 * SOH StationGroup and Priority interface definition
 */
export interface SOHStationGroupNameWithPriority {
  name: string;
  priority: number;
}

/**
 * Selector interface for config service
 */
export interface Selector {
  criterion: string;
  value: string;
}

/**
 * Analyst configurations loaded from service
 */
export enum AnalystConfigs {
  DEFAULT = 'ui.analyst-settings'
}

/**
 * Common configurations loaded from service
 */
export enum CommonConfigs {
  DEFAULT = 'ui.common-settings'
}

/**
 * Operational time periods loaded from service
 */
export enum OperationalTimePeriodConfigs {
  DEFAULT = 'global.operational-time-period'
}

/**
 * SOH configurations loaded from service
 */
export const SohConfig = 'ui.soh-settings';

/**
 * IAN Station Definition station group names loaded from service
 */
export enum StationGroupNamesConfig {
  DEFAULT = 'station-definition-manager.station-group-names'
}

/**
 * SOH Control station group names loaded from service
 */
export enum SohControlStationGroupNamesConfig {
  DEFAULT = 'soh-control.station-group-names'
}

/**
 * UI Analyst Processing Configuration Default Values
 */

export const defaultUnassociatedSignalDetectionLengthMeters = 11100000;

/**
 * The default colors for the fallback @interface UITheme (what is loaded if no theme is found).
 */
export const defaultColorTheme: ColorTheme = {
  gmsMain: '#f5f8fa',
  gmsMainInverted: '#10161a',
  gmsBackground: '#182026',
  gmsSelection: '#1589d1',
  gmsTableSelection: '#f5f8fa',
  mapVisibleStation: '#D9822B',
  mapStationDefault: '#6F6E74',
  waveformDimPercent: 0.75,
  waveformFilterLabel: '#f5f8fa',
  waveformRaw: '#4580e6',
  unassociatedSDColor: '#C23030',
  openEventSDColor: '#C87619',
  completeEventSDColor: '#62D96B',
  otherEventSDColor: '#FFFFFF',
  predictionSDColor: '#C58C1B'
};
