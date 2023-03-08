import type { CommonTypes } from '@gms/common-model';
import { EventTypes } from '@gms/common-model';
import type { Table } from '@gms/ui-core-components';
import type { AppDispatch, EventStatus, UpdateEventStatusMutationFunc } from '@gms/ui-state';
import {
  analystActions,
  AnalystWorkspaceTypes,
  useAppDispatch,
  useAppSelector,
  useGetProcessingAnalystConfigurationQuery,
  useUpdateEventStatusMutation,
  waveformActions
} from '@gms/ui-state';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import React from 'react';

import { setRowNodeSelection } from '~common-ui/common/table-utils';

import type { EventRow } from './types';
import { EdgeTypes } from './types';

/**
 * Dispatches the openEventId to the open event or null along will call mutation
 * to update the event status
 *
 * @param eventStatus the event status to update for the event
 * @param updateEventStatusMutation mutation for the update
 */
export const setEventStatus = (
  eventStatus: EventTypes.EventStatus,
  userName: string,
  stageName: string,
  configuredPhase: CommonTypes.PhaseType,
  dispatch: AppDispatch,
  updateEventMutation: UpdateEventStatusMutationFunc
) => async (id: string): Promise<void> => {
  const eventStatusRequestData: EventStatus = {
    stageId: {
      name: stageName
    },
    eventId: id,
    eventStatusInfo: {
      eventStatus,
      activeAnalystIds: [userName]
    }
  };
  switch (eventStatus) {
    case EventTypes.EventStatus.NOT_COMPLETE:
    case EventTypes.EventStatus.COMPLETE:
      dispatch(analystActions.setOpenEventId(null));
      dispatch(
        analystActions.setSelectedSortType(AnalystWorkspaceTypes.WaveformSortType.stationNameAZ)
      );
      dispatch(analystActions.setAlignWaveformsOn(AlignWaveformsOn.TIME));
      break;
    default:
      dispatch(analystActions.setOpenEventId(id));
      dispatch(analystActions.setSelectedSortType(AnalystWorkspaceTypes.WaveformSortType.distance));
      dispatch(analystActions.setAlignWaveformsOn(AlignWaveformsOn.PREDICTED_PHASE));
      dispatch(analystActions.setPhaseToAlignOn(configuredPhase));
  }

  // update Redux to show predicted phases in waveform display by default
  dispatch(waveformActions.setShouldShowPredictedPhases(true));

  await updateEventMutation(eventStatusRequestData);
};

/**
 * Hook used as a helper for updating event status. Gets the username from the store
 *
 * @param eventStatus to update
 * @returns higher order function to perform the redux dispatch and mutation
 */
export const useSetEvent = (
  eventStatus: EventTypes.EventStatus
): ((id: string) => Promise<void>) => {
  const dispatch = useAppDispatch();
  const [updateEventStatusMutation] = useUpdateEventStatusMutation();
  const processingAnalystConfiguration = useGetProcessingAnalystConfigurationQuery();
  const userName = useAppSelector(state => state.app.userSession.authenticationStatus.userName);
  const stageName = useAppSelector(state => state.app.workflow.openIntervalName);
  return React.useMemo(
    () =>
      setEventStatus(
        eventStatus,
        userName,
        stageName,
        processingAnalystConfiguration.data?.zasDefaultAlignmentPhase,
        dispatch,
        updateEventStatusMutation
      ),
    [
      dispatch,
      eventStatus,
      processingAnalystConfiguration.data?.zasDefaultAlignmentPhase,
      stageName,
      updateEventStatusMutation,
      userName
    ]
  );
};

/**
 * Opens an event and updates the redux state with the open event id
 * Hits a endpoint to update the event status with in progress and username
 */
export const useSetOpenEvent = (): ((id: string) => Promise<void>) => {
  return useSetEvent(EventTypes.EventStatus.IN_PROGRESS);
};

/**
 * Closes an event and updates the redux state to have no open event id
 * Hits a endpoint to update the event status with no complete and removes username
 */
export const useSetCloseEvent = (): ((id: string) => Promise<void>) => {
  return useSetEvent(EventTypes.EventStatus.NOT_COMPLETE);
};

/**
 * Build a row for the event display table.
 *
 * @param eventId The Id for the event
 * @param eventHypothesis The preferred event hypothesis for the currently open stage
 * @param timeRange The open interval time range (used to determine if this is an edge event and should be displayed or not)
 * @param findEventStatusQueryData event status query data
 * @param isOpen boolean flag for if this is the currently open event
 * @returns
 */
export const buildEventRow = (
  eventId: string,
  eventHypothesis: EventTypes.EventHypothesis,
  locationSolutionId: string,
  timeRange: CommonTypes.TimeRange,
  findEventStatusQueryData: Record<string, EventStatus>,
  isOpen: boolean
): EventRow => {
  const locationSolution = eventHypothesis.locationSolutions?.find(
    ls => ls.id === locationSolutionId
  );

  const magnitude: Record<string, number> = {};

  locationSolution?.networkMagnitudeSolutions.forEach(netMag => {
    magnitude[netMag.type] = netMag.magnitude.value;
  });

  const ellipsisCoverage = locationSolution?.locationUncertainty?.ellipses.find(
    value => value.scalingFactorType === EventTypes.ScalingFactorType.COVERAGE
  );

  const ellipsisConfidence = locationSolution?.locationUncertainty?.ellipses.find(
    value => value.scalingFactorType === EventTypes.ScalingFactorType.CONFIDENCE
  );

  let edgeEventType;
  if (locationSolution?.location?.time < timeRange.startTimeSecs) {
    edgeEventType = EdgeTypes.BEFORE;
  } else if (locationSolution?.location?.time > timeRange.endTimeSecs) {
    edgeEventType = EdgeTypes.AFTER;
  } else {
    edgeEventType = EdgeTypes.INTERVAL;
  }

  return {
    id: eventId,
    edgeEventType,
    time: locationSolution?.location?.time,
    activeAnalysts: findEventStatusQueryData
      ? findEventStatusQueryData[eventId]?.eventStatusInfo?.activeAnalystIds ?? []
      : [],
    conflict: false,
    depthKm: locationSolution?.location?.depthKm,
    latitudeDegrees: locationSolution?.location?.latitudeDegrees,
    longitudeDegrees: locationSolution?.location?.longitudeDegrees,
    magnitudeMb: magnitude[EventTypes.MagnitudeType.MB],
    magnitudeMs: magnitude[EventTypes.MagnitudeType.MS],
    magnitudeMl: magnitude[EventTypes.MagnitudeType.ML],
    confidenceSemiMajorAxis: ellipsisConfidence?.semiMajorAxisLengthKm,
    confidenceSemiMinorAxis: ellipsisConfidence?.semiMinorAxisLengthKm,
    coverageSemiMajorAxis: ellipsisCoverage?.semiMajorAxisLengthKm,
    coverageSemiMinorAxis: ellipsisCoverage?.semiMinorAxisLengthKm,
    preferred: 'TBD',
    region: 'TBD',
    status: findEventStatusQueryData
      ? findEventStatusQueryData[eventId]?.eventStatusInfo?.eventStatus
      : undefined,
    isOpen,
    rejected: eventHypothesis.rejected ? 'True' : 'False'
  };
};

/**
 * Cycles through all rows in the table and updates their row selection attribute
 * Sets rowSelection to true if their row id is in {@param selectedEvents}, false otherwise
 *
 * @param tableRef
 * @param selectedEvents
 */
export function updateRowSelection(
  tableRef: React.MutableRefObject<Table<EventRow, unknown>>,
  selectedEvents: string[]
): React.MutableRefObject<Table<EventRow, unknown>> {
  const tableApi = tableRef?.current?.getTableApi();
  if (tableApi) {
    tableApi.forEachNode(node => setRowNodeSelection(node, selectedEvents.includes(node.id)));
    return tableRef;
  }
  return null;
}
