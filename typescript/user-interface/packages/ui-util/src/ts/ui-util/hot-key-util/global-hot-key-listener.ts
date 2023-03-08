import Immutable from 'immutable';
import isEqual from 'lodash/isEqual';
import uniqueId from 'lodash/uniqueId';
import React from 'react';

import { convertCommandToHotKeyArray, getHotKeyArray, getKeyPressed } from './hot-key-util';

/**
 * A map of key strings to whether or not they are currently being pressed.
 * Tracks modifier hotkeys as well as other keys. See @enum ModifierHotKeys.
 */
let keyDownMap: Immutable.Map<string, boolean>;

/**
 * A map of subscription keys. We use a map for quick lookup and removal.
 */
let subscriptionIdMap: Immutable.Map<string, boolean> = Immutable.Map<string, boolean>();

/**
 * Sets the keyDownMap to a new, empty map.
 */
const initializeKeyDownMap = () => {
  keyDownMap = Immutable.Map<string, boolean>();
};

/**
 * Records that a key has been pressed.
 *
 * @param ev the keyboard event for which to record that a key is pressed
 */
const recordKey = (ev: KeyboardEvent) => {
  // Don't update the keyDownMap if we have just done so.
  // This is a performance optimization, since memory allocation is costly.
  if (ev.repeat) {
    return;
  }
  // now set each key that is pressed to true.
  getHotKeyArray(ev).forEach(k => {
    keyDownMap = keyDownMap.set(k, true);
  });
};

/**
 * Records that a key has been released
 *
 * @param ev the keyboard event for which to record that a key was released
 */
const releaseKey = (ev: KeyboardEvent) => {
  // now set each key that is pressed to true.
  keyDownMap = keyDownMap.set(getKeyPressed(ev), false);
};

/**
 * Initializes a global hotkey listener on the document body. Any keyboard events that propagate up to
 * the body will be recorded, and subsequent calls to @function isKeyDown  will return true if that key
 * is currently held down, and false otherwise.
 *
 * Be sure to eat your vegetables and call @function unsubscribeFromGlobalHotkeyListener to clean up these
 * listeners when appropriate, such as in the componentDidUnmount lifecycle method of the component that
 * registered this listener.
 *
 * @returns an id which should be used to unsubscribe by calling @function unsubscribeFromGlobalHotkeyListener
 */
export const subscribeToGlobalHotkeyListener = (): string => {
  const id = uniqueId();
  subscriptionIdMap = subscriptionIdMap.set(id, true);
  if (keyDownMap === undefined) {
    initializeKeyDownMap();
    document.body.addEventListener('keydown', recordKey);
    document.body.addEventListener('keyup', releaseKey);
    // clear all tracked keys if the user leaves the app
    window.addEventListener('contextmenu', initializeKeyDownMap);
    window.addEventListener('blur', initializeKeyDownMap);
  }
  return id;
};

/**
 * Cleans up the global hotkey listener. Subsequent calls to @function isKeyDown
 * or @function isGlobalHotkeyCommandSatisfied will throw an error.
 * This should be called only once per subscribe.
 *
 * @param id the ID that was given when subscribing using @function subscribeToGlobalHotkeyListener
 */
export const unsubscribeFromGlobalHotkeyListener = (id: string): void => {
  if (subscriptionIdMap.get(id)) {
    subscriptionIdMap = subscriptionIdMap.remove(id);
    // if nobody is listening, clean up
    if (subscriptionIdMap.size === 0) {
      keyDownMap = undefined;
      document.body.removeEventListener('keydown', recordKey);
      document.body.removeEventListener('keyup', releaseKey);
      window.removeEventListener('contextmenu', initializeKeyDownMap);
      window.removeEventListener('blur', initializeKeyDownMap);
    }
  }
};

/**
 * If not initialized, throws an error saying so and recommending subscribing
 *
 * @throws an error if not initialized
 */
const assertIsInitialized = () => {
  if (keyDownMap === undefined) {
    throw new Error(
      `Attempting to use global hotkey listener, but no listener exists. Call ${subscribeToGlobalHotkeyListener.name} first.`
    );
  }
};

/**
 * Returns a sorted array of keys pressed
 *
 * @throws if the listener has not been initialized
 */
const getKeysPressedArray = (): string[] => {
  assertIsInitialized();
  return Array.from(keyDownMap.filter(val => val).keys()).sort();
};

/**
 * Returns true if the @param keyToCheck is current held down, false otherwise.
 * Note: if an element is calling stopPropagation for a keypress event, that keypress will not be registered, and this command
 * may not be matched.
 *
 * @throws if called before @function subscribeToGlobalHotkeyListener has
 * been called to initialize the listener, or if called after @function unsubscribeFromGlobalHotkeyListener
 * has been called to remove the global listener.
 *
 * @param keyToCheck the string representing the key code (for example, the string returned by event.code)
 * See https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/code
 * @returns true if the key is held down, false if otherwise.
 */
export const isKeyDown = (keyToCheck: string): boolean => {
  assertIsInitialized();
  return !!keyDownMap.get(keyToCheck);
};

/**
 * Returns true if all of the keys in a hotkey command are pressed and have propagated up to the document.body element.
 * Note: if an element is calling stopPropagation for a keypress event, that keypress will not be registered, and this command
 * may not be matched.
 *
 * @throws if called before @function subscribeToGlobalHotkeyListener or @function useGlobalHotkeyListener has
 * been called to initialize the listener, or if called after @function unsubscribeFromGlobalHotkeyListener
 * has been called to remove the global listener.
 *
 * @param hotKeyCommand a hotkey command of the form Ctrl+Alt+Shift+KeyS combined of meta keys strings and key codes.
 * If an undefined command is provided, returns false.
 * @returns true if all keys are pressed. False otherwise.
 */
export const isGlobalHotKeyCommandSatisfied = (hotKeyCommand: string | undefined): boolean => {
  assertIsInitialized();

  // guard against undefined because a lot of commands are optional.
  if (!hotKeyCommand) {
    return false;
  }
  return isEqual(convertCommandToHotKeyArray(hotKeyCommand), getKeysPressedArray());
};

/**
 * A hook that initializes the global hotkey listener on mount, and cleans
 * it up when unmounted.
 */
export const useGlobalHotkeyListener = (): void => {
  const listenerIdRef = React.useRef<string>();
  React.useEffect(() => {
    listenerIdRef.current = subscribeToGlobalHotkeyListener();
    return () => unsubscribeFromGlobalHotkeyListener(listenerIdRef.current);
  }, []);
};
