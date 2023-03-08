import { H1 } from '@blueprintjs/core';
import { render } from '@testing-library/react';
import React from 'react';

import type { PopoverButton } from '../../../../../src/ts/components/ui-widgets';
import { PopoverButtonToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/popover-button-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const element = <H1>Hello</H1>;
const mockOnPopoverDismissed = jest.fn();
const mockPopoverButtonMap = new Map<number, PopoverButton>();

describe('PopoverButtonToolbarItem', () => {
  // TODO Unskip tests and fix
  test.skip('PopoverButtonToolbarItem renders directly', () => {
    const { container } = render(
      <PopoverButtonToolbarItem
        key="popover"
        popoverContent={element}
        popoverButtonMap={mockPopoverButtonMap}
        onPopoverDismissed={mockOnPopoverDismissed}
        tooltip="Hello Popover"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
