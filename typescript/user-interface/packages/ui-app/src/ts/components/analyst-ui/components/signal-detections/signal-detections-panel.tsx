import type { CommonTypes, EventTypes, SignalDetectionTypes } from '@gms/common-model';
import type { EventStatus } from '@gms/ui-state';
import {
  SignalDetectionColumn,
  signalDetectionsActions,
  useAppDispatch,
  useAppSelector,
  useEventStatusQuery,
  useGetEvents,
  useViewableInterval
} from '@gms/ui-state';
import Immutable from 'immutable';
import React from 'react';

import {
  nonIdealStateLoadingSignalDetections,
  nonIdealStateSelectAnInterval
} from '~analyst-ui/components/signal-detections/signal-detections-non-ideal-states';
import { buildSignalDetectionRows } from '~analyst-ui/components/signal-detections/table/signal-detections-table-utils';
import { SignalDetectionsToolbar } from '~analyst-ui/components/signal-detections/toolbar/signal-detections-toolbar';

import { convertMapToObject } from '../../../common-ui/common/table-utils';
import { SignalDetectionsTable } from './table/signal-detections-table';
import type { SignalDetectionRow, SignalDetectionsPanelProps } from './types';

/**
 * Takes the column definition records from redux and converts it to a {@link Immutable.Map}.
 */
const convertObjectToSDColumnMap = (
  columnArguments: Record<string, boolean>
): Immutable.Map<SignalDetectionColumn, boolean> => {
  const notableValues = [...Object.keys(columnArguments)];
  return Immutable.Map<SignalDetectionColumn, boolean>([
    ...Object.values(SignalDetectionColumn)
      .filter(v => notableValues.includes(v))
      .map<[SignalDetectionColumn, boolean]>(v => [v, columnArguments[v]])
  ]);
};

/**
 * Returns a memoized list of {@link SignalDetectionRow}s
 *
 * @param signalDetections Data is used as the basis for each row
 * @param events Used to query for association status
 * @param openEventId Used to query for association status
 * @param eventsStatuses Used to query for association status
 * @param timeRange Used to determine if SD is an edge SD or not
 */
const useSignalDetectionRows = (
  signalDetections: SignalDetectionTypes.SignalDetection[],
  events: EventTypes.Event[],
  eventsStatuses: Record<string, EventStatus>,
  openEventId: string,
  timeRange: CommonTypes.TimeRange
) => {
  return React.useMemo(() => {
    return buildSignalDetectionRows(
      signalDetections,
      events,
      eventsStatuses,
      openEventId,
      timeRange
    );
  }, [signalDetections, events, eventsStatuses, openEventId, timeRange]);
};

/**
 * IAN signal detections component.
 */
// eslint-disable-next-line react/function-component-definition
export const SignalDetectionsPanelComponent: React.FunctionComponent<SignalDetectionsPanelProps> = (
  props: SignalDetectionsPanelProps
) => {
  const { signalDetectionsQuery } = props;
  const dispatch = useAppDispatch();
  const isSynced = useAppSelector(
    state => state.app.signalDetections.displayedSignalDetectionConfiguration.syncWaveform
  );

  const currentIntervalWithBuffer = useViewableInterval()[0];

  const selectedSDColumnsToDisplayObject = useAppSelector(
    state => state.app.signalDetections.signalDetectionsColumns
  );

  const selectedSDColumnsToDisplay = React.useMemo(
    () => convertObjectToSDColumnMap(selectedSDColumnsToDisplayObject),
    [selectedSDColumnsToDisplayObject]
  );
  const setSelectedSDColumnsToDisplay = React.useCallback(
    (cols: Immutable.Map<SignalDetectionColumn, boolean>) =>
      dispatch(signalDetectionsActions.updateSignalDetectionColumns(convertMapToObject(cols))),
    [dispatch]
  );

  const eventResults = useGetEvents();
  const eventStatusQuery = useEventStatusQuery();
  const openEventId = useAppSelector(state => state.app.analyst.openEventId);
  const timeRange = useAppSelector(state => state.app.workflow.timeRange);

  const rowData: SignalDetectionRow[] = useSignalDetectionRows(
    signalDetectionsQuery?.data,
    eventResults.data,
    eventStatusQuery.data,
    openEventId,
    timeRange
  );

  // non ideal state checks
  if (
    !currentIntervalWithBuffer ||
    currentIntervalWithBuffer?.startTimeSecs === null ||
    currentIntervalWithBuffer?.endTimeSecs === null
  )
    return nonIdealStateSelectAnInterval;
  if (signalDetectionsQuery.isLoading || !signalDetectionsQuery.data)
    return nonIdealStateLoadingSignalDetections;

  return (
    <div className="signalDetectionPanel" data-cy="signal-detection-panel">
      <SignalDetectionsToolbar
        key="sdtoolbar"
        selectedSDColumnsToDisplay={selectedSDColumnsToDisplay}
        setSelectedSDColumnsToDisplay={setSelectedSDColumnsToDisplay}
      />
      <SignalDetectionsTable
        key="sdtable"
        isSynced={isSynced}
        signalDetectionsQuery={signalDetectionsQuery}
        data={rowData}
        columnsToDisplay={selectedSDColumnsToDisplay}
      />
    </div>
  );
};

export const SignalDetectionsPanel = React.memo(SignalDetectionsPanelComponent);
