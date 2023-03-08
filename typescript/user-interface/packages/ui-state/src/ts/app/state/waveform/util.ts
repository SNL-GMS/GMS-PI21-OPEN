import type { ChannelTypes, CommonTypes, StationTypes } from '@gms/common-model';
import { WaveformTypes } from '@gms/common-model';
import produce from 'immer';
import flatMap from 'lodash/flatMap';

import type {
  DefaultStationVisibility,
  StationVisibilityChanges,
  StationVisibilityChangesDictionary
} from './types';
import { defaultStationVisibility } from './types';

/**
 * @param stationOrName a station or station name from which to get the station's name
 * @returns the name of the station
 */
export const getStationName = (stationOrName: StationTypes.Station | string): string =>
  typeof stationOrName === 'string' ? stationOrName : stationOrName.name;

/**
 * @param channelOrName a channel or channel name from which to get the channel's name
 * @returns the name of the channel
 */
export const getChannelName = (channelOrName: ChannelTypes.Channel | string): string =>
  typeof channelOrName === 'string' ? channelOrName : channelOrName.name;

/**
 * For a given @interface StationVisibilityChanges object, return whether it is visible.
 * If none is given, return the default value.
 *
 * @param sv optional station visibility object. If not provided, defaults to the defaultStationVisibilityObject.
 * @returns whether the station is visible in the waveform display.
 */
export const isStationVisible = (
  sv: StationVisibilityChanges | DefaultStationVisibility = defaultStationVisibility
): boolean => {
  return sv.visibility ?? false;
};

/**
 * For a given @interface StationVisibilityChanges object, return whether the station has been expanded
 * in the waveform display. If no @interface StationVisibilityChanges object is given, return the default value.
 *
 * @param sv optional station visibility object. If not provided, defaults to the defaultStationVisibilityObject.
 * @returns whether the station has been expanded to show all channels in the waveform display.
 */
export const isStationExpanded = (
  sv: StationVisibilityChanges | DefaultStationVisibility = defaultStationVisibility
): boolean => {
  return sv.isStationExpanded ?? false;
};

/**
 * Checks in the provided @interface StationVisibilityChanges object to see if a station is expanded.
 * If the station is not found in @param visD, or if @param visD is undefined, then the default is returned.
 *
 * @param visD the visibility dictionary to check, mapping station names to @interface StationVisibilityChanges objects.
 * @param station the station object in question, or the station name
 * @default false
 * @returns whether the station is considered visible according to the @param visD provided
 */
export const isStationVisibleInChangesDict = (
  visD: StationVisibilityChangesDictionary,
  station: StationTypes.Station | string
): boolean => isStationVisible(visD[getStationName(station)]);

/**
 * For a given @interface StationVisibilityChanges object, return whether the named channel is considered visible,
 * meaning it is shown when the user expands that station in the waveform display.
 * If no @interface StationVisibilityChanges object is given, return the default value.
 *
 * @param channelName the name of the channel. Note, this does not check to make sure that the channelName a valid channel.
 * If the channelName is invalid, this will give a false positive.
 * @param sv optional station visibility object. If not provided, defaults to the defaultStationVisibilityObject.
 * @returns whether the channel with the given name is considered visible when the station is expanded to show channels.
 */
export const isChannelVisible = (
  channelName: string,
  sv: StationVisibilityChanges | DefaultStationVisibility = defaultStationVisibility
): boolean => {
  return !sv.hiddenChannels?.includes(channelName);
};

/**
 * When given a list of stations, filters that list to only include stations that are considered "visible,"
 * which indicates that the station appears in the waveform display (even if it is scrolled out of view).
 * If the station is not found in @param visD, or if the @param visD is undefined, then the
 * default (not visible) is returned.
 *
 * @param visD the visibility dictionary to check, mapping station names to @interface StationVisibilityChanges objects.
 * @param stations the list of stations to filter
 * @returns a list containing all of the stations that were considered visible from the @param stations list
 */
export const getVisibleStations = (
  visD: StationVisibilityChangesDictionary,
  stations: StationTypes.Station[]
): StationTypes.Station[] =>
  stations ? stations.filter(station => isStationVisible(visD[station.name])) : stations;

/**
 * Creates a station visibility changes object.
 *
 * @param stationName the name of the station - Required
 * @param visible is the station visible? Default to false
 * @param isExpanded is the station expanded? Default to false
 * @param hiddenChannels a list of channels that are hidden. Default to none.
 * @returns a new StationVisibilityChanges object with the provided settings.
 */
export const newStationVisibilityChangesObject = (
  stationName: string,
  visible: boolean = defaultStationVisibility.visibility,
  isExpanded: boolean = defaultStationVisibility.isStationExpanded,
  hiddenChannels: string[] = defaultStationVisibility.hiddenChannels
): StationVisibilityChanges => ({
  ...defaultStationVisibility,
  stationName,
  visibility: visible,
  isStationExpanded: isExpanded,
  hiddenChannels
});

/**
 * Creates a @interface StationVisibilityChanges object with the provided channel hidden
 *
 * @param vis the station visibility changes object for the hidden channel
 * @param channel the channel or name of the channel to hide
 * @returns a new visibility changes object with the channel hidden. Note, if the channel is already
 * hidden, this is a no-op, and the returned object will be exactly the same as @param vis.
 */
export const getChangesForHiddenChannel = (
  vis: StationVisibilityChanges,
  channel: string | ChannelTypes.Channel
): StationVisibilityChanges => {
  const channelName = getChannelName(channel);
  if (vis.hiddenChannels?.includes(channelName)) {
    return vis;
  }
  let hiddenChannels: string[];
  if (vis.hiddenChannels) {
    hiddenChannels = [...vis.hiddenChannels, channelName];
  } else {
    hiddenChannels = [channelName];
  }
  return {
    ...vis,
    hiddenChannels
  };
};

/**
 * Creates a @interface StationVisibilityChanges object with the provided channel hidden
 *
 * @param vis the station visibility changes object for the hidden channel
 * @param channel the channel or name of the channel to hide
 * @returns a new visibility changes object with the channel hidden. Note, if the channel is already
 * hidden, this is a no-op, and the returned object will be exactly the same as @param vis.
 */
export const getChangesForVisibleChannel = (
  vis: StationVisibilityChanges,
  channel: string | ChannelTypes.Channel
): StationVisibilityChanges => {
  const channelName = typeof channel === 'string' ? channel : channel.name;
  if (!vis.hiddenChannels?.includes(channelName)) {
    return vis;
  }
  return produce(vis, draft => {
    draft.hiddenChannels = vis.hiddenChannels.filter(cName => cName !== channelName);
  });
};

/**
 * When given a station, returns a list of that station's channels that includes all channels that are
 * considered "visible," which indicates that the channel appears in the waveform display (even if it
 * is scrolled out of view or its parent station is collapsed).
 * If the station is not found in @param visD, or if the @param visD is undefined, or the channel
 * is not listed as a hidden channel in the corresponding @interface StationVisibilityChanges object,
 * then the default (visible) is assumed for that channel.
 *
 * @param visD the visibility dictionary to check, mapping station names to @interface StationVisibilityChanges objects.
 * @param station the station containing the channels to filter
 * @returns a list containing all of the station's channels that are considered visible
 */
export const getVisibleChannels = (
  visD: StationVisibilityChangesDictionary,
  station: StationTypes.Station
): ChannelTypes.Channel[] =>
  station?.allRawChannels?.filter(chan =>
    isChannelVisible(chan.name, visD[getStationName(station)])
  );

/**
 * For a given station, returns a list of that station's channels that are displayed. This means the channel
 * must be considered "visible," and, for all non-default channels, that the station must be expanded. For
 * default channels, they are considered "displayed" even if the station is collapsed.
 * Note, the default visibility for all channels is assumed if @param visD is undefined, or if it does
 * not contain a @interface StationVisibilityChanges corresponding to the changes.
 *
 * @param visD the visibility dictionary to check, mapping station names to @interface StationVisibilityChanges objects.
 * @param station the station object in question
 * @returns a list of all the channels belonging to the provided station that should be displayed
 */
export const getAllDisplayedChannelsForStation = (
  visD: StationVisibilityChangesDictionary,
  station: StationTypes.Station
): ChannelTypes.Channel[] => {
  let visibleChannels = [];
  const staVis = visD[getStationName(station)];
  if (isStationVisible(staVis)) {
    if (isStationExpanded(staVis)) {
      visibleChannels = visibleChannels.concat(getVisibleChannels(visD, station));
    }
  }
  return visibleChannels;
};

/**
 * Creates a list of all channels that are displayed in the waveform display. Note that this includes
 * channels that are included in the display, but are out of view. It does not include non-default
 * channels that are beneath a station that is not expanded.
 *
 * @param visD the visibility dictionary to check, mapping station names to @interface StationVisibilityChanges objects.
 * @param stations the list of all stations to check
 * @returns a list of channels that should be displayed in the waveform display
 */
export const getAllDisplayedChannels = (
  visD: StationVisibilityChangesDictionary,
  stations: StationTypes.Station[]
): ChannelTypes.Channel[] => {
  const visibleStations = getVisibleStations(visD, stations);
  return flatMap(visibleStations, sta => getAllDisplayedChannelsForStation(visD, sta));
};

/**
 * Gets a list of channel names that are displayed in Weavess for the provided stations. Note that this includes
 * channels that are included in the display, but are out of view. It does not include non-default
 * channels that are beneath a station that is not expanded.
 *
 * @param visD the visibility dictionary to check, mapping station names to @interface StationVisibilityChanges objects.
 * @param stations the list of all stations to check
 */
export const getNamesOfAllDisplayedChannels = (
  visD: StationVisibilityChangesDictionary,
  stations: StationTypes.Station[]
): string[] => {
  const visibleChannels = getAllDisplayedChannels(visD, stations);
  return visibleChannels.map(chan => chan.name).sort();
};

/**
 * Calculate the interval representing the time range that should be visible after panning.
 * The interval is based on the ratio of current zoom interval.
 *
 * @param panDirection pan left or right
 * @param zoomInterval
 * @param waveformPanRatio
 * @returns pan interval
 */
export const getPannedTimeInterval = (
  panDirection: WaveformTypes.PanType,
  zoomInterval: CommonTypes.TimeRange,
  waveformPanRatio: number
): CommonTypes.TimeRange => {
  const viewDuration: number = Math.abs(zoomInterval.endTimeSecs - zoomInterval.startTimeSecs);
  const timeToPanBy: number = Math.ceil(viewDuration * waveformPanRatio);
  return panDirection === WaveformTypes.PanType.Left
    ? {
        startTimeSecs: Number(zoomInterval.startTimeSecs) - timeToPanBy,
        endTimeSecs: Number(zoomInterval.endTimeSecs) - timeToPanBy
      }
    : {
        startTimeSecs: Number(zoomInterval.startTimeSecs) + timeToPanBy,
        endTimeSecs: Number(zoomInterval.endTimeSecs) + timeToPanBy
      };
};

/**
 * Check the time interval (calculated from getPannedViewTimeInterval) constrained
 * by the waveformPanningBoundaryDuration. If it doesn't hit the boundary will return
 * the pannedTimeInterval
 *
 * @param panTimeInterval initial pan time interval to boundary check
 * @param currentInterval current open interval
 * @param waveformPanningBoundaryDuration max panning duration
 * @param onPanningBoundaryReached callback to call if panning boundary is reached
 * @returns pan interval
 */
export const adjustIntervalIfBoundaryReached = (
  panTimeInterval: CommonTypes.TimeRange,
  currentInterval: CommonTypes.TimeRange,
  waveformPanningBoundaryDuration: number,
  onPanningBoundaryReached?: () => void
): CommonTypes.TimeRange => {
  const startPanningBoundary = currentInterval.startTimeSecs - waveformPanningBoundaryDuration;
  const checkedInterval = panTimeInterval;
  const viewDuration = panTimeInterval.endTimeSecs - panTimeInterval.startTimeSecs;
  if (panTimeInterval.startTimeSecs < startPanningBoundary) {
    if (onPanningBoundaryReached) {
      onPanningBoundaryReached();
    }
    checkedInterval.startTimeSecs = startPanningBoundary;
    checkedInterval.endTimeSecs = startPanningBoundary + viewDuration;
  }
  const endPanningBoundary = currentInterval.endTimeSecs + waveformPanningBoundaryDuration;
  if (checkedInterval.endTimeSecs > endPanningBoundary) {
    if (onPanningBoundaryReached) {
      onPanningBoundaryReached();
    }
    checkedInterval.endTimeSecs = endPanningBoundary;
    checkedInterval.startTimeSecs = endPanningBoundary - viewDuration;
  }
  return checkedInterval;
};

/**
 * Checks pan interval against viewable interval to see if query for additional data
 * is needed. After the interval is calculated the interval is checked to see if
 * the requested data range needs to be adjustIntervalIfBoundaryReached.
 *
 * @param panTimeInterval to check
 * @param currentInterval workflow open interval
 * @param viewableInterval current viewable interval of data loaded
 * @param panDirection left or right
 * @param minimumRequestDuration minimum duration to be requested if query is necessary
 * @param waveformPanningBoundaryDuration max panning duration
 * @returns interval of data to loads
 */
export const getPossibleDataRangeInterval = (
  panTimeInterval: CommonTypes.TimeRange,
  currentInterval: CommonTypes.TimeRange,
  viewableInterval: CommonTypes.TimeRange,
  panDirection: WaveformTypes.PanType,
  minimumRequestDuration: number,
  waveformPanningBoundaryDuration: number
): CommonTypes.TimeRange => {
  // If panning start/end time will result in a request then confirm requested interval
  // is at least minimumRequestDuration
  let adjustQueryDuration = 0;
  const needToRequestQuery =
    viewableInterval.endTimeSecs < panTimeInterval.endTimeSecs ||
    viewableInterval.startTimeSecs > panTimeInterval.startTimeSecs;
  if (
    minimumRequestDuration &&
    panTimeInterval.endTimeSecs - panTimeInterval.startTimeSecs < minimumRequestDuration &&
    needToRequestQuery
  ) {
    // figure out how much past viewable are we already requesting
    const alreadyQueryRequesting =
      panDirection === WaveformTypes.PanType.Left
        ? viewableInterval.startTimeSecs - panTimeInterval.startTimeSecs
        : panTimeInterval.endTimeSecs - viewableInterval.endTimeSecs;
    adjustQueryDuration = minimumRequestDuration - Math.abs(alreadyQueryRequesting);
  }

  // Figure out the the requested viewable interval of data needed
  let possibleRangeOfDataToLoad: CommonTypes.TimeRange =
    panDirection === WaveformTypes.PanType.Left
      ? {
          startTimeSecs: panTimeInterval.startTimeSecs - adjustQueryDuration,
          endTimeSecs: viewableInterval.startTimeSecs
        }
      : {
          startTimeSecs: viewableInterval.endTimeSecs,
          endTimeSecs: panTimeInterval.endTimeSecs + adjustQueryDuration
        };

  // Again check if reached boundary but don't warn due since
  // have reached viewable interval boundary possible not the zoom interval boundary
  possibleRangeOfDataToLoad = adjustIntervalIfBoundaryReached(
    possibleRangeOfDataToLoad,
    currentInterval,
    waveformPanningBoundaryDuration
  );
  return possibleRangeOfDataToLoad;
};
