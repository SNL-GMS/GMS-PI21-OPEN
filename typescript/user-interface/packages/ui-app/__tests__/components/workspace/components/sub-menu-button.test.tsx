import { IconNames } from '@blueprintjs/icons';
import { fireEvent, render } from '@testing-library/react';
import * as React from 'react';

import { SubMenuButton } from '~components/workspace/components/menus/sub-menu-button';

describe('SubMenuButton', () => {
  it('calls handleClick when clicked', () => {
    const mockHandleClick = jest.fn();
    const mockHandleKeydown = jest.fn();
    const view = render(
      <SubMenuButton
        disabled={false}
        handleClick={mockHandleClick}
        handleKeyDown={mockHandleKeydown}
        iconName={IconNames.OPEN_APPLICATION}
      />
    );
    const btn = view.getByRole('button');
    btn.focus();
    fireEvent.click(btn);
    expect(mockHandleClick).toHaveBeenCalledTimes(1);
  });
  it('calls handleKeyDown when enter is pressed', () => {
    const mockHandleClick = jest.fn();
    const mockHandleKeydown = jest.fn();
    const view = render(
      <SubMenuButton
        disabled={false}
        handleClick={mockHandleClick}
        handleKeyDown={mockHandleKeydown}
        iconName={IconNames.OPEN_APPLICATION}
      />
    );
    const btn = view.getByRole('button');
    btn.focus();
    fireEvent.keyDown(btn, {
      key: 'Enter',
      charCode: 13
    });
    expect(mockHandleKeydown).toHaveBeenCalledTimes(1);
  });
  it('does not call handleKeyDown when a key other than enter is pressed', () => {
    const mockHandleClick = jest.fn();
    const mockHandleKeydown = jest.fn();
    const view = render(
      <SubMenuButton
        disabled={false}
        handleClick={mockHandleClick}
        handleKeyDown={mockHandleKeydown}
        iconName={IconNames.OPEN_APPLICATION}
      />
    );
    const btn = view.getByRole('button');
    btn.focus();
    fireEvent.keyDown(btn, {
      key: 'a',
      charCode: 65
    });
    expect(mockHandleKeydown).toHaveBeenCalledTimes(0);
  });
});
