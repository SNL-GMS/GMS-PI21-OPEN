import { delayExecutionReturnClearTimeout } from '../../src/ts/common-util/delay-execution-util';

describe('Delay execution utilities', () => {
  jest.useFakeTimers();
  test('a function can be passed to delayExecutionReturnClearTimeout', () => {
    const mockFunction = jest.fn(() => 1);
    delayExecutionReturnClearTimeout(mockFunction);
    jest.runOnlyPendingTimers();
    expect(mockFunction).toHaveBeenCalled();
  });
  test('a function with arguments can be passed to delayExecutionReturnClearTimeout', () => {
    const mockFunction = jest.fn((x: number) => x + 1);
    delayExecutionReturnClearTimeout(mockFunction, 0, 1);
    jest.runOnlyPendingTimers();
    expect(mockFunction).toBeCalledWith(1);
  });
  test('a timeout can be passed to delayExecutionReturnClearTimeout', () => {
    const timeout = 1000;
    const mockFunction = jest.fn(() => 1);
    delayExecutionReturnClearTimeout(mockFunction, timeout);
    expect(mockFunction).not.toBeCalled();
    jest.advanceTimersByTime(1000);
    expect(mockFunction).toBeCalled();
    expect(mockFunction).toHaveBeenCalledTimes(1);
  });
});
