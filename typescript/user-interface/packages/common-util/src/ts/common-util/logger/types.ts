/** the defined log levels */
export enum LogLevel {
  OFF = 'off',
  ERROR = 'error',
  WARN = 'warn',
  INFO = 'info',
  DEBUG = 'debug'
}

/** the defined log level order */
export enum LogOrder {
  OFF,
  ERROR,
  WARN,
  INFO,
  DEBUG
}

/** the log settings */
export interface LogSettings {
  readonly shouldLogDebug?: boolean;
  readonly shouldLogInfo?: boolean;
  readonly shouldLogWarn?: boolean;
  readonly shouldLogError?: boolean;
}

/** the logger interface */
export interface ILogger {
  debug(message: string, ...optionalParams: unknown[]): void;
  info(message: string, ...optionalParams: unknown[]): void;
  warn(message: string, ...optionalParams: unknown[]): void;
  error(message: string, ...optionalParams: unknown[]): void;
}
