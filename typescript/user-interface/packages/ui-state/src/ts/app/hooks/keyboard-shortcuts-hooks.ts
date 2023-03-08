import * as React from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { batch } from 'react-redux';

import { commonActions } from '../state/common/common-slice';
import { isCommandPaletteOpen, isKeyboardShortcutPopupOpen } from '../state/common/selectors';
import { useAppDispatch, useAppSelector } from './react-redux-hooks';

/**
 * Creates a set of helper functions for manipulating the keyboard dialog state.
 * Functions are referentially stable.
 *
 * @returns toggleKeyboardShortcuts a function to turn on and off the keyboard shortcuts dialog,
 * isKeyboardShortcutsDialogOpen a function to determine if the dialog is open
 * closeKeyboardShortcuts closes the dialog
 * openKeyboardShortcuts opens the dialog
 */
export const useKeyboardShortcutsDisplayVisibility = (): {
  toggleKeyboardShortcuts: () => void;
  isKeyboardShortcutsDialogOpen: boolean;
  closeKeyboardShortcuts: () => void;
  openKeyboardShortcuts: () => void;
} => {
  const dispatch = useAppDispatch();
  const areKeyboardShortcutsVisible = useAppSelector(isKeyboardShortcutPopupOpen);
  const isCommandPaletteVisible = useAppSelector(isCommandPaletteOpen);
  const toggleKeyboardShortcuts = React.useCallback(() => {
    if (!isCommandPaletteVisible) {
      dispatch(commonActions.setKeyboardShortcutsVisibility(!areKeyboardShortcutsVisible));
    }
  }, [isCommandPaletteVisible, dispatch, areKeyboardShortcutsVisible]);
  const closeKeyboardShortcuts = React.useCallback(() => {
    dispatch(commonActions.setKeyboardShortcutsVisibility(false));
  }, [dispatch]);
  const openKeyboardShortcuts = React.useCallback(() => {
    batch(() => {
      if (isCommandPaletteVisible) {
        dispatch(commonActions.setCommandPaletteVisibility(false));
      }
      dispatch(commonActions.setKeyboardShortcutsVisibility(true));
    });
  }, [dispatch, isCommandPaletteVisible]);
  useHotkeys('control+/, command+/', toggleKeyboardShortcuts, {}, [toggleKeyboardShortcuts]);
  return {
    toggleKeyboardShortcuts,
    isKeyboardShortcutsDialogOpen: areKeyboardShortcutsVisible,
    closeKeyboardShortcuts,
    openKeyboardShortcuts
  };
};
