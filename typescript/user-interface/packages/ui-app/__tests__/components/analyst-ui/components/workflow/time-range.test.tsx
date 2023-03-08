/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SECONDS_IN_HOUR } from '@gms/common-util';
import React from 'react';

import type { TimeRangeProps } from '../../../../../src/ts/components/analyst-ui/components/workflow/time-range';
import { TimeRange } from '../../../../../src/ts/components/analyst-ui/components/workflow/time-range';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

const secondsForDayPlus10 = SECONDS_IN_HOUR * 24 + 10;

const props: TimeRangeProps = {
  startTime: 0,
  endTime: secondsForDayPlus10
};

describe('workflow time axis tests', () => {
  it('is exported', () => {
    expect(TimeRange).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = Enzyme.mount(
      <TimeRange startTime={props.startTime} endTime={props.endTime} />
    );
    expect(component).toMatchSnapshot();

    component.setProps({ startTime: props.startTime, endTime: props.endTime });
    expect(component).toMatchSnapshot();

    component.setProps({ startTime: props.startTime, endTime: props.endTime + 10 });
    expect(component).toMatchSnapshot();

    component.setProps({
      startTime: props.startTime,
      endTime: props.endTime + secondsForDayPlus10 * 3
    });
    expect(component).toMatchSnapshot();
  });

  it('can call update', () => {
    const component = Enzyme.mount(
      <TimeRange startTime={props.startTime} endTime={props.endTime} />
    );
    component.find(TimeRange).instance().update(1000, 2000);
    expect(TimeRange).toBeDefined();
  });

  it('can call should component update', () => {
    const component = Enzyme.mount(
      <TimeRange startTime={props.startTime} endTime={props.endTime} />
    );
    const mockWorkflowTimeRangeProps2: TimeRangeProps = {
      startTime: 4000,
      endTime: 5600
    };

    component.find(TimeRange).instance().shouldComponentUpdate(mockWorkflowTimeRangeProps2);
    expect(TimeRange).toBeDefined();
  });
});
