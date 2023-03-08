import isEqual from 'lodash/isEqual';

/** Constant string representing the key separator */
export const HotKeySeparator = '+' as const;

export enum ModifierHotKeys {
  /** Constant string representing the `Meta` key */
  META = 'Meta',

  /** Constant string representing the `Control` key */
  CONTROL = 'Control',

  /** Constant string representing the `Alt` key */
  ALT = 'Alt',

  /** Constant string representing the `Shift` key */
  SHIFT = 'Shift'
}

/**
 * Returns the key that was just pressed.
 *
 * Normalizes left and right modifier keys, so, for example,
 * the left and right Alt keys will return simply "Alt".
 *
 * Non-meta keys are represented via their event.code value.
 *
 * @param event an event from which to get the key pressed
 * @returns a string representing the key pressed.
 */
export const getKeyPressed = (event: KeyboardEvent): string => {
  // we use the event.key for meta keys so we don't have to deal with AltLeft or ShiftLeft codes.
  if (
    event.key === 'Alt' ||
    event.key === 'Meta' ||
    event.key === 'Control' ||
    event.key === 'Shift'
  ) {
    return event.key;
  }
  return event.code;
};

/**
 * The Hot Key array based on the KeyboardEvent.
 *
 * @param event the keyboard event as KeyboardEvent
 *
 * @returns The Hot Key array
 */
export const getHotKeyArray = (event: KeyboardEvent): string[] => {
  const hotKeyArray: string[] = [];

  if (event.metaKey) {
    hotKeyArray.push(ModifierHotKeys.META);
  }

  if (event.ctrlKey) {
    hotKeyArray.push(ModifierHotKeys.CONTROL);
  }

  if (event.altKey) {
    hotKeyArray.push(ModifierHotKeys.ALT);
  }

  if (event.shiftKey) {
    hotKeyArray.push(ModifierHotKeys.SHIFT);
  }

  // add non-control characters
  // see: https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
  if (
    event.key !== ModifierHotKeys.META &&
    event.key !== ModifierHotKeys.CONTROL &&
    event.key !== ModifierHotKeys.ALT &&
    event.key !== ModifierHotKeys.SHIFT
  ) {
    hotKeyArray.push(event.code);
  }

  return hotKeyArray;
};

/**
 * Hot Key string based on the KeyboardEvent.
 *
 * @param event the keyboard event
 *
 * @returns Hot Key string
 */
export const getHotKeyString = (event: KeyboardEvent): string =>
  getHotKeyArray(event).join(HotKeySeparator);

/**
 *
 * @param hotKeyCommand a hot key command such as "Ctrl+Alt+KeyS"
 * @returns an array of strings representing each key
 */
export const convertCommandToHotKeyArray = (hotKeyCommand: string): string[] => {
  // remove all whitespace
  const noWhiteSpaceCommand = hotKeyCommand.replace(/\s/g, '');
  return noWhiteSpaceCommand.split(HotKeySeparator);
};

/**
 * Is hot key satisfied
 *
 * @param event the keyboard event to check
 * @param hotKeyCommand the hotkey command such as "Alt+KeyS" where meta keys use event.key
 * and other keys use the event.code
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key }
 *
 * @returns true if the hotkey command is satisfied. False otherwise.
 */
export const isHotKeyCommandSatisfied = (event: KeyboardEvent, hotKeyCommand: string): boolean => {
  if (!event) {
    return false;
  }

  if (!hotKeyCommand) {
    return false;
  }

  const hotKeyArray = getHotKeyArray(event);
  const commandArray = convertCommandToHotKeyArray(hotKeyCommand);

  hotKeyArray.sort();
  commandArray.sort();

  return isEqual(hotKeyArray, commandArray);
};
