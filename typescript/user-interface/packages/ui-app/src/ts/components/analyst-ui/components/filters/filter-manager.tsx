import {
  useFilterCycle,
  useGetFilterListsDefinitionQuery,
  useKeyboardShortcutConfig
} from '@gms/ui-state';
import type React from 'react';
import { useHotkeys } from 'react-hotkeys-hook';

/**
 * Creates a component that fetches the filter lists and listens for the filter hotkeys.
 */
// eslint-disable-next-line react/function-component-definition
export const FilterManager: React.FC = () => {
  useGetFilterListsDefinitionQuery();
  const { selectNextFilter, selectPreviousFilter, selectUnfiltered } = useFilterCycle();
  const keyboardShortcutConfig = useKeyboardShortcutConfig();
  const selectNextFilterHotkeyConfig = keyboardShortcutConfig?.selectNextFilter;
  const selectPreviousFilterHotkeyConfig = keyboardShortcutConfig?.selectPreviousFilter;
  const selectUnfilteredHotkeyConfig = keyboardShortcutConfig?.selectUnfiltered;
  useHotkeys(selectNextFilterHotkeyConfig?.hotkeys ?? 'f', selectNextFilter, {}, [
    selectNextFilter,
    selectNextFilterHotkeyConfig?.hotkeys
  ]);
  useHotkeys(selectPreviousFilterHotkeyConfig?.hotkeys ?? 'shift+f', selectPreviousFilter, {}, [
    selectPreviousFilter,
    selectPreviousFilterHotkeyConfig?.hotkeys
  ]);
  useHotkeys(selectUnfilteredHotkeyConfig?.hotkeys ?? 'alt+f, option+f', selectUnfiltered, {}, [
    selectUnfiltered,
    selectUnfilteredHotkeyConfig?.hotkeys
  ]);
  return null;
};
