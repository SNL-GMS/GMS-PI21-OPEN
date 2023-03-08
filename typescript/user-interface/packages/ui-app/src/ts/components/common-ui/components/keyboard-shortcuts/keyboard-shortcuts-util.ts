import type { ConfigurationTypes } from '@gms/common-model';
import { doTagsMatch } from '@gms/common-util';

/**
 * Does a literal check of search terms against hotkey combos with no attempts at
 * handling abbreviations, such as control/ctrl. Case insensitive.
 *
 * @param combos a combo string to check, such as 'shift+a, shift+left'
 * @param term a search term to check against the combo, such as `shift'
 * @returns whether the combo is a match for the term
 */
export const isSearchTermFoundInCombo = (combos: string, term: string): boolean => {
  if (combos.toLowerCase().includes(term)) {
    return true;
  }
  let isFound = false;
  const comboArray = combos.split(', ');
  comboArray.forEach(combo => {
    if (combo.toLowerCase().replace(`+`, ' ').includes(term)) {
      isFound = true;
    }
  });
  return isFound;
};

/**
 * Does a check of search terms against hotkey combos.
 * Attempts to handle abbreviations, such as control/ctrl, or command/cmd.
 * Case insensitive.
 *
 * @param combos a combo string to check, such as 'ctrl+a, command+left'
 * @param term a search term to check against the combo, such as `control'
 * @returns whether the combo is a match for the term
 */
export const doHotkeyCombosMatch = (combos: string, term: string): boolean => {
  let isFound = isSearchTermFoundInCombo(combos, term);
  if (!isFound && combos.includes('command')) {
    isFound = isSearchTermFoundInCombo(combos.replace(/command/g, 'cmd'), term);
  }
  if (!isFound && combos.includes('cmd')) {
    isFound = isSearchTermFoundInCombo(combos.replace(/cmd/g, 'command'), term);
  }
  if (!isFound && combos.includes('control')) {
    isFound = isSearchTermFoundInCombo(combos.replace(/control/g, 'ctrl'), term);
  }
  if (!isFound && combos.includes('ctrl')) {
    isFound = isSearchTermFoundInCombo(combos.replace(/ctrl/g, 'control'), term);
  }
  return isFound;
};

/**
 * Checks if a search term is a match for a hotkey.
 * It's considered a match if any words in the search term match a hokey's display text, category, or tags
 *
 * @param hotkey a KeyboardShortcut object to determine if it matches
 * @param term a string to check against the hotkey
 */
export const hotkeyMatchesSearchTerm: (
  hotkey: ConfigurationTypes.KeyboardShortcut,
  term: string
) => boolean = (hotkey, term) => {
  const cleanTerm = term.replace(/\s\s+/g, ' ').trim().toLowerCase();
  return (
    hotkey &&
    (hotkey.description.toLowerCase().includes(cleanTerm) ||
      doTagsMatch(hotkey.tags, cleanTerm) ||
      hotkey.category?.toLowerCase().includes(cleanTerm) ||
      doHotkeyCombosMatch(hotkey.hotkeys, cleanTerm))
  );
};

/**
 * Sorts a list of keyboard shortcuts into a record of shortcut arrays
 * with a key equal to the category name.
 */
export const categorizeKeyboardShortcuts = (
  shortcuts: ConfigurationTypes.KeyboardShortcut[] | undefined
): Record<string, ConfigurationTypes.KeyboardShortcut[]> => {
  if (!shortcuts) return {};
  const shortcutRecord: Record<string, ConfigurationTypes.KeyboardShortcut[]> = {};
  shortcuts.forEach(shortcut => {
    if (shortcutRecord[shortcut.category]) {
      shortcutRecord[shortcut.category].push(shortcut);
    } else {
      shortcutRecord[shortcut.category] = [shortcut];
    }
  });
  return shortcutRecord;
};

/**
 * @returns true if it is a key combo containing a mac-only key such as command or option
 */
export const isMacKeyCombo = (combo: string): boolean =>
  combo.includes('cmd') || combo.includes('command') || combo.includes('option');

/**
 * @returns true if the key combo contains non-mac hotkeys, such as `alt`, which is not on Mac keyboards.
 */
export const isNonMacKeyCombo = (combo: string): boolean => combo.includes('alt');

/**
 * @returns true if the key combo contains no hotkeys that are OS specific.
 */
export const isNonOSSpecificKeyCombo = (combo: string): boolean =>
  !(isMacKeyCombo(combo) || isNonMacKeyCombo(combo));
