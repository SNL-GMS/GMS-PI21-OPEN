import { createEnumTypeGuard } from '@gms/common-util';
import Immutable from 'immutable';

export enum CommonKeyAction {
  OPEN_COMMAND_PALETTE = 'Open Command Palette'
}

export const CommonKeyActions: Immutable.Map<string, CommonKeyAction> = Immutable.Map([
  ['Control+Shift+KeyX', CommonKeyAction.OPEN_COMMAND_PALETTE],
  ['Control+Shift+KeyP', CommonKeyAction.OPEN_COMMAND_PALETTE]
]);

export enum GLDisplayState {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED'
}

export const isCommonKeyAction = createEnumTypeGuard(CommonKeyAction);

export interface CommonState {
  commandPaletteIsVisible: boolean;
  keyboardShortcutsVisibility: boolean;
  keyPressActionQueue: Record<string, number>;
  selectedStationIds: string[];
  glLayoutState: Record<string, GLDisplayState>;
}
