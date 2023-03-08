/* eslint-disable react/jsx-props-no-spreading */

import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { PercentBar } from '../../../src/ts/components/table/percent-bar';
import type * as PercentBarTypes from '../../../src/ts/components/table/types/percent-bar';

const firstPercntValue = 50;
const secondPercentValue = 75;
const initialClassNames = 'percent-bar-half';
const nextClassNames = 'percent-bar-three-fourths';

const props: PercentBarTypes.PercentBarProps = {
  percentage: firstPercntValue,
  classNames: initialClassNames
};

const wrapper = Enzyme.mount(<PercentBar {...props} />);
const instance: PercentBar = wrapper.find(PercentBar).instance() as PercentBar;

describe('Percent Bar', () => {
  it('to be defined', () => {
    expect(PercentBar).toBeDefined();
  });

  it('renders', () => {
    const { container } = render(<PercentBar {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('updates', () => {
    wrapper.setProps({ percentage: secondPercentValue, classNames: nextClassNames });
    wrapper.update();
    expect(wrapper.props().percentage).toEqual(secondPercentValue);
    expect(wrapper.props().classNames).toBe(nextClassNames);
    wrapper.setProps({ classNames: undefined });
    wrapper.update();
    expect(wrapper.props().classNames).toBeUndefined();
  });

  it('instance is defined', () => {
    expect(instance).toBeDefined();
  });
});
