import { render } from '@testing-library/react';
import Immutable from 'immutable';
import React from 'react';

import { CheckboxDropdownToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/checkbox-dropdown-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { CheckboxDropdownOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/checkbox-dropdown-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

enum MenuItemTestEnum {
  Test
}

const checkboxDropdownToolbarItem = (
  <CheckboxDropdownToolbarItem
    key="checkboxdropdown"
    values={Immutable.Map<string, boolean>()}
    enumOfKeys={MenuItemTestEnum}
    onChange={jest.fn()}
    tooltip="Hello CheckboxDropdown"
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

describe('CheckboxDropdownOverflowMenuToolbarItem', () => {
  test('CheckboxDropdownOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <CheckboxDropdownOverflowMenuToolbarItem
        item={checkboxDropdownToolbarItem.props}
        menuKey={checkboxDropdownToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('CheckboxDropdownOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <CheckboxDropdownOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
