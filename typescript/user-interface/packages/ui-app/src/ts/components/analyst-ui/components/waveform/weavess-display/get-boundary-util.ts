/**
 * Creates a key that is derived from the inputs and is unique given those inputs.
 *
 * @param channelId the id (name) of the channel
 * @param startTimeSecs the start time for the request
 * @param endTimeSecs the end time for the request
 * @returns a deterministic ID generated from these inputs.
 */
export const getBoundaryCacheKey = (channelId: string, isMeasureWindow = false): string =>
  isMeasureWindow ? `${channelId}-measure-window` : channelId;
