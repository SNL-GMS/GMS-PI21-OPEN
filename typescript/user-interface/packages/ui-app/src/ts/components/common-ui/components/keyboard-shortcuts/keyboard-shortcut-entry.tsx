import type { ConfigurationTypes } from '@gms/common-model';
import React from 'react';

import { HelpText } from './help-text';
import { KeyComboMarks } from './key-combo-marks';

/**
 * Render a single keyboard shortcut object from config. Includes all key combos,
 * help text (if provided), and the description.
 */
// eslint-disable-next-line react/function-component-definition
export const KeyboardShortcutEntry: React.FC<ConfigurationTypes.KeyboardShortcut> = shortcut => {
  const { description, hotkeys, helpText } = shortcut;
  return (
    <div key={hotkeys} className="keyboard-shortcuts__hotkey-entry">
      <span className="keyboard-shortcuts__description">
        {helpText && <HelpText>{helpText}</HelpText>}
        {description}
        :&nbsp;
      </span>
      <KeyComboMarks hotkeys={hotkeys} description={description} />
    </div>
  );
};
