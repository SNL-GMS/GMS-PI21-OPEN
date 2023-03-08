import { render } from '@testing-library/react';
import React from 'react';

import { LabelValueToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/label-value-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { LabelValueOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/label-value-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

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

const loadingSpinnerToolbarItem = (
  <LoadingSpinnerToolbarItem
    key="loadingspinner"
    itemsToLoad={1}
    tooltip="Hello LoadingSpinner"
    hasIssue={false}
    widthPx={50}
  />
);

describe('LabelValueOverflowMenuToolbarItem', () => {
  test('LabelValueOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <LabelValueOverflowMenuToolbarItem
        item={labelValueToolbarItem.props}
        menuKey={labelValueToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('LabelValueOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <LabelValueOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
