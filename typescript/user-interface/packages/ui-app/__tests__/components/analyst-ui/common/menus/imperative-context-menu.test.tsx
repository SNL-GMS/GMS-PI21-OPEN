import { Menu, MenuItem } from '@blueprintjs/core';
import * as React from 'react';
import { act, create } from 'react-test-renderer';

import { ImperativeContextMenu } from '../../../../../src/ts/components/analyst-ui/common/menus/imperative-context-menu';

jest.mock('react-dom', () => {
  const actual = jest.requireActual('react-dom');
  return {
    ...actual,
    createPortal: node => node
  };
});

describe('imperative context menu', () => {
  it('is defined', () => {
    expect(ImperativeContextMenu).toBeDefined();
  });
  it('should match the snapshot', () => {
    expect(
      create(
        <ImperativeContextMenu
          content={
            <Menu className="test-menu">
              <MenuItem text="Test Menu" />
            </Menu>
          }
          getOpenCallback={jest.fn()}
        />
      ).toJSON()
    ).toMatchSnapshot();
  });

  it('should call the callback', async () => {
    const callback = jest.fn();
    await act(() => {
      create(
        <ImperativeContextMenu
          content={
            <Menu className="test-menu">
              <MenuItem text="Test Menu" />
            </Menu>
          }
          getOpenCallback={callback}
        />
      );
    });
    expect(callback).toBeCalled();
  });
});
