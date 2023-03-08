import type { Draft } from 'immer';

import type { UiChannelSegment } from '../../../../types';

/**
 * Mutates, applies a channel segment result to an existing channel segment record.
 * ! Mutates the draft in place
 *
 * @param draft the Immer writable channel segment record draft
 * @param channelName the unique channel name to associate to channel segment records
 * @param uiChannelSegment the channel segment to add/update
 */
const mutateChannelSegment = (
  draft: Draft<Record<string, Record<string, UiChannelSegment[]>>>,
  channelName: string,
  uiChannelSegment: UiChannelSegment
): void => {
  if (channelName && uiChannelSegment) {
    const filterId = uiChannelSegment.channelSegment.wfFilterId;
    if (filterId) {
      // If haven't seen the channel
      if (!draft[channelName]) {
        draft[channelName] = {};
      }

      const channelEntry = draft[channelName];

      // If we haven't seen this filter id entry in the dictionary
      if (!channelEntry[filterId]) {
        channelEntry[filterId] = [];
      }

      channelEntry[filterId].push(uiChannelSegment);
    }
  }
};

/**
 * Builds an immer recipe to apply channel segment results to channel segment record.
 *
 * @param channelName the unique channel name to associate to channel segment records
 * @param uiChannelSegments the channel segments to add/update
 * @returns Immer produce function
 */
export const createRecipeToMutateUiChannelSegmentsRecord = (
  channelName: string,
  uiChannelSegments: UiChannelSegment[]
): ((draft: Draft<Record<string, Record<string, UiChannelSegment[]>>>) => void) => {
  return (draft: Draft<Draft<Record<string, Record<string, UiChannelSegment[]>>>>) => {
    if (channelName && uiChannelSegments) {
      uiChannelSegments.forEach(uiChannelSegment => {
        mutateChannelSegment(draft, channelName, uiChannelSegment);
      });
    }
  };
};
