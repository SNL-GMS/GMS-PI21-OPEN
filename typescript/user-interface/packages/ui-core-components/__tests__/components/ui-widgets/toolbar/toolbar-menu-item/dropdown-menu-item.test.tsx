import { render } from '@testing-library/react';
import React from 'react';

import { DropdownToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/dropdown-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { DropdownOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/dropdown-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const dropdownToolbarItem = (
  <DropdownToolbarItem
    key="dropdown"
    dropdownOptions={[]}
    value="Hello Dropdown"
    onChange={jest.fn()}
    tooltip="Hello Dropdown"
    hasIssue={false}
    widthPx={50}
  />
);

const loadingSpinnerToolbarItem = (
  <LoadingSpinnerToolbarItem
    key="loadingspinner"
    itemsToLoad={1}
    tooltip="Hello LoadingSpinner"
    hasIssue={false}
    widthPx={50}
  />
);

describe('DropdownOverflowMenuToolbarItem', () => {
  test('DropdownOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <DropdownOverflowMenuToolbarItem
        item={dropdownToolbarItem.props}
        menuKey={dropdownToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('DropdownOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <DropdownOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
