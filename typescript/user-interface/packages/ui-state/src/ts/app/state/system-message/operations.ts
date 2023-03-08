import type { SystemMessageTypes } from '@gms/common-model';
import orderBy from 'lodash/orderBy';
import reverse from 'lodash/reverse';
import { batch } from 'react-redux';

import type { AppDispatch, AppState } from '../../store';
import { systemMessageSlice } from './system-message-slice';
import type { SystemMessageState } from './types';

/**
 * Redux operation that adds system messages to the state.
 *
 * @param messages the system messages to add
 * @param limit (optional) limit the number of messages in the redux state
 * when adding new messages; if set when adding new messages the message list
 * result will not be larger than the size of the `limit` specified.
 * @param pageSizeBuffer (optional) the size of the page buffer; if specified and the
 * limit is reached then messages will be removed at increments of the page buffer size
 */
export const addSystemMessages = (
  messages: SystemMessageTypes.SystemMessage[],
  limit?: number /* default to delete zero messages */,
  pageSizeBuffer?: number
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  const state: SystemMessageState = getState().app.systemMessage;
  if (messages && messages.length > 0) {
    // batch the dispatches - this will only result in one combined re-render, not two
    batch(() => {
      const allMessages = state.systemMessages
        ? [...state.systemMessages, ...messages]
        : [...messages];

      // apply the size limit if necessary before updating the redux state
      const actualLimit = limit && limit > 0 ? limit : 0;
      if (actualLimit > 0 && allMessages.length > actualLimit) {
        const actualPageSizeBuffer =
          pageSizeBuffer && pageSizeBuffer > 0 && pageSizeBuffer < limit ? pageSizeBuffer : 0;
        if (actualPageSizeBuffer > 0) {
          // remove page size increments until the number of messages is less than the limit
          let updatedMessages = reverse([...allMessages]);
          while (updatedMessages.length > limit) {
            updatedMessages = updatedMessages.splice(
              0,
              updatedMessages.length - actualPageSizeBuffer
            );
          }
          dispatch(systemMessageSlice.actions.setSystemMessages(reverse([...updatedMessages])));
        } else {
          // ensure that the list size is equal to the `limit`
          dispatch(
            systemMessageSlice.actions.setSystemMessages(
              reverse(reverse(allMessages).splice(0, limit))
            )
          );
        }
      } else {
        dispatch(systemMessageSlice.actions.setSystemMessages(allMessages));
      }
      dispatch(
        systemMessageSlice.actions.setLatestSystemMessages(
          orderBy<SystemMessageTypes.SystemMessage>(messages, ['time'], ['desc'])
        )
      );
      dispatch(systemMessageSlice.actions.setLastUpdated(Date.now()));
    });
  }
};

/**
 * Redux operation that clears all system messages to the state.
 */
export const clearAllSystemMessages = () => (dispatch: AppDispatch): void => {
  // batch the dispatches - this will only result in one combined re-render, not two
  batch(() => {
    dispatch(systemMessageSlice.actions.setSystemMessages([]));
  });
};
