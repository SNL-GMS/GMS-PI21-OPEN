import type { EventTypes } from '@gms/common-model';
import type { CellRendererParams, Row } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import Immutable from 'immutable';

/**
 * Event Table generic CellRendererParams type
 */
export type EventTableCellRendererParams<T> = CellRendererParams<
  EventRow,
  unknown,
  T,
  unknown,
  unknown
>;

/**
 * used to match the display strings to values in the site table column picker dropdown
 */
export const columnDisplayStrings: Immutable.Map<EventsColumn, string> = Immutable.Map<
  EventsColumn,
  string
>([
  [EventsColumn.conflict, 'Conflict'],
  [EventsColumn.time, 'Time'],
  [EventsColumn.latitudeDegrees, 'Lat (°)'],
  [EventsColumn.longitudeDegrees, 'Lon (°)'],
  [EventsColumn.depthKm, 'Depth (km)'],
  [EventsColumn.magnitudeMb, 'mb'],
  [EventsColumn.magnitudeMs, 'ms'],
  [EventsColumn.magnitudeMl, 'ml'],
  [EventsColumn.coverageSemiMajorAxis, 'Coverage semi-major'],
  [EventsColumn.coverageSemiMinorAxis, 'Coverage semi-minor'],
  [EventsColumn.confidenceSemiMajorAxis, 'Confidence semi-major'],
  [EventsColumn.confidenceSemiMinorAxis, 'Confidence semi-minor'],
  [EventsColumn.region, 'Region'],
  [EventsColumn.activeAnalysts, 'Active analysts'],
  [EventsColumn.preferred, 'Preferred in stage'],
  [EventsColumn.status, 'Workflow status'],
  [EventsColumn.rejected, 'Rejected']
]);

/**
 * Enumerated time types based on whether event or signal detection is within the interval or a before/after edge event
 */
export enum EdgeTypes {
  BEFORE = 'Before',
  AFTER = 'After',
  INTERVAL = 'Interval'
}

/**
 * Event row for event display
 */
export interface EventRow extends Row {
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
  readonly magnitudeMb: number;
  readonly magnitudeMs: number;
  readonly magnitudeMl: number;
  readonly activeAnalysts: string[];
  readonly preferred: string;
  readonly status: EventTypes.EventStatus;
  readonly rejected: string;
}
