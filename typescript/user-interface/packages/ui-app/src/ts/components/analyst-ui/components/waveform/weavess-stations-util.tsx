/* eslint-disable @typescript-eslint/no-use-before-define */
import type {
  ChannelTypes,
  ConfigurationTypes,
  EventTypes,
  QcMaskTypes,
  StationTypes
} from '@gms/common-model';
import { CommonTypes, SignalDetectionTypes, WaveformTypes } from '@gms/common-model';
import type { PhaseType } from '@gms/common-model/lib/common/types';
import { findArrivalTimeFeatureMeasurementUsingSignalDetection } from '@gms/common-model/lib/signal-detection/util';
import { isNumber } from '@gms/common-util';
import type {
  AnalystWaveformTypes,
  EventStatus,
  ReceiverLocationResponse,
  UiChannelSegment
} from '@gms/ui-state';
import { AnalystWaveformUtil, AnalystWorkspaceTypes } from '@gms/ui-state';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import { UILogger } from '@gms/ui-util';
import { WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import type { Draft } from 'immer';
import produce, { original } from 'immer';
import type { WritableDraft } from 'immer/dist/internal';
import flatMap from 'lodash/flatMap';
import includes from 'lodash/includes';
import isEmpty from 'lodash/isEmpty';
import isEqual from 'lodash/isEqual';
import orderBy from 'lodash/orderBy';
import sortBy from 'lodash/sortBy';
import React from 'react';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import { getSignalDetectionAssociationStatus } from '~analyst-ui/common/utils/event-util';
import { isArrivalTimeMeasurementValue } from '~analyst-ui/common/utils/instance-of-util';
import {
  filterSignalDetectionsByStationId,
  getAssocStatusColor,
  isPeakTroughInWarning
} from '~analyst-ui/common/utils/signal-detection-util';
import { sortStationDefinitionChannels } from '~analyst-ui/common/utils/station-definition-util';
import { getSelectedWaveformFilter } from '~analyst-ui/common/utils/waveform-util';
import type { QcMaskDisplayFilters } from '~analyst-ui/config';
import { systemConfig, userPreferences } from '~analyst-ui/config';
import { semanticColors } from '~scss-config/color-preferences';

import type { WaveformDisplayProps, WaveformDisplayState } from './types';
import type { Offset } from './utils';
import {
  calculateOffsetsObservedPhase,
  calculateOffsetsPredictedPhase,
  getAlignmentTime,
  getChannelLabelAndToolTipFromSignalDetections
} from './utils';

const logger = UILogger.create('GMS_WEAVESS_STATIONS_UTIL', process.env.GMS_WEAVESS_STATIONS_UTIL);
/**
 * Interface used to bundle all of the parameters need to create the
 * weavess stations for the waveform display.
 */
export interface CreateWeavessStationsParameters {
  defaultStations: StationTypes.Station[];
  measurementMode: AnalystWorkspaceTypes.MeasurementMode;
  featurePredictions: Record<string, ReceiverLocationResponse>;
  signalDetections: SignalDetectionTypes.SignalDetection[];
  selectedSdIds: string[];
  events: EventTypes.Event[];
  qcMasksByChannelName: QcMaskTypes.QcMask[];
  channelHeight: number;
  maskDisplayFilters: QcMaskDisplayFilters;
  channelFilters: Record<string, WaveformTypes.WaveformFilter>;
  uiChannelSegments: Record<string, Record<string, UiChannelSegment[]>>;
  defaultWaveformFilters: WaveformTypes.WaveformFilter[];
  startTimeSecs: number;
  endTimeSecs: number;
  currentOpenEvent?: EventTypes.Event;
  showPredictedPhases: boolean;
  showSignalDetectionUncertainty: boolean;
  distances: EventTypes.LocationDistance[];
  offsets: Offset[];
  phaseToAlignOn?: PhaseType;
  stationVisibilityDictionary: AnalystWaveformTypes.StationVisibilityChangesDictionary;
  stations: StationTypes.Station[];
  processingAnalystConfiguration: ConfigurationTypes.ProcessingAnalystConfiguration;
  uiTheme: ConfigurationTypes.UITheme;
  eventStatuses: Record<string, EventStatus>;
}

/**
 * Return sorted, filtered stations given sort type and current open event
 *
 * @param props current {@link WaveformDisplayProps}
 * @param sortByDistance override sort mode and sort by distance
 * @returns a {@link StationTypes.Station} array
 */
export function getSortedFilteredDefaultStations(
  props: WaveformDisplayProps,
  sortByDistance = false
): StationTypes.Station[] {
  const { events } = props;

  const currentOpenEvent = events?.find(event => event.id === props.currentOpenEventId);

  const signalDetectionsByStation = props.signalDetections;

  const theStations = props.stationsQuery?.data;
  const filteredStations = theStations
    ? // filter the stations based on the mode setting
      theStations.filter(stationToFilterOnMode =>
        filterStationOnMode(
          props.measurementMode.mode,
          stationToFilterOnMode,
          currentOpenEvent,
          signalDetectionsByStation
        )
      )
    : [];

  if (sortByDistance) {
    return sortProcessingStations(
      filteredStations,
      AnalystWorkspaceTypes.WaveformSortType.distance,
      props.distances
    );
  }

  return currentOpenEvent
    ? sortProcessingStations(filteredStations, props.selectedSortType, props.distances)
    : filteredStations;
}

/**
 * Calculate the zoom interval for the current open event,
 * 30 seconds before and after the alignment time at the closest station
 *
 * @param props current {@link WaveformDisplayProps}
 * @param sortByDistance override sort mode and sort by distance
 * @returns the zoom interval as a {@link CommonTypes.TimeRange} or undefined
 */
export function calculateZoomIntervalForCurrentOpenEvent(
  props: WaveformDisplayProps,
  sortByDistance = false
): CommonTypes.TimeRange | undefined {
  const zasZoomInterval = props.processingAnalystConfigurationQuery.data?.zasZoomInterval;
  let timeIntervalBuffer = 30;
  if (zasZoomInterval !== undefined && !Number.isNaN(zasZoomInterval)) {
    timeIntervalBuffer = zasZoomInterval / 2;
  }
  const sortedFilteredDefaultStations = getSortedFilteredDefaultStations(props, sortByDistance);
  const sortedVisibleStations = props.getVisibleStationsFromStationList(
    sortedFilteredDefaultStations
  );
  if (
    props.featurePredictionQuery.data &&
    props.featurePredictionQuery.data.receiverLocationsByName &&
    sortedVisibleStations !== undefined &&
    sortedVisibleStations.length > 0
  ) {
    const defaultPhaseAlignment =
      props.processingAnalystConfigurationQuery.data.zasDefaultAlignmentPhase ??
      CommonTypes.PhaseType.P;
    const alignmentTime = getAlignmentTime(
      props.featurePredictionQuery.data.receiverLocationsByName,
      sortedVisibleStations[0].name,
      defaultPhaseAlignment
    );
    if (alignmentTime) {
      return {
        startTimeSecs: alignmentTime - timeIntervalBuffer,
        endTimeSecs: alignmentTime + timeIntervalBuffer
      };
    }
  }

  return undefined;
}

/**
 * Creates CreateWeavessStationsParameters with the required fields used
 * for to creating the weavess stations for the waveform display.
 *
 * @param props The WaveformDisplayProps
 * @param state The WaveformDisplayState
 * @param channelHeight The height of rendered channels in weavess in px
 * @returns CreateWeavessStationsParameters
 */
export function populateCreateWeavessStationsParameters(
  props: WaveformDisplayProps,
  state: WaveformDisplayState,
  channelHeight: number
): CreateWeavessStationsParameters {
  const { events } = props;

  const currentOpenEvent = events?.find(event => event.id === props.currentOpenEventId);

  const signalDetectionsByStation = props.signalDetections;

  const analystConfiguration = props.processingAnalystConfigurationQuery?.data;

  const sortedFilteredDefaultStations = getSortedFilteredDefaultStations(props);
  const individualWeavesMeasurementMode: AnalystWorkspaceTypes.MeasurementMode = {
    mode: props.measurementMode.mode,
    entries: props.measurementMode.entries
  };

  // Calculate the offsets if not aligned on time
  let offsets: Offset[] = [];
  const sortedVisibleStations = props.getVisibleStationsFromStationList(
    sortedFilteredDefaultStations
  );
  if (
    props.alignWaveformsOn === AlignWaveformsOn.PREDICTED_PHASE &&
    props.featurePredictionQuery.data &&
    props.featurePredictionQuery.data.receiverLocationsByName &&
    sortedVisibleStations !== undefined &&
    sortedVisibleStations.length > 0
  ) {
    offsets = calculateOffsetsPredictedPhase(
      props.featurePredictionQuery.data.receiverLocationsByName,
      sortedVisibleStations[0].name,
      props.phaseToAlignOn
    );
  } else if (
    props.alignWaveformsOn === AlignWaveformsOn.OBSERVED_PHASE &&
    props.signalDetections &&
    props.featurePredictionQuery.data &&
    props.featurePredictionQuery.data.receiverLocationsByName &&
    sortedVisibleStations !== undefined &&
    sortedVisibleStations.length > 0
  ) {
    offsets = calculateOffsetsObservedPhase(
      props.signalDetections,
      props.featurePredictionQuery.data.receiverLocationsByName,
      sortedVisibleStations[0].name,
      events,
      currentOpenEvent.id,
      props.phaseToAlignOn
    );
  }

  return {
    defaultStations: sortedFilteredDefaultStations,
    measurementMode: individualWeavesMeasurementMode,
    featurePredictions: props.featurePredictionQuery.data?.receiverLocationsByName,
    signalDetections: signalDetectionsByStation,
    selectedSdIds: props.selectedSdIds,
    events,
    qcMasksByChannelName: [],
    channelHeight,
    maskDisplayFilters: state.maskDisplayFilters,
    channelFilters: props.channelFilters,
    uiChannelSegments: props.channelSegments,
    defaultWaveformFilters: props.processingAnalystConfigurationQuery?.data?.defaultFilters,
    startTimeSecs: props.currentTimeInterval.startTimeSecs,
    endTimeSecs: props.currentTimeInterval.endTimeSecs,
    currentOpenEvent,
    distances: props.distances,
    showPredictedPhases: props.shouldShowPredictedPhases,
    showSignalDetectionUncertainty: props.shouldShowTimeUncertainty,
    offsets,
    phaseToAlignOn: props.phaseToAlignOn,
    stationVisibilityDictionary: props.stationsVisibility,
    stations: props.stationsQuery?.data,
    processingAnalystConfiguration: analystConfiguration,
    uiTheme: props.uiTheme,
    eventStatuses: props.eventStatuses
  };
}

/**
 * Filter the stations based on the mode setting.
 *
 * @param mode the mode of the waveform display
 * @param station the station
 * @param signalDetectionsByStation the signal detections for all stations
 */
function filterStationOnMode(
  mode: AnalystWorkspaceTypes.WaveformDisplayMode,
  station: StationTypes.Station,
  currentOpenEvent: EventTypes.Event,
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[]
): boolean {
  if (AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT === mode) {
    if (currentOpenEvent) {
      // eslint-disable-next-line max-len
      const associatedSignalDetectionHypothesisIds = currentOpenEvent.overallPreferred?.associatedSignalDetectionHypotheses.map(
        hypothesis => hypothesis.id.id
      );

      const signalDetections = signalDetectionsByStation
        ? signalDetectionsByStation.filter(sd => {
            // filter out the sds for the other stations and the rejected sds
            if (
              sd.station.name !== station.name ||
              SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).rejected
            ) {
              return false;
            }

            // filter sds that are associated to the current open event
            if (
              includes(
                associatedSignalDetectionHypothesisIds,
                SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id
              )
            ) {
              return true;
            }

            return false;
          })
        : [];
      // display the station only if sds were returned
      return signalDetections.length > 0;
    }
  }

  return true; // show all stations (DEFAULT)
}

/**
 * Returns the `green` interval markers.
 *
 * @param startTimeSecs start time seconds for the interval start marker
 * @param endTimeSecs end time seconds for the interval end marker
 */
function getIntervalMarkers(startTimeSecs: number, endTimeSecs: number): WeavessTypes.Marker[] {
  return [
    {
      id: 'startTime',
      color: semanticColors.waveformIntervalBoundary,
      lineStyle: WeavessTypes.LineStyle.SOLID,
      timeSecs: startTimeSecs
    },
    {
      id: 'endTime',
      color: semanticColors.waveformIntervalBoundary,
      lineStyle: WeavessTypes.LineStyle.SOLID,
      timeSecs: endTimeSecs
    }
  ];
}

/**
 * If there are Signal Detections populate Weavess Channel Segment from the FK_BEAM
 * else use the default channel Weavess Channel Segment built
 *
 * @param signalDetections signal detections
 * @param defaultWaveformFilters default waveform filters
 * @ returns channelSegmentDict
 */
// TODO: what is this function supposed to do
export function populateWeavessChannelSegmentAndAddFilter(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  params: CreateWeavessStationsParameters
): Record<string, UiChannelSegment[]> {
  const channelSegmentsRecord: Record<string, UiChannelSegment[]> = {};
  if (signalDetections && signalDetections.length > 0 && params.uiChannelSegments) {
    // TODO leave this till filters come back
    // const allFilters = [...params.defaultWaveformFilters, WaveformTypes.UNFILTERED_FILTER];
    const allFilters = [WaveformTypes.UNFILTERED_FILTER];
    signalDetections.forEach(signalDetection => {
      const signalDetectionChannelSegmentsRecord =
        params.uiChannelSegments[signalDetection.station.name] ?? {};

      if (signalDetectionChannelSegmentsRecord && !isEmpty(signalDetectionChannelSegmentsRecord)) {
        allFilters.forEach(filter => {
          const signalDetectionChannelSegments = signalDetectionChannelSegmentsRecord[filter.name];
          if (signalDetectionChannelSegments && signalDetectionChannelSegments.length > 0) {
            channelSegmentsRecord[filter.id] = signalDetectionChannelSegments;
          }
        });
      }
    });
  }
  return channelSegmentsRecord;
}

/**
 * Create the amplitude selection windows for a signal detection
 *
 * @param arrivalTime arrival time (signal detection time epoch secs)
 * @param amplitudeMeasurementValue amplitude of signal detection
 * @param measurementMode
 * @returns a WeavessTypes.SelectionWindow[]
 */
export function generateAmplitudeSelectionWindows(
  sdId: string,
  arrivalTime: number,
  amplitudeMeasurementValue: SignalDetectionTypes.AmplitudeMeasurementValue,
  measurementMode: AnalystWorkspaceTypes.MeasurementMode
): WeavessTypes.SelectionWindow[] {
  const selectionStartOffset: number =
    systemConfig.measurementMode.selection.startTimeOffsetFromSignalDetection;
  const selectionEndOffset: number =
    systemConfig.measurementMode.selection.endTimeOffsetFromSignalDetection;
  const { period } = amplitudeMeasurementValue;
  const troughTime: number = amplitudeMeasurementValue.startTime;
  const peakTime = troughTime + period / 2; // display only period/2
  const isWarning = isPeakTroughInWarning(arrivalTime, period, troughTime, peakTime);
  const isMoveable =
    measurementMode.mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT &&
    systemConfig.measurementMode.peakTroughSelection.isMoveable;

  const selections: WeavessTypes.SelectionWindow[] = [];
  selections.push({
    id: `${systemConfig.measurementMode.peakTroughSelection.id}${sdId}`,
    startMarker: {
      id: 'start',
      color: !isWarning
        ? systemConfig.measurementMode.peakTroughSelection.borderColor
        : systemConfig.measurementMode.peakTroughSelection.warning.borderColor,
      lineStyle: isMoveable
        ? systemConfig.measurementMode.peakTroughSelection.lineStyle
        : systemConfig.measurementMode.peakTroughSelection.nonMoveableLineStyle,
      timeSecs: troughTime,
      minTimeSecsConstraint: arrivalTime + selectionStartOffset
    },
    endMarker: {
      id: 'end',
      color: !isWarning
        ? systemConfig.measurementMode.peakTroughSelection.borderColor
        : systemConfig.measurementMode.peakTroughSelection.warning.borderColor,
      lineStyle: isMoveable
        ? systemConfig.measurementMode.peakTroughSelection.lineStyle
        : systemConfig.measurementMode.peakTroughSelection.nonMoveableLineStyle,
      timeSecs: peakTime,
      maxTimeSecsConstraint: arrivalTime + selectionEndOffset
    },
    isMoveable,
    color: !isWarning
      ? systemConfig.measurementMode.peakTroughSelection.color
      : systemConfig.measurementMode.peakTroughSelection.warning.color
  });
  return selections;
}

/**
 * Creates the selection window and markers for weavess for a list of signal detections
 *
 * @param signalDetections signal detections
 * @param currentOpenEvent the current open event
 * @param measurementMode measurement mode
 *
 * @returns a WeavessTypes.SelectionWindow[]
 */
export function generateSelectionWindows(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  currentOpenEvent: EventTypes.Event,
  measurementMode: AnalystWorkspaceTypes.MeasurementMode
): WeavessTypes.SelectionWindow[] {
  return flatMap(
    signalDetections
      // eslint-disable-next-line complexity
      .map(sd => {
        const arrivalTimeValue: SignalDetectionTypes.ArrivalTimeMeasurementValue = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
          SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
            .featureMeasurements
        );
        // Check if arrival time is set
        if (!arrivalTimeValue?.arrivalTime || !isNumber(arrivalTimeValue.arrivalTime.value)) {
          return undefined;
        }
        const associatedSignalDetectionHypothesisIds = currentOpenEvent
          ? currentOpenEvent.overallPreferred?.associatedSignalDetectionHypotheses?.map(
              hypothesis => hypothesis.id.id
            )
          : [];

        const arrivalTime: number = arrivalTimeValue.arrivalTime.value;

        const { value } = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
          SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
            .featureMeasurements
        );

        const isSdAssociatedToOpenEvent =
          includes(
            associatedSignalDetectionHypothesisIds,
            SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id
          ) &&
          // sd must have phase type that is contained in the measurement mode phase filter list
          includes(systemConfig.measurementMode.phases, value);

        const amplitudeMeasurementValue = SignalDetectionUtils.findAmplitudeFeatureMeasurementValue(
          SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
            .featureMeasurements,
          SignalDetectionTypes.FeatureMeasurementType.AMPLITUDE_A5_OVER_2
        );

        const selectionStartOffset: number =
          systemConfig.measurementMode.selection.startTimeOffsetFromSignalDetection;
        const selectionEndOffset: number =
          systemConfig.measurementMode.selection.endTimeOffsetFromSignalDetection;

        // measurement.entries is a dictionary where key is the
        // signal detection id and the entry is boolean to show or hide
        // start undefined i.e. not in the map. If in map means SD is either manually
        // added to map to show or be hidden
        let shouldShow;
        if (measurementMode.entries[sd.id] !== undefined) {
          shouldShow = measurementMode.entries[sd.id];
        }

        // display the measurement selection windows if the sd is associated
        // to the open event and its phase is included in one of the measurement mode phases
        // and not excluded in the entries dictionary
        if (
          shouldShow ||
          (measurementMode.mode === AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT &&
            isSdAssociatedToOpenEvent &&
            shouldShow === undefined)
        ) {
          let selections: WeavessTypes.SelectionWindow[] = [];
          selections.push({
            id: `${systemConfig.measurementMode?.selection?.id}${sd.id}`,
            startMarker: {
              id: 'start',
              color: systemConfig.measurementMode?.selection?.borderColor,
              lineStyle: systemConfig.measurementMode?.selection?.lineStyle,
              timeSecs: arrivalTime + selectionStartOffset
            },
            endMarker: {
              id: 'end',
              color: systemConfig.measurementMode?.selection?.borderColor,
              lineStyle: systemConfig.measurementMode?.selection?.lineStyle,
              timeSecs: arrivalTime + selectionEndOffset
            },
            isMoveable: systemConfig.measurementMode?.selection?.isMoveable,
            color: systemConfig.measurementMode?.selection?.color
          });

          if (amplitudeMeasurementValue) {
            // Add the amplitude measurement selection windows
            selections = selections.concat(
              generateAmplitudeSelectionWindows(
                sd.id,
                arrivalTime,
                amplitudeMeasurementValue,
                measurementMode
              )
            );
          }
          return selections;
        }
        return [];
      })
      .filter(sw => sw !== undefined)
  );
}

/**
 * if the contents of the draft have changed in the new object for a particular key, then
 * replace the draft version with the version in the new object.
 *
 * @param draft the draft on which we are operating
 * @param key the key to check. Must be a key of the type T
 * @param newObj the new object to compare against.
 */
function maybeMutateDraft<T>(draft: WritableDraft<T>, key: keyof T, newObj: T) {
  const orig = original(draft);
  if (!isEqual(orig[key], newObj[key])) {
    draft[key] = (newObj[key] as unknown) as Draft<T[keyof T]>;
  }
}

/**
 * Creates a shallowly-immutable update of the @param existingChannel, such that any
 * changed parameters of that channel are replaced with the version from @param newChannel
 *
 * @param existingChannel the channel as it currently exists
 * @param newChannel What a new channel should look like
 * @returns a copy of @param existingChannel that matches @param newChannel, but that
 * preserves referential equality for any parameters that were unchanged.
 *
 * Note: it does this shallowly, so if any deeply nested value within the channel has changed,
 * this will replace the whole tree. For example, if the start time of @param newChannel.defaultRange has
 * changed, then @param existingChannel.defaultRange will be entirely replaced. This does not provide deep
 * immutability. It is a performance optimization, since deep comparison for deep immutability
 * is time consuming, and the performance hit for rerendering the interiors of a channel is
 * lower than the performance hit for many equality checks.
 */
function updateWeavessChannel(
  existingChannel: WeavessTypes.Channel,
  newChannel: WeavessTypes.Channel
): WeavessTypes.Channel | undefined {
  // Figure out if either/both are undefined or are equal first
  if (!existingChannel && newChannel) {
    return newChannel;
  }
  if ((!newChannel && existingChannel) || isEqual(newChannel, existingChannel)) {
    return existingChannel;
  }
  return produce(existingChannel, draft => {
    // Update any simple parameters that have changed
    Object.keys(existingChannel).forEach((k: keyof WeavessTypes.Channel) => {
      maybeMutateDraft(draft, k, newChannel);
    });
  });
}

/**
 * Updates a Weavess station, treating the station as an immutable object, and thus preserving
 * strict equality for unchanged parameters inside of the station
 *
 * @param existingWeavessStation the existing @interface WeavessTypes.Station to update
 * @param station station
 * @param selectedFilter selected filter
 * @param channelSegmentsRecord channel segment dictionary
 * @param signalDetections signal detections
 * @param params CreateWeavessStationsParameters the parameters required for
 * @returns a new @interface WeavessTypes.Station with any changed parameters updated.
 */
export function updateWeavessStation(
  existingWeavessStation: WeavessTypes.Station,
  station: StationTypes.Station,
  selectedFilter: WaveformTypes.WaveformFilter,
  channelSegmentsRecord: Record<string, UiChannelSegment[]>,
  signalDetections: SignalDetectionTypes.SignalDetection[],
  params: CreateWeavessStationsParameters
): WeavessTypes.Station {
  return produce(existingWeavessStation, draft => {
    const newStation = createWeavessStation(
      station,
      selectedFilter,
      channelSegmentsRecord,
      signalDetections,
      params
    );

    // Update any simple parameters that have changed
    Object.keys(existingWeavessStation).forEach((k: keyof WeavessTypes.Station) => {
      if (k === 'nonDefaultChannels' || k === 'defaultChannel') return; // handle separately
      maybeMutateDraft(draft, k, newStation);
    });

    draft.defaultChannel = updateWeavessChannel(
      existingWeavessStation.defaultChannel,
      newStation.defaultChannel
    );

    draft.nonDefaultChannels = updateWeavessNonDefaultChannels(
      existingWeavessStation,
      newStation,
      station,
      params
    );
  });
}

/**
 * Updates a Weavess station's non default channels, treating the channels as an immutable object,
 * and thus preserving strict equality for unchanged parameters inside of the non default channels
 *
 * @param draftWeavessStation
 * @param existingStation
 * @param newStation
 * @param station
 * @param params
 * @returns
 */
function updateWeavessNonDefaultChannels(
  existingStation: WeavessTypes.Station,
  newStation: WeavessTypes.Station,
  station: StationTypes.Station,
  params: CreateWeavessStationsParameters
): WeavessTypes.Channel[] {
  const modStation = produce(existingStation, draft => {
    // remove any nonDefaultChannels that are hidden
    draft.nonDefaultChannels.forEach((chan, index) => {
      if (!newStation.nonDefaultChannels.find(c => c.id === chan.id)) {
        draft.nonDefaultChannels.splice(index, 1);
      }
    });

    // Get the order of the Station Definition raw channels
    // to build the order of the weavess channels
    const sortedRawChannels = sortStationDefinitionChannels(station.allRawChannels);

    // Used to check the channel is visible before adding weavess channel
    const stationVis = params.stationVisibilityDictionary[station.name];

    // Add weavess channel to nonDefaultChannels. Use the order of the sorted raw channels.
    // Determine which WeavessChannel to add depending on if the channel has been updated
    // Review how sorting is being done and could we just sort based
    // on Weavess Channels?
    const channelsWithNewlyVisible: WeavessTypes.Channel[] = [];
    let addedHiddenChannel = false;
    sortedRawChannels.forEach(channel => {
      if (AnalystWaveformUtil.isChannelVisible(channel.name, stationVis)) {
        const newChannel = WeavessUtil.findChannelInStation(newStation, channel.name);
        const currentChannel = WeavessUtil.findChannelInStation(existingStation, channel.name);
        const updatedChannel = updateWeavessChannel(currentChannel, newChannel);
        // Replace existing channel with updated channel if it was changed. If that
        // channel was hidden then we will use the new list of channels
        if (updatedChannel !== currentChannel) {
          channelsWithNewlyVisible.push(updatedChannel);
          const index = draft.nonDefaultChannels.findIndex(chan => chan.id === newChannel.id);
          if (index === -1) {
            addedHiddenChannel = true;
          } else {
            // eslint-disable-next-line no-param-reassign
            draft.nonDefaultChannels[index] = updatedChannel;
          }
        } else if (currentChannel) {
          channelsWithNewlyVisible.push(currentChannel);
        }
      }
    });
    // If we added a hidden channel then use the new list
    if (addedHiddenChannel) {
      draft.nonDefaultChannels = channelsWithNewlyVisible;
    }
  });
  return modStation.nonDefaultChannels;
}

/**
 * Creates a station for weavess with the waveform data map
 *
 * @param station station
 * @param selectedFilter selected filter
 * @param channelSegmentsRecord channel segment dictionary
 * @param signalDetections signal detections
 * @param params CreateWeavessStationsParameters the parameters required for
 *
 * @returns a WaveformWeavessStation
 */
export function createWeavessStation(
  station: StationTypes.Station,
  selectedFilter: WaveformTypes.WaveformFilter,
  channelSegmentsRecord: Record<string, UiChannelSegment[]>,
  signalDetections: SignalDetectionTypes.SignalDetection[],
  params: CreateWeavessStationsParameters
): WeavessTypes.Station {
  const distanceToEvent = params.distances
    ? params.distances.find(d => d.id === station.name)
    : undefined;

  const stationVisObject = params.stationVisibilityDictionary[station.name];
  return {
    id: station.name,
    name: station.name,
    distance: distanceToEvent ? distanceToEvent.distance.degrees : 0,

    azimuth: distanceToEvent ? distanceToEvent.azimuth : 0,
    distanceUnits: userPreferences.distanceUnits,
    defaultChannel: createWeavessDefaultChannel(
      station,
      selectedFilter,
      channelSegmentsRecord,
      signalDetections,
      params
    ),
    nonDefaultChannels: createWeavessNonDefaultChannels(station, params, signalDetections),
    areChannelsShowing: AnalystWaveformUtil.isStationExpanded(stationVisObject)
  };
}

/**
 * Creates a default channel waveform for weavess
 *
 * @param station a processing station
 * @param selectedFilter the currently selected filter
 * @param filterUiChannelSegments dictionary of channel segment id (filter id) to filtered channel segment
 * @param signalDetections signal detections
 * @param params CreateWeavessStationsParameters the parameters required for
 * creating the weavess stations for the waveform display.
 *
 * @returns a WeavessTypes.Channel
 */
export function createWeavessDefaultChannel(
  station: StationTypes.Station,
  // These params will be used in creating default channel when we have Signal Detections
  selectedFilter: WaveformTypes.WaveformFilter,
  filterUiChannelSegments: Record<string, UiChannelSegment[]>,
  signalDetections: SignalDetectionTypes.SignalDetection[],
  params: CreateWeavessStationsParameters
): WeavessTypes.Channel {
  // Build a default channel segment to use if no Signal Detections are found
  // The segment type is FK_BEAM since that is all that is drawn on the default channels
  const stationOffset = params.offsets.find(offset => offset.receiverName === station.name);

  let channelName = '';
  let channelLabelTooltip;
  try {
    const res = getChannelLabelAndToolTipFromSignalDetections(signalDetections);
    channelName = res.channelLabel;
    channelLabelTooltip = res.tooltip;
  } catch (error) {
    logger.warn(`Error generating station label for ${station.name} msg: ${error}`);
  }
  const stationLabel = (
    <span className="station-name" data-cy="station-name">
      {station.name}
      <span className="station-name__channel-name"> {channelName}</span>
    </span>
  );
  return {
    id: station.name,
    name: stationLabel,
    height: params.channelHeight,
    timeOffsetSeconds: stationOffset ? stationOffset.offset : 0,
    baseStationTime: params.offsets[0]?.baseStationTime ?? null,
    waveform: createWeavessDefaultChannelWaveform(
      station,
      signalDetections,
      selectedFilter,
      filterUiChannelSegments,
      params
    ),
    channelLabelTooltip
  };
}

/**
 * Creates a non default channel for weavess
 *
 * @param station a processing station
 * @param params CreateWeavessStationsParameters the parameters required for
 * creating the weavess stations for the waveform display.
 *
 * @returns a WeavessTypes.Channel[]
 */
export function createWeavessNonDefaultChannels(
  station: StationTypes.Station,
  params: CreateWeavessStationsParameters,
  stationsSignalDetections: SignalDetectionTypes.SignalDetection[]
): WeavessTypes.Channel[] {
  // sds are only displayed on the default channel;
  // hide all non-default channels in measurement mode

  // Check the station is showing the channels and the channel is visible before creating weavess channel
  const { offsets } = params;
  const stationVis = params.stationVisibilityDictionary[station.name];

  // if in measurement mode or if the channels are not showing then return an empty array
  if (
    AnalystWorkspaceTypes.WaveformDisplayMode.MEASUREMENT === params.measurementMode.mode ||
    !stationVis.isStationExpanded
  ) {
    return [];
  }

  // Sort the channels based on the channel grouping and orientation
  const rawChannelsToProcess = sortStationDefinitionChannels(station.allRawChannels);

  // Build the visible child channels to return
  return rawChannelsToProcess
    .map(channel => {
      if (!AnalystWaveformUtil.isChannelVisible(channel.name, stationVis)) {
        return undefined;
      }
      const rawChannelSignalDetections = stationsSignalDetections.filter(
        sd => SignalDetectionUtils.getSignalDetectionChannelName(sd) === channel.name
      );
      const channelOffset = offsets.find(offset => offset.receiverName === channel.name);
      const nonDefaultChannel = createWeavessNonDefaultChannel(
        channel,
        params,
        channelOffset,
        rawChannelSignalDetections
      );
      nonDefaultChannel.name = <span className="station-name__channel-name">{channel.name}</span>;
      return nonDefaultChannel;
    })
    .filter(channel => channel !== undefined);
}

/**
 * Creates a non default channel for weavess
 *
 * @param channel a processing channel
 * @param params CreateWeavessStationsParameters the parameters required for
 * @param stationOffset offset in seconds
 *
 * @returns a WeavessTypes.Channel
 */
export function createWeavessNonDefaultChannel(
  channel: ChannelTypes.Channel,
  params: CreateWeavessStationsParameters,
  channelOffset: Offset,
  rawChannelSignalDetections: SignalDetectionTypes.SignalDetection[]
): WeavessTypes.Channel {
  const nonDefaultChannelSegments = getChannelSegments(
    params.measurementMode.mode,
    channel.name,
    channel.nominalSampleRateHz,
    params.channelFilters,
    params.uiChannelSegments,
    params.defaultWaveformFilters
  );

  const channelDistance = params.distances?.find(distance => distance.id === channel.name);
  return {
    id: channel.name,
    name: channel.name,
    timeOffsetSeconds: channelOffset ? channelOffset.offset : 0,
    baseStationTime: params.offsets[0]?.baseStationTime ?? null,
    height: params.channelHeight,
    waveform: createWeavessNonDefaultChannelWaveform(
      nonDefaultChannelSegments,
      channel,
      params,
      rawChannelSignalDetections
    ),
    distance:
      userPreferences.distanceUnits === 'degrees'
        ? channelDistance?.distance.degrees
        : channelDistance?.distance.km,
    azimuth: channelDistance?.azimuth,
    distanceUnits: userPreferences.distanceUnits
  };
}

/**
 * Updates the list of UiChannelSegments with the isSelected flag
 * set to try if signal detection is in the SdIds' list.
 *
 * @param uiChannelSegments
 * @param signalDetections
 * @param selectedSdIds
 * @returns Record<string, WeavessTypes.ChannelSegment[]>
 */
export function updateSelectedChannelSegments(
  selectedSdIds: string[],
  signalDetections: SignalDetectionTypes.SignalDetection[],
  uiChannelSegments: Record<string, UiChannelSegment[]>
): Record<string, WeavessTypes.ChannelSegment[]> {
  const channelSegments: Record<string, WeavessTypes.ChannelSegment[]> = {};
  if (uiChannelSegments && Object.keys(uiChannelSegments).length > 0) {
    Object.keys(uiChannelSegments).forEach(key => {
      channelSegments[key] = [];
      uiChannelSegments[key].forEach(uiChannelSegment => {
        // figure out if channel segment corresponds to a selected SD
        const foundSd = signalDetections.find(sd => {
          const arrivalTimeFm = findArrivalTimeFeatureMeasurementUsingSignalDetection(sd);
          if (
            isEqual(
              arrivalTimeFm?.measuredChannelSegment.id,
              uiChannelSegment.channelSegmentDescriptor
            )
          ) {
            return true;
          }
          return false;
        });
        // Add channel segment from the uiChannelSegment. If selected
        // set the isSelected flag
        let { channelSegment } = uiChannelSegment;
        if (foundSd && selectedSdIds.includes(foundSd.id)) {
          channelSegment = produce(uiChannelSegment.channelSegment, draft => {
            draft.isSelected = true;
          });
        }
        channelSegments[key].push(channelSegment);
      });
    });
  }
  return channelSegments;
}

/**
 * Creates a default channel waveform for weavess
 *
 * @param station a processing station
 * @param signalDetections signal detections
 * @param selectedFilter current selected filter
 * @param uiChannelSegments map of channel segment id (filter id) to filtered channel segment
 * @param params CreateWeavessStationsParameters the parameters required for
 * creating the weavess stations for the waveform display.
 *
 * @returns a WeavessTypes.ChannelWaveformContent
 */
export function createWeavessDefaultChannelWaveform(
  station: StationTypes.Station,
  signalDetections: SignalDetectionTypes.SignalDetection[],
  selectedFilter: WaveformTypes.WaveformFilter,
  uiChannelSegments: Record<string, UiChannelSegment[]>,
  params: CreateWeavessStationsParameters
): WeavessTypes.ChannelWaveformContent {
  const channelSegments = updateSelectedChannelSegments(
    params.selectedSdIds,
    signalDetections,
    uiChannelSegments
  );
  return {
    channelSegmentId: selectedFilter ? selectedFilter.id : '',
    channelSegmentsRecord: channelSegments,
    predictedPhases: buildPredictedPhasePickMarkers(station.name, params),
    signalDetections: buildSignalDetectionPickMarkers(signalDetections, params),
    masks: undefined,
    markers: {
      verticalMarkers: getIntervalMarkers(params.startTimeSecs, params.endTimeSecs),
      selectionWindows: generateSelectionWindows(
        signalDetections,
        params.currentOpenEvent,
        params.measurementMode
      )
    }
  };
}

/**
 * Builds the Weavess Signal Detections used in a WeavessChannel
 *
 * @param signalDetections
 * @returns list of Weavess Pick Markers
 */
function buildSignalDetectionPickMarkers(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  params: CreateWeavessStationsParameters
): WeavessTypes.PickMarker[] {
  return signalDetections
    ? signalDetections.map(detection => {
        const assocStatus = getSignalDetectionAssociationStatus(
          detection,
          params.events,
          params.currentOpenEvent ? params.currentOpenEvent.id : undefined,
          params.eventStatuses
        );
        const color = getAssocStatusColor(assocStatus, params.uiTheme);

        const arrivalTimeFeatureMeasurementValue = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
          SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
            .featureMeasurements
        );
        const fmPhase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
          SignalDetectionTypes.Util.getCurrentHypothesis(detection.signalDetectionHypotheses)
            .featureMeasurements
        );
        const sdUncertainty = arrivalTimeFeatureMeasurementValue.arrivalTime?.standardDeviation;
        return {
          timeSecs: arrivalTimeFeatureMeasurementValue?.arrivalTime?.value // its okay for 0 case since value is epoch seconds
            ? arrivalTimeFeatureMeasurementValue.arrivalTime.value
            : 0,
          uncertaintySecs: sdUncertainty || 0,
          showUncertaintyBars: sdUncertainty && params.showSignalDetectionUncertainty,
          label: fmPhase.value,
          id: detection.id,
          color,
          isConflicted: false,
          isDisabled: false,
          isSelected: params.selectedSdIds?.find(id => id === detection.id) !== undefined
        };
      })
    : [];
}

/**
 * Function to check if a phase marker should be displayed depending on its status as a priority phase
 * or a selected default/non-priority phase
 *
 * @param fpPhase
 * @param phaseToAlignOn
 * @param config
 * @returns
 */
export function isDisplayedPhase(
  fpPhase: CommonTypes.PhaseType,
  phaseToAlignOn: CommonTypes.PhaseType,
  config: ConfigurationTypes.ProcessingAnalystConfiguration
): boolean {
  return config?.priorityPhases.includes(fpPhase) || fpPhase === phaseToAlignOn;
}

/**
 * Builds the Weavess Predicted Phases used in a WeavessChannel
 *
 * @param station
 * @returns list of Weavess Pick Markers
 */
export function buildPredictedPhasePickMarkers(
  receiverName: string,
  params: CreateWeavessStationsParameters
): WeavessTypes.PickMarker[] {
  if (params.showPredictedPhases && params.featurePredictions) {
    return params.featurePredictions[receiverName]?.featurePredictions.map((fp, index) => {
      const { predictedValue } = fp.predictionValue;
      if (
        isArrivalTimeMeasurementValue(predictedValue) &&
        isDisplayedPhase(fp.phase, params.phaseToAlignOn, params.processingAnalystConfiguration)
      ) {
        return {
          timeSecs: predictedValue.arrivalTime.value,
          uncertaintySecs: predictedValue.arrivalTime.standardDeviation,
          showUncertaintyBars: false,
          label: fp.phase,
          id: `${index}`,
          color: params.uiTheme.colors.predictionSDColor,
          filter: `opacity(${params.uiTheme.display.predictionSDOpacity})`,
          isConflicted: false,
          isSelected: false
        };
      }
      return undefined;
    });
  }
  return [];
}

/**
 * Creates a non default channel waveform for weavess
 *
 * @param nonDefaultChannel non default channel
 * @param channel processing channel
 * @param params CreateWeavessStationsParameters the parameters required for
 * creating the weavess stations for the waveform display.
 *
 * @returns a WeavessTypes.ChannelWaveformContent
 */
export function createWeavessNonDefaultChannelWaveform(
  nonDefaultChannel: {
    channelSegmentId: string;
    channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]>;
  },
  channel: ChannelTypes.Channel,
  params: CreateWeavessStationsParameters,
  rawChannelSignalDetections: SignalDetectionTypes.SignalDetection[]
): WeavessTypes.ChannelWaveformContent {
  return {
    channelSegmentId: nonDefaultChannel.channelSegmentId,
    channelSegmentsRecord: nonDefaultChannel.channelSegmentsRecord,
    signalDetections: buildSignalDetectionPickMarkers(rawChannelSignalDetections, params),
    // if the mask category matches the enabled masks then return the mask else skip it
    masks: channel
      ? params.qcMasksByChannelName
          .filter(m => m.channelName === channel.name)
          .filter(qcMask =>
            Object.keys(params.maskDisplayFilters).find(
              key =>
                qcMask.currentVersion.category === key && params.maskDisplayFilters[key].visible
            )
          )
          .map(qcMask => ({
            id: qcMask.id,
            startTimeSecs: qcMask.currentVersion.startTime,
            endTimeSecs: qcMask.currentVersion.endTime,
            color: userPreferences.colors.maskDisplayFilters[qcMask.currentVersion.category].color
          }))
      : undefined,
    predictedPhases: buildPredictedPhasePickMarkers(channel.name, params),
    markers: {
      verticalMarkers: getIntervalMarkers(params.startTimeSecs, params.endTimeSecs)
    }
  };
}

/**
 * Creates the weavess stations for the waveform display.
 *
 * @param params CreateWeavessStationsParameters the parameters required for
 * creating the weavess stations for the waveform display.
 *
 * @returns a WeavessTypes.WeavessStation[]
 */
export function createWeavessStations(
  params: CreateWeavessStationsParameters,
  selectedSortType: AnalystWorkspaceTypes.WaveformSortType,
  existingWeavessStations: WeavessTypes.Station[]
): WeavessTypes.Station[] {
  const weavessStations = AnalystWaveformUtil.getVisibleStations(
    params.stationVisibilityDictionary,
    params.stations
  )
    // filter the stations based on the mode setting
    .filter(stationToFilterOnMode =>
      filterStationOnMode(
        params.measurementMode.mode,
        stationToFilterOnMode,
        params.currentOpenEvent,
        params.signalDetections
      )
    )
    .map(station => {
      const selectedFilter: WaveformTypes.WaveformFilter = getSelectedWaveformFilter(
        params.measurementMode.mode,
        station.name,
        station.allRawChannels[0].nominalSampleRateHz,
        params.channelFilters,
        params.defaultWaveformFilters
      );
      const signalDetections = params.signalDetections
        ? filterSignalDetectionsByStationId(station.name, params.signalDetections)
        : [];
      const channelSegmentsRecord = populateWeavessChannelSegmentAndAddFilter(
        signalDetections,
        params
      );

      const existingStation = existingWeavessStations.find(s => s.id === station.name);
      return existingStation
        ? updateWeavessStation(
            existingStation,
            station,
            selectedFilter,
            channelSegmentsRecord,
            signalDetections,
            params
          )
        : createWeavessStation(
            station,
            selectedFilter,
            channelSegmentsRecord,
            signalDetections,
            params
          );
    })
    .filter(weavessStation => weavessStation !== undefined);

  // Return the weavess station list sorted by station name
  return sortWaveformList(weavessStations, selectedSortType);
}

/**
 * Gets the raw channel's channelSegments for the currently applied filter
 *
 * @param mode current mode
 * @param channelName Id of the channel
 * @param sampleRate the sample rate of the channel
 * @param channelFilters Mapping of ids to filters
 * @param uiChannelSegments Raw or filtered channel segments for child channel
 * @param defaultWaveformFilters A list of filters retrieved from the gateway
 * @param startTimeSecs The start time of the channel Segments
 *
 * @returns an object containing a channelSegmentId, list of channel segments, and the type of segment
 */
export function getChannelSegments(
  mode: AnalystWorkspaceTypes.WaveformDisplayMode,
  channelName: string,
  sampleRate: number,
  channelFilters: Record<string, WaveformTypes.WaveformFilter>,
  uiChannelSegments: Record<string, Record<string, UiChannelSegment[]>>,
  defaultWaveformFilters: WaveformTypes.WaveformFilter[]
): {
  channelSegmentId: string;
  channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]>;
} {
  const selectedFilter: WaveformTypes.WaveformFilter = getSelectedWaveformFilter(
    mode,
    channelName,
    sampleRate,
    channelFilters,
    defaultWaveformFilters
  );

  // Get the ChannelSegment map for the channel name from the Waveform Cache
  // The key to the map is the waveform filter name
  const channelSegments = (uiChannelSegments && uiChannelSegments[channelName]) ?? {};
  const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
  Object.keys(channelSegments).forEach(filterId => {
    channelSegmentsRecord[filterId] = channelSegments[filterId].map(uiCs => uiCs.channelSegment);
  });
  return { channelSegmentId: selectedFilter?.id, channelSegmentsRecord };
}

/**
 * sort WeavessStations based on SortType
 *
 * @param stations WeavessStations
 * @param waveformSortType Alphabetical or by distance to selected event
 *
 * @returns sortedWeavessStations
 */
export function sortWaveformList(
  stations: WeavessTypes.Station[],
  waveformSortType: AnalystWorkspaceTypes.WaveformSortType
): WeavessTypes.Station[] {
  // apply sort based on sort type
  let sortedStations: WeavessTypes.Station[] = [];
  // Sort by distance if in global scan
  if (waveformSortType === AnalystWorkspaceTypes.WaveformSortType.distance) {
    sortedStations = sortBy<WeavessTypes.Station>(stations, [station => station.distance]);
  } else if (waveformSortType === AnalystWorkspaceTypes.WaveformSortType.stationNameAZ) {
    sortedStations = orderBy<WeavessTypes.Station>(stations, [station => station.name], ['asc']);
  } else if (waveformSortType === AnalystWorkspaceTypes.WaveformSortType.stationNameZA) {
    sortedStations = orderBy<WeavessTypes.Station>(stations, [station => station.name], ['desc']);
  }
  return sortedStations;
}

/**
 * sort waveform list based on sort type
 *
 * @param stations StationDefinition list
 * @param waveformSortType Alphabetical or by distance to selected event
 * @distance distance to stations list
 *
 * @returns sortedWeavessStations
 */
export function sortProcessingStations(
  stations: StationTypes.Station[],
  waveformSortType: AnalystWorkspaceTypes.WaveformSortType,
  distances: EventTypes.LocationDistance[]
): StationTypes.Station[] {
  // apply sort based on sort type
  let sortedStations: StationTypes.Station[] = [];
  // Sort by distance if in global scan

  if (waveformSortType === AnalystWorkspaceTypes.WaveformSortType.distance) {
    sortedStations = sortBy<StationTypes.Station>(
      stations,
      station => distances.find(source => source.id === station.name).distance.degrees
    );
    // For station name sort, order a-z by station config name
  } else if (waveformSortType === AnalystWorkspaceTypes.WaveformSortType.stationNameAZ) {
    sortedStations = orderBy<StationTypes.Station>(stations, [station => station.name], ['asc']);
  } else if (waveformSortType === AnalystWorkspaceTypes.WaveformSortType.stationNameZA) {
    sortedStations = orderBy<StationTypes.Station>(stations, [station => station.name], ['desc']);
  }
  return sortedStations;
}

/**
 * Returns a list of phases that are present for FP alignment
 *
 * @param fpForCurrentOpenEvent a list of the feature predictions for open event
 * @returns a list of phases that may be aligned
 */
export function getAlignablePhases(
  fpForCurrentOpenEvent: EventTypes.FeaturePrediction[]
): CommonTypes.PhaseType[] {
  if (!fpForCurrentOpenEvent || fpForCurrentOpenEvent.length <= 0) return [];
  return systemConfig.defaultSdPhases.filter(phase => {
    return fpForCurrentOpenEvent.filter(fp => fp.phase === phase).length > 0;
  });
}
