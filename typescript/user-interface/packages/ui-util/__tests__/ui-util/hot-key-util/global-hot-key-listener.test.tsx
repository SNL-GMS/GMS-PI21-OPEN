/* eslint-disable no-void */
import Enzyme from 'enzyme';
import React from 'react';
import { act } from 'react-dom/test-utils';

import { HotkeyListener } from '../../../src/ts/ui-util';

const keyDownEvent: KeyboardEvent = new KeyboardEvent('keydown', {
  key: 's',
  code: 'KeyY'
});
const modifierKeyDownEvent: KeyboardEvent = new KeyboardEvent('keydown', {
  key: 'Alt',
  code: 'AltLeft',
  altKey: true
});
const keyUpEvent: KeyboardEvent = new KeyboardEvent('keyup', {
  key: 's',
  code: 'KeyY'
});
const modifierKeyUpEvent: KeyboardEvent = new KeyboardEvent('keyup', {
  key: 'Alt',
  code: 'AltLeft',
  altKey: true
});
describe('Global Hot Key Listener', () => {
  describe('subscription', () => {
    let subscriptionId: string;
    beforeEach(() => {
      subscriptionId = HotkeyListener.subscribeToGlobalHotkeyListener();
    });
    afterEach(() => {
      HotkeyListener.unsubscribeFromGlobalHotkeyListener(subscriptionId);
    });
    it('does not throw when calling isKeyDown after subscribing', () => {
      expect(() => {
        HotkeyListener.isKeyDown('KeyY');
      }).not.toThrowError();
    });
    it('does not throw when calling isGlobalHotKeyCommandSatisfied after subscribing', () => {
      expect(() => {
        HotkeyListener.isGlobalHotKeyCommandSatisfied('Alt+KeyY');
      }).not.toThrowError();
    });
    it('throws when calling isKeyDown after unsubscribing', () => {
      expect(() => {
        HotkeyListener.unsubscribeFromGlobalHotkeyListener(subscriptionId);
        HotkeyListener.isKeyDown('KeyY');
      }).toThrowErrorMatchingSnapshot();
    });
    it('throws when calling isGlobalHotKeyCommandSatisfied after unsubscribing', () => {
      expect(() => {
        HotkeyListener.unsubscribeFromGlobalHotkeyListener(subscriptionId);
        HotkeyListener.isGlobalHotKeyCommandSatisfied('Alt+KeyY');
      }).toThrowErrorMatchingSnapshot();
    });
  });

  describe('useGlobalHotkeyListener', () => {
    it('subscribes when calling useGlobalHotkeyListener hook', () => {
      function TestComponent() {
        HotkeyListener.useGlobalHotkeyListener();
        return null;
      }
      const wrapper = Enzyme.mount(<TestComponent />);
      expect(() => {
        void act(() => {
          wrapper.update();
        });
        HotkeyListener.isGlobalHotKeyCommandSatisfied('Alt+KeyY');
        void act(() => {
          wrapper.unmount();
        });
      }).not.toThrowError();
    });
    it('unsubscribes when unmounting useGlobalHotkeyListener hook', () => {
      function TestComponent() {
        HotkeyListener.useGlobalHotkeyListener();
        return null;
      }
      const wrapper = Enzyme.mount(<TestComponent />);
      expect(() => {
        void act(() => {
          wrapper.update();
        });
        void act(() => {
          wrapper.unmount();
        });
        HotkeyListener.isGlobalHotKeyCommandSatisfied('Alt+KeyY');
      }).toThrowErrorMatchingSnapshot();
    });
  });

  describe('key presses', () => {
    let listenerId: string;
    beforeAll(() => {
      listenerId = HotkeyListener.subscribeToGlobalHotkeyListener();
    });
    beforeEach(() => {
      document.body.dispatchEvent(keyDownEvent);
    });
    afterEach(() => {
      document.body.dispatchEvent(keyUpEvent);
    });
    afterAll(() => {
      HotkeyListener.unsubscribeFromGlobalHotkeyListener(listenerId);
    });
    it('knows if a key is pressed', () => {
      expect(HotkeyListener.isKeyDown('KeyY')).toBe(true);
    });
    it('knows if a key has been released', () => {
      document.body.dispatchEvent(keyUpEvent);
      expect(HotkeyListener.isKeyDown('KeyY')).toBe(false);
    });
  });

  describe('commands', () => {
    let listenerId: string;
    beforeAll(() => {
      listenerId = HotkeyListener.subscribeToGlobalHotkeyListener();
    });
    beforeEach(() => {
      document.body.dispatchEvent(modifierKeyDownEvent);
      document.body.dispatchEvent(keyDownEvent);
    });
    afterEach(() => {
      document.body.dispatchEvent(modifierKeyUpEvent);
      document.body.dispatchEvent(keyUpEvent);
    });
    afterAll(() => {
      HotkeyListener.unsubscribeFromGlobalHotkeyListener(listenerId);
    });
    it('knows if a multi-key command is satisfied', () => {
      expect(HotkeyListener.isGlobalHotKeyCommandSatisfied('Alt+KeyY')).toBe(true);
    });
    it('returns false if too many KeyY are pressed', () => {
      expect(HotkeyListener.isGlobalHotKeyCommandSatisfied('KeyY')).toBe(false);
    });
    it('returns false if not enough KeyY are pressed', () => {
      expect(HotkeyListener.isGlobalHotKeyCommandSatisfied('Control+Alt+KeyY')).toBe(false);
    });
    it('returns false if the wrong KeyY are pressed', () => {
      expect(HotkeyListener.isGlobalHotKeyCommandSatisfied('Control+KeyY')).toBe(false);
    });
    it('returns false if command is undefined', () => {
      expect(HotkeyListener.isGlobalHotKeyCommandSatisfied(undefined)).toBe(false);
    });
  });
});
