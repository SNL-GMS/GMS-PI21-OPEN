import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';
import sortBy from 'lodash/sortBy';

import type { CommonState, GLDisplayState } from './types';

/**
 * The initial state for the common state.
 */
export const commonInitialState: CommonState = {
  commandPaletteIsVisible: false,
  keyboardShortcutsVisibility: false,
  keyPressActionQueue: {},
  selectedStationIds: [],
  glLayoutState: {}
};

/**
 * The common reducer slice.
 */
export const commonSlice = createSlice({
  name: 'common',
  initialState: commonInitialState,
  reducers: {
    /**
     * Sets the command palette visibility
     *
     * @param state the state
     * @param action the action
     */
    setCommandPaletteVisibility(state, action: PayloadAction<boolean>) {
      state.commandPaletteIsVisible = action.payload;
    },

    /**
     * Sets the keyboard shortcuts dialog visibility
     */
    setKeyboardShortcutsVisibility(state, action: PayloadAction<boolean>) {
      state.keyboardShortcutsVisibility = action.payload;
    },

    /**
     * Sets key press action queue
     *
     * @param state the state
     * @param action the action
     */
    setKeyPressActionQueue(state, action: PayloadAction<Record<string, number>>) {
      state.keyPressActionQueue = action.payload;
    },

    /**
     * Sets the selected station ids
     *
     * @param state the state
     * @param action the action
     */
    setSelectedStationIds(state, action: PayloadAction<string[]>) {
      state.selectedStationIds = sortBy(action.payload);
    },

    /**
     * Sets the golden layout state
     *
     * @param state the state
     * @param action the action
     */
    setGlLayoutState(state, action: PayloadAction<Record<string, GLDisplayState>>) {
      state.glLayoutState = action.payload;
    }
  }
});

/**
 * The common actions.
 */
export const commonActions = commonSlice.actions;
