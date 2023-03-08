import { render } from '@testing-library/react';
import React from 'react';

import { DateRangePickerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/date-range-picker-item';
import { LoadingSpinnerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/loading-spinner-item';
import { DateRangePickerOverflowMenuToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-overflow-menu-item/date-range-picker-menu-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const dateRangePickerToolbarItem = (
  <DateRangePickerToolbarItem
    key="datepicker"
    startTimeMs={100}
    endTimeMs={1000}
    onChange={jest.fn()}
    format="YYYY-MM-DD HH:mm"
    onApplyButton={jest.fn()}
    tooltip="Hello Date Range"
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

describe('DateRangePickerOverflowMenuToolbarItem', () => {
  test('DateRangePickerOverflowMenuToolbarItem renders', () => {
    const { container } = render(
      <DateRangePickerOverflowMenuToolbarItem
        item={dateRangePickerToolbarItem.props}
        menuKey={dateRangePickerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
  test('DateRangePickerOverflowMenuToolbarItem renders empty due to incorrect type', () => {
    const { container } = render(
      <DateRangePickerOverflowMenuToolbarItem
        item={loadingSpinnerToolbarItem.props}
        menuKey={loadingSpinnerToolbarItem.key}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
