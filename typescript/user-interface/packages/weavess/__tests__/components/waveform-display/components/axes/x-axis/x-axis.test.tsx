/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import type { XAxisProps } from '../../../../../../src/ts/components/waveform-display/components/axes/x-axis/types';
import {
  subMillisecondFormatter,
  tickFormatter,
  XAxis
} from '../../../../../../src/ts/components/waveform-display/components/axes/x-axis/x-axis';
import { timeRangeDisplayString } from '../../../../../../src/ts/components/waveform-display/utils';

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

const props: XAxisProps = {
  displayInterval: {
    startTimeSecs: 100,
    endTimeSecs: 400
  },
  label: 'x axis',
  labelWidthPx: 20,
  scrollbarWidthPx: 10,
  borderTop: true
};

const wrapper = Enzyme.mount(<XAxis {...props} />);
const instance: XAxis = wrapper.find(XAxis).instance() as XAxis;

describe('Weavess X Axis', () => {
  it('to be defined', () => {
    expect(XAxis).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<XAxis {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('renders without border', () => {
    const { container } = render(<XAxis {...props} borderTop={false} />);
    expect(container).toMatchSnapshot();
  });

  it('componentDidUpdate', () => {
    const spy = jest.spyOn(instance, 'componentDidUpdate');

    instance.componentDidUpdate(props);
    expect(spy).toBeCalledTimes(1);
  });

  it('componentDidCatch', () => {
    const spy = jest.spyOn(instance, 'componentDidCatch');
    instance.componentDidCatch(new Error('error'), { componentStack: undefined });
    expect(spy).toBeCalled();
  });

  it('tickFormatter', () => {
    expect(tickFormatter(new Date('Jan 1, 2021 07:01:00.00 GMT'))).toEqual('07:01:00.000');
  });

  it('sub millisecond tickFormatter', () => {
    expect(subMillisecondFormatter(100009999.8)).toMatchInlineSnapshot(`"12:33:19.800"`);
  });

  it('timeRangeDisplayString string for 2 seconds', () => {
    const interval = {
      startTimeSecs: 1,
      endTimeSecs: 3
    };
    expect(timeRangeDisplayString(interval)).toMatchInlineSnapshot(
      `"1970-01-01 00:00:01.000 + 2 seconds"`
    );
  });

  it('timeRangeDisplayStringstring for 0.9 seconds', () => {
    const interval = {
      startTimeSecs: 1,
      endTimeSecs: 1.9
    };
    expect(timeRangeDisplayString(interval)).toMatchInlineSnapshot(
      `"1970-01-01 00:00:01.000 + 0.9 seconds"`
    );
  });

  it('timeRangeDisplayString string 1 millisecond', () => {
    const interval = {
      startTimeSecs: 0.001,
      endTimeSecs: 0.002
    };
    expect(timeRangeDisplayString(interval)).toMatchInlineSnapshot(
      `"1970-01-01 00:00:00.001 + 0.001 seconds"`
    );
  });

  it('timeRangeDisplayString string .1 millisecond', () => {
    const interval = {
      startTimeSecs: 0.001,
      endTimeSecs: 0.0011
    };
    expect(timeRangeDisplayString(interval)).toMatchInlineSnapshot(
      `"1970-01-01 00:00:00.001 + 0.0001 seconds"`
    );
  });

  it('timeRangeDisplayString string undefined interval', () => {
    expect(timeRangeDisplayString(undefined)).toEqual('');
  });
});
