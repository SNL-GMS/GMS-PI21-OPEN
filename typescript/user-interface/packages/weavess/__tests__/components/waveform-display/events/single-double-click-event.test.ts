/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SingleDoubleClickEvent } from '../../../../src/ts/components/waveform-display/events/single-double-click-event';

// Tell Jest to mock all timeout functions
jest.useFakeTimers();

describe('SingleDoubleClickEvent', () => {
  it('are defined', () => {
    expect(SingleDoubleClickEvent).toBeDefined();
    expect(SingleDoubleClickEvent.SINGLE_CLICK_DELAY_MS).toEqual(600);
    expect(SingleDoubleClickEvent.DOUBLE_CLICK_TIMEOUT_MS).toEqual(2000);
  });

  it('testing single click event', () => {
    const event: any = { type: 'click' };
    const mockHandler = jest.fn();
    const singleDoubleClickEvent = new SingleDoubleClickEvent();
    singleDoubleClickEvent.onSingleClickEvent(event, mockHandler);
    singleDoubleClickEvent.onSingleClickEvent(event, mockHandler);
    // Fast-forward time
    jest.runAllTimers();
    expect(mockHandler).toBeCalled();
  });

  it('testing double click event', () => {
    const event: any = { type: 'click' };
    const mockSingleClickHandler = jest.fn();
    const mockDoubleClickHandler = jest.fn();
    const singleDoubleClickEvent = new SingleDoubleClickEvent();
    singleDoubleClickEvent.onDoubleClick(event, mockDoubleClickHandler);
    singleDoubleClickEvent.onSingleClickEvent(event, mockSingleClickHandler);
    singleDoubleClickEvent.onDoubleClick(event, mockDoubleClickHandler);
    // Fast-forward time
    jest.runAllTimers();
    expect(mockSingleClickHandler).not.toBeCalled();
    expect(mockDoubleClickHandler).toBeCalled();
  });

  // eslint-disable-next-line jest/expect-expect
  it('call with undefined functions', () => {
    const event: any = { type: 'click' };
    const mockSingleClickHandler = undefined;
    const mockDoubleClickHandler = undefined;
    const singleDoubleClickEvent = new SingleDoubleClickEvent();
    singleDoubleClickEvent.onDoubleClick(event, mockDoubleClickHandler);
    singleDoubleClickEvent.onSingleClickEvent(event, mockSingleClickHandler);
    singleDoubleClickEvent.onDoubleClick(event, mockDoubleClickHandler);
  });
});
