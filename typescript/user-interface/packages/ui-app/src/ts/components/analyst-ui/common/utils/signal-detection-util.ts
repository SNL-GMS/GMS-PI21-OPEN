/* eslint-disable @typescript-eslint/no-use-before-define */
import type { ChannelSegmentTypes, ConfigurationTypes, LegacyEventTypes } from '@gms/common-model';
import { CommonTypes, EventTypes, SignalDetectionTypes } from '@gms/common-model';
import {
  findArrivalTimeFeatureMeasurement,
  findFeatureMeasurementByType
} from '@gms/common-model/lib/signal-detection/util';
import { AnalystWorkspaceTypes } from '@gms/ui-state';
import { WeavessTypes } from '@gms/weavess-core';
import forEach from 'lodash/forEach';
import sortBy from 'lodash/sortBy';

import { systemConfig } from '~analyst-ui/config';
import { messageConfig } from '~analyst-ui/config/message-config';

import {
  AMPLITUDE_VALUES,
  FREQUENCY_VALUES,
  NOMINAL_CALIBRATION_PERIOD
} from './amplitude-scale-constants';

/**
 * Returns the first part of the name of the channel associated with the arrival time
 * feature measurement if available, otherwise, returns the first part of the name of
 * the channel associated with the first feature measurement associated with a channel
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns the first part of the channel name or undefined if not found
 */
export function findFeatureMeasurementChannelName(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): string | undefined {
  const arrivalTimeFeatureMeasurement = findArrivalTimeFeatureMeasurement(featureMeasurements);
  const channelName =
    arrivalTimeFeatureMeasurement?.channel?.name ??
    findFeatureMeasurementChannelNameHelper(featureMeasurements);

  return channelName ? channelName.split('/')[0] : undefined;
}

/**
 * Returns the first part of the name of the channel associated with the first feature
 * measurement associated with a channel
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns the first part of the channel name or undefined if not found
 */
export function findFeatureMeasurementChannelNameHelper(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): string | undefined {
  return featureMeasurements && featureMeasurements.length > 0
    ? featureMeasurements.find(fm => fm?.channel?.name != null && fm?.channel?.name.length > 0)
        ?.channel?.name
    : undefined;
}

/**
 * Searches Feature Measurements for the Azimuth Feature Measurement
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns Azimuth FeatureMeasurement or undefined if not found
 */
export function findAzimuthFeatureMeasurement(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.FeatureMeasurement | undefined {
  const azimuthList: SignalDetectionTypes.FeatureMeasurementType[] = [
    SignalDetectionTypes.FeatureMeasurementType.RECEIVER_TO_SOURCE_AZIMUTH,
    SignalDetectionTypes.FeatureMeasurementType.SOURCE_TO_RECEIVER_AZIMUTH
  ];
  // Search FeatureMeasurements to find which type of Azimuth was supplied
  return featureMeasurements.find(
    fm => azimuthList.find(azTypeName => azTypeName === fm.featureMeasurementType) !== undefined
  );
}

/**
 * Searches Feature Measurements for the Azimuth Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns Azimuth FeatureMeasurementValue or undefined if not found
 */
export function findAzimuthFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.NumericMeasurementValue | undefined {
  const fm = findAzimuthFeatureMeasurement(featureMeasurements);
  return fm ? (fm.measurementValue as SignalDetectionTypes.NumericMeasurementValue) : undefined;
}

/**
 * Searches Feature Measurements for the Slowness Feature Measurement
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns Slowness FeatureMeasurement or undefined if not found
 */
export function findSlownessFeatureMeasurement(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.FeatureMeasurement | undefined {
  return SignalDetectionTypes.Util.findFeatureMeasurementByType(
    featureMeasurements,
    SignalDetectionTypes.FeatureMeasurementType.SLOWNESS
  );
}

/**
 * Searches Feature Measurements for the Slowness Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns Slowness FeatureMeasurementValue or undefined if not found
 */
export function findSlownessFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.NumericMeasurementValue | undefined {
  const fm = findSlownessFeatureMeasurement(featureMeasurements);
  return fm ? (fm.measurementValue as SignalDetectionTypes.NumericMeasurementValue) : undefined;
}

/**
 * Searches Feature Measurements for the Amplitude Feature Measurement
 *
 * @param featureMeasurements List of feature measurements
 * @param amplitudeName
 * @returns Phase FeatureMeasurement or undefined if not found
 */
export function findAmplitudeFeatureMeasurement(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[],
  amplitudeName: SignalDetectionTypes.FeatureMeasurementType
): SignalDetectionTypes.FeatureMeasurement | undefined {
  // Search FeatureMeasurements to find which type of Amplitude was supplied
  return featureMeasurements.find(fm => fm.featureMeasurementType === amplitudeName);
}

/**
 * Searches Feature Measurements for the Amplitude Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 * @param amplitudeName
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findAmplitudeFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[],
  amplitudeName: SignalDetectionTypes.FeatureMeasurementType
): SignalDetectionTypes.AmplitudeMeasurementValue | undefined {
  const maybeMeasurement = findAmplitudeFeatureMeasurement(featureMeasurements, amplitudeName);
  return maybeMeasurement
    ? (maybeMeasurement.measurementValue as SignalDetectionTypes.AmplitudeMeasurementValue)
    : undefined;
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns Phase FeatureMeasurement or undefined if not found
 */
export function findPhaseFeatureMeasurement(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.FeatureMeasurement | undefined {
  return SignalDetectionTypes.Util.findFeatureMeasurementByType(
    featureMeasurements,
    SignalDetectionTypes.FeatureMeasurementType.PHASE
  );
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 *
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findPhaseFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.PhaseTypeMeasurementValue | undefined {
  const fm = findPhaseFeatureMeasurement(featureMeasurements);
  return fm ? (fm.measurementValue as SignalDetectionTypes.PhaseTypeMeasurementValue) : undefined;
}

/**
 * Searches Feature Measurements for the Rectilinearity Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 *
 * @returns Rectilinearity FeatureMeasurementValue or undefined if not found
 */
export function findRectilinearityFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.RectilinearityMeasurementValue | undefined {
  const fm = findFeatureMeasurementByType(
    featureMeasurements,
    SignalDetectionTypes.FeatureMeasurementType.RECTILINEARITY
  );
  return fm
    ? (fm.measurementValue as SignalDetectionTypes.RectilinearityMeasurementValue)
    : undefined;
}
/**
 * Searches Feature Measurements for the Emergence_Angle Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 *
 * @returns Emergence_Angle FeatureMeasurementValue or undefined if not found
 */
export function findEmergenceAngleFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.EmergenceAngleMeasurementValue | undefined {
  const fm = findFeatureMeasurementByType(
    featureMeasurements,
    SignalDetectionTypes.FeatureMeasurementType.EMERGENCE_ANGLE
  );
  return fm
    ? (fm.measurementValue as SignalDetectionTypes.EmergenceAngleMeasurementValue)
    : undefined;
}
/**
 * Searches Feature Measurements for the ShortPeriodFirstMotion Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 *
 * @returns SHORT_PERIOD_FIRST_MOTION FeatureMeasurementValue or undefined if not found
 */
export function findShortPeriodFirstMotionFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.ShortPeriodFirstMotionMeasurementValue | undefined {
  const fm = findFeatureMeasurementByType(
    featureMeasurements,
    SignalDetectionTypes.FeatureMeasurementType.SHORT_PERIOD_FIRST_MOTION
  );
  return fm
    ? (fm.measurementValue as SignalDetectionTypes.ShortPeriodFirstMotionMeasurementValue)
    : undefined;
}
/**
 * Searches Feature Measurements for the LongPeriodFirstMotion Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 *
 * @returns LongPeriodFirstMotion FeatureMeasurementValue or undefined if not found
 */
export function findLongPeriodFirstMotionFeatureMeasurementValue(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[]
): SignalDetectionTypes.LongPeriodFirstMotionMeasurementValue | undefined {
  const fm = findFeatureMeasurementByType(
    featureMeasurements,
    SignalDetectionTypes.FeatureMeasurementType.LONG_PERIOD_FIRST_MOTION
  );
  return fm
    ? (fm.measurementValue as SignalDetectionTypes.LongPeriodFirstMotionMeasurementValue)
    : undefined;
}

/**
 * Searches Feature Measurements for the Phase Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 * @param filterDefinitionId The filter ID
 *
 * @returns Phase FeatureMeasurementValue or undefined if not found
 */
export function findFilteredBeamFeatureMeasurement(
  featureMeasurements: SignalDetectionTypes.FeatureMeasurement[],
  filterDefinitionId: string
): SignalDetectionTypes.FeatureMeasurement | undefined {
  if (featureMeasurements) {
    return featureMeasurements.find(
      (fm: any) =>
        fm.featureMeasurementType === SignalDetectionTypes.FeatureMeasurementType.FILTERED_BEAM &&
        fm.measurementValue.strValue === filterDefinitionId
    );
  }
  return undefined;
}

/**
 * Create a unique string (used as a key to ChannelSegmentMap)
 *
 * @param id ChannelSegmentDescriptor
 * @returns unique string representing the ChannelSegmentDescriptor
 */
export function createChannelSegmentString(
  id: ChannelSegmentTypes.ChannelSegmentDescriptor
): string {
  return `${id.channel.name}.${id.channel.effectiveAt}.${id.creationTime}.${id.startTime}.${id.endTime}`;
}

/**
 * Calculates a new amplitude measurement value given the [min,max] peak/trough
 *
 * @param peakAmplitude the peak amplitude
 * @param troughAmplitude the trough amplitude
 * @param peakTime the peak time
 * @param troughTime the trough time
 */
export function calculateAmplitudeMeasurementValue(
  peakAmplitude: number,
  troughAmplitude: number,
  peakTime: number,
  troughTime: number
): SignalDetectionTypes.AmplitudeMeasurementValue {
  const amplitudeValue = (peakAmplitude - troughAmplitude) / 2;
  const period = Math.abs(peakTime - troughTime) * 2;
  const amplitudeMeasurementValue: SignalDetectionTypes.AmplitudeMeasurementValue = {
    amplitude: {
      value: amplitudeValue,
      standardDeviation: 0,
      units: CommonTypes.Units.UNITLESS
    },
    period,
    startTime: Math.min(troughTime, peakTime)
  };
  return amplitudeMeasurementValue;
}

/**
 * Returns true if the period, trough, or peak times are in warning.
 *
 * @para signalDetectionArrivalTime the arrival time of the signal detection
 * @param period The period value to check
 * @param troughTime The trough time (seconds)
 * @param peakTime The peak time (seconds)
 */
export function isPeakTroughInWarning(
  signalDetectionArrivalTime: number,
  period: number,
  troughTime: number,
  peakTime: number
): boolean {
  const { min } = systemConfig.measurementMode.peakTroughSelection.warning;
  const { max } = systemConfig.measurementMode.peakTroughSelection.warning;
  const { startTimeOffsetFromSignalDetection } = systemConfig.measurementMode.selection;
  const { endTimeOffsetFromSignalDetection } = systemConfig.measurementMode.selection;
  // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
  const selectionStart = signalDetectionArrivalTime + startTimeOffsetFromSignalDetection;
  // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
  const selectionEnd = signalDetectionArrivalTime + endTimeOffsetFromSignalDetection;

  // check that the period is within the correct limits
  // check that peak trough start/end are within the selection area
  return (
    period < min ||
    period > max ||
    peakTime < troughTime ||
    troughTime < selectionStart ||
    troughTime > selectionEnd ||
    peakTime < selectionStart ||
    peakTime > selectionEnd
  );
}

/**
 * Helper function used by {@link findMinMaxAmplitudeForPeakTrough} to find
 * minimum and maximum values in a given array.
 */
const findMinMax = (values: { index: number; value: number }[]) => {
  const startValue = values.slice(0)[0];
  const nextDiffValue = values.find(v => v.value !== startValue.value);
  const isFindingMax = nextDiffValue && nextDiffValue.value > startValue.value;
  const result = { min: startValue, max: startValue };
  // eslint-disable-next-line consistent-return
  forEach(values, nextValue => {
    if (isFindingMax && nextValue.value >= result.max.value) {
      result.max = nextValue;
    } else if (!isFindingMax && nextValue.value <= result.min.value) {
      result.min = nextValue;
    }
  });
  return result;
};

/** Reducer for the minimum value. Used by {@link findMinMaxAmplitudeForPeakTrough}. */
const minReducer = (
  previous: { value: number; index: number },
  current: { value: number; index: number },
  startIndex: number
) => {
  if (current.value < previous.value) {
    return current;
  }
  if (
    current.value === previous.value &&
    Math.abs(startIndex - current.index) > Math.abs(startIndex - previous.index)
  ) {
    return current;
  }
  return previous;
};

/** Reducer for the maximum value. Used by {@link findMinMaxAmplitudeForPeakTrough}. */
const maxReducer = (
  previous: { value: number; index: number },
  current: { value: number; index: number },
  startIndex: number
) => {
  if (current.value > previous.value) {
    return current;
  }
  if (
    current.value === previous.value &&
    Math.abs(startIndex - current.index) > Math.abs(startIndex - previous.index)
  ) {
    return current;
  }
  return previous;
};

/**
 * Finds the [min,max] for the amplitude for peak trough.
 *
 * @param startIndex the starting index into the array
 * @param data the array of values of data
 */
export function findMinMaxAmplitudeForPeakTrough(
  startIndex: number,
  data: number[] | Float32Array
): {
  min: { index: number; value: number };
  max: { index: number; value: number };
} {
  if (
    startIndex !== undefined &&
    data !== undefined &&
    startIndex >= 0 &&
    startIndex < data.length &&
    data.length > 0
  ) {
    const numericalData = Array.from(data);
    const valuesAndIndex = numericalData.map((value: number, index: number) => ({ index, value }));
    // eslint-disable-next-line newline-per-chained-call
    const left = valuesAndIndex.slice(0, startIndex + 1).reverse();
    const right = valuesAndIndex.slice(startIndex, data.length);

    const leftMinMax = findMinMax(left);
    const rightMinMax = findMinMax(right);
    const minMax = [leftMinMax.min, leftMinMax.max, rightMinMax.min, rightMinMax.max];

    const min = minMax.reduce((prev, curr) => minReducer(prev, curr, startIndex));

    const max = minMax.reduce((prev, curr) => maxReducer(prev, curr, startIndex));

    return min.value !== max.value
      ? { min, max }
      : {
          // handle the case for a flat line; ensure the furthest indexes
          min: {
            value: min.value,
            index: Math.min(...minMax.map(v => v.index))
          },
          max: {
            value: max.value,
            index: Math.max(...minMax.map(v => v.index))
          }
        };
  }
  return { min: { index: 0, value: 0 }, max: { index: 0, value: 0 } };
}

/**
 * Scales the amplitude measurement value.
 *
 * @param amplitudeMeasurementValue the amplitude measurement value to scale
 */
export function scaleAmplitudeMeasurementValue(
  amplitudeMeasurementValue: SignalDetectionTypes.AmplitudeMeasurementValue
): SignalDetectionTypes.AmplitudeMeasurementValue {
  if (amplitudeMeasurementValue === null && amplitudeMeasurementValue === undefined) {
    throw new Error(`amplitude measurement value must be defined`);
  }
  return {
    ...amplitudeMeasurementValue,
    amplitude: {
      ...amplitudeMeasurementValue.amplitude,
      value: scaleAmplitudeForPeakTrough(
        amplitudeMeasurementValue.amplitude.value,
        amplitudeMeasurementValue.period
      )
    }
  };
}

/**
 * Scales the amplitude value using the provided period,
 * nominal calibration period, and the frequency and amplitude values.
 *
 * @param amplitude the amplitude value to scale
 * @param period the period value
 * @param nominalCalibrationPeriod the nominal calibration period
 * @param frequencyValues the frequency values
 * @param amplitudeValues the amplitude values
 */
export function scaleAmplitudeForPeakTrough(
  amplitude: number,
  period: number,
  nominalCalibrationPeriod: number = NOMINAL_CALIBRATION_PERIOD,
  frequencyValues: number[] = FREQUENCY_VALUES,
  amplitudeValues: number[] = AMPLITUDE_VALUES
): number {
  if (
    frequencyValues === null ||
    frequencyValues === undefined ||
    frequencyValues.length === 0 ||
    amplitudeValues === null ||
    amplitudeValues === undefined ||
    amplitudeValues.length === 0
  ) {
    throw new Error(`frequency scale values and amplitude scale values must be defined`);
  }

  if (frequencyValues.length !== amplitudeValues.length) {
    throw new Error(
      `frequency scale values and amplitude scale values do not have the same length: ` +
        `[${frequencyValues.length} !== ${amplitudeValues.length}]`
    );
  }

  // calculate the period
  const periodValues = frequencyValues.map(freq => 1 / freq);

  const findClosestCorrespondingValue = (
    value: number,
    values: number[]
  ): { index: number; value: number } =>
    values
      .map((val: number, index: number) => ({ index, value: val }))
      .reduce(
        (previous: { index: number; value: number }, current: { index: number; value: number }) =>
          Math.abs(current.value - value) < Math.abs(previous.value - value) ? current : previous
      );

  const calculatedPeriod = findClosestCorrespondingValue(period, periodValues);
  const calculatedAmplitude = amplitudeValues[calculatedPeriod.index];

  const calibrationPeriod = findClosestCorrespondingValue(nominalCalibrationPeriod, periodValues);
  const calibrationAmplitude = amplitudeValues[calibrationPeriod.index];
  const normalizedAmplitude = calculatedAmplitude / calibrationAmplitude;
  return amplitude / normalizedAmplitude;
}

/**
 * Returns the waveform value and index (into the values) for a given time in seconds
 *
 * @param waveform the waveform
 * @param timeSecs the time in seconds
 */
export function getWaveformValueForTime(
  dataSegment: WeavessTypes.DataSegment,
  timeSecs: number
): { index: number; value: number } | undefined {
  const data = dataSegment?.data;
  if (data && WeavessTypes.isDataBySampleRate(data)) {
    const index =
      timeSecs <= data.startTimeSecs
        ? 0
        : Math.round((timeSecs - data.startTimeSecs) * data.sampleRate);
    return { index, value: data?.values[index] ?? 0 };
  }
  return undefined;
}

/**
 * Sorts the provided signal detections by arrival time and the
 * specified sort type.
 *
 * @param signalDetections the list of signal detections to sort
 * @param waveformSortType the sort type
 * @param distances the distance to source for each station
 */
export function sortAndOrderSignalDetections(
  signalDetections: SignalDetectionTypes.SignalDetection[],
  waveformSortType: AnalystWorkspaceTypes.WaveformSortType,
  distances: LegacyEventTypes.LocationToStationDistance[]
): SignalDetectionTypes.SignalDetection[] {
  // sort the sds by the arrival time
  const sortByArrivalTime: SignalDetectionTypes.SignalDetection[] = sortBy<
    SignalDetectionTypes.SignalDetection
  >(
    signalDetections,
    sd =>
      SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurementValue(
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses)
          .featureMeasurements
      ).arrivalTime?.value
  );

  // sort by the selected sort type
  return sortBy<SignalDetectionTypes.SignalDetection>(
    sortByArrivalTime,
    [
      sd =>
        // eslint-disable-next-line no-nested-ternary
        waveformSortType === AnalystWorkspaceTypes.WaveformSortType.distance
          ? distances.find(d => d.stationId === sd.station.name)
            ? distances.find(d => d.stationId === sd.station.name).distance
            : 0
          : sd.station.name
    ],
    waveformSortType === AnalystWorkspaceTypes.WaveformSortType.stationNameZA ? ['desc'] : ['asc']
  );
}

/**
 * Filter the signal detections for a given station.
 *
 * @param stationId the station is
 * @param signalDetectionsByStation the signal detections to filter
 */
export function filterSignalDetectionsByStationId(
  stationId: string,
  signalDetectionsByStation: SignalDetectionTypes.SignalDetection[]
): SignalDetectionTypes.SignalDetection[] {
  return signalDetectionsByStation.filter(sd => {
    // filter out the sds for the other stations and the rejected sds
    if (
      sd.station.name !== stationId ||
      SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).rejected
    ) {
      return false;
    }
    return true; // return all other sds
  });
}

/**
 * Create Channel Segment string (key used in ChannelSegmentMap) for current hypothesis
 *
 * @param signalDetection
 * @returns Channel Segment string
 */
export function getChannelSegmentStringForCurrentHypothesis(
  signalDetection: SignalDetectionTypes.SignalDetection
): string | undefined {
  const sdHypothesis = SignalDetectionTypes.Util.getCurrentHypothesis(
    signalDetection.signalDetectionHypotheses
  );
  if (!sdHypothesis) {
    return undefined;
  }
  const arrivalFM = sdHypothesis.featureMeasurements
    ? SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(sdHypothesis.featureMeasurements)
    : undefined;
  return arrivalFM ? createChannelSegmentString(arrivalFM.measuredChannelSegment.id) : undefined;
}
/**
 * Retrieve the full channel name from the signal detection
 *
 * @param signalDetection
 * @returns string channel name
 */
export function getSignalDetectionChannelName(
  signalDetection: SignalDetectionTypes.SignalDetection
): string | undefined {
  const sdHypothesis = SignalDetectionTypes.Util.getCurrentHypothesis(
    signalDetection.signalDetectionHypotheses
  );
  if (!sdHypothesis) {
    return undefined;
  }
  const arrivalFM = sdHypothesis.featureMeasurements
    ? SignalDetectionTypes.Util.findArrivalTimeFeatureMeasurement(sdHypothesis.featureMeasurements)
    : undefined;
  return arrivalFM ? arrivalFM.channel.name : undefined;
}

/**
 * Returns the station name or undefined parsed from channel segment
 * TODO: Remove this function once WeavessTypes.ChannelSegment contains the necessary field
 *
 * @param channelSegment
 * @returns station name
 */
export function parseStationNameFromChannelSegment(
  channelSegment: WeavessTypes.ChannelSegment
): string | undefined {
  if (!channelSegment) {
    return undefined;
  }
  const splitValues = channelSegment.channelName?.split('.');
  return splitValues?.length > 0 ? splitValues[0] : undefined;
}

/**
 * Returns the beam type or undefined parsed from channel name
 *
 * @param channelName
 * @returns string beam type
 */
export function parseBeamType(channelName: string): string | undefined {
  if (!channelName) {
    return undefined;
  }

  if (!channelName.includes('/')) {
    return 'Raw channel';
  }
  const elements = channelName.split('/');
  if (elements.length <= 1 || elements[1].length === 0) {
    return undefined;
  }
  if (elements[1].startsWith('beam,fk')) {
    return 'Fk beam';
  }

  if (elements[1].startsWith('beam,event')) {
    return 'Event beam';
  }

  if (elements[1].startsWith('beam,detection')) {
    return 'Detection beam';
  }
  return undefined;
}

/**
 * Given a signal detection's association status to an event and the present UI theme
 * Returns appropriate color for signal detection pick marker and details popover
 *
 * @param assocStatus
 * @param uiTheme
 * @returns
 */
export const getAssocStatusColor = (
  assocStatus: string,
  uiTheme: ConfigurationTypes.UITheme
): string => {
  if (assocStatus === EventTypes.AssociationStatus.OPEN_ASSOCIATED) {
    return uiTheme?.colors.openEventSDColor;
  }
  if (assocStatus === EventTypes.AssociationStatus.COMPLETE_ASSOCIATED) {
    return uiTheme?.colors.completeEventSDColor;
  }
  if (assocStatus === EventTypes.AssociationStatus.OTHER_ASSOCIATED) {
    return uiTheme?.colors.otherEventSDColor;
  }
  return uiTheme?.colors.unassociatedSDColor;
};

/**
 * Given a signal detection's association status to an event
 * Returns formatted string to display on signal detection details popover and SD table
 *
 * @param assocStatus
 * @returns
 */
export const getAssocStatusString = (assocStatus: string): string => {
  if (assocStatus === EventTypes.AssociationStatus.OPEN_ASSOCIATED) {
    return messageConfig.tooltipMessages.events.associatedOpen;
  }
  if (assocStatus === EventTypes.AssociationStatus.COMPLETE_ASSOCIATED) {
    return messageConfig.tooltipMessages.events.associatedComplete;
  }
  if (assocStatus === EventTypes.AssociationStatus.OTHER_ASSOCIATED) {
    return messageConfig.tooltipMessages.events.associatedOther;
  }
  if (assocStatus === EventTypes.AssociationStatus.UNASSOCIATED) {
    return messageConfig.tooltipMessages.events.unassociated;
  }
  return messageConfig.invalidCellText;
};
