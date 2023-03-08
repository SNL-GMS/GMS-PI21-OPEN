import type { WeavessTypes } from '@gms/weavess-core';

/**
 * @returns true if the channel segments provided have data segments,
 * and if those data segments have pre-calculated boundaries.
 */
export const hasUserProvidedBoundaries = (
  channelSegments: WeavessTypes.ChannelSegment[]
): boolean => {
  if (channelSegments && channelSegments.length > 0) {
    return channelSegments.every(
      channelSegment => channelSegment.channelSegmentBoundaries !== undefined
    );
  }
  return false;
};

/**
 * Searches thru each channel segment's channelSegmentBoundaries finding the min/max amplitudes
 * Note assumes there are populated channelSegmentBoundaries in the channelSegments (hasUserProvidedBoundaries)
 *
 * @param channelSegments
 * @returns found min/max amplitudes
 */
export const getMinMaxAmplitudes = (
  channelSegments: WeavessTypes.ChannelSegment[]
): { minAmplitude: number; maxAmplitude: number } => {
  let minAmplitude = Infinity;
  let maxAmplitude = -Infinity;
  channelSegments.forEach(channelSegment => {
    if (
      channelSegment.channelSegmentBoundaries?.bottomMax &&
      channelSegment.channelSegmentBoundaries.bottomMax < minAmplitude
    ) {
      minAmplitude = channelSegment.channelSegmentBoundaries.bottomMax;
    }
    if (
      channelSegment.channelSegmentBoundaries?.topMax &&
      channelSegment.channelSegmentBoundaries.topMax > maxAmplitude
    ) {
      maxAmplitude = channelSegment.channelSegmentBoundaries.topMax;
    }
  });

  return { minAmplitude, maxAmplitude };
};
