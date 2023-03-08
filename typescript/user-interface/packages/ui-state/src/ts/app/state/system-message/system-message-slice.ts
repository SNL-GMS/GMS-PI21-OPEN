import type { SystemMessageTypes } from '@gms/common-model';
import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { SystemMessageState } from './types';

/**
 * The initial state for the system message state.
 */
export const systemMessageInitialState: SystemMessageState = {
  lastUpdated: null,
  systemMessages: null,
  latestSystemMessages: null,
  isSoundEnabled: true
};

/**
 * The system message reducer slice.
 */
export const systemMessageSlice = createSlice({
  name: 'systemMessage',
  initialState: systemMessageInitialState,
  reducers: {
    /**
     * Sets the timestamp for last updated for the system messages
     *
     * @param state the state
     * @param action the action
     */
    setLastUpdated(state, action: PayloadAction<number>) {
      state.lastUpdated = action.payload;
    },

    /**
     * Sets the latest system messages
     *
     * @param state the state
     * @param action the action
     */
    setLatestSystemMessages(state, action: PayloadAction<SystemMessageTypes.SystemMessage[]>) {
      state.latestSystemMessages = action.payload;
    },

    /**
     * Sets the system messages
     *
     * @param state the state
     * @param action the action
     */
    setSystemMessages(state, action: PayloadAction<SystemMessageTypes.SystemMessage[]>) {
      state.systemMessages = action.payload;
    },

    /**
     * Sets whether sounds are enabled
     */
    setIsSoundEnabled(state, action: PayloadAction<boolean>) {
      state.isSoundEnabled = action.payload;
    }
  }
});

/**
 * The system message actions.
 */
export const systemMessageActions = systemMessageSlice.actions;
