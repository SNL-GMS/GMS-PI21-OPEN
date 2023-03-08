import { render } from '@testing-library/react';
import React from 'react';

import { LabelValueToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/label-value-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { LoadingSpinnerOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/loading-spinner-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const loadingSpinnerToolbarItem = (
  <LoadingSpinnerToolbarItem
    key="loadingspinner"
    itemsToLoad={1}
    tooltip="Hello LoadingSpinner"
    hasIssue={false}
    widthPx={50}
  />
);

const labelValueToolbarItem = (
  <LabelValueToolbarItem
    key="labelvalue"
    labelValue=""
    tooltip="Hello label"
    label="label"
    menuLabel="menu label"
    widthPx={50}
  />
);

describe('LoadingSpinnerOverflowMenuToolbarItem', () => {
  test('LoadingSpinnerOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <LoadingSpinnerOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('LoadingSpinnerOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <LoadingSpinnerOverflowMenuToolbarItem
        item={labelValueToolbarItem.props}
        menuKey={labelValueToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
