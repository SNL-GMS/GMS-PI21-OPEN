import type { ChannelTypes, CommonTypes, EventTypes, StationTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import type { ReceiverLocationResponse } from '@gms/ui-state';
import type { WeavessTypes } from '@gms/weavess-core';

import { SignalDetectionUtils } from '~analyst-ui/common/utils';
import { isSignalDetectionOpenAssociated } from '~analyst-ui/common/utils/event-util';
import {
  isArrivalTimeMeasurementValue,
  isPhaseMeasurementValue
} from '~analyst-ui/common/utils/instance-of-util';

export interface Offset {
  receiverName: string;
  offset: number;
  baseStationTime: number;
}

/**
 * Sort feature predictions with Phase feature measurements
 *
 * @param featurePredictions to sort
 * @returns sorted Feature Predictions
 */
export const sortFeaturePredictions = (
  featurePredictions: EventTypes.FeaturePrediction[]
): EventTypes.FeaturePrediction[] => {
  return featurePredictions.sort((a, b) => {
    if (
      isPhaseMeasurementValue(a.predictionValue.predictedValue) &&
      isPhaseMeasurementValue(b.predictionValue.predictedValue)
    ) {
      const aValue = a.predictionValue.predictedValue.value.toString();
      const bValue = b.predictionValue.predictedValue.value.toString();
      return aValue.localeCompare(bValue);
    }
    return 0;
  });
};

/**
 * Get the alignment time based on station with earliest arrival.
 *
 * @param featurePredictions feature predictions
 * @param baseStationName station name
 * @param phaseToAlignBy phase to align by
 * @returns alignment time or undefined
 */
export const getAlignmentTime = (
  featurePredictions: Record<string, ReceiverLocationResponse>,
  baseStationName: string,
  phaseToAlignBy: string
): number | undefined => {
  if (featurePredictions) {
    const baseFeaturePrediction = featurePredictions[baseStationName].featurePredictions.find(
      fp =>
        fp.phase === phaseToAlignBy &&
        fp.predictionType === SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME
    );
    if (
      baseFeaturePrediction &&
      isArrivalTimeMeasurementValue(baseFeaturePrediction.predictionValue.predictedValue)
    ) {
      return baseFeaturePrediction.predictionValue.predictedValue.arrivalTime.value;
    }
  }

  return undefined;
};

/**
 * Calculate offsets based on station with earliest arrival.
 * Helper function for {@link calculateOffsetsObservedPhase}.
 * Determines if a given signal detection is OpenAssociated and of a specified phase.
 */
const filterByOpenAssociatedAndPhase = (
  sd: SignalDetectionTypes.SignalDetection,
  events: EventTypes.Event[],
  currentOpenEventId: string,
  phaseToOffset: string
): boolean => {
  if (isSignalDetectionOpenAssociated(sd, events, currentOpenEventId)) {
    // Filter for matching phase last because this operation is somewhat heavy.
    const fmPhase = SignalDetectionUtils.findPhaseFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
        .featureMeasurements
    );
    return fmPhase.value === phaseToOffset;
  }
  return false;
};

/**
 * Helper function for {@link calculateOffsetsObservedPhase}.
 * Calculates an {@link Offset} using the arrivalTimeFeatureMeasurement derived from
 * a given Signal Detection and baseStationTime.
 */
const calcOffsetFromSignalDetection = (
  sd: SignalDetectionTypes.SignalDetection,
  baseStationTime: number
): Offset | undefined => {
  const arrivalTimeFeatureMeasurement = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
    SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).featureMeasurements
  );
  return arrivalTimeFeatureMeasurement
    ? {
        receiverName: sd.station.name,
        offset: baseStationTime - arrivalTimeFeatureMeasurement.arrivalTime.value,
        baseStationTime
      }
    : undefined;
};

/**
 * Helper function for {@link calculateOffsetsObservedPhase} and {@link calculateOffsetsPredictedPhase}.
 * Calculates an {@link Offset} from a Predicted Feature entry using a given phase and baseStationTime.
 *
 * @param entry
 * @param baseStationTime
 * @param phaseToOffset
 * @returns
 */
const calcOffsetFromFeaturePrediction = (
  [entryName, response]: [string, ReceiverLocationResponse],
  baseStationTime: number,
  phaseToOffset: string
): Offset | undefined => {
  const featurePrediction = response.featurePredictions.find(
    fp =>
      fp.phase === phaseToOffset &&
      fp.predictionType === SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME
  );
  if (
    featurePrediction &&
    isArrivalTimeMeasurementValue(featurePrediction.predictionValue.predictedValue)
  ) {
    return {
      receiverName: entryName,
      offset: baseStationTime - featurePrediction.predictionValue.predictedValue.arrivalTime.value,
      baseStationTime
    };
  }
  return undefined;
};

/**
 * Calculate offsets alignment on Predicted phase based on station with earliest arrival.
 */
export const calculateOffsetsPredictedPhase = (
  featurePredictions: Record<string, ReceiverLocationResponse>,
  baseStationName: string,
  phaseToOffset: string
): Offset[] => {
  if (featurePredictions) {
    const baseFeaturePrediction = featurePredictions[baseStationName].featurePredictions.find(
      fp =>
        fp.phase === phaseToOffset &&
        fp.predictionType === SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME
    );
    if (
      baseFeaturePrediction &&
      isArrivalTimeMeasurementValue(baseFeaturePrediction.predictionValue.predictedValue)
    ) {
      const baseStationTime: number =
        baseFeaturePrediction.predictionValue.predictedValue.arrivalTime.value;
      return Object.entries(featurePredictions).map(entry =>
        calcOffsetFromFeaturePrediction(entry, baseStationTime, phaseToOffset)
      );
    }
  }
  return [];
};

/**
 * Calculate offsets for alignment on Observed phase based on station with earliest arrival.
 * Falls back to Predicted phase if an observed phase is not associated to a channel's open event.
 */
export const calculateOffsetsObservedPhase = (
  signalDetections: SignalDetectionTypes.SignalDetection[],
  featurePredictions: Record<string, ReceiverLocationResponse>,
  baseStationName: string,
  events: EventTypes.Event[],
  currentOpenEventId: string,
  phaseToOffset: string
): Offset[] => {
  let baseStationTime: number;
  /** Signal Detections that are openAssociated and match {@link phaseToOffset} */
  const openAssociatedPhaseSDs = signalDetections.filter(sd =>
    filterByOpenAssociatedAndPhase(sd, events, currentOpenEventId, phaseToOffset)
  );

  const baseStationSD = openAssociatedPhaseSDs.find(sd => sd.station.name === baseStationName);

  // If the base station does not have any Observed phases, default to the Predicted phase
  if (!baseStationSD) {
    const baseFP = featurePredictions[baseStationName].featurePredictions.find(
      fp =>
        fp.phase === phaseToOffset &&
        fp.predictionType === SignalDetectionTypes.FeatureMeasurementType.ARRIVAL_TIME
    );
    if (baseFP && isArrivalTimeMeasurementValue(baseFP.predictionValue.predictedValue)) {
      baseStationTime = baseFP.predictionValue.predictedValue.arrivalTime.value;
    }
  } else {
    baseStationTime = SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
      SignalDetectionTypes.Util.getCurrentHypothesis(baseStationSD.signalDetectionHypotheses)
        .featureMeasurements
    ).arrivalTime.value;
  }

  const fmOffsets = openAssociatedPhaseSDs.map(sd =>
    calcOffsetFromSignalDetection(sd, baseStationTime)
  );
  // Remaining phases not associated to the open event should fall back to "predicted" phases
  const fpOffsets = Object.entries(featurePredictions)
    .filter(entry => !fmOffsets.find(offset => offset.receiverName === entry[0]))
    .map(entry => calcOffsetFromFeaturePrediction(entry, baseStationTime, phaseToOffset));

  return [...fmOffsets, ...fpOffsets].filter(offset => offset); // Filter out undefined/null values;
};

/**
 * TODO: Remove if/when we convert the UI TimeRange to use the same property keys.
 * Converts a UI time range to the Weavess format.
 *
 * @param timeRange a time range in the common model format
 * @returns a timeRange in the weavess format
 */
export const convertToWeavessTimeRange = (
  timeRange: CommonTypes.TimeRange
): WeavessTypes.TimeRange => ({
  startTimeSecs: timeRange.startTimeSecs,
  endTimeSecs: timeRange.endTimeSecs
});

/**
 * Gets the parent station for a provided channel.
 *
 * @param channel the channel or channel name for which to find the parent station
 * @param stations the list of all stations to search
 * @returns the station object from that list (by reference)
 */
export const getStationContainingChannel = (
  channel: ChannelTypes.Channel | string,
  stations: StationTypes.Station[]
): StationTypes.Station =>
  stations.find(s => {
    const channelName = typeof channel === 'string' ? channel : channel.name;
    return !!s.allRawChannels.find(c => c.name === channelName);
  });

/**
 * Status object to help passing status back and forth
 */
interface ParseDerivedChannelStatus {
  stationName: string;
  beamName: string;
  channelOrientation: string;
}

/**
 * Helper function for getChannelNameFromSignalDetections function
 * break up derived channel name into 3 components and returns them
 * A bit brittle since based channel name structure: 'ARCES.beam.SHZ/beam,b/...'
 *
 * @param derivedChannelName
 * @param lastResult to compare current derivedChannelName against
 * @returns ParseDerivedChannelStatus
 */
// eslint-disable-next-line complexity
const parseDerivedChannelName = (
  derivedChannelName: string,
  lastResult: ParseDerivedChannelStatus
): ParseDerivedChannelStatus => {
  // Break up based on '/' first
  if (!derivedChannelName) {
    return lastResult;
  }
  // Get first element and check it has '.' to split on for 3 elements to return
  let elementString = derivedChannelName;
  if (derivedChannelName.includes('/')) {
    const splitString = derivedChannelName.split('/');
    elementString = splitString.shift();
  }

  if (!elementString || elementString.length === 0 || !elementString.includes('.')) {
    return lastResult;
  }
  const elements = elementString.split('.');
  if (elements.length !== 3) {
    return lastResult;
  }

  const beamType = SignalDetectionUtils.parseBeamType(derivedChannelName);
  const parsedResults: ParseDerivedChannelStatus = {
    stationName: elements[0],
    beamName: !lastResult.beamName || lastResult.beamName === beamType ? beamType : '*',
    channelOrientation:
      !lastResult.channelOrientation || lastResult.channelOrientation === elements[2]
        ? elements[2]
        : '*'
  };

  if (lastResult.stationName && lastResult.stationName !== parsedResults.stationName) {
    throw new Error(
      `Couldn't determine station label from Signal Detections when multiple stations are in list.`
    );
  }
  return parsedResults;
};

/**
 * Computes the channel label name from a list of SD from a station.
 * Returning three components in the string if consistent
 * first 'station name' or throws exception if mixed
 * second 'beam' or * if mixed
 * third 'channel name' i.e. SHZ or * if mixed
 * if list is empty then returns empty string
 *
 * @param signalDetections for one station
 * @returns station string
 */
export const getChannelLabelAndToolTipFromSignalDetections = (
  signalDetections: SignalDetectionTypes.SignalDetection[]
): { channelLabel: string; tooltip: string } => {
  if (!signalDetections || signalDetections.length === 0) {
    return { channelLabel: '', tooltip: undefined };
  }
  let results = { stationName: undefined, beamName: undefined, channelOrientation: undefined };
  signalDetections.forEach(sd => {
    const sdh =
      sd?.signalDetectionHypotheses?.length > 0 ? sd.signalDetectionHypotheses[0] : undefined;
    const sdfm = sdh?.featureMeasurements?.length > 0 ? sdh.featureMeasurements[0] : undefined;
    if (!sdfm) {
      throw new Error(`Couldn't determine station label from Signal Detections.`);
    }
    results = parseDerivedChannelName(sdfm.channel.name, results);
  });

  // Figure out the tooltip based on if one or both are mixed
  let tooltip;
  if (results.channelOrientation === '*' && results.beamName === '*') {
    tooltip = `Multiple beam types and channels`;
  } else if (results.channelOrientation === '*') {
    tooltip = 'Multiple channels';
  } else if (results.beamName === '*') {
    tooltip = 'Multiple beam types';
  }

  // if beam name is not mixed then change to beam and not Fk beam, Event beam etc
  if (results.beamName !== '*') {
    results.beamName = 'beam';
  }

  // Figure out the channel label if both mixed beam and channel orientation
  // then replace *.* with *
  let channelLabel = `${results.beamName}.${results.channelOrientation}`;
  if (channelLabel === '*.*') {
    channelLabel = '*';
  }
  return { channelLabel, tooltip };
};

/**
 * Given a channel, returns the station name
 *
 * @param derivedChannelName
 */
export const getStationNameFromChannel = (channel: ChannelTypes.Channel): string => {
  if (channel.name) {
    let elementString = channel.name;
    if (channel.name.includes('/')) {
      const splitString = channel.name.split('/');
      elementString = splitString.shift();
    }
    const elements = elementString.split('.');
    return elements[0];
  }
  return '';
};
