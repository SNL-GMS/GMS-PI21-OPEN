/* eslint-disable @typescript-eslint/no-magic-numbers */
import {
  DATE_FORMAT,
  DATE_TIME_FORMAT,
  DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  DATE_TIME_FORMAT_WITH_SECOND_PRECISION
} from '@gms/common-util';
import React from 'react';

import { DateRangePicker } from '../../../../src/ts/components/ui-widgets/date-range-picker';
import type { DateRangePickerProps } from '../../../../src/ts/components/ui-widgets/date-range-picker/types';

jest.useFakeTimers();

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

const MOCK_TIME = 1609506000000;

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

describe('DateRangePicker', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(MOCK_TIME);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should be defined', () => {
    expect(DateRangePicker).toBeDefined();
  });

  it('matches the snapshot', () => {
    const component = Enzyme.mount(
      <DateRangePicker
        durations={[
          {
            description: 'one second',
            value: 1000
          },
          {
            description: 'two seconds',
            value: 2000
          }
        ]}
        // eslint-disable-next-line @typescript-eslint/no-magic-numbers
        startTimeMs={MOCK_TIME - 2000}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
      />
    );
    expect(component).toMatchSnapshot();

    component.update();
    component.setProps({
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      startTimeMs: MOCK_TIME - 3000,
      endTimeMs: MOCK_TIME
    });
    component.update();
    expect(component).toMatchSnapshot();
    component.instance().onApply();
    expect(component).toMatchSnapshot();

    component.setProps({
      format: DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
    });
    component.update();
    expect(component).toMatchSnapshot();

    component.setProps({
      format: DATE_TIME_FORMAT_WITH_SECOND_PRECISION
    });
    component.update();
    expect(component).toMatchSnapshot();

    component.setProps({
      format: DATE_FORMAT,
      minStartTimeMs: MOCK_TIME - 8000,
      maxEndTimeMs: MOCK_TIME
    });
    component.update();
    expect(component).toMatchSnapshot();
  });

  it('button opens the popup', () => {
    const component = Enzyme.mount(
      <DateRangePicker
        durations={[
          {
            description: 'one second',
            value: 1000
          },
          {
            description: 'two seconds',
            value: 2000
          }
        ]}
        // eslint-disable-next-line @typescript-eslint/no-magic-numbers
        startTimeMs={MOCK_TIME - 2000}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
      />
    );

    let popup = component.find('DateRangePopup');
    expect(popup.prop('isOpen')).toBeFalsy();

    const button = component.find({ 'data-cy': 'date-range-picker-edit' }).first();
    button.simulate('click');

    popup = component.find('DateRangePopup');
    expect(popup.prop('isOpen')).toBeTruthy();

    const button2 = component.find({ 'data-cy': 'date-picker-cancel-button' }).first();
    button2.simulate('click');
    popup = component.find('DateRangePopup');
    expect(popup.prop('isOpen')).toBeFalsy();
  });

  it('can test DateRangePicker functions', () => {
    const onNewInterval = jest.fn();
    const onApply = jest.fn();
    const props: DateRangePickerProps = {
      durations: [
        {
          description: 'one second',
          value: 1000
        },
        {
          description: 'two seconds',
          value: 2000
        }
      ],
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      startTimeMs: MOCK_TIME - 2000,
      endTimeMs: MOCK_TIME,
      format: DATE_TIME_FORMAT,
      onNewInterval,
      onApply
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    let component = Enzyme.mount(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <DateRangePicker {...props} onApply={undefined} onNewInterval={undefined} />
    );
    let picker: any = component.find(DateRangePicker).instance();
    picker.onApply(MOCK_TIME - 2000, MOCK_TIME);
    jest.runAllTimers();
    expect(onApply).toBeCalledTimes(0);

    // eslint-disable-next-line react/jsx-props-no-spreading
    component = Enzyme.mount(<DateRangePicker {...props} />);
    picker = component.find(DateRangePicker).instance();
    picker.onApply(MOCK_TIME - 2000, MOCK_TIME);
    jest.runAllTimers();
    expect(onApply).toBeCalledTimes(1);
  });
});
