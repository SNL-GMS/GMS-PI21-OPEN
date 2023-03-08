import type { ChannelTypes, CommonTypes, StationTypes, WaveformTypes } from '@gms/common-model';
import * as React from 'react';

import { Operations } from '../state/waveform/operations';
import type { StationVisibilityChangesDictionary } from '../state/waveform/types';
import * as Utils from '../state/waveform/util';
import { waveformActions, waveformSlice } from '../state/waveform/waveform-slice';
import { useAppDispatch, useAppSelector } from './react-redux-hooks';

/**
 * Hook for managing the viewable interval state. The viewable interval is the range of time
 * that has been loaded and is "viewable"
 *
 * @returns a touple consisting of the viewable interval and a setter to update the viewable interval
 */
export const useViewableInterval = (): [
  CommonTypes.TimeRange,
  (timeRange: CommonTypes.TimeRange) => void
] => {
  const viewableInterval = useAppSelector(state => state.app.waveform.viewableInterval);
  const dispatch = useAppDispatch();
  const setViewableInterval = React.useCallback(
    (theInterval: CommonTypes.TimeRange) => {
      dispatch(waveformActions.setViewableInterval(theInterval));
    },
    [dispatch]
  );
  return [viewableInterval, setViewableInterval];
};

/**
 * Hook for managing the minimum offset state. The minimum offset is the largest offset from the
 * reference waveform when not aligning by time
 *
 * @returns a touple consisting of the minimum offset and a setter to update the minimum offset
 */
export const useMinimumOffset = (): [number, (newValue: number) => void] => {
  const minimumOffset = useAppSelector(state => state.app.waveform.minimumOffset);
  const dispatch = useAppDispatch();
  const setMinimumOffset = React.useCallback(
    (newValue: number) => dispatch(waveformActions.setMinimumOffset(newValue)),
    [dispatch]
  );
  return [minimumOffset, setMinimumOffset];
};

/**
 * Hook for managing the base station time state. The minimum offset is the arrival time of the station
 * nearest an open event when not aligning by time
 *
 * @returns a touple consisting of the minimum offset and a setter to update the minimum offset
 */
export const useBaseStationTime = (): [number, (newValue: number) => void] => {
  const baseStationTime = useAppSelector(state => state.app.waveform.baseStationTime);
  const dispatch = useAppDispatch();
  const setBaseStationTime = React.useCallback(
    (newValue: number) => dispatch(waveformActions.setBaseStationTime(newValue)),
    [dispatch]
  );
  return [baseStationTime, setBaseStationTime];
};

/**
 * Hook for managing the maximum offset state. The maximum offset is the largest offset from the
 * reference waveform when not aligning by time
 *
 * @returns a touple consisting of the maximum offset and a setter to update the maximum offset
 */
export const useMaximumOffset = (): [number, (maximumOffset: number) => void] => {
  const maximumOffset = useAppSelector(state => state.app.waveform.maximumOffset);
  const dispatch = useAppDispatch();
  const setMaximumOffset = React.useCallback(
    (theOffset: number) => {
      dispatch(waveformActions.setMaximumOffset(theOffset));
    },
    [dispatch]
  );
  return [maximumOffset, setMaximumOffset];
};

export interface PanningOptions {
  shouldLoadAdditionalData: boolean;
  onPanningBoundaryReached: () => void;
}

/**
 * Returns a pan function. Panning shifts the zoom interval, and changes the viewable interval to
 * include the new zoom interval.
 *
 * @returns the pan function that takes a pan direction and panning boundary reached callback.
 * @throws if the panned time interval is of a different length than the original zoom interval
 * (pan shouldn't also change the zoom level).
 */
export const usePan = (): ((
  panDirection: WaveformTypes.PanType,
  panningOptions?: PanningOptions
) => CommonTypes.TimeRange) => {
  const dispatch = useAppDispatch();
  return React.useCallback(
    (panDirection: WaveformTypes.PanType, panningOptions?: PanningOptions) => {
      return dispatch(
        Operations.pan(panDirection, {
          shouldLoadAdditionalData: panningOptions?.shouldLoadAdditionalData ?? true,
          onPanningBoundaryReached: panningOptions?.onPanningBoundaryReached
        })
      );
    },
    [dispatch]
  );
};

/**
 * Hook for managing the zoom interval. The zoom interval is the range of time that is actually
 * drawn to the screen in the waveform display. It must always be within the viewable interval
 * (inclusive of end points)
 *
 * @returns a touple consisting of the zoom interval and a setter to update the zoom interval
 */
export const useZoomInterval = (): [
  CommonTypes.TimeRange,
  (timeRange: CommonTypes.TimeRange) => void
] => {
  const dispatch = useAppDispatch();
  const zoomInterval = useAppSelector(state => state.app.waveform.zoomInterval);
  const setZoomInterval = React.useCallback(
    (timeRange: CommonTypes.TimeRange) => {
      // mark this as a transition to indicate that it is lower priority
      React.startTransition(() => dispatch(Operations.setZoomInterval(timeRange)));
    },
    [dispatch]
  );
  return [zoomInterval, setZoomInterval];
};

/**
 * Hook that calls/returns either {@link useZoomInterval} or {@link useViewableInterval}
 * based on an "isSynced" flag.
 *
 * @returns a touple consisting of either the zoom or viewable
 * interval and a setter to update it.
 */
export const useInterval = (
  isSynced = false
): [CommonTypes.TimeRange, (timeRange: CommonTypes.TimeRange) => void] => {
  const [viewableInterval, setViewableInterval] = useViewableInterval();
  const [zoomInterval, setZoomInterval] = useZoomInterval();
  return isSynced ? [zoomInterval, setZoomInterval] : [viewableInterval, setViewableInterval];
};

/**
 * @returns a function that tells whether the provided station is currently visible.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useIsStationVisible = (): ((station: StationTypes.Station | string) => boolean) => {
  const stationsVisibility = useAppSelector(state => state.app.waveform.stationsVisibility);
  return React.useCallback(
    (station: StationTypes.Station | string) =>
      Utils.isStationVisible(stationsVisibility[Utils.getStationName(station)]),
    [stationsVisibility]
  );
};

/**
 * @returns a function that tells whether the provided station is currently expanded
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useIsStationExpanded = (): ((station: StationTypes.Station | string) => boolean) => {
  const stationsVisibility = useAppSelector(state => state.app.waveform.stationsVisibility);
  return React.useCallback(
    (station: StationTypes.Station | string) =>
      Utils.isStationExpanded(stationsVisibility[Utils.getStationName(station)]),
    [stationsVisibility]
  );
};

/**
 * @returns a function that filters a list of stations down to only the ones that are visible.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useGetVisibleStationsFromStationList = (): ((
  stations: StationTypes.Station[]
) => StationTypes.Station[]) => {
  const stationsVisibility = useAppSelector(state => state.app.waveform.stationsVisibility);
  return React.useCallback(
    (stations: StationTypes.Station[]) => Utils.getVisibleStations(stationsVisibility, stations),
    [stationsVisibility]
  );
};

/**
 * @returns a function that sets the station visibility. False is hidden, true is visible.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useSetStationVisibility = (): ((
  station: StationTypes.Station | string,
  isVisible: boolean
) => void) => {
  const dispatch = useAppDispatch();
  return React.useCallback(
    (station: StationTypes.Station | string, isVisible: boolean) => {
      dispatch(Operations.setStationVisibility(Utils.getStationName(station), isVisible));
    },
    [dispatch]
  );
};

/**
 * @returns a function that sets whether the provided station is expanded or not. If no
 * boolean is provided, it defaults to setting the station to be expanded.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useSetStationExpanded = (): ((
  station: StationTypes.Station | string,
  isExpanded?: boolean
) => void) => {
  const dispatch = useAppDispatch();
  return React.useCallback(
    (station: StationTypes.Station | string, isExpanded = true) => {
      dispatch(Operations.setStationExpanded(station, isExpanded));
    },
    [dispatch]
  );
};

/**
 * @returns a function that sets a channel's visibility in the provided station. True is visible, false is hidden.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useSetChannelVisibility = (): ((
  station: StationTypes.Station | string,
  channel: ChannelTypes.Channel | string,
  isVisible: boolean
) => void) => {
  const dispatch = useAppDispatch();
  return React.useCallback(
    (
      station: StationTypes.Station | string,
      channel: ChannelTypes.Channel | string,
      isVisible: boolean
    ) => {
      dispatch(
        Operations.setChannelVisibility(
          Utils.getStationName(station),
          Utils.getChannelName(channel),
          isVisible
        )
      );
    },
    [dispatch]
  );
};

/**
 * @returns a function that sets all channels for the provided station to visible.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useShowAllChannels = (): ((station: StationTypes.Station | string) => void) => {
  const dispatch = useAppDispatch();
  return React.useCallback(
    (station: StationTypes.Station | string) => {
      dispatch(Operations.showAllChannels(station));
    },
    [dispatch]
  );
};

/**
 * This hook provides access to the stationsVisibility changes dictionary object out of the Redux state.
 * It also exposes util functions and setter functions that make it easier to interact with the stationsVisibility.
 * This hook is subscribed to the stationsVisibility redux state, and will trigger rerenders when the
 * stationsVisibility updates.
 *
 * Note, all functions will be referentially equal when returned unless the station visibility changes,
 * at which point they all will be recreated. It is safe to use them as props for memoized and pure components.
 *
 * @returns an object containing:
 * stationsVisibility: the current dictionary with station names as keys assigned to StationVisibilityChanges objects.
 * isStationVisible: a util function that tells if the station provided is visible.
 * isStationExpanded: a util function that tells if the station provided is expanded.
 * getVisibleStationsFromStationList: a util function that takes a list of stations and returns only the stations that are visible from that list.
 * setStationVisibility: a setter function to set whether the station is visible.
 * setStationExpanded: a setter function to set whether the station is expanded.
 * setChannelVisibility: a setter function to set the visibility of a channel within a station.
 * showAllChannels: a setter function that sets all channels within a provided station to visible.
 */
export const useStationsVisibility = (): {
  stationsVisibility: StationVisibilityChangesDictionary;
  /**
   * function def
   *
   * @param station the station or station name.
   * @returns whether the station is visible in the waveform display (not hidden)
   */
  isStationVisible: (station: StationTypes.Station | string) => boolean;
  isStationExpanded: (station: StationTypes.Station | string) => boolean;
  getVisibleStationsFromStationList: (stations: StationTypes.Station[]) => StationTypes.Station[];
  setStationVisibility: (station: StationTypes.Station | string, isVisible: boolean) => void;
  setStationExpanded: (station: StationTypes.Station | string, isExpanded: boolean) => void;
  setChannelVisibility: (
    station: StationTypes.Station | string,
    channel: ChannelTypes.Channel | string,
    isVisible: boolean
  ) => void;
  showAllChannels: (station: StationTypes.Station | string) => void;
} => {
  const stationsVisibility = useAppSelector(state => state.app.waveform.stationsVisibility);
  return {
    stationsVisibility,
    isStationVisible: useIsStationVisible(),
    isStationExpanded: useIsStationExpanded(),
    getVisibleStationsFromStationList: useGetVisibleStationsFromStationList(),
    setStationExpanded: useSetStationExpanded(),
    setChannelVisibility: useSetChannelVisibility(),
    setStationVisibility: useSetStationVisibility(),
    showAllChannels: useShowAllChannels()
  };
};

/**
 * @returns a touple containing the boolean value shouldShowTimeUncertainty from redux, and a setter function
 * for updating that value.
 * The created function will be referentially equal when returned unless the station visibility changes,
 * at which point it will be recreated. It is safe to use as a prop for memoized and pure components.
 */
export const useShouldShowTimeUncertainty = (): [boolean, (newValue: boolean) => void] => {
  const shouldShowTimeUncertainty = useAppSelector(
    state => state.app.waveform.shouldShowTimeUncertainty
  );
  const dispatch = useAppDispatch();
  const setShouldShowTimeUncertainty = React.useCallback(
    (newValue: boolean) => dispatch(waveformSlice.actions.setShouldShowTimeUncertainty(newValue)),
    [dispatch]
  );
  return [shouldShowTimeUncertainty, setShouldShowTimeUncertainty];
};

/**
 * @returns tuple containing the boolean value shouldShowPredictedPhases from redux and a setter function
 * for updating that value
 */
export const useShouldShowPredictedPhases = (): [boolean, (newValue: boolean) => void] => {
  const shouldShowPredictedPhases = useAppSelector(
    state => state.app.waveform.shouldShowPredictedPhases
  );
  const dispatch = useAppDispatch();
  const setShouldShowPredictedPhases = React.useCallback(
    (newValue: boolean) => dispatch(waveformSlice.actions.setShouldShowPredictedPhases(newValue)),
    [dispatch]
  );
  return [shouldShowPredictedPhases, setShouldShowPredictedPhases];
};
