/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { ContextMenu } from '@blueprintjs/core';
import {
  AnalystWorkspaceTypes,
  CommonKeyAction,
  getKeyPressAction,
  isCommonKeyAction
} from '@gms/ui-state';
import produce from 'immer';
import throttle from 'lodash/throttle';
import React from 'react';

import { InteractionContext } from '../interaction-provider/types';
import type { InteractionConsumerProps } from './types';

const THROTTLE_HOTKEY_REPEAT_MS = 500;

const isHotkeyListenerAttached = () => {
  if (document.getElementById('app')) {
    return document.getElementById('app').dataset.hotkeyListenerAttached === 'true';
  }
  return false;
};

const setHotkeyListenerAttached = () => {
  if (document.getElementById('app')) {
    document.getElementById('app').dataset.hotkeyListenerAttached = 'true';
  }
};

/**
 * Consumes keypress from the redux store and calls the Interaction Provider context to perform the appropriate action
 */
export const InteractionConsumer: React.FunctionComponent<React.PropsWithChildren<
  InteractionConsumerProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  const callbacks = React.useContext(InteractionContext);

  /**
   * Checks to see if an action should be performed, and if so consumes the keypress and performs it
   *
   * @param keyAction the key action
   * @param callback the callback
   * @param shouldConsumeAllKeypress true if should consume all key presses
   */
  const maybeConsumeKeypress = (
    keyAction: AnalystWorkspaceTypes.AnalystKeyAction | CommonKeyAction,
    callback: () => void,
    shouldConsumeAllKeypress = false
  ) => {
    if (props.keyPressActionQueue) {
      const maybeKeyCount = props.keyPressActionQueue[keyAction];
      // eslint-disable-next-line no-restricted-globals
      if (!isNaN(maybeKeyCount) && maybeKeyCount > 0) {
        props.setKeyPressActionQueue(
          produce(props.keyPressActionQueue, draft => {
            draft[keyAction] = shouldConsumeAllKeypress ? 0 : maybeKeyCount - 1;
          })
        );
        callback();
      }
    }
  };

  React.useEffect(() => {
    maybeConsumeKeypress(
      AnalystWorkspaceTypes.AnalystKeyAction.UNDO_GLOBAL,
      () => {
        if (callbacks.undo) {
          callbacks.undo(1);
        }
      },
      true
    );

    maybeConsumeKeypress(
      AnalystWorkspaceTypes.AnalystKeyAction.ESCAPE,
      () => {
        // close/hide any opened context menus
        ContextMenu.hide();
      },
      true
    );

    maybeConsumeKeypress(
      AnalystWorkspaceTypes.AnalystKeyAction.REDO_GLOBAL,
      () => {
        if (callbacks.redo) {
          callbacks.redo(1);
        }
      },
      true
    );

    maybeConsumeKeypress(
      CommonKeyAction.OPEN_COMMAND_PALETTE,
      () => {
        if (callbacks.toggleCommandPaletteVisibility) {
          callbacks.toggleCommandPaletteVisibility();
        }
      },
      true
    );
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.keyPressActionQueue]); // use effect if queue has changed

  /**
   * checks to see if the keypress matches a configured hotkey, and if so,
   * adds it to the keypress action queue
   */
  const handleHotkey = (keyEvent: KeyboardEvent): void => {
    if (props.keyPressActionQueue && !keyEvent.repeat) {
      if (props.keyPressActionQueue) {
        const keyPressAction = getKeyPressAction(keyEvent);
        if (
          keyPressAction &&
          (isCommonKeyAction(keyPressAction) ||
            AnalystWorkspaceTypes.isAnalystKeyAction(keyPressAction))
        ) {
          keyEvent.stopPropagation();
          keyEvent.preventDefault();
          const entryForKeyMap = props.keyPressActionQueue[keyPressAction]
            ? props.keyPressActionQueue[keyPressAction]
            : 0;
          props.setKeyPressActionQueue(
            produce(props.keyPressActionQueue, draft => {
              draft[keyPressAction] = Number(entryForKeyMap) + 1;
            })
          );
        }
      }
    }
  };

  /**
   * Adds a keydown listener to the document, so we will catch anything that bubbles up to the top.
   */
  React.useEffect(() => {
    if (!isHotkeyListenerAttached()) {
      document.addEventListener('keydown', throttle(handleHotkey, THROTTLE_HOTKEY_REPEAT_MS));
      setHotkeyListenerAttached();
    }

    // Clean up the event listener on unmount
    return () => document.removeEventListener('keydown', handleHotkey);
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // eslint-disable-next-line react/jsx-no-useless-fragment
  return <>{props.children}</>;
};
