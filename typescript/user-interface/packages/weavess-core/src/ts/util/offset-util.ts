import type { Station } from '../types';

/**
 * @param stations the list of WeavessStations to be displayed
 * @returns the min and max offset times from all channels
 */
export const calculateMinMaxOffsets = (
  stations: Station[]
): { minOffset: number; maxOffset: number } => {
  let minOffset = Math.min(
    ...stations.map(s =>
      s.defaultChannel.timeOffsetSeconds ? s.defaultChannel.timeOffsetSeconds : 0
    ),
    ...stations.map(s =>
      s.nonDefaultChannels
        ? Math.min(
            ...s.nonDefaultChannels.map(c => (c.timeOffsetSeconds ? c.timeOffsetSeconds : 0))
          )
        : 0
    )
  );
  minOffset = minOffset < 0 ? minOffset : 0;

  let maxOffset = Math.max(
    ...stations.map(s =>
      s.defaultChannel.timeOffsetSeconds ? s.defaultChannel.timeOffsetSeconds : 0
    ),
    ...stations.map(s =>
      s.nonDefaultChannels
        ? Math.max(
            ...s.nonDefaultChannels.map(c => (c.timeOffsetSeconds ? c.timeOffsetSeconds : 0))
          )
        : 0
    )
  );
  maxOffset = maxOffset > 0 ? maxOffset : 0;
  return { minOffset, maxOffset };
};
