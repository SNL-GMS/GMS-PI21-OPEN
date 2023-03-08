import { WaveformTypes } from '@gms/common-model';
import { WeavessTypes, WeavessUtil } from '@gms/weavess-core';
import * as d3 from 'd3';

import type { AmplitudeBounds } from '../types';
import { WaveformStore } from '../worker-store/waveform-store';

/**
 * Merges two ChannelSegmentBoundaries so that the new set encompasses the two provided sets of bounds.
 *
 * @param existingBounds one set of bounds
 * @param newBounds another set of bounds
 * @returns a set of bounds which encompasses both sets of bounds
 */
export const mergeBounds = (
  existingBounds: WeavessTypes.ChannelSegmentBoundaries,
  newBounds: WeavessTypes.ChannelSegmentBoundaries | undefined
): WeavessTypes.ChannelSegmentBoundaries => {
  if (!newBounds) {
    return existingBounds;
  }
  if (!existingBounds) {
    return newBounds;
  }
  const bottomMax = Math.min(existingBounds.bottomMax, newBounds.bottomMax);
  const bottomMaxSecs =
    existingBounds.bottomMax < newBounds.bottomMax
      ? existingBounds.bottomMaxSecs
      : newBounds.bottomMaxSecs;
  const topMax = Math.max(existingBounds.topMax, newBounds.topMax);
  const topMaxSecs =
    existingBounds.topMax > newBounds.topMax ? existingBounds.topMaxSecs : newBounds.topMaxSecs;

  return {
    channelSegmentId: existingBounds.channelSegmentId,
    bottomMax,
    topMax,
    offset: Math.max(Math.abs(topMax), Math.abs(bottomMax)),
    channelAvg: 0,
    topMaxSecs,
    bottomMaxSecs
  };
};

/**
 * Return the channelSegmentBoundaries if the Data Segments were converted to
 * typed arrays and if the Amplitude min, max and average are set
 *
 * @param param0 AmplitudeBounds object for the channelSegment
 * @returns the channel segment boundaries, or undefined if the amplitude bounds were not set
 */
export const buildChannelSegmentBounds = ({
  amplitudeMax,
  amplitudeMin,
  amplitudeMaxSecs,
  amplitudeMinSecs
}: AmplitudeBounds): WeavessTypes.ChannelSegmentBoundaries => {
  if (amplitudeMax > -Infinity && amplitudeMin < Infinity) {
    return {
      topMax: amplitudeMax,
      bottomMax: amplitudeMin,
      channelAvg: 0, // as per guidance, we always want to be centered on 0
      offset: amplitudeMax, // we have already calculated this as the max absolute value of max and min
      channelSegmentId: WaveformTypes.UNFILTERED_FILTER.name,
      topMaxSecs: amplitudeMaxSecs,
      bottomMaxSecs: amplitudeMinSecs
    };
  }
  return undefined;
};

/**
 * Function to aid with indexing into a position buffer (x y x y) given a time domain.
 *
 * @param dataLength - Number of total points (the *entire* length of the position buffer)
 * @param dataDomain - Two points that indicate the time stamp at the beginning and end of the array
 * @param startTimeSecs - Start of the windowed time. The values are clamped to the minimum range
 * @param endTimeSecs - End of the windowed time. The values are clamped to the maximum range
 * @returns a [startIdx, endIdx] array. These values are the inclusive subset of the windowed range. The values are in range from 1 to dataLength - 1.
 */
export function scaleToPositionBufferIndex(
  dataLength: number,
  dataDomain: number[],
  startTimeSecs?: number,
  endTimeSecs?: number
): number[] {
  if (startTimeSecs > endTimeSecs)
    throw new Error(
      `scaleToPositionBufferIndex: startTimeSecs must be greater than or equal to endTimeSecs, was passed startTimeSecs = ${startTimeSecs}, endTimeSecs = ${endTimeSecs}`
    );
  const lastIdx = dataLength / 2 - 1;
  const scale = d3.scaleLinear().domain(dataDomain).rangeRound([0, lastIdx]).clamp(true);

  let startIdx = startTimeSecs === undefined ? 0 : scale(startTimeSecs);
  let endIdx = endTimeSecs === undefined ? lastIdx : scale(endTimeSecs);

  // convert index to x y x y
  startIdx = startIdx * 2 + 1;
  endIdx = endIdx * 2 + 1;

  return [startIdx, endIdx];
}

/**
 * Calculates the boundaries for a weavess data segment claim check.
 *
 * @param dataByClaim the data using a DataClaimCheck under the hood
 * @param startTimeSecs the start time in epoch seconds. Will default to the start of the data if not provided.
 * @param endTimeSecs the end time in epoch seconds. Will default to the end of the data if not provided.
 * @returns a promise for the calculated boundaries
 */
export const calculateDataSegmentBounds = async (
  dataByClaim: WeavessTypes.DataBySampleRate,
  startTimeSecs?: number,
  endTimeSecs?: number
): Promise<AmplitudeBounds> => {
  if (WeavessTypes.isDataClaimCheck(dataByClaim)) {
    const posBuffer = await WaveformStore.retrieve(dataByClaim.id);
    if (!posBuffer) {
      throw new Error(`Cannot calculate DataSegment bounds for id ${dataByClaim.id}`);
    }
    const domain = [dataByClaim.startTimeSecs, dataByClaim.endTimeSecs];
    const [startIdx, endIdx] = scaleToPositionBufferIndex(
      posBuffer.length,
      domain,
      startTimeSecs,
      endTimeSecs
    );
    const { max, maxSecs, min, minSecs } = WeavessUtil.getBoundsForPositionBuffer(
      posBuffer,
      startIdx,
      endIdx
    );
    const offset = Math.max(Math.abs(max), Math.abs(min));
    return {
      amplitudeMax: offset,
      amplitudeMin: -offset,
      amplitudeMaxSecs: maxSecs,
      amplitudeMinSecs: minSecs
    };
  }
  throw new Error('Invalid channel segment type: Channel segment must be of DataClaimCheck type.');
};

/**
 * Checks to see if a data segment has data between a provided start and end time, inclusive.
 *
 * @param startTimeSecs in epoch seconds
 * @param endTimeSecs in epoch seconds
 * @param dataSeg a DataSegment that wraps a DataClaimCheck
 * @returns whether the data segment has data in the range provided, inclusive. Returns undefined if given bad data.
 */
const isDataSegmentWithinWindow = (
  startTimeSecs: number,
  endTimeSecs: number,
  dataSeg: WeavessTypes.DataSegment
) =>
  WeavessTypes.isDataClaimCheck(dataSeg.data) &&
  dataSeg.data.startTimeSecs <= endTimeSecs &&
  dataSeg.data.endTimeSecs >= startTimeSecs;

/**
 * Filters the data segments provided to only include data by claim check that includes
 * one or more points between the start and end times provided (inclusive).
 *
 * @param channelSegment a channel segment to filter
 * @param startTimeSecs in epoch seconds
 * @param endTimeSecs in epoch seconds
 * @returns a filtered array of dataSegments.
 */
const getDataSegmentsWithinWindow = (
  channelSegment: WeavessTypes.ChannelSegment,
  startTimeSecs: number,
  endTimeSecs: number
) =>
  channelSegment.dataSegments.filter((dataSeg: WeavessTypes.DataSegment) => {
    if (WeavessTypes.isDataClaimCheck(dataSeg.data)) {
      return isDataSegmentWithinWindow(
        startTimeSecs ?? dataSeg.data.startTimeSecs,
        endTimeSecs ?? dataSeg.data.endTimeSecs,
        dataSeg
      );
    }
    throw new Error(
      'Cannot calculate bounds for channel segment that is not of the claim check type'
    );
  });

/**
 * For a channel segment, gets all boundaries for any data that falls within the provided start and end times.
 *
 * @param channelSegment
 * @param startTimeSecs in epoch seconds
 * @param endTimeSecs in epoch seconds
 * @returns an array of the boundaries that fall within the range provided.
 * @throws if the data is not formatted as the DataByClaimCheck type
 */
const getAllBoundsWithinWindow = async (
  channelSegment: WeavessTypes.ChannelSegment,
  startTimeSecs: number,
  endTimeSecs: number
) => {
  return Promise.all(
    getDataSegmentsWithinWindow(channelSegment, startTimeSecs, endTimeSecs).map(async dataSeg => {
      if (WeavessTypes.isDataClaimCheck(dataSeg.data)) {
        return buildChannelSegmentBounds(
          await calculateDataSegmentBounds(
            dataSeg.data,
            startTimeSecs ?? dataSeg.data.startTimeSecs,
            endTimeSecs ?? dataSeg.data.endTimeSecs
          )
        );
      }
      throw new Error(
        'Cannot calculate bounds for channel segment that is not of the claim check type'
      );
    })
  );
};

/**
 * Calculate the channel segment boundaries for the given channel segment. Will merge them with any existing boundaries.
 *
 * @throws if the channelSegment is undefined, or if it does not contain dataSegments, or if the data segments do not contain a DataClaimCheck.
 * @param id the id of this channel segment (name), used to check to see if any channel segment boundaries already exist
 * @param channelSegment the channelSegment for which to calculate the boundaries
 * @param store the store from which to get any existing channel segment boundaries to use for merging with the new ones.
 * @returns a promise for the computed boundaries, which can be undefined if no boundaries were found.
 */
export const calculateChannelSegBounds = async (
  channelSegment: WeavessTypes.ChannelSegment,
  startTimeSecs?: number,
  endTimeSecs?: number
): Promise<WeavessTypes.ChannelSegmentBoundaries | undefined> => {
  if (!channelSegment || !channelSegment.dataSegments) {
    throw new Error('Cannot calculate bounds for invalid or undefined channel segment');
  }
  const allBounds = await getAllBoundsWithinWindow(channelSegment, startTimeSecs, endTimeSecs);
  // guard against length of 0, in case no bounds were found. If we return undefined,
  // then merging it will take any existing bounds over this result.
  return allBounds.length > 0 ? allBounds.reduce(mergeBounds) : undefined;
};

/**
 * Function to calculate boundaries for a windowed view of channel segment data.
 * If the startTime or endTime are not included, then the min/max of the position
 * buffer is used.
 *
 * @param channelSegment - The channel segment used to calculate the boundaries
 * @param startTimeSecs - A start time in seconds for the window.
 * @param endTimeSecs - An end time in seconds for the window.
 * @returns a promise to the channel segment boundaries object for this channel segment given a bounding time window.
 */
export async function calculateWindowedBounds(
  channelSegment: WeavessTypes.ChannelSegment,
  startTimeSecs?: number,
  endTimeSecs?: number
): Promise<WeavessTypes.ChannelSegmentBoundaries> {
  return calculateChannelSegBounds(channelSegment, startTimeSecs, endTimeSecs);
}
