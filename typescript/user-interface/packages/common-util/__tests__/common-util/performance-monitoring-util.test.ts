/* eslint-disable no-console */
import { Timer } from '../../src/ts/common-util';

jest.mock('../../src/ts/common-util/environment-util.ts', () => ({
  GMS_PERFORMANCE_MONITORING_ENABLED: 'verbose'
}));

describe('Performance Monitoring Utils', () => {
  it('makes console time calls if GMS_PERFORMANCE_MONITORING_ENABLED is set', () => {
    console.time = jest.fn();
    Date.now = jest.fn().mockReturnValue(0);
    Timer.start('SHOULD MAKE CALLS');
    Date.now = jest.fn().mockReturnValue(Timer.ONE_FRAME_MS);
    Timer.end('SHOULD MAKE CALLS');
    expect(console.time).toHaveBeenCalled();
  });

  it('Does not log a warning if the time elapsed is under one frame', () => {
    console.warn = jest.fn();
    Date.now = jest.fn().mockReturnValue(0);
    Timer.start('test');
    Date.now = jest.fn().mockReturnValue(Timer.ONE_FRAME_MS);
    Timer.end('test');
    expect(console.warn).not.toHaveBeenCalled();
  });

  it('Logs a warning if the time elapsed is greater than 1.5 frames', () => {
    console.warn = jest.fn();
    Date.now = jest.fn().mockReturnValue(0);
    Timer.start('too long');
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    Date.now = jest.fn().mockReturnValue(Timer.ONE_FRAME_MS * 1.5 + 1);
    Timer.end('too long');
    expect(console.warn).toHaveBeenCalled();
  });

  it('can set a custom max time limit', () => {
    console.warn = jest.fn();
    Date.now = jest.fn().mockReturnValue(0);
    Timer.start('with custom limit');
    Date.now = jest.fn().mockReturnValue(1000);
    Timer.end('with custom limit', 1000);
    expect(console.warn).not.toHaveBeenCalled();
  });

  it('can tell if a duplicate key was set', () => {
    console.warn = jest.fn();
    Date.now = jest.fn().mockReturnValue(0);
    Timer.start('with duplicate key');
    Date.now = jest.fn().mockReturnValue(1);
    Timer.start('with duplicate key');
    expect(console.warn).toHaveBeenCalled();
    Timer.end('with duplicate key', 2);
  });

  it('clears a key on end', () => {
    console.warn = jest.fn();
    Date.now = jest.fn().mockReturnValue(0);
    Timer.start('with key cleared');
    Date.now = jest.fn().mockReturnValue(1);
    Timer.end('with key cleared');
    Date.now = jest.fn().mockReturnValue(2);
    Timer.start('with key cleared');
    Date.now = jest.fn().mockReturnValue(3);
    Timer.end('with key cleared');
    expect(console.warn).not.toHaveBeenCalled();
  });
});
