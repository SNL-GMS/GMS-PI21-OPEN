import type { ILogger, LogLevel } from '@gms/common-util';
import { ConsoleLogger, IS_NODE_ENV_PRODUCTION, IS_NODE_ENV_TEST, Logger } from '@gms/common-util';

import { GMS_UI_LOGGERS, LoggerType, windowIsDefined } from './types';

/**
 * @returns returns the logger instances that are configured
 */
const getConfiguredLoggerInstances = (): ILogger[] => {
  // window is not defined; default to console logger only
  if (!windowIsDefined) {
    return [ConsoleLogger.Instance()];
  }

  // ! require this library only if `window` is defined
  // eslint-disable-next-line @typescript-eslint/no-var-requires, global-require, @typescript-eslint/no-require-imports
  const { Log4JavascriptLogger } = require('./log4js-logger');

  // configured override use both loggers
  if (GMS_UI_LOGGERS === LoggerType.ALL) {
    return [ConsoleLogger.Instance(), Log4JavascriptLogger.Instance()];
  }

  // configured override use only console logger
  if (GMS_UI_LOGGERS === LoggerType.CONSOLE) {
    return [ConsoleLogger.Instance()];
  }

  // configured override use only log4javascript logger
  if (GMS_UI_LOGGERS === LoggerType.LOG4JAVASCRIPT) {
    return [Log4JavascriptLogger.Instance()];
  }

  // no override; production only use log4javascript logger
  if (IS_NODE_ENV_PRODUCTION) {
    return [Log4JavascriptLogger.Instance()];
  }

  // no override; test only use console logger
  if (IS_NODE_ENV_TEST) {
    return [ConsoleLogger.Instance()];
  }

  // no override; development use both loggers
  return [ConsoleLogger.Instance(), Log4JavascriptLogger.Instance()];
};

/**
 * A simple UI logger that provides settings for enabling and disabling logs.
 */
export const UILogger = {
  /**
   * Create a logger instance
   *
   * @param id (optional) the unique id
   * @param level (optional) the unique id
   */
  create: (id: string, level: LogLevel | string | undefined): Logger => {
    return Logger.create(id, level).setConfiguredLoggers(getConfiguredLoggerInstances());
  },

  /**
   * Shows the log pop out window.
   */
  showLogPopUpWindow: (): void => {
    if (windowIsDefined) {
      // ! require this library only if `window` is defined
      // eslint-disable-next-line @typescript-eslint/no-var-requires, global-require, @typescript-eslint/no-require-imports
      const { Log4JavascriptLogger } = require('./log4js-logger');
      Log4JavascriptLogger.Instance().showLogPopUpWindow();
    }
  }
};
