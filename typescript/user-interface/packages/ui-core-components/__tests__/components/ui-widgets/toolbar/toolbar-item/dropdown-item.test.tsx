import { render } from '@testing-library/react';
import React from 'react';

import { DropdownToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/dropdown-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('DropdownToolbarItem', () => {
  test('DropdownToolbarItem renders directly', () => {
    const { container } = render(
      <DropdownToolbarItem
        key="dropdown"
        dropdownOptions={[]}
        value="Hello Dropdown"
        onChange={jest.fn()}
        tooltip="Hello Dropdown"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
