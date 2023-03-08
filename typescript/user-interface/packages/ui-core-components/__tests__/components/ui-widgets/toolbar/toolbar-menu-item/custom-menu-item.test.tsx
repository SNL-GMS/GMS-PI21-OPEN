import { render } from '@testing-library/react';
import React from 'react';

import { CustomToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/custom-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { CustomOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/custom-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const customToolbarItem = (
  <CustomToolbarItem key="custom-test" element={<div>Custom ToolbarItem</div>} />
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

describe('CustomOverflowMenuToolbarItem', () => {
  test('CustomOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <CustomOverflowMenuToolbarItem
        item={customToolbarItem.props}
        menuKey={customToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('CustomOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <CustomOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
