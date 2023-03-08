import type { AppState } from '../../store';

export const isKeyboardShortcutPopupOpen = (state: AppState): boolean =>
  state.app.common.keyboardShortcutsVisibility;

export const isCommandPaletteOpen = (state: AppState): boolean =>
  state.app.common.commandPaletteIsVisible;
