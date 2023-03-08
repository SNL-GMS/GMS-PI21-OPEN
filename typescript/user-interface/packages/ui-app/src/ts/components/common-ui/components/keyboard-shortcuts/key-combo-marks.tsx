import { getOS, OSTypes } from '@gms/ui-util';
import React from 'react';

import { KeyMark } from './key-mark';
import {
  isMacKeyCombo,
  isNonMacKeyCombo,
  isNonOSSpecificKeyCombo
} from './keyboard-shortcuts-util';

interface KeyComboMarksProps {
  description: string;
  hotkeys: string;
}

/**
 * Component to render a list of one or more hotkey combos, side by side.
 */
// eslint-disable-next-line react/function-component-definition
export const KeyComboMarks: React.FC<KeyComboMarksProps> = ({
  description,
  hotkeys = '' as string
}: KeyComboMarksProps) => {
  const os = getOS();
  const theCombos = hotkeys
    .split(', ')
    .map(combo => combo.toLowerCase())
    .filter(
      combo =>
        (os === OSTypes.MAC && !isNonMacKeyCombo(combo)) ||
        (os !== OSTypes.MAC && !isMacKeyCombo(combo)) ||
        isNonOSSpecificKeyCombo(combo)
    );
  return (
    <ul className="keyboard-shortcuts__hotkey-list">
      {theCombos.map((combo, index) => (
        <React.Fragment key={`shortcuts: ${combo} in "${description}"`}>
          <span
            key={`combo: ${combo} in "${description}"`}
            className="keyboard-shortcuts__hotkey-combo"
          >
            {combo.split('+').map(hotkey => (
              <KeyMark key={`${hotkey}:${combo}`}>{hotkey}</KeyMark>
            ))}
          </span>
          {index !== theCombos.length - 1 && (
            <span
              key={`${combo} conjunction-junction`}
              className="keyboard-shortcuts__conjunction-junction"
            >
              &nbsp;or&nbsp;
            </span>
          )}
        </React.Fragment>
      ))}
    </ul>
  );
};
