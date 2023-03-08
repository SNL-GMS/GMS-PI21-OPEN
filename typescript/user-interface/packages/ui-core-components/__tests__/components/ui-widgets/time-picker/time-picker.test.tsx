/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';

import { TimePicker } from '../../../../src/ts/components/ui-widgets/time-picker';
import type * as TimePickerTypes from '../../../../src/ts/components/ui-widgets/time-picker/types';

const MOCK_TIME = 1611153271425;
Date.now = jest.fn(() => MOCK_TIME);
Date.constructor = jest.fn(() => new Date(MOCK_TIME));

const initialDate = new Date(Date.now());

const state: TimePickerTypes.TimePickerState = {
  isValid: true,
  showDatePicker: true,
  displayString: '2021-01-21T14:34:31+00:00',
  hasHold: false,
  datePickerOnBottom: true
};

const props: TimePickerTypes.TimePickerProps = {
  date: initialDate,
  datePickerEnabled: true,
  shortFormat: false,
  hasHold: false,
  onMaybeDate: jest.fn(),
  setHold: jest.fn(),
  onEnter: jest.fn()
};
const wrapper = Enzyme.mount(<TimePicker {...props} />);

const props2: TimePickerTypes.TimePickerProps = {
  date: initialDate,
  datePickerEnabled: true,
  shortFormat: true,
  hasHold: false,
  onMaybeDate: jest.fn(),
  setHold: jest.fn(),
  onEnter: jest.fn()
};

describe('Time Picker', () => {
  it('to be defined', () => {
    expect(TimePicker).toBeDefined();
  });

  it('Time Picker renders', () => {
    const { container } = render(<TimePicker {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('Time Picker renders with short format', () => {
    const { container } = render(<TimePicker {...props2} />);
    expect(container).toMatchSnapshot();
  });

  // TODO Unskip tests and fix
  it.skip('Time Picker renders and responds', () => {
    const wrapper3 = Enzyme.mount(<TimePicker {...props} />);
    const instance: TimePicker = wrapper.find(TimePicker).instance() as TimePicker;

    expect(wrapper3.find('.time-picker')).toHaveLength(1);
    expect(wrapper3.find('.time-picker__date-picker')).toHaveLength(0);
    wrapper3.setState({ showDatePicker: true });
    expect(wrapper3.find('.time-picker__date-picker')).toHaveLength(2);

    expect(wrapper3.find('.time-picker__input--invalid')).toHaveLength(0);
    wrapper3.setState({ isValid: false, hasHold: true });
    expect(wrapper3.find('.time-picker__input--invalid')).toHaveLength(1);

    const spy = jest.spyOn(instance, 'componentWillUnmount');
    instance.componentWillUnmount();
    expect(spy).toHaveBeenCalled();

    const spy2 = jest.spyOn(instance, 'componentDidUpdate');
    instance.componentDidUpdate(props2, state);
    expect(spy2).toHaveBeenCalled();

    wrapper3.setState({ isValid: true, hasHold: false });
    const input = wrapper3.find('.time-picker__input');
    const spy3 = jest.spyOn(instance, 'render');
    input.simulate('focus');
    input.simulate('change');
    input.simulate('blur');
    instance.render();
    expect(spy3).toHaveBeenCalled();

    const datePicker = wrapper3.find('.time-picker__date-picker').first();
    datePicker.simulate('change');
    instance.render();
    expect(spy3).toHaveBeenCalledTimes(2);
  });
});
