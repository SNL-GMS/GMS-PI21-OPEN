import { ConsoleLogger } from '../../../src/ts/common-util/logger/console-logger';

describe('console logger', () => {
  test('exists', () => {
    expect(ConsoleLogger).toBeDefined();
  });

  test('ConsoleLogger', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyTime = jest.spyOn(console, 'time').mockImplementation();
    const spyTimeEnd = jest.spyOn(console, 'timeEnd').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = ConsoleLogger.Instance();

    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(1);

    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(1);

    logger.time('time string');
    expect(spyTime).toBeCalledTimes(1);

    logger.timeEnd('time string');
    expect(spyTimeEnd).toBeCalledTimes(1);

    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(1);

    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyTime.mockRestore();
    spyTimeEnd.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });
});
