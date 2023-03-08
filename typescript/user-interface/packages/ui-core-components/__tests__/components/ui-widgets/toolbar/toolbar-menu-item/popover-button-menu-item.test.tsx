import { H1 } from '@blueprintjs/core';
import { render } from '@testing-library/react';
import React from 'react';

import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { PopoverButtonToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/popover-button-item';
import { PopoverButtonOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/popover-button-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const element = <H1>Test</H1>;

const popoverButtonToolbarItem = (
  <PopoverButtonToolbarItem
    key="popover"
    popoverContent={element}
    onPopoverDismissed={jest.fn()}
    tooltip="Hello Popover"
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

describe('PopoverButtonOverflowMenuToolbarItem', () => {
  test('PopoverButtonOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <PopoverButtonOverflowMenuToolbarItem
        item={popoverButtonToolbarItem.props}
        menuKey={popoverButtonToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('PopoverButtonOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <PopoverButtonOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
