import { isWindowDefined } from '@gms/common-util';

/** the defined logger types */
export enum LoggerType {
  ALL = 'all',
  CONSOLE = 'console',
  LOG4JAVASCRIPT = 'log4javascript'
}

export const windowIsDefined = isWindowDefined();

/**
 * The configured enabled loggers
 * ? Possible values: `all`, 'console', 'log4javascript'
 */
export const { GMS_UI_LOGGERS } = process.env;
