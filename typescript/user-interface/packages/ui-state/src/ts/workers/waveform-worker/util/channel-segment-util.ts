import { ChannelSegmentTypes, WaveformTypes } from '@gms/common-model';
import type { FkPowerSpectra } from '@gms/common-model/lib/fk/types';
import type { Waveform } from '@gms/common-model/lib/waveform/types';
import { WeavessTypes } from '@gms/weavess-core';

import type { UiChannelSegment } from '../../../types';
import type { ChannelSegmentColorOptions } from '../types';
import { formatAndStoreDataSegments } from './data-segment-util';

/**
 * Higher order function that generates a converter that converts waveforms to typed arrays
 * within the given time range (domain).
 *
 * @param domain the low to high bound (inclusive) of timestamps visible in the window
 * @returns a converter function that will return the Weavess.ChannelSegment
 */
export function channelSegToWeavessChannelSegment(domain: WeavessTypes.TimeRange) {
  return async function convertWaveformToTypedArray(
    chanSeg: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>,
    colors: ChannelSegmentColorOptions
  ): Promise<UiChannelSegment> {
    const dataSegments = await formatAndStoreDataSegments(chanSeg, domain, colors.waveformColor);
    const uiChannelSegment: UiChannelSegment = {
      channelSegment: {
        channelName: chanSeg.id.channel.name,
        wfFilterId: WeavessTypes.UNFILTERED,
        isSelected: false,
        description: WaveformTypes.UNFILTERED_FILTER.name,
        descriptionLabelColor: colors.labelTextColor,
        dataSegments,
        channelSegmentBoundaries: undefined
      },
      channelSegmentDescriptor: chanSeg.id
    };
    return uiChannelSegment;
  };
}

/**
 * Converts the channel segment into the TypedArray format that Weavess can render.
 *
 * @param chanSegment the waveform channel segment to convert.
 * @param originalDomain the start and end times of the viewable time span in the Weavess Display
 *
 * @returns UiChannelSegment
 */
export const convertChannelSegmentToWeavessTypedArray = async (
  chanSegment: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>,
  originalDomain: WeavessTypes.TimeRange,
  colors: ChannelSegmentColorOptions
): Promise<UiChannelSegment> => {
  const converter = channelSegToWeavessChannelSegment(originalDomain);
  return converter(chanSegment, colors);
};

/**
 * Converts the channel segments into the TypedArray format that Weavess can render.
 *
 * @param chanSegments the list of waveform channel segments to convert.
 * @param originalDomain the start and end times of the viewable time span in the Weavess Display
 */
export const convertChannelSegmentsToWeavessTypedArrays = async (
  chanSegments: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[],
  originalDomain: WeavessTypes.TimeRange,
  colors: ChannelSegmentColorOptions
): Promise<UiChannelSegment[]> => {
  return Promise.all(
    chanSegments.map(async channelSegment =>
      convertChannelSegmentToWeavessTypedArray(channelSegment, originalDomain, colors)
    )
  );
};

/**
 * Checks if FK spectra channel segment
 *
 * @param object Channel Segment
 * @returns boolean
 */
export function isFkSpectraChannelSegment(
  object: ChannelSegmentTypes.ChannelSegment<ChannelSegmentTypes.TimeSeries>
): object is ChannelSegmentTypes.ChannelSegment<FkPowerSpectra> {
  return object.timeseriesType === ChannelSegmentTypes.TimeSeriesType.FK_SPECTRA;
}

/**
 * Checks if FK Spectra time series
 *
 * @param object Time Series
 * @param type Time Series Type
 * @returns boolean
 */
export function isFkSpectraTimeSeries(
  object: ChannelSegmentTypes.TimeSeries,
  type: ChannelSegmentTypes.TimeSeriesType
): object is FkPowerSpectra {
  return type === ChannelSegmentTypes.TimeSeriesType.FK_SPECTRA;
}

/**
 * Checks if Waveforms time series
 *
 * @param object Time Series
 * @param type Time Series Type
 * @returns boolean
 */
export function isWaveformTimeSeries(
  object: ChannelSegmentTypes.TimeSeries,
  type: ChannelSegmentTypes.TimeSeriesType
): object is Waveform {
  return type === ChannelSegmentTypes.TimeSeriesType.WAVEFORM;
}

/**
 * Checks if waveform channel segment
 *
 * @param object Channel segment
 * @returns boolean
 */
export function isWaveformChannelSegment(
  object: ChannelSegmentTypes.ChannelSegment<ChannelSegmentTypes.TimeSeries>
): object is ChannelSegmentTypes.ChannelSegment<Waveform> {
  return (
    object?.timeseries && object.timeseriesType === ChannelSegmentTypes.TimeSeriesType.WAVEFORM
  );
}

/**
 * Checks if time series
 *
 * @param object Time series or time series OSD representation
 * @returns boolean
 */
export function isTimeSeries(
  object: ChannelSegmentTypes.TimeSeries
): object is ChannelSegmentTypes.TimeSeries {
  return typeof object.startTime === 'number';
}

/**
 * Create a unique string
 *
 * @param id ChannelSegmentDescriptor
 * @returns unique string representing the ChannelSegmentDescriptor
 */
export function createChannelSegmentString(
  id: ChannelSegmentTypes.ChannelSegmentDescriptor
): string {
  return `${id.channel.name}.${id.channel.effectiveAt}.${id.creationTime}.${id.startTime}.${id.endTime}`;
}
