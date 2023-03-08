import type { WeavessTypes } from '@gms/weavess-core';

import { calculateChannelSegBounds } from '../util/boundary-util';

export interface GetBoundariesParams {
  channelSegment?: WeavessTypes.ChannelSegment;
  startTimeSecs?: number;
  endTimeSecs?: number;
}

/**
 * Retrieves the boundaries object from the worker store, calculating it if it has not already been calculated.
 *
 * @param params an object containing the ID of the channel segment for which to get boundaries
 * @returns a promise for the boundaries object.
 */
export const getBoundaries = async ({
  channelSegment,
  startTimeSecs,
  endTimeSecs
}: GetBoundariesParams): Promise<WeavessTypes.ChannelSegmentBoundaries> => {
  return calculateChannelSegBounds(channelSegment, startTimeSecs, endTimeSecs);
};
