import type { CommonTypes } from '@gms/common-model';
import { EventTypes, SignalDetectionTypes } from '@gms/common-model';
import { formatTimeForDisplay } from '@gms/common-util';
import type { AgGridCommunity, RowNode } from '@gms/ui-core-components';
import type {
  DisplayedSignalDetectionConfigurationEnum,
  EventStatus,
  SignalDetectionFetchResult
} from '@gms/ui-state';
import { SignalDetectionColumn } from '@gms/ui-state';
import type { AgGridReact } from 'ag-grid-react';
import Immutable from 'immutable';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import { getSignalDetectionAssociationStatus } from '~analyst-ui/common/utils/event-util';
import {
  findAmplitudeFeatureMeasurementValue,
  findAzimuthFeatureMeasurementValue,
  findEmergenceAngleFeatureMeasurementValue,
  findFeatureMeasurementChannelName,
  findLongPeriodFirstMotionFeatureMeasurementValue,
  findRectilinearityFeatureMeasurementValue,
  findShortPeriodFirstMotionFeatureMeasurementValue,
  findSlownessFeatureMeasurementValue
} from '~analyst-ui/common/utils/signal-detection-util';
import { EdgeTypes } from '~analyst-ui/components/events/types';
import type { SignalDetectionRow } from '~analyst-ui/components/signal-detections/types';
import { messageConfig } from '~analyst-ui/config/message-config';
import {
  formatNumberForDisplayFixedThreeDecimalPlaces,
  getTableCellStringValue,
  setRowNodeSelection
} from '~common-ui/common/table-utils';

import {
  nonIdealStateLoadingSignalDetections,
  nonIdealStateNoSignalDetections,
  nonIdealStateNoSignalDetectionsErrorState,
  nonIdealStateNoSignalDetectionsSyncedTimeRange
} from '../signal-detections-non-ideal-states';

/**
 * Determines which non-ideal state, if any, should be presented.
 *
 * @returns NonIdealState, or undefined if not needed.
 */
export const handleNonIdealState = (
  signalDetectionsQuery: SignalDetectionFetchResult,
  isSynced: boolean
): JSX.Element | undefined => {
  if (!!signalDetectionsQuery.pending || !signalDetectionsQuery.data)
    return nonIdealStateLoadingSignalDetections;

  if (
    !signalDetectionsQuery.isLoading &&
    !signalDetectionsQuery.isError &&
    !signalDetectionsQuery.pending &&
    signalDetectionsQuery.data?.length === 0
  )
    return isSynced
      ? nonIdealStateNoSignalDetectionsSyncedTimeRange
      : nonIdealStateNoSignalDetections;

  if (signalDetectionsQuery.isError && signalDetectionsQuery.data?.length === 0)
    return nonIdealStateNoSignalDetectionsErrorState;

  return undefined;
};

/**
 * Given a timeRange, determines if the provided arrivalTime falls within that range
 * Returns corresponding {@link edgeType}
 *
 * @param timeRange if not defined, return Unknown
 * @param arrivalTime if not defined, return Unknown
 */
export function getEdgeType(
  timeRange: CommonTypes.TimeRange,
  arrivalTime: number
): string | EdgeTypes {
  if (
    (!arrivalTime && arrivalTime !== 0) ||
    !timeRange ||
    (!timeRange?.startTimeSecs && timeRange.startTimeSecs !== 0) ||
    (!timeRange?.endTimeSecs && timeRange.endTimeSecs !== 0)
  )
    return messageConfig.invalidCellText;
  if (arrivalTime < timeRange.startTimeSecs) return EdgeTypes.BEFORE;
  if (arrivalTime > timeRange.endTimeSecs) return EdgeTypes.AFTER;
  return EdgeTypes.INTERVAL;
}

/**
 * in charge of setting the row class that dims edge event rows
 */
export const edgeSDRowClassRules: AgGridCommunity.RowClassRules = {
  'edge-SD-row': params => params.data.edgeType !== EdgeTypes.INTERVAL
};

/**
 * This is the set of default columns to be displayed in the SD table
 * This object gets updated when columns are selected/deselected in the column picker so that state
 * doesn't get lost
 */
export const signalDetectionsColumnsToDisplay: Immutable.Map<
  SignalDetectionColumn,
  boolean
> = Immutable.Map([
  ...Object.values(SignalDetectionColumn).map<[SignalDetectionColumn, boolean]>(v => [v, true]),
  [SignalDetectionColumn.phaseConfidence, false],
  [SignalDetectionColumn.rectilinearity, false],
  [SignalDetectionColumn.emergenceAngle, false],
  [SignalDetectionColumn.shortPeriodFirstMotion, false],
  [SignalDetectionColumn.longPeriodFirstMotion, false]
]);

/**
 * These two fields have in-band error codes, if we see a '-1', we need to instead display "Unknown"
 * these fields are otherwise treated as the rest of the numeric fields
 *
 * @param value
 */
export function formatRectilinearityOrEmergenceForDisplay(value: number): string {
  if (value === -1) return 'Unknown';
  return formatNumberForDisplayFixedThreeDecimalPlaces(value);
}

/**
 * Called by ag-grid to determine if an external filter is present/active.
 */
export function agGridIsExternalFilterPresent(
  signalDetectionFilterState: Record<DisplayedSignalDetectionConfigurationEnum, boolean>
): boolean {
  const {
    signalDetectionUnassociated,
    signalDetectionAssociatedToOpenEvent,
    signalDetectionAssociatedToCompletedEvent,
    signalDetectionAssociatedToOtherEvent,
    signalDetectionBeforeInterval,
    signalDetectionAfterInterval
  } = signalDetectionFilterState;

  // If any of the event associations are unchecked then the table must be filtered.
  return (
    !signalDetectionUnassociated ||
    !signalDetectionAssociatedToOpenEvent ||
    !signalDetectionAssociatedToCompletedEvent ||
    !signalDetectionAssociatedToOtherEvent ||
    !signalDetectionBeforeInterval ||
    !signalDetectionAfterInterval
  );
}

/**
 * Passed to ag-grid. Should return true if external filter passes, otherwise false.
 */
export function agGridDoesExternalFilterPass(
  node: RowNode,
  signalDetectionFilterState: Record<DisplayedSignalDetectionConfigurationEnum, boolean>
): boolean {
  const {
    signalDetectionUnassociated,
    signalDetectionAssociatedToOpenEvent,
    signalDetectionAssociatedToCompletedEvent,
    signalDetectionAssociatedToOtherEvent,
    signalDetectionBeforeInterval,
    signalDetectionAfterInterval
  } = signalDetectionFilterState;

  if (!signalDetectionAfterInterval && node.data.edgeType === EdgeTypes.AFTER) {
    return false;
  }
  if (!signalDetectionBeforeInterval && node.data.edgeType === EdgeTypes.BEFORE) {
    return false;
  }

  switch ((node.data as SignalDetectionRow).assocStatus) {
    case EventTypes.AssociationStatus.COMPLETE_ASSOCIATED:
      return signalDetectionAssociatedToCompletedEvent;
    case EventTypes.AssociationStatus.OPEN_ASSOCIATED:
      return signalDetectionAssociatedToOpenEvent;
    case EventTypes.AssociationStatus.OTHER_ASSOCIATED:
      return signalDetectionAssociatedToOtherEvent;
    case EventTypes.AssociationStatus.UNASSOCIATED:
      return signalDetectionUnassociated;
    default:
      return true;
  }
}

/**
 * Builds a single Signal detection row given a signalDetection object
 *
 * @param sd Signal detection data for which the row is built
 * @param events Used to query for association status
 * @param openEventId Used to query for association status
 * @param eventsStatuses Used to query for association status
 * @param timeRange Used to determine if SD is an edge SD or not
 */
export function buildSignalDetectionRow(
  sd: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[],
  eventsStatuses: Record<string, EventStatus>,
  openEventId: string,
  timeRange: CommonTypes.TimeRange
): SignalDetectionRow {
  const featureMeasurements = SignalDetectionTypes.Util.getCurrentHypothesis(
    sd.signalDetectionHypotheses
  )?.featureMeasurements;
  return {
    id: sd.id,
    unsavedChanges: 'TBD',
    assocStatus: getSignalDetectionAssociationStatus(sd, events, openEventId, eventsStatuses),
    conflict: 'TBD',
    station: getTableCellStringValue(sd?.station.name),
    channel: findFeatureMeasurementChannelName(featureMeasurements),
    phase: getTableCellStringValue(
      SignalDetectionUtils.findPhaseFeatureMeasurementValue(featureMeasurements)?.value
    ),
    phaseConfidence: formatNumberForDisplayFixedThreeDecimalPlaces(
      SignalDetectionUtils.findPhaseFeatureMeasurementValue(featureMeasurements)?.confidence
    ),
    time: formatTimeForDisplay(
      SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(featureMeasurements)
        ?.arrivalTime?.value
    ),
    timeStandardDeviation: formatNumberForDisplayFixedThreeDecimalPlaces(
      SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(featureMeasurements)
        ?.arrivalTime?.standardDeviation
    ),
    azimuth: formatNumberForDisplayFixedThreeDecimalPlaces(
      findAzimuthFeatureMeasurementValue(featureMeasurements)?.measuredValue?.value
    ),
    azimuthStandardDeviation: formatNumberForDisplayFixedThreeDecimalPlaces(
      findAzimuthFeatureMeasurementValue(featureMeasurements)?.measuredValue?.standardDeviation
    ),
    slowness: formatNumberForDisplayFixedThreeDecimalPlaces(
      findSlownessFeatureMeasurementValue(featureMeasurements)?.measuredValue?.value
    ),
    slownessStandardDeviation: formatNumberForDisplayFixedThreeDecimalPlaces(
      findSlownessFeatureMeasurementValue(featureMeasurements)?.measuredValue?.standardDeviation
    ),
    amplitude: formatNumberForDisplayFixedThreeDecimalPlaces(
      findAmplitudeFeatureMeasurementValue(
        featureMeasurements,
        SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE_A5_OVER_2
      )?.amplitude?.value
    ),
    period: formatNumberForDisplayFixedThreeDecimalPlaces(
      findAmplitudeFeatureMeasurementValue(
        featureMeasurements,
        SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE_A5_OVER_2
      )?.period
    ),
    sNR: formatNumberForDisplayFixedThreeDecimalPlaces(
      SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(featureMeasurements)?.snr?.value
    ),
    rectilinearity: formatRectilinearityOrEmergenceForDisplay(
      findRectilinearityFeatureMeasurementValue(featureMeasurements)?.measuredValue?.value
    ),
    emergenceAngle: formatRectilinearityOrEmergenceForDisplay(
      findEmergenceAngleFeatureMeasurementValue(featureMeasurements)?.measuredValue?.value
    ),
    shortPeriodFirstMotion: getTableCellStringValue(
      findShortPeriodFirstMotionFeatureMeasurementValue(featureMeasurements)?.value
    ),
    longPeriodFirstMotion: getTableCellStringValue(
      findLongPeriodFirstMotionFeatureMeasurementValue(featureMeasurements)?.value
    ),
    rejected: SignalDetectionTypes.Util.getCurrentHypothesis(sd?.signalDetectionHypotheses).rejected
      ? 'True'
      : 'False',
    edgeType: getEdgeType(
      timeRange,
      SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(featureMeasurements)
        ?.arrivalTime?.value
    )
  };
}

/**
 * Given an array of signal detections, builds rows for the SD table
 * returns an empty array when given null/empty/undefined input
 *
 * @param signalDetections Data is used as the basis for each row
 * @param events Used to query for association status
 * @param openEventId Used to query for association status
 * @param eventsStatuses Used to query for association status
 * @param timeRange Used to determine if SD is an edge SD or not
 */
export function buildSignalDetectionRows(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  events: EventTypes.Event[],
  eventsStatuses: Record<string, EventStatus>,
  openEventId: string,
  timeRange: CommonTypes.TimeRange
): SignalDetectionRow[] {
  if (!signalDetections || signalDetections.length === 0) return [];

  return signalDetections.map(sd =>
    buildSignalDetectionRow(sd, events, eventsStatuses, openEventId, timeRange)
  );
}

/**
 * Given a table ref, checks to make sure the table exists and then updates the displayed columns
 * to match {@param columnsToDisplay}
 *
 * @param tableRef
 * @param columnsToDisplay
 */
export function updateColumns(
  tableRef: React.MutableRefObject<AgGridReact>,
  columnsToDisplay: Immutable.Map<SignalDetectionColumn, boolean>
): Immutable.Map<SignalDetectionColumn, boolean> | undefined {
  if (tableRef?.current?.columnApi) {
    columnsToDisplay.forEach((shouldDisplay, columnName) => {
      tableRef.current?.columnApi.setColumnVisible(columnName, shouldDisplay);
    });
    return columnsToDisplay;
  }
  return undefined;
}

/**
 * Cycles through all rows in the table and updates their row selection attribute
 * Sets rowSelection to true if their row id is in {@param selectedSdIds}, false otherwise
 *
 * @param tableRef
 * @param selectedSdIds
 */
export function updateRowSelection(
  tableRef: React.MutableRefObject<AgGridReact>,
  selectedSdIds: string[]
): React.MutableRefObject<AgGridReact> {
  tableRef.current.api.forEachNode(node =>
    setRowNodeSelection(node, selectedSdIds.includes(node.id))
  );
  return tableRef;
}
