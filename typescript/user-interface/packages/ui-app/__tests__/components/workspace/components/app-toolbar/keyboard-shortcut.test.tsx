import { getStore } from '@gms/ui-state';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import { KeyboardShortcuts } from '../../../../../src/ts/components/common-ui/components/keyboard-shortcuts/keyboard-shortcuts';
import {
  doHotkeyCombosMatch,
  hotkeyMatchesSearchTerm,
  isSearchTermFoundInCombo
} from '../../../../../src/ts/components/common-ui/components/keyboard-shortcuts/keyboard-shortcuts-util';

const mockShortcuts = {
  zoomInOneStep: {
    description: 'Zoom in one step',
    helpText:
      'Zoom in by a percentage (zooming in and then out one step returns you to the same view).',
    hotkeys: 'W',
    tags: ['zoom', 'in', 'waveform'],
    category: 'Waveform Display'
  },
  zoomOutOneStep: {
    description: 'Zoom out one step',
    helpText:
      'Zoom out by a configured percentage (zooming in and then out one step returns you to the same view).',
    hotkeys: 'S',
    tags: ['zoom', 'out', 'waveform'],
    category: 'Waveform Display'
  },
  panRight: {
    description: 'Pan Right',
    helpText: 'Will only show data within the current interval (plus lead/lag).',
    hotkeys: 'd, right',
    tags: ['step', 'scroll', 'right', 'move'],
    category: 'Waveform Display'
  },
  panLeft: {
    description: 'Pan Left',
    helpText: 'Will not load additional data outside of the current interval (plus lead/lag).',
    hotkeys: 'a, left',
    tags: ['step', 'scroll', 'left', 'move'],
    category: 'Waveform Display'
  },
  panRightHard: {
    description: 'Pan Right & Load',
    helpText: 'Will load additional data outside of the current interval (past lead/lag).',
    hotkeys: 'shift+d, shift+right',
    tags: ['step', 'scroll', 'right', 'move'],
    category: 'Waveform Display'
  },
  panLeftHard: {
    description: 'Pan Left & Load',
    helpText: 'Will load additional data outside of the current interval (past lead/lag).',
    hotkeys: 'shift+a, shift+left',
    tags: ['step', 'scroll', 'left', 'move'],
    category: 'Waveform Display'
  },
  zoomToRange: {
    description: 'Zoom to range',
    helpText: 'Zoom in on a selection by clicking and dragging (with a modifier key).',
    hotkeys: 'ctrl+drag, cmd+drag',
    tags: ['zoom', 'in', 'waveform', 'click', 'mouse'],
    category: 'Waveform Display'
  },
  zoomMouseWheel: {
    description: 'Zoom — smooth',
    helpText: 'Animated zoom in and out using the mouse wheel.',
    hotkeys: 'ctrl+mousewheel',
    tags: ['zoom', 'in', 'out', 'scroll', 'waveform'],
    category: 'Waveform Display'
  },
  showKeyboardShortcuts: {
    description: 'Keyboard Shortcuts',
    helpText: 'Shows the list of keyboard shortcuts (this list).',
    hotkeys: 'control+/, command+/',
    tags: ['keyboard', 'shortcuts', 'hotkeys', 'app', 'help'],
    category: 'App'
  },
  toggleCommandPalette: {
    description: 'Command Palette',
    helpText: 'Open a popup tool for typing commands.',
    hotkeys: 'control+shift+X, command+shift+X',
    tags: ['command', 'palette', 'workspace', 'app', 'help'],
    category: 'App'
  }
};

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useKeyboardShortcutConfig: jest.fn(() => mockShortcuts)
  };
});

describe('Keyboard Shortcuts', () => {
  describe('isSearchTermFoundInCombo', () => {
    it('can identify a match with shift key', () => {
      expect(isSearchTermFoundInCombo(mockShortcuts.panRightHard.hotkeys, 'shift')).toBe(true);
    });
    it('can identify when a term does not match', () => {
      expect(isSearchTermFoundInCombo(mockShortcuts.panRightHard.hotkeys, 'ctrl')).toBe(false);
    });
  });
  describe('doHotkeyCombosMatch', () => {
    it('can identify a match with control key', () => {
      expect(doHotkeyCombosMatch(mockShortcuts.zoomToRange.hotkeys, 'control')).toBe(true);
    });
    it('can identify a match with ctrl key', () => {
      expect(doHotkeyCombosMatch(mockShortcuts.zoomToRange.hotkeys, 'ctrl')).toBe(true);
    });
    it('can identify a match with command key', () => {
      expect(doHotkeyCombosMatch(mockShortcuts.zoomToRange.hotkeys, 'command')).toBe(true);
    });
    it('can identify a match with cmd key', () => {
      expect(doHotkeyCombosMatch(mockShortcuts.zoomToRange.hotkeys, 'cmd')).toBe(true);
    });
    it('can identify when keys do not match', () => {
      expect(doHotkeyCombosMatch(mockShortcuts.zoomToRange.hotkeys, 'shift')).toBe(false);
    });
  });
  describe('hotkeyMatchesSearchTerm', () => {
    it('can identify a match in the tags', () => {
      expect(hotkeyMatchesSearchTerm(mockShortcuts.zoomToRange, 'click')).toBe(true);
    });
    it('can identify a match in the category', () => {
      expect(hotkeyMatchesSearchTerm(mockShortcuts.zoomToRange, 'display')).toBe(true);
    });
    it('can identify a match in the description', () => {
      expect(hotkeyMatchesSearchTerm(mockShortcuts.zoomToRange, 'range')).toBe(true);
    });
    it('can identify when there is no match', () => {
      expect(hotkeyMatchesSearchTerm(mockShortcuts.zoomToRange, 'fail')).toBe(false);
    });
  });
  describe('KeyboardShortcuts component', () => {
    it('matches a snapshot', () => {
      expect(
        create(
          <Provider store={getStore()}>
            <KeyboardShortcuts />
          </Provider>
        ).toJSON()
      ).toMatchSnapshot();
    });
  });
});
