/* eslint-disable @blueprintjs/classes-constants */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { DATE_TIME_FORMAT, MILLISECONDS_IN_DAY } from '@gms/common-util';
import React from 'react';

import { DateRangePopup } from '../../../../src/ts/components/ui-widgets/date-range-popup';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

const MOCK_TIME = 1609506000000;

describe('DateRangePopup', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(MOCK_TIME);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should be defined', () => {
    expect(Date.now()).toEqual(MOCK_TIME);
    expect(DateRangePopup).toBeDefined();
  });

  it('matches the snapshot', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );
    expect(component).toMatchSnapshot();
    component.unmount();
  });

  it('matches the duration snapshot', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        durations={[{ description: 'last 24 hours', value: MILLISECONDS_IN_DAY }]}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );
    expect(component).toMatchSnapshot();
    component.unmount();
  });

  it('overlapping dates are not useable', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME}
        endTimeMs={MOCK_TIME - 10000}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    expect(openButton.prop('disabled')).toBeTruthy();
    component.unmount();
  });

  it('out of range start dates are not useable', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={0}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    jest.runAllTimers();

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    expect(openButton.prop('disabled')).toBeTruthy();
    const errorText = component.find('.date-input-error-text').text();
    expect(errorText.trim()).toEqual('Start date is before minimum start date 2020-12-31 13:00');
    component.unmount();
  });

  it('out of range end dates are not useable', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME}
        endTimeMs={MOCK_TIME + 2 * MILLISECONDS_IN_DAY}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    expect(openButton.prop('disabled')).toBeTruthy();

    component.unmount();
  });

  it('dates exceeding maximum range are unusable', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        maxSelectedRangeMs={1}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    expect(openButton.prop('disabled')).toBeTruthy();

    component.unmount();
  });

  it('single dates are unusable', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={null}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    expect(openButton.prop('disabled')).toBeTruthy();

    component.unmount();
  });

  it('valid dates are usable', () => {
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={jest.fn()}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    expect(openButton.prop('disabled')).toBeFalsy();

    component.unmount();
  });

  it('apply button calls methods', () => {
    const onApply = jest.fn();
    const onNewInterval = jest.fn();
    const onClose = jest.fn();
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={onNewInterval}
        onApply={onApply}
        onClose={onClose}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-apply-button' }).first();
    openButton.simulate('click');
    expect(onApply).toHaveBeenCalled();
    expect(onNewInterval).toHaveBeenCalledTimes(1);
    expect(onClose).toHaveBeenCalledTimes(0);

    component.unmount();
  });

  it('close button calls methods', () => {
    const onApply = jest.fn();
    const onNewInterval = jest.fn();
    const onClose = jest.fn();
    const isOpen = true;
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={onNewInterval}
        onApply={onApply}
        onClose={onClose}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const openButton = component.find({ 'data-cy': 'date-picker-cancel-button' }).first();
    openButton.simulate('click');
    expect(onApply).toHaveBeenCalledTimes(0);
    expect(onNewInterval).toHaveBeenCalledTimes(0);
    expect(onClose).toHaveBeenCalled();

    component.unmount();
  });

  it('handles start time changing', () => {
    const isOpen = true;
    const onNewInterval = jest.fn();
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={onNewInterval}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const timePicker = component.find('[data-cy="start_time"]');
    const newTime = new Date(1970, 0, 0, 23, 59, 59, 0);
    const expectedDate = new Date(MOCK_TIME).setHours(23, 59, 59, 0);
    timePicker.prop('onChange')(newTime);
    jest.runAllTimers();
    expect(onNewInterval).toHaveBeenCalledWith(expectedDate, MOCK_TIME);
    component.unmount();
  });

  it('handles end time changing', () => {
    const isOpen = true;
    const onNewInterval = jest.fn();
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={onNewInterval}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const timePicker = component.find('[data-cy="end_time"]');
    const newTime = new Date(1970, 0, 0, 23, 59, 59, 0);
    const expectedDate = new Date(MOCK_TIME).setHours(23, 59, 59, 0);
    timePicker.prop('onChange')(newTime);
    jest.runAllTimers();
    expect(onNewInterval).toHaveBeenCalledWith(MOCK_TIME - 10000, expectedDate);
    component.unmount();
  });

  // TODO Unskip tests and fix
  it.skip('handles start date changing', () => {
    const isOpen = true;
    const onNewInterval = jest.fn();
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={onNewInterval}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const datePicker = component.find('[data-cy="start_date"]');

    // Invalid Date
    const badDate = new Date('a');
    datePicker.prop('onChange')(badDate);
    jest.runAllTimers();
    expect(onNewInterval).toHaveBeenCalledTimes(0);
    let errorText = component.find('.date-input-error-text').text();
    expect(errorText.trim()).toEqual('Invalid Start Date');

    // Valid Date
    const newDate = new Date(MOCK_TIME - MILLISECONDS_IN_DAY);
    datePicker.prop('onChange')(newDate);
    jest.runAllTimers();
    expect(onNewInterval).toHaveBeenCalledWith(newDate.getTime(), MOCK_TIME);

    errorText = component.find('.date-input-error-text').text();
    expect(errorText.trim()).toEqual('');
    component.unmount();
  });

  // TODO Unskip tests and fix
  it.skip('handles end date changing', () => {
    const isOpen = true;
    const onNewInterval = jest.fn();
    const component = Enzyme.mount(
      <DateRangePopup
        isOpen={isOpen}
        title="Test Popup"
        format={DATE_TIME_FORMAT}
        startTimeMs={MOCK_TIME - 10000}
        endTimeMs={MOCK_TIME}
        onNewInterval={onNewInterval}
        onApply={jest.fn()}
        onClose={jest.fn()}
        minStartTimeMs={MOCK_TIME - MILLISECONDS_IN_DAY}
        maxEndTimeMs={MOCK_TIME + MILLISECONDS_IN_DAY}
      />
    );

    const datePicker = component.find('[data-cy="end_date"]');
    // Invalid Date
    const badDate = new Date('a');
    datePicker.prop('onChange')(badDate);
    jest.runAllTimers();
    expect(onNewInterval).toHaveBeenCalledTimes(0);
    let errorText = component.find('.date-input-error-text').text();
    expect(errorText.trim()).toEqual('Invalid End Date');

    // Valid Date
    const newDate = new Date(MOCK_TIME + MILLISECONDS_IN_DAY);
    datePicker.prop('onChange')(newDate);
    jest.runAllTimers();
    expect(onNewInterval).toHaveBeenCalledWith(MOCK_TIME - 10000, newDate.getTime());
    errorText = component.find('.date-input-error-text').text();
    expect(errorText.trim()).toEqual('');

    component.unmount();
  });
});
