/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { CommonTypes } from '@gms/common-model';
import { SECONDS_IN_HOUR } from '@gms/common-util';
import { render } from '@testing-library/react';
import * as React from 'react';

import type { DayBoundaryIndicatorProps } from '../../../../../src/ts/components/analyst-ui/components/workflow/day-boundary-indicator';
import { DayBoundaryIndicator } from '../../../../../src/ts/components/analyst-ui/components/workflow/day-boundary-indicator';
// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

const secondsForDayPlus10 = SECONDS_IN_HOUR * 24 + 10;

const timeRange: CommonTypes.TimeRange = { startTimeSecs: 0, endTimeSecs: secondsForDayPlus10 };
const props: DayBoundaryIndicatorProps = {
  timeRange,
  height: 300,
  width: 300
};

const wrapper = Enzyme.mount(
  <DayBoundaryIndicator timeRange={props.timeRange} width={props.width} height={props.height} />
);

const renderDayBoundaryIndicator = innerProps =>
  render(
    <DayBoundaryIndicator
      timeRange={innerProps.timeRange}
      width={innerProps.width}
      height={innerProps.height}
    />
  );

describe('Daily boundary indicator', () => {
  it('should be defined', () => {
    expect(DayBoundaryIndicator).toBeDefined();
  });

  it('should match snapshot with standard props', () => {
    const { container } = renderDayBoundaryIndicator(props);
    expect(container).toMatchSnapshot();
  });

  it('should match snapshot with large height', () => {
    const { container } = renderDayBoundaryIndicator({ ...props, height: 500 });
    expect(container).toMatchSnapshot();
  });

  it('should match snapshot with updated small time range', () => {
    const { container } = renderDayBoundaryIndicator({
      ...props,
      timeRange: {
        startTimeSecs: timeRange.startTimeSecs,
        endTimeSecs: timeRange.endTimeSecs + 5
      },
      height: 500
    });
    expect(container).toMatchSnapshot();
  });

  it('should match snapshot with updated large time range', () => {
    const { container } = renderDayBoundaryIndicator({
      ...props,
      timeRange: {
        startTimeSecs: timeRange.startTimeSecs,
        endTimeSecs: timeRange.endTimeSecs + secondsForDayPlus10 * 3
      },
      height: 500
    });
    expect(container).toMatchSnapshot();
  });

  it('should match snapshot with updated height and time', () => {
    const { container } = renderDayBoundaryIndicator({
      ...props,
      timeRange: {
        startTimeSecs: timeRange.startTimeSecs,
        endTimeSecs: timeRange.endTimeSecs + secondsForDayPlus10 * 4
      },
      width: 600,
      height: 600
    });
    expect(container).toMatchSnapshot();
  });

  it('should scroll', () => {
    const spy = jest.spyOn(wrapper.find(DayBoundaryIndicator).instance(), 'scrollDayIndicator');

    wrapper.find(DayBoundaryIndicator).instance().scrollDayIndicator(5);

    expect(spy).toBeCalled();
  });
});
