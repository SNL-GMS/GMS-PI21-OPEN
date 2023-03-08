import { render } from '@testing-library/react';
import React from 'react';

import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { SwitchToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/switch-item';
import { SwitchOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/switch-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const switchToolbarItem = (
  <SwitchToolbarItem key="switch-test" switchValue onChange={jest.fn()} widthPx={50} />
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

describe('SwitchOverflowMenuToolbarItem', () => {
  test('SwitchOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <SwitchOverflowMenuToolbarItem
        item={switchToolbarItem.props}
        menuKey={switchToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('SwitchOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <SwitchOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
