import type {
  CommonTypes,
  EventTypes,
  LegacyEventTypes,
  SignalDetectionTypes,
  WorkflowTypes
} from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type { CheckboxListEntry } from '@gms/ui-core-components';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import type * as Cesium from 'cesium';
import type { CesiumMovementEvent } from 'resium';

import type { EdgeTypes, EventRow } from '../events/types';

/**
 * Mutations used in the map
 */
export interface MapMutations {
  updateEvents: (args: any) => Promise<void>;
  rejectDetections: (args: any) => Promise<void>;
  changeSignalDetectionAssociations: (args: any) => Promise<void>;
  createEvent: (args: any) => Promise<void>;
  updateDetections: (args: any) => Promise<void>;
}

export interface IANMapComponentProps {
  // passed in from golden-layout
  glContainer?: GoldenLayout.Container;
}

/**
 * Props mapped in from Redux state
 */
export interface MapReduxProps {
  // passed in from golden-layout
  glContainer?: GoldenLayout.Container;
  currentTimeInterval: CommonTypes.TimeRange;
  selectedEventIds: string[];
  openEventId: string;
  selectedSdIds: string[];
  analysisMode: WorkflowTypes.AnalysisMode;
  measurementMode: AnalystWorkspaceTypes.MeasurementMode;
  sdIdsToShowFk: string[];
  unassociatedSDColor: string;

  // callbacks
  setSelectedEventIds(eventIds: string[]): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
  setSelectedSdIds(SdIds: string[]): void;
  setOpenEventId(
    event: LegacyEventTypes.Event | undefined,
    latestLocationSolutionSet: LegacyEventTypes.LocationSolutionSet | undefined,
    preferredLocationSolutionId: string | undefined
  ): void;
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
}

/**
 * entities to be converted into a custom ian-map-data-source, such that each entity contains the
 * provided handlers. The provided name will be assigned to the resulting datasource.
 */
export interface IanMapDataSourceProps {
  key: string;
  entities: Cesium.Entity[];
  leftClickHandler?: (targetEntity: Cesium.Entity) => () => void;
  rightClickHandler?: (movement: CesiumMovementEvent, target: Cesium.Entity) => void;
  doubleClickHandler?: (movement: CesiumMovementEvent, target: Cesium.Entity) => void;
  name: string;
  onMount?: () => void;
  show: boolean;
}

export enum LayerTooltips {
  Events = 'Seismic Event',
  Stations = 'Station',
  Assoc = 'Signal Detections associated to currently open event',
  OtherAssoc = 'Signal Detections associated to events that are not open',
  UnAssociated = 'Signal Detections unassociated from all events'
}
export enum LayerLabels {
  Events = 'Events',
  Stations = 'Stations',
  Assoc = 'Open Assoc.',
  OtherAssoc = 'Other Assoc.',
  UnAssociated = 'Unassociated'
}

export interface IANReduxProps {
  glContainer?: GoldenLayout.Container;
}

/**
 * settingsEntries refers to an array of CheckboxListEntries that make up the body of the settings panel
 * onCheckedCallback takes the name of the checked/unchecked box as an argument
 */
export interface MapLayerSettingsPopoutProps {
  settingsEntries: CheckboxListEntry[];
  onCheckedCallback;
}

/**
 * Event source for map display
 * Extending EventRow type with additional semi-major axis trend property
 */
export interface MapEventSource extends EventRow {
  readonly id: string;
  readonly edgeEventType: EdgeTypes;
  readonly isOpen: boolean;
  readonly conflict: boolean;
  readonly time: number;
  readonly latitudeDegrees: number;
  readonly longitudeDegrees: number;
  readonly depthKm: number;
  readonly region: string;
  readonly confidenceSemiMajorAxis: number;
  readonly confidenceSemiMinorAxis: number;
  readonly coverageSemiMajorAxis: number;
  readonly coverageSemiMinorAxis: number;
  readonly coverageSemiMajorAxisTrend?: number;
  readonly confidenceSemiMajorAxisTrend?: number;
  readonly magnitudeMb: number;
  readonly magnitudeMs: number;
  readonly magnitudeMl: number;
  readonly activeAnalysts: string[];
  readonly preferred: string;
  readonly status: EventTypes.EventStatus;
}

/**
 * Values to be added to signal detection entity properties
 */
export interface MapSDEntityValues {
  readonly detectionTime: {
    detectionTimeValue: string;
    detectionTimeUncertainty: number;
  };
  readonly azimuth: {
    azimuthValue: number;
    azimuthUncertainty: number;
  };
  readonly slowness: {
    slownessValue: number;
    slownessUncertainty: number;
  };
  readonly phaseValue: SignalDetectionTypes.PhaseTypeMeasurementValue;
  readonly associatedEventTimeValue: string;
  readonly signalDetectionColor: string;
  readonly status: string;
  readonly edgeSDType: EdgeTypes;
  readonly stationName: string;
}

export interface MapSDFormValues {
  readonly phaseValue: string;
  readonly stationName: string;
  readonly detectionTimeValue: string;
  readonly detectionTimeUncertainty: string;
  readonly azimuthValue: string;
  readonly azimuthUncertainty: string;
  readonly slownessValue: string;
  readonly slownessUncertainty: string;
  readonly status: string;
}

/**
 * Consolidating signal detection status and edge type for ease of signal detection processing
 */
export interface MapSDConditions {
  readonly status: string;
  readonly edgeSDType: EdgeTypes;
}
