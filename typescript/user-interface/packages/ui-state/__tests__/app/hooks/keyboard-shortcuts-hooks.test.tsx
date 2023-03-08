/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/display-name */
/* eslint-disable jest/expect-expect */
import { renderHook } from '@testing-library/react-hooks';
import axios from 'axios';
import * as React from 'react';
import { Provider } from 'react-redux';
import type Redux from 'redux';

import { useKeyboardShortcutsDisplayVisibility } from '../../../src/ts/app/hooks/keyboard-shortcuts-hooks';
import type { AppState } from '../../../src/ts/app/store';
import { processingAnalystConfiguration } from '../../__data__/processing-analyst-configuration';
import { configureNewStore } from '../../test-util';

axios.request = jest.fn().mockImplementation();

jest.mock(
  '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice',
  () => {
    const actual = jest.requireActual(
      '../../../src/ts/app/api/processing-configuration/processing-configuration-api-slice'
    );

    return {
      ...actual,
      processingConfigurationApiSlice: {
        middleware: actual.processingConfigurationApiSlice.middleware,
        endpoints: {
          getProcessingAnalystConfiguration: {
            select: jest.fn(() =>
              jest.fn(() => ({
                data: processingAnalystConfiguration
              }))
            )
          }
        }
      }
    };
  }
);

describe('keyboard shortcuts hooks', () => {
  describe('useKeyboardShortcutsDisplayVisibility', () => {
    const store: Redux.Store<AppState> = configureNewStore();
    const { result } = renderHook(() => useKeyboardShortcutsDisplayVisibility(), {
      wrapper: (props: React.PropsWithChildren<unknown>) => (
        <Provider store={store}>{props.children}</Provider>
      )
    });
    describe('toggleKeyboardShortcuts', () => {
      it('exists', () => {
        expect(result.current.toggleKeyboardShortcuts).toBeDefined();
      });
      it('toggles the keyboard shortcuts visibility', () => {
        const initialVal = store.getState().app.common.keyboardShortcutsVisibility;
        result.current.toggleKeyboardShortcuts();
        expect(store.getState().app.common.keyboardShortcutsVisibility).toBe(!initialVal);
      });
    });
    describe('closeKeyboardShortcuts and openKeyboardShortcuts', () => {
      it('exist', () => {
        expect(result.current.closeKeyboardShortcuts).toBeDefined();
        expect(result.current.openKeyboardShortcuts).toBeDefined();
      });
      it('turn off and on the keyboard shortcuts visibility', () => {
        result.current.closeKeyboardShortcuts();
        expect(store.getState().app.common.keyboardShortcutsVisibility).toBe(false);
        result.current.openKeyboardShortcuts();
        expect(store.getState().app.common.keyboardShortcutsVisibility).toBe(true);
        result.current.closeKeyboardShortcuts();
        expect(store.getState().app.common.keyboardShortcutsVisibility).toBe(false);
      });
    });
  });
});
