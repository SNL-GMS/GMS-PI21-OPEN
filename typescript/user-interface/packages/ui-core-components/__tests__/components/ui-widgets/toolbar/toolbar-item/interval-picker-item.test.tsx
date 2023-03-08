import { render } from '@testing-library/react';
import React from 'react';

import { IntervalPickerToolbarItem } from '../../../../../src/ts/components/ui-widgets/toolbar/toolbar-item/interval-picker-item';

// set up window alert and open map so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

describe('IntervalPickerToolbarItem', () => {
  test('IntervalPickerToolbarItem renders directly', () => {
    const { container } = render(
      <IntervalPickerToolbarItem
        key="intervalpicker"
        defaultIntervalInHours={12}
        startDate={new Date(100)}
        endDate={new Date(1000)}
        onChange={jest.fn()}
        onApplyButton={jest.fn()}
        tooltip="Hello Interval"
      />
    );

    expect(container).toMatchSnapshot();
  });
});
