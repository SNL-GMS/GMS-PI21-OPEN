import {
  categorizeKeyboardShortcuts,
  doHotkeyCombosMatch,
  hotkeyMatchesSearchTerm,
  isNonOSSpecificKeyCombo,
  isSearchTermFoundInCombo
} from '../../../../../src/ts/components/common-ui/components/keyboard-shortcuts/keyboard-shortcuts-util';

describe('keyboard shortcuts util', () => {
  describe('isNonOSSpecificKeyCombo', () => {
    test('should recognize if a hotkey combo is not specific to a particular OS', () => {
      expect(isNonOSSpecificKeyCombo('shift+x')).toBe(true);
    });
    test('should recognize if a hotkey combo is specific to MacOS', () => {
      expect(isNonOSSpecificKeyCombo('command+x')).toBe(false);
    });
    test('should recognize if a hotkey combo is specific to Windows', () => {
      expect(isNonOSSpecificKeyCombo('alt+x')).toBe(false);
    });
  });
  describe('categorizeKeyboardShortcuts', () => {
    test('should return an empty object if given no shortcuts', () => {
      expect(categorizeKeyboardShortcuts(undefined)).toMatchObject({});
    });
    test('should return an empty object if given an empty list', () => {
      expect(categorizeKeyboardShortcuts([])).toMatchObject({});
    });
  });
  describe('hotkeyMatchesSearchTerm', () => {
    test('handles hotkeys without categories', () => {
      expect(
        hotkeyMatchesSearchTerm(
          {
            hotkeys: 'shift+s',
            description: 'Does a thing'
          },
          'foo'
        )
      ).toBe(false);
    });
  });
  describe('doHotkeyCombosMatch', () => {
    test('handles control and ctrl the same', () => {
      const combo = 'ctrl+a';
      expect(doHotkeyCombosMatch(combo, 'ctrl')).toBe(doHotkeyCombosMatch(combo, 'control'));
      const combo2 = 'control+a';
      expect(doHotkeyCombosMatch(combo2, 'ctrl')).toBe(doHotkeyCombosMatch(combo2, 'control'));
    });
    test('handles command and cmd the same', () => {
      const combo = 'cmd+a';
      expect(doHotkeyCombosMatch(combo, 'cmd')).toBe(doHotkeyCombosMatch(combo, 'command'));
      const combo2 = 'command+a';
      expect(doHotkeyCombosMatch(combo2, 'cmd')).toBe(doHotkeyCombosMatch(combo2, 'command'));
    });
  });
  describe('isSearchTermFoundInCombo', () => {
    test('matches combos that use + as though they used a space', () => {
      expect(isSearchTermFoundInCombo('shift+s', 'shift s')).toBe(true);
    });
  });
});
