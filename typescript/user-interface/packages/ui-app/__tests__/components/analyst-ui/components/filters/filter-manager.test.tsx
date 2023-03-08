import { getStore } from '@gms/ui-state';
import React from 'react';
import ReactHotkeys from 'react-hotkeys-hook';
import { Provider } from 'react-redux';
import renderer from 'react-test-renderer';

import { FilterManager } from '../../../../../src/ts/components/analyst-ui/components/filters/filter-manager';

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  const selectNextFilter = {
    hotkeys: 'Control+A',
    description: 'Test A'
  };
  const selectPreviousFilter = {
    hotkeys: 'Control+B',
    description: 'Test B'
  };
  const selectUnfiltered = {
    hotkeys: 'Control+C',
    description: 'Test C'
  };
  return {
    ...actual,
    useKeyboardShortcutConfig: jest.fn(() => ({
      selectNextFilter,
      selectPreviousFilter,
      selectUnfiltered
    }))
  };
});

describe('FilterManager', () => {
  const mockUseHotkeys = jest.spyOn(ReactHotkeys, 'useHotkeys');
  const store = getStore();

  const rendered = renderer.create(
    <Provider store={store}>
      <FilterManager />
    </Provider>
  );
  it("doesn't render anything", () => {
    expect(rendered.toJSON()).toMatchInlineSnapshot('null');
  });
  it('creates a hotkey with selectNextFilter hotkeys', () => {
    const didCallWithSelectNextFilter = mockUseHotkeys.mock.calls[0][0] === 'Control+A';
    expect(didCallWithSelectNextFilter).toBe(true);
  });
  it('creates a hotkey with selectPreviousFilter hotkeys', () => {
    const didCallWithSelectNextFilter = mockUseHotkeys.mock.calls[1][0] === 'Control+B';
    expect(didCallWithSelectNextFilter).toBe(true);
  });
  it('creates a hotkey with selectUnfiltered hotkeys', () => {
    const didCallWithSelectNextFilter = mockUseHotkeys.mock.calls[2][0] === 'Control+C';
    expect(didCallWithSelectNextFilter).toBe(true);
  });
});
