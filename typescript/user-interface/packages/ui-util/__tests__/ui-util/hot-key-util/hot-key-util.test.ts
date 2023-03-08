/* eslint-disable jest/no-conditional-expect */
import { HotkeyListener } from '../../../src/ts/ui-util';
import {
  getHotKeyArray,
  getHotKeyString,
  isHotKeyCommandSatisfied,
  ModifierHotKeys
} from '../../../src/ts/ui-util/hot-key-util';

const testEvent = {
  metaKey: true,
  key: 'S',
  code: 'S'
} as KeyboardEvent;

const anotherEvent = {
  ctrlKey: true,
  shiftKey: true,
  key: 'W',
  code: 'W'
} as KeyboardEvent;

const aBoringEvent = {
  key: 'A',
  code: 'A'
} as KeyboardEvent;

const forceKillEvent = {
  ctrlKey: true,
  altKey: true,
  key: 'Del',
  code: 'Del'
} as KeyboardEvent;

const keyboardEvents = [testEvent, anotherEvent, aBoringEvent, forceKillEvent];

describe('WEAVESS Core: Hot Key Util', () => {
  it('defines its functions', () => {
    expect(HotkeyListener.getHotKeyArray).toBeDefined();
    expect(HotkeyListener.getHotKeyString).toBeDefined();
    expect(HotkeyListener.isHotKeyCommandSatisfied).toBeDefined();
  });

  it('can get a hot key array from a keyboard event', () => {
    keyboardEvents.forEach(keyEvent => {
      const arr = getHotKeyArray(keyEvent);
      if (keyEvent.ctrlKey) {
        expect(arr).toContain(ModifierHotKeys.CONTROL);
      }
      if (keyEvent.altKey) {
        expect(arr).toContain(ModifierHotKeys.ALT);
      }
      if (keyEvent.shiftKey) {
        expect(arr).toContain(ModifierHotKeys.SHIFT);
      }
      if (keyEvent.metaKey) {
        expect(arr).toContain(ModifierHotKeys.META);
      }
      expect(arr[arr.length - 1]).toEqual(keyEvent.code);
    });
  });

  it('can generate a hot key string from an event', () => {
    const forceKillString = getHotKeyString(forceKillEvent);
    expect(forceKillString).toEqual('Control+Alt+Del');
  });

  it('can verify if a hotkey combination has been pressed', () => {
    const forceKillString = getHotKeyString(forceKillEvent);
    expect(isHotKeyCommandSatisfied(forceKillEvent, forceKillString)).toBe(true);
  });

  it('can verify if a hotkey combination has not been pressed', () => {
    expect(isHotKeyCommandSatisfied(forceKillEvent, 'Control+Alt+P')).toBe(false);
  });

  it('returns false when given an undefined event', () => {
    expect(isHotKeyCommandSatisfied(undefined, 'Control+B')).toBe(false);
  });

  it('returns false when given an empty string', () => {
    expect(isHotKeyCommandSatisfied(forceKillEvent, '')).toBe(false);
  });
});
