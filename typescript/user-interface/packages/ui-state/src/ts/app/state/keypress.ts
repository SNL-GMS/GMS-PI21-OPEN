import type * as React from 'react';

import * as AnalystWorkspaceTypes from './analyst/types';
import * as CommonWorkspaceTypes from './common/types';
import type { DataAcquisitionKeyAction } from './data-acquisition/types';
import { DataAcquisitionKeyActions } from './data-acquisition/types';

export type GenericKeyAction =
  | AnalystWorkspaceTypes.AnalystKeyAction
  | CommonWorkspaceTypes.CommonKeyAction
  | DataAcquisitionKeyAction;

const createKeyString = (e: React.KeyboardEvent | KeyboardEvent, keyCode: string) =>
  `${e.ctrlKey || e.metaKey ? 'Control+' : ''}${e.altKey ? 'Alt+' : ''}${
    e.shiftKey ? 'Shift+' : ''
  }${keyCode}`;

const getKeyAction = (
  e: React.KeyboardEvent | KeyboardEvent,
  keyCode: string
): GenericKeyAction => {
  const keyStr = createKeyString(e, keyCode);
  return (
    CommonWorkspaceTypes.CommonKeyActions.get(keyStr) ??
    AnalystWorkspaceTypes.AnalystKeyActions.get(keyStr) ??
    DataAcquisitionKeyActions.get(keyStr)
  );
};

/**
 * Gets the keypress event, if any are defined, that matches the action provided.
 * Handles events on React wrapped HTML elements. For example,
 * this may be used to handle keypress events on elements created by JSX, like <div>
 *
 * @param e a React wrapped keypress event
 */
export function getReactKeyPressAction(e: React.KeyboardEvent<HTMLElement>): GenericKeyAction {
  return getKeyAction(e, e.nativeEvent.code);
}

/**
 * Gets the keypress event, if any are defined, that matches the action provided.
 * Handles events on native HTML elements. For example,
 * this may be used to handle keypress events on document or window.
 *
 * @param e a keyboard event
 */
export function getKeyPressAction(e: KeyboardEvent): GenericKeyAction {
  return getKeyAction(e, e.code);
}
