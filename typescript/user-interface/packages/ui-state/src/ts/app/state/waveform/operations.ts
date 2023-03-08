import type { ChannelTypes, CommonTypes, StationTypes, WorkflowTypes } from '@gms/common-model';
import { WaveformTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';
import flatMap from 'lodash/flatMap';
import isEqual from 'lodash/isEqual';
import sortBy from 'lodash/sortBy';
import uniq from 'lodash/uniq';
import { batch } from 'react-redux';

import { processingConfigurationApiSlice } from '../../api/processing-configuration/processing-configuration-api-slice';
import type { StationGroupsByNamesProps } from '../../api/station-definition/station-definition-api-slice';
import { stationDefinitionSlice } from '../../api/station-definition/station-definition-api-slice';
import { UIStateError } from '../../error-handling/ui-state-error';
import type { AppDispatch, AppState } from '../../store';
import type {
  StationVisibilityChanges,
  StationVisibilityChangesDictionary,
  WaveformState,
  ZoomIntervalProperties
} from './types';
import { ZOOM_INTERVAL_TOO_LARGE_ERROR_MESSAGE } from './types';
import * as Utils from './util';
import { waveformActions } from './waveform-slice';

const logger = UILogger.create('[UI State Manager - Waveform]', process.env.GMS_LOG_UI_STATE_STORE);

/**
 * Checks for null and undefined of the replacement station visibility map
 *
 * @param sVis
 * @returns boolean false if input is undefined or null
 */
export const stationsVisibilityDictionaryIsDefined = (
  sVis: StationVisibilityChangesDictionary
): boolean => {
  return typeof sVis !== 'undefined' && sVis !== null;
};

/**
 * Overwrites the StationsVisibility dictionary in the Redux state.
 *
 * @param sVis a map from station names (string) to StationVisibilityObjects
 */
export const setStationsVisibility = (sVis: StationVisibilityChangesDictionary) => (
  dispatch: AppDispatch
): void => {
  if (stationsVisibilityDictionaryIsDefined(sVis)) {
    dispatch(waveformActions.setStationsVisibility(sVis));
  }
};

/**
 * Internal function for resetting the station visibility to an empty map.
 *
 * @param dispatch the redux dispatch function
 */
export const resetStationsVisibility = (dispatch: AppDispatch): void => {
  dispatch(waveformActions.setStationsVisibility({}));
};

/**
 * Internal function for resetting the viewable interval and zoom interval to their default state: null.
 *
 * @param dispatch the redux dispatch function
 */
export const resetWaveformIntervals = (dispatch: AppDispatch): void => {
  batch(() => {
    dispatch(waveformActions.setViewableInterval(null));
    dispatch(waveformActions.setZoomInterval(null));
  });
};

/**
 * Internal function to calculate the boundaries for zoomInterval
 *
 * @param waveform
 */
export const calculateZoomIntervalProperties = (
  waveform: WaveformState
): ZoomIntervalProperties => {
  const viewableInterval = waveform.viewableInterval ?? null;
  const minOffset = waveform.minimumOffset ?? 0;
  const maxOffset = waveform.maximumOffset ?? 0;
  const startTimeSecs = viewableInterval?.startTimeSecs ?? 0;
  const endTimeSecs = viewableInterval?.endTimeSecs ?? 0;
  let diffStartTimeSecs = startTimeSecs + minOffset;
  let diffEndTimeSecs = endTimeSecs + maxOffset;
  let startTimeDiff = 0;
  let endTimeDiff = 0;
  const baseStationTime = waveform.baseStationTime ?? null;
  const prevZoomInterval = waveform.zoomInterval ?? null;
  if (baseStationTime) {
    startTimeDiff = baseStationTime - diffStartTimeSecs;
    endTimeDiff = diffEndTimeSecs - baseStationTime;
    if (startTimeDiff < endTimeDiff) {
      diffStartTimeSecs -= endTimeDiff - startTimeDiff;
      endTimeDiff = 0;
    } else {
      diffEndTimeSecs += startTimeDiff - endTimeDiff;
      startTimeDiff = 0;
    }
  }
  return {
    prevZoomInterval,
    viewableInterval,
    diffStartTimeSecs,
    diffEndTimeSecs,
    minOffset,
    maxOffset,
    startTimeDiff,
    endTimeDiff
  };
};

/**
 * Sets the zoom interval to the range provided. Rounds to the nearest millisecond.
 *
 * @param zoomInterval the interval to set it to. Must be within the viewable interval, and non-nullish.
 * @throws if the zoom interval is outside of the viewable interval, or if the viewable
 * interval is not set.
 */
export const setZoomInterval = (zoomInterval: CommonTypes.TimeRange) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const { waveform } = getState().app;
  const {
    prevZoomInterval,
    viewableInterval,
    diffStartTimeSecs,
    diffEndTimeSecs,
    minOffset,
    maxOffset,
    startTimeDiff,
    endTimeDiff
  } = calculateZoomIntervalProperties(waveform);

  if (isEqual(zoomInterval, prevZoomInterval)) {
    return;
  }
  if (zoomInterval == null) {
    dispatch(waveformActions.setZoomInterval(null));
    return;
  }
  if (!viewableInterval) {
    throw new UIStateError(ZOOM_INTERVAL_TOO_LARGE_ERROR_MESSAGE);
  }

  let zoomIntervalInRange = zoomInterval;
  if (
    zoomInterval.startTimeSecs < diffStartTimeSecs ||
    zoomInterval.endTimeSecs > diffEndTimeSecs
  ) {
    logger.warn(
      `Cannot set a zoom interval that is outside of the viewable interval. zoomInterval: ${JSON.stringify(
        zoomInterval
      )}, viewableInterval: ${JSON.stringify(
        viewableInterval
      )}, offsets: ${minOffset}, ${maxOffset}, timeDiffs: ${startTimeDiff}, ${endTimeDiff}
      }. Truncating to fit within viewable interval.`
    );
    zoomIntervalInRange = {
      startTimeSecs: Math.max(
        zoomInterval.startTimeSecs,
        viewableInterval.startTimeSecs + minOffset
      ),
      endTimeSecs: Math.min(zoomInterval.endTimeSecs, viewableInterval.endTimeSecs + maxOffset)
    };
  }
  dispatch(waveformActions.setZoomInterval(zoomIntervalInRange));
};

/**
 * Initializes the viewable range and zoom interval based on the value of the current interval time range in
 * redux, and the lead and lag times set in processing configuration.
 */
export const initializeWaveformIntervals = () => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const { timeRange: currentInterval } = getState().app.workflow;

  const processingAnalystConfigurationQuery = processingConfigurationApiSlice.endpoints.getProcessingAnalystConfiguration.select()(
    getState()
  );

  const leadBufferDuration = processingAnalystConfigurationQuery?.data?.leadBufferDuration ?? 0;
  const lagBufferDuration = processingAnalystConfigurationQuery?.data?.lagBufferDuration ?? 0;

  if (currentInterval.startTimeSecs != null && currentInterval.endTimeSecs != null) {
    const startTimeSecs = currentInterval.startTimeSecs - Number(leadBufferDuration);
    const endTimeSecs = currentInterval.endTimeSecs + Number(lagBufferDuration);

    const timeRange: CommonTypes.TimeRange = {
      startTimeSecs: Number.isNaN(startTimeSecs) ? undefined : startTimeSecs,
      endTimeSecs: Number.isNaN(endTimeSecs) ? undefined : endTimeSecs
    };
    batch(() => {
      dispatch(waveformActions.setViewableInterval(timeRange));
      setZoomInterval(timeRange)(dispatch, getState);
    });
  } else {
    throw new Error('Cannot initialize waveform intervals if no current interval is defined.');
  }
};

/**
 * Initializes the station visibility map based on the value of the current station group in redux
 */
export const initializeStationVisibility = (
  stationGroup: WorkflowTypes.StationGroup,
  effectiveTime: number
) => async (dispatch: AppDispatch, getState: () => AppState): Promise<void> => {
  const stationGroupQueryProps: StationGroupsByNamesProps = {
    stationGroupNames: [stationGroup.name],
    effectiveTime
  };

  await dispatch(
    stationDefinitionSlice.endpoints.getStationGroupsByNames.initiate(stationGroupQueryProps)
  );

  const stationGroupsQuery = stationDefinitionSlice.endpoints.getStationGroupsByNames.select(
    stationGroupQueryProps
  )(getState());

  const stationsVisibility = {};
  const stationNames = sortBy(
    uniq(
      flatMap(
        stationGroupsQuery?.data?.map(x =>
          x.name === stationGroup.name ? x.stations.map(y => y.name) : []
        )
      )
    )
  );

  const newStationsVisibility = produce(stationsVisibility, draft =>
    stationNames.forEach((stationName: string) => {
      draft[stationName] = {
        visibility: true,
        isStationExpanded: false,
        stationName
      };
    })
  );

  dispatch(waveformActions.setStationsVisibility(newStationsVisibility));
};

/**
 * Pans in the direction and updates the zoomInterval and viewableInterval accordingly
 *
 * @param panDirection the direction to pan
 * @param options containing @param shouldLoadAdditionalData which determines if it should
 * load new data if panning would take the user out of the currently loaded data (defaults to true),
 * and @param onPanningBoundaryReached which is a callback that is called if a boundary is reached.
 */
export const pan = (
  panDirection: WaveformTypes.PanType,
  {
    shouldLoadAdditionalData = true,
    onPanningBoundaryReached
  }: { shouldLoadAdditionalData?: boolean; onPanningBoundaryReached?: () => void }
) => (dispatch: AppDispatch, getState: () => AppState): CommonTypes.TimeRange => {
  const { waveform } = getState().app;
  const { viewableInterval, zoomInterval } = waveform;
  const { timeRange: currentInterval } = getState().app.workflow;
  const processingAnalystConfigurationQuery = processingConfigurationApiSlice.endpoints.getProcessingAnalystConfiguration.select()(
    getState()
  );

  let pannedViewTimeInterval = Utils.getPannedTimeInterval(
    panDirection,
    zoomInterval,
    processingAnalystConfigurationQuery.data.waveformPanRatio
  );

  // if panning boundary is reached, will adjust start/end time and invoke callback
  pannedViewTimeInterval = Utils.adjustIntervalIfBoundaryReached(
    pannedViewTimeInterval,
    currentInterval,
    processingAnalystConfigurationQuery.data.waveformPanningBoundaryDuration,
    shouldLoadAdditionalData ? onPanningBoundaryReached : undefined
  );

  // if not loading new data, if we reach the edge of the viewable interval, stop.
  if (!shouldLoadAdditionalData) {
    pannedViewTimeInterval = Utils.adjustIntervalIfBoundaryReached(
      pannedViewTimeInterval,
      viewableInterval,
      0
    );
  }

  const newViewDuration = pannedViewTimeInterval.endTimeSecs - pannedViewTimeInterval.startTimeSecs;
  const newZoomInterval: CommonTypes.TimeRange =
    panDirection === WaveformTypes.PanType.Left
      ? {
          startTimeSecs: pannedViewTimeInterval.startTimeSecs,
          endTimeSecs: pannedViewTimeInterval.startTimeSecs + newViewDuration
        }
      : {
          startTimeSecs: pannedViewTimeInterval.endTimeSecs - newViewDuration,
          endTimeSecs: pannedViewTimeInterval.endTimeSecs
        };

  const possibleRangeOfDataToLoad = Utils.getPossibleDataRangeInterval(
    pannedViewTimeInterval,
    currentInterval,
    viewableInterval,
    panDirection,
    processingAnalystConfigurationQuery.data.minimumRequestDuration,
    processingAnalystConfigurationQuery.data.waveformPanningBoundaryDuration
  );

  // determine if we need to load data or just pan the current view
  // floor/ceil the values to minimize the chance of erroneous reloading
  if (
    Math.ceil(possibleRangeOfDataToLoad.startTimeSecs) <
      Math.floor(viewableInterval.startTimeSecs) ||
    Math.floor(possibleRangeOfDataToLoad.endTimeSecs) > Math.ceil(viewableInterval.endTimeSecs)
  ) {
    dispatch(
      waveformActions.setViewableInterval({
        startTimeSecs: Math.min(
          viewableInterval.startTimeSecs,
          possibleRangeOfDataToLoad.startTimeSecs
        ),
        endTimeSecs: Math.max(viewableInterval.endTimeSecs, possibleRangeOfDataToLoad.endTimeSecs)
      })
    );
  }
  return newZoomInterval;
};

/**
 * Sets the station visibility for the provided station. Will create a new StationVisibilityChanges object
 * if none exists in the existing store.
 *
 * @param station a station or station name for which to set the visibility
 * @param isVisible whether the station should be visible or not.
 */
export const setStationVisibility = (
  station: StationTypes.Station | string,
  isVisible: boolean
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  const { stationsVisibility } = getState().app.waveform;
  const newVisMap = produce(stationsVisibility, draft => {
    const stationName = Utils.getStationName(station);
    draft[stationName] = draft[stationName] ?? Utils.newStationVisibilityChangesObject(stationName);
    draft[stationName].visibility = isVisible;
  });
  dispatch(waveformActions.setStationsVisibility(newVisMap));
};

/**
 * Sets the station to be expanded. Will create a new StationVisibilityChanges object
 * if none exists in the existing store.
 *
 * @param station a station or station name for which to set the visibility
 * @param isVisible whether the station should be visible or not.
 */
export const setStationExpanded = (station: StationTypes.Station | string, isExpanded = true) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const { stationsVisibility } = getState().app.waveform;
  const newVisMap = produce(stationsVisibility, draft => {
    const stationName = Utils.getStationName(station);
    draft[stationName] = draft[stationName] ?? Utils.newStationVisibilityChangesObject(stationName);
    draft[stationName].isStationExpanded = isExpanded;
  });
  dispatch(waveformActions.setStationsVisibility(newVisMap));
};

/**
 * Sets the channel visibility within the provided station. Will create a new StationVisibilityChanges object
 * if none exists in the existing store.
 *
 * @param station a station or station name for which to set the channel's visibility.
 * @param channel a channel or channel name for which to set the visibility.
 * @param isVisible whether the station should be visible or not.
 */
export const setChannelVisibility = (
  station: StationTypes.Station | string,
  channelName: ChannelTypes.Channel | string,
  isVisible: boolean
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  const { stationsVisibility } = getState().app.waveform;
  const newVisMap = produce(stationsVisibility, draft => {
    const vis: StationVisibilityChanges =
      draft[Utils.getStationName(station)] ??
      Utils.newStationVisibilityChangesObject(Utils.getStationName(station));
    if (isVisible) {
      draft[Utils.getStationName(station)] = Utils.getChangesForVisibleChannel(vis, channelName);
    } else {
      draft[Utils.getStationName(station)] = Utils.getChangesForHiddenChannel(vis, channelName);
    }
  });

  dispatch(waveformActions.setStationsVisibility(newVisMap));
};

/**
 * Sets all channels within a station to visible.
 *
 * @param station the station or station name for which to show all channels
 */
export const showAllChannels = (station: StationTypes.Station | string) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const { stationsVisibility } = getState().app.waveform;
  const newVisMap = produce(stationsVisibility, draft => {
    draft[Utils.getStationName(station)].hiddenChannels = undefined;
  });
  dispatch(waveformActions.setStationsVisibility(newVisMap));
};

export const Operations = {
  initializeStationVisibility,
  initializeWaveformIntervals,
  pan,
  resetStationsVisibility,
  setStationsVisibility,
  setStationVisibility,
  setStationExpanded,
  setChannelVisibility,
  setZoomInterval,
  showAllChannels
};
