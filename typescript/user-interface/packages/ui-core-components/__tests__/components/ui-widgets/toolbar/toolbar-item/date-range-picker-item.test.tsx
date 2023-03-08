import { render } from '@testing-library/react';
import React from 'react';

import { DateRangePickerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/date-range-picker-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('DateRangePickerToolbarItem', () => {
  test('DateRangePickerToolbarItem renders directly', () => {
    const { container } = render(
      <DateRangePickerToolbarItem
        key="datepicker"
        startTimeMs={100}
        endTimeMs={1000}
        onChange={jest.fn()}
        format={undefined}
        onApplyButton={jest.fn()}
        tooltip="Hello Date Range"
        hasIssue={false}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
