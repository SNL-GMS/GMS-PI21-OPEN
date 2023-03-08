import { render } from '@testing-library/react';
import React from 'react';

import { IntervalPickerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/interval-picker-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { IntervalPickerOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/interval-picker-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const intervalPickerToolbarItem = (
  <IntervalPickerToolbarItem
    key="intervalpicker"
    defaultIntervalInHours={12}
    startDate={new Date(100)}
    endDate={new Date(1000)}
    onChange={jest.fn()}
    onApplyButton={jest.fn()}
    tooltip="Hello Interval"
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

describe('IntervalPickerOverflowMenuToolbarItem', () => {
  test('IntervalPickerOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <IntervalPickerOverflowMenuToolbarItem
        item={intervalPickerToolbarItem.props}
        menuKey={intervalPickerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('IntervalPickerOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <IntervalPickerOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
