import type { CommonTypes, SignalDetectionTypes, StationTypes } from '@gms/common-model';
import { EventTypes } from '@gms/common-model';
import {
  analystActions,
  mapActions,
  setSelectedStationIds,
  useAppDispatch,
  useAppSelector,
  useEventStatusQuery,
  useGetAllStationsQuery,
  useGetEvents,
  useGetSignalDetections,
  useOldQueryDataIfReloading,
  useWorkflowQuery
} from '@gms/ui-state';
import type Cesium from 'cesium';
import { JulianDate } from 'cesium';
import React from 'react';

import {
  applyEventMultiSelectionLogic,
  applySdMultiSelectionLogic,
  applyStationMultiSelectionLogic,
  buildMapEventSource,
  intervalIsSelected,
  waveformDisplayIsOpen
} from './ian-map-utils';
import type { MapEventSource } from './types';

/**
 * The hide/show station context menu should not be available (currently) unless the following is true:
 * 1: An interval is selected
 * 2: The waveform display is open (this condition may change later)
 *
 * if both of these conditions are true, then canOpenContext menu is set to true, and this function also returns that
 */
export const useHideShowContextMenuState = (): boolean => {
  const [canOpenContextMenu, setCanOpenContextMenu] = React.useState(false);
  const openDisplays = useAppSelector(state => state.app.common.glLayoutState);
  const currentInterval = useAppSelector(state => state.app.workflow.timeRange);
  React.useEffect(() => {
    if (waveformDisplayIsOpen(openDisplays) && intervalIsSelected(currentInterval)) {
      setCanOpenContextMenu(true);
    } else {
      setCanOpenContextMenu(false);
    }
  }, [currentInterval, openDisplays]);

  return canOpenContextMenu;
};

/**
 * Gets the station data based on if the current interval is selected
 *
 * @returns Station[]
 */
export const useStationData = (): StationTypes.Station[] => {
  const currentInterval = useAppSelector(state => state.app.workflow.timeRange);
  const timeNow = useAppSelector(state => state.app.analyst.effectiveNowTime);
  const effectiveTime = currentInterval?.startTimeSecs ?? timeNow;
  const result = useGetAllStationsQuery(effectiveTime);
  return useOldQueryDataIfReloading<StationTypes.Station[]>(result);
};

/**
 * Get the map synced value from redux and return it
 *
 * @returns boolean
 */
export const useIsMapSyncedToWaveformZoom = (): boolean => {
  return useAppSelector(state => state.app.map.isSyncedWithWaveformZoom);
};

/**
 * Set the map synced value into redux
 *
 * @param isSynced boolean
 * @returns void
 */
export const useSetIsMapSyncedToWaveformZoom = (isSynced: boolean): void => {
  const dispatch = useAppDispatch();
  dispatch(mapActions.setIsMapSyncedWithWaveformZoom(isSynced));
};

/**
 * Gets the signal detection based on if the current interval is selected
 *
 * @returns SignalDetection[]
 */
export const useSignalDetectionForMap = (
  interval: CommonTypes.TimeRange
): SignalDetectionTypes.SignalDetection[] => {
  const query = useGetSignalDetections(interval);
  return query.data;
};

/**
 * Uses an array of event sources to produce data for map panel props
 *
 */
export const useMapPreferredEventData = (): MapEventSource[] => {
  const timeRange = useAppSelector(state => state.app.workflow.timeRange);
  const eventQuery = useGetEvents();

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);

  const findEventStatusQuery = useEventStatusQuery();

  const openEventId = useAppSelector(state => state.app.analyst.openEventId);

  const workflowQuery = useWorkflowQuery();

  const stageNames = React.useMemo(
    () => (workflowQuery.isSuccess ? workflowQuery.data?.stages.map(stage => stage.name) : []),
    [workflowQuery.isSuccess, workflowQuery.data?.stages]
  );

  const emptyArrayRef = React.useRef<MapEventSource[]>([]);

  return React.useMemo(
    () =>
      eventQuery.data.map(event => {
        let preferredEventHypothesis = EventTypes.findPreferredEventHypothesis(
          event,
          openIntervalName,
          stageNames
        );
        if (
          preferredEventHypothesis === undefined ||
          preferredEventHypothesis.preferredLocationSolution === undefined ||
          preferredEventHypothesis.rejected
        ) {
          // TODO: remove this fall back one rejected hypothesis are in their own source
          // Fall back to the parent hypothesis of the preferred

          preferredEventHypothesis = EventTypes.findEventHypothesisParent(
            event,
            preferredEventHypothesis
          );
        }
        if (preferredEventHypothesis === undefined) {
          return undefined;
        }
        return buildMapEventSource(
          event.id,
          preferredEventHypothesis,
          preferredEventHypothesis.preferredLocationSolution.id,
          timeRange,
          findEventStatusQuery.data,
          openEventId === event.id
        );
      }) || emptyArrayRef.current,
    [
      eventQuery.data,
      openIntervalName,
      stageNames,
      timeRange,
      findEventStatusQuery.data,
      openEventId
    ]
  );
};

/**
 * Uses an array of event sources to produce data for map panel props
 *
 */
export const useMapNonPreferredEventData = (): MapEventSource[] => {
  const timeRange = useAppSelector(state => state.app.workflow.timeRange);
  const eventQuery = useGetEvents();

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);

  const findEventStatusQuery = useEventStatusQuery();

  const openEventId = useAppSelector(state => state.app.analyst.openEventId);

  const workflowQuery = useWorkflowQuery();

  const stageNames = React.useMemo(
    () => (workflowQuery.isSuccess ? workflowQuery.data?.stages.map(stage => stage.name) : []),
    [workflowQuery.isSuccess, workflowQuery.data?.stages]
  );

  return React.useMemo(() => {
    const mapEventSources: MapEventSource[] = [];

    eventQuery.data.forEach(event => {
      const preferredEventHypothesis = EventTypes.findPreferredEventHypothesis(
        event,
        openIntervalName,
        stageNames
      );
      preferredEventHypothesis.locationSolutions.forEach(locationSolution => {
        if (locationSolution.id !== preferredEventHypothesis.preferredLocationSolution.id) {
          mapEventSources.push(
            buildMapEventSource(
              event.id,
              preferredEventHypothesis,
              locationSolution.id,
              timeRange,
              findEventStatusQuery.data,
              openEventId === event.id
            )
          );
        }
      });
    });
    return mapEventSources;
  }, [
    eventQuery.data,
    findEventStatusQuery.data,
    openEventId,
    openIntervalName,
    stageNames,
    timeRange
  ]);
};

/**
 * The on-left-click handler for Ian station entities displayed on the map, defined as a custom hook.
 */
export const useStationOnClickHandler = (): ((targetEntity: Cesium.Entity) => () => void) => {
  const dispatch = useAppDispatch();
  const selectedStations = useAppSelector(state => state.app.common?.selectedStationIds);
  return React.useCallback(
    (targetEntity: Cesium.Entity) => () => {
      if (targetEntity?.properties?.type?.getValue(JulianDate.now()) === 'Station') {
        if (selectedStations.includes(targetEntity.id)) {
          applyStationMultiSelectionLogic(dispatch, selectedStations, targetEntity.id);
        } else {
          dispatch(setSelectedStationIds([targetEntity.id]));
        }
      }
    },
    [selectedStations, dispatch]
  );
};

/**
 * The on-left-click handler for Ian event entities displayed on the map, defined as a custom hook.
 */
export const useEventOnClickHandler = (): ((targetEntity: Cesium.Entity) => () => void) => {
  const dispatch = useAppDispatch();
  const selectedEvents = useAppSelector(state => state.app.analyst.selectedEventIds);
  return React.useCallback(
    (targetEntity: Cesium.Entity) => () => {
      if (targetEntity?.properties?.type?.getValue(JulianDate.now()) === 'Event location') {
        if (selectedEvents.includes(targetEntity.id)) {
          applyEventMultiSelectionLogic(dispatch, selectedEvents, targetEntity.id);
        } else {
          dispatch(analystActions.setSelectedEventIds([targetEntity.id]));
        }
      }
    },
    [selectedEvents, dispatch]
  );
};

/**
 * Returns the left-click handler for signal detections on the map display
 *
 * @param
 * @param target
 */
export const useSdOnClickHandler = (): ((target: Cesium.Entity) => () => void) => {
  const dispatch = useAppDispatch();
  const selectedSdIds = useAppSelector(state => state.app.analyst.selectedSdIds);
  return React.useCallback(
    (targetEntity: Cesium.Entity) => () => {
      if (targetEntity?.properties?.type?.getValue(JulianDate.now()) === 'Signal detection') {
        if (selectedSdIds.includes(targetEntity.id)) {
          applySdMultiSelectionLogic(dispatch, selectedSdIds, targetEntity.id);
        } else {
          dispatch(analystActions.setSelectedSdIds([targetEntity.id]));
        }
      }
    },
    [selectedSdIds, dispatch]
  );
};
