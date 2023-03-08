import { LogLevel } from '@gms/common-util';

import { Log4JavascriptLogger } from '../../../src/ts/ui-util/logger/log4js-logger';
import { UILogger } from '../../../src/ts/ui-util/logger/logger';
import * as constants from '../../../src/ts/ui-util/logger/types';

describe('uilogger', () => {
  test('exists', () => {
    expect(UILogger).toBeDefined();
  });

  test('initialization', () => {
    const { windowIsDefined } = constants as any;
    const { GMS_UI_LOGGERS } = constants as any;

    (constants as any).windowIsDefined = false;
    let logger = UILogger.create('test', '');
    expect(logger.getConfiguredLoggers().size).toEqual(1);

    (constants as any).windowIsDefined = true;
    logger = UILogger.create('test', '');
    expect(logger.getConfiguredLoggers().size).toEqual(1); // NODE_ENV === test

    (constants as any).GMS_UI_LOGGERS = constants.LoggerType.ALL;
    logger = UILogger.create('test', '');
    expect(logger.getConfiguredLoggers().size).toEqual(2);

    (constants as any).GMS_UI_LOGGERS = constants.LoggerType.CONSOLE;
    logger = UILogger.create('test', '');
    expect(logger.getConfiguredLoggers().size).toEqual(1);

    (constants as any).GMS_UI_LOGGERS = constants.LoggerType.LOG4JAVASCRIPT;
    logger = UILogger.create('test', '');
    expect(logger.getConfiguredLoggers().size).toEqual(1);

    (constants as any).windowIsDefined = true;
    logger = UILogger.create('test', '');
    expect(logger.getConfiguredLoggers().size).toEqual(1); // NODE_ENV === test

    (constants as any).windowIsDefined = windowIsDefined;
    (constants as any).GMS_UI_LOGGERS = GMS_UI_LOGGERS;
  });

  test('logger invalid', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    let logger = UILogger.create('test', undefined);
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(1);
    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(1);
    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);

    logger = UILogger.create('test', '');
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(2);
    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(2);
    logger.error('error string');
    expect(spyError).toBeCalledTimes(2);

    logger = UILogger.create('test', 'bad');
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(2);
    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(2);
    logger.error('error string');
    expect(spyError).toBeCalledTimes(2);

    logger = UILogger.create('test', 'debug');
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(1);
    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(3);
    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(3);
    logger.error('error string');
    expect(spyError).toBeCalledTimes(3);

    logger = UILogger.create('test', 'DEBUG');
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(2);
    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(4);
    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(4);
    logger.error('error string');
    expect(spyError).toBeCalledTimes(4);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger disabled', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = UILogger.create('test', LogLevel.OFF);
    logger.debug('debug string', 'optional params');
    expect(spyDebug).not.toBeCalled();
    logger.info('info string', 'optional params');
    expect(spyInfo).not.toBeCalled();
    logger.warn('warn string', 'optional params');
    expect(spyWarn).not.toBeCalled();
    logger.error('error string', 'optional params');
    expect(spyError).not.toBeCalled();

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger enabled default', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = UILogger.create('test', process.env.test);
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.debug('debug string', 'optional params');
    expect(spyDebug).toBeCalledTimes(0);

    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(1);
    logger.info('info string', 'optional params');
    expect(spyInfo).toBeCalledTimes(2);

    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(1);
    logger.warn('warn string', 'optional params');
    expect(spyWarn).toBeCalledTimes(2);

    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);
    logger.error('error string', 'optional params');
    expect(spyError).toBeCalledTimes(2);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger enabled debug', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = UILogger.create('test', LogLevel.DEBUG);
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(1);
    logger.debug('debug string', 'optional params');
    expect(spyDebug).toBeCalledTimes(2);

    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(1);
    logger.info('info string', 'optional params');
    expect(spyInfo).toBeCalledTimes(2);

    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(1);
    logger.warn('warn string', 'optional params');
    expect(spyWarn).toBeCalledTimes(2);

    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);
    logger.error('error string', 'optional params');
    expect(spyError).toBeCalledTimes(2);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger enabled info', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = UILogger.create('test', LogLevel.INFO);
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.debug('debug string', 'optional params');
    expect(spyDebug).toBeCalledTimes(0);

    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(1);
    logger.info('info string', 'optional params');
    expect(spyInfo).toBeCalledTimes(2);

    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(1);
    logger.warn('warn string', 'optional params');
    expect(spyWarn).toBeCalledTimes(2);

    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);
    logger.error('error string', 'optional params');
    expect(spyError).toBeCalledTimes(2);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger enabled warn', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = UILogger.create('test', LogLevel.WARN);
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.debug('debug string', 'optional params');
    expect(spyDebug).toBeCalledTimes(0);

    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(0);
    logger.info('info string', 'optional params');
    expect(spyInfo).toBeCalledTimes(0);

    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(1);
    logger.warn('warn string', 'optional params');
    expect(spyWarn).toBeCalledTimes(2);

    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);
    logger.error('error string', 'optional params');
    expect(spyError).toBeCalledTimes(2);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger enabled error', () => {
    const spyDebug = jest.spyOn(console, 'debug').mockImplementation();
    const spyInfo = jest.spyOn(console, 'info').mockImplementation();
    const spyWarn = jest.spyOn(console, 'warn').mockImplementation();
    const spyError = jest.spyOn(console, 'error').mockImplementation();

    const logger = UILogger.create('test', LogLevel.ERROR);
    logger.debug('debug string');
    expect(spyDebug).toBeCalledTimes(0);
    logger.debug('debug string', 'optional params');
    expect(spyDebug).toBeCalledTimes(0);

    logger.info('info string');
    expect(spyInfo).toBeCalledTimes(0);
    logger.info('info string', 'optional params');
    expect(spyInfo).toBeCalledTimes(0);

    logger.warn('warn string');
    expect(spyWarn).toBeCalledTimes(0);
    logger.warn('warn string', 'optional params');
    expect(spyWarn).toBeCalledTimes(0);

    logger.error('error string');
    expect(spyError).toBeCalledTimes(1);
    logger.error('error string', []);
    expect(spyError).toBeCalledTimes(2);

    spyDebug.mockRestore();
    spyInfo.mockRestore();
    spyWarn.mockRestore();
    spyError.mockRestore();
  });

  test('logger popup', () => {
    const { windowIsDefined } = constants as any;
    const logger = Log4JavascriptLogger.Instance();
    const spy = jest.spyOn(logger, 'showLogPopUpWindow').mockImplementation();

    (constants as any).windowIsDefined = false;
    UILogger.showLogPopUpWindow();
    expect(spy).toBeCalledTimes(0);

    (constants as any).windowIsDefined = true;
    UILogger.showLogPopUpWindow();
    expect(spy).toBeCalledTimes(1);

    (constants as any).windowIsDefined = windowIsDefined;
    spy.mockRestore();
  });
});
