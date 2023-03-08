/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import type { YAxisProps } from '../../../../../../src/ts/components/waveform-display/components/axes/y-axis/types';
import { YAxis } from '../../../../../../src/ts/components/waveform-display/components/axes/y-axis/y-axis';
import { actAndWaitForComponentToPaint } from '../../../../../test-util/test-util';

const props: YAxisProps = {
  heightInPercentage: 100,
  minAmplitude: 0,
  maxAmplitude: 100,
  yAxisTicks: [0, 2, 4, 6, 8, 10]
};

const wrapper = Enzyme.mount(<YAxis {...props} />);
const instance: YAxis = wrapper.find(YAxis).instance() as YAxis;

describe('Weavess Y Axis', () => {
  it('to be defined', () => {
    expect(YAxis).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<YAxis {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('calls display', () => {
    const yAxis = new YAxis(props);
    const spy = jest.spyOn(yAxis, 'display');
    yAxis.display();
    expect(spy).toBeCalledTimes(1);
  });

  it('componentDidUpdate', () => {
    const spy = jest.spyOn(instance, 'componentDidUpdate');

    instance.componentDidUpdate();
    expect(spy).toBeCalledTimes(1);
  });

  it('componentDidCatch', () => {
    const spy = jest.spyOn(instance, 'componentDidCatch');
    instance.componentDidCatch(new Error('error'), { componentStack: undefined });
    expect(spy).toBeCalled();
  });

  it('try to display a negative with no ticks', () => {
    const { container } = render(
      <YAxis {...props} yAxisTicks={undefined} minAmplitude={-0.05} maxAmplitude={0} />
    );
    expect(container).toMatchSnapshot();
  });

  it('try to display a large range without ticks', () => {
    const { container } = render(<YAxis {...props} yAxisTicks={undefined} />);
    expect(container).toMatchSnapshot();
  });

  it('updates when display is called', async () => {
    await actAndWaitForComponentToPaint(wrapper, () => {
      instance.display();
    });
    expect(wrapper).toMatchSnapshot();
  });

  // TODO Unskip tests and fix
  it.skip('try to handle where min amplitude is undefined', async () => {
    const localWrapper = Enzyme.mount(<YAxis {...props} minAmplitude={undefined} />);
    const localInstance: YAxis = localWrapper.find(YAxis).instance() as YAxis;
    await actAndWaitForComponentToPaint(wrapper, () => {
      instance.display();
    });
    expect(localInstance).toMatchSnapshot();
  });

  // TODO Unskip tests and fix
  it.skip('try to handle where max amplitude is undefined', async () => {
    const localWrapper = Enzyme.mount(<YAxis {...props} maxAmplitude={undefined} />);
    const localInstance: YAxis = localWrapper.find(YAxis).instance() as YAxis;
    await actAndWaitForComponentToPaint(wrapper, () => {
      instance.display();
    });
    expect(localInstance).toMatchSnapshot();
  });

  it('can unmount component', () => {
    expect(() => instance.componentWillUnmount()).not.toThrow();
  });
});
