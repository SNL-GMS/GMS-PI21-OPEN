import { render } from '@testing-library/react';
import Immutable from 'immutable';
import React from 'react';

import type { PopoverButton } from '../../../../../src/ts/components/ui-widgets';
import { CheckboxDropdownToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/checkbox-dropdown-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const mockOnChange = value => jest.fn(value);
const mockOnPopUp = event => jest.fn(event);
const mockOnPopoverDismissed = jest.fn();
const mockPopoverButtonMap = new Map<number, PopoverButton>();

describe('CheckboxDropdownToolbarItem', () => {
  // TODO Unskip tests and fix
  test.skip('CheckboxDropdownToolbarItem renders directly', () => {
    const { container } = render(
      <CheckboxDropdownToolbarItem
        key="checkboxdropdown"
        values={Immutable.Map<string, boolean>()}
        enumOfKeys={undefined}
        popoverButtonMap={mockPopoverButtonMap}
        onChange={mockOnChange}
        onPopUp={mockOnPopUp}
        onPopoverDismissed={mockOnPopoverDismissed}
        tooltip="Hello CheckboxDropdown"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
