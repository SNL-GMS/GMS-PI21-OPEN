/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import type { FkTypes, SignalDetectionTypes } from '@gms/common-model';
import { ChannelSegmentTypes, WaveformTypes } from '@gms/common-model';

/**
 * Checks if FK spectra channel segment
 *
 * @param object Channel Segment
 * @returns boolean
 */
// eslint-disable-next-line max-len
export function isFkSpectraChannelSegment(
  object: ChannelSegmentTypes.ChannelSegment<ChannelSegmentTypes.TimeSeries>
): object is ChannelSegmentTypes.ChannelSegment<FkTypes.FkPowerSpectra> {
  return object.timeseriesType === ChannelSegmentTypes.TimeSeriesType.FK_SPECTRA;
}

/**
 * Checks if Signal detection ArrivalTimeMeasurementValue
 *
 * @param object FeatureMeasurementValue
 * @returns boolean
 */
export function isArrivalTimeMeasurementValue(
  object: any
): object is SignalDetectionTypes.ArrivalTimeMeasurementValue {
  return object.arrivalTime !== undefined;
}

/**
 * Checks if Signal detection NumericMeasurementValue
 *
 * @param object FeatureMeasurementValue
 * @returns boolean
 */
export function isNumericMeasurementValue(
  object: any
): object is SignalDetectionTypes.NumericMeasurementValue {
  return object.measuredValue !== undefined;
}

/**
 * Checks if Signal detection PhaseMeasurementValue
 *
 * @param object FeatureMeasurementValue
 * @returns boolean
 */
export function isPhaseMeasurementValue(
  object: any
): object is SignalDetectionTypes.PhaseTypeMeasurementValue {
  return object.value !== undefined && object.standardDeviation !== undefined;
}

/**
 * Checks if Signal detection AmplitudeMeasurementValue
 *
 * @param object FeatureMeasurementValue
 * @returns boolean
 */
export function isAmplitudeFeatureMeasurementValue(
  object: any
): object is SignalDetectionTypes.AmplitudeMeasurementValue {
  return (
    object.amplitude !== undefined && object.period !== undefined && object.startTime !== undefined
  );
}

/**
 * Creates/Returns an unfiltered waveform filter
 */
export function createUnfilteredWaveformFilter(): WaveformTypes.WaveformFilter {
  return (WaveformTypes.UNFILTERED_FILTER as any) as WaveformTypes.WaveformFilter;
}
