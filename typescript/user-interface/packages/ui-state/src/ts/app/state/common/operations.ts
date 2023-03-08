import { Displays } from '@gms/common-model';
import { areListsSame, Timer } from '@gms/common-util';
import produce from 'immer';
import { batch } from 'react-redux';

import type { AppDispatch, AppState } from '../../store';
import { commonSlice } from './common-slice';
import { isKeyboardShortcutPopupOpen } from './selectors';
import type { CommonState, GLDisplayState } from './types';

// return true if the visibility has changed, false otherwise
const commandPaletteVisibilityHasChanged = (visibility: boolean, currentVisibility: boolean) =>
  visibility !== currentVisibility;

/**
 * Redux operation to set the visibility of the command palette
 *
 * @param visibility the true/false state of whether or not the command palette should be visible
 */
export const setCommandPaletteVisibility = (visibility: boolean) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const state = getState();
  const { common } = state.app;
  if (commandPaletteVisibilityHasChanged(visibility, common.commandPaletteIsVisible)) {
    if (isKeyboardShortcutPopupOpen(state) && visibility) {
      dispatch(commonSlice.actions.setKeyboardShortcutsVisibility(false));
    }
    dispatch(commonSlice.actions.setCommandPaletteVisibility(visibility));
  }
};

/**
 * @param ids new set of selected ids
 * @returns whether they are the same, irrespective of order
 */
export const idsHaveChanged = (ids: string[]): ((getState: () => AppState) => boolean) => (
  getState: () => AppState
): boolean => {
  Timer.start('[common workspace operations] idsHaveChanged');
  // Sorting to ensure actual different selections ex: [2, 1] [1, 2] are not really different
  const state: CommonState = getState().app.common;
  const equal = !areListsSame(ids, state.selectedStationIds);
  Timer.end('[common workspace operations] idsHaveChanged');
  return equal;
};

/**
 * Overwrites the selected stations in the Redux state.
 *
 * @param ids the ids to set
 */
export const setSelectedStationIds = (ids: string[]) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  if (idsHaveChanged(ids)(getState)) {
    dispatch(commonSlice.actions.setSelectedStationIds(ids));
  }
};

/**
 * Sets a single display's state in the glLayoutState tracked in Redux.
 *
 * @param displayName a string uniquely identifying a golden layout display
 * @param displayState the state of the display (open, closed, etc)
 * @returns a dispatch function that dispatches the state update.
 */
export const setGlDisplayState = (displayName: string, displayState: GLDisplayState) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  if (displayName && displayState && Displays.isValidDisplayName(displayName)) {
    batch(() => {
      const { glLayoutState } = getState().app.common;
      const newLayoutState = produce(glLayoutState, draft => {
        draft[displayName] = displayState;
      });
      dispatch(commonSlice.actions.setGlLayoutState(newLayoutState));
    });
  } else if (displayName && !Displays.isValidDisplayName(displayName)) {
    throw new Error(`Invalid display name: ${displayName} is not a name of a known display.`);
  }
};
