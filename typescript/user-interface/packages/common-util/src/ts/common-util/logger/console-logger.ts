/* eslint-disable class-methods-use-this */
/* eslint-disable no-console */
import type { ILogger } from './types';

/**
 * A simple console logger for logging console logs.
 */
export class ConsoleLogger implements ILogger {
  private static instance: ConsoleLogger;

  /** returns the logger instance */
  public static Instance(): ConsoleLogger {
    if (!ConsoleLogger.instance) {
      ConsoleLogger.instance = new ConsoleLogger();
    }
    return ConsoleLogger.instance;
  }

  // eslint-disable-next-line no-useless-constructor
  private constructor() {
    /* no-op */
  }

  /**
   * The `console.debug()` function.
   *
   * @param message the message
   * @param optionalParams the optional parameters
   */
  public readonly debug = (message: string, ...optionalParams: unknown[]): void => {
    console.debug(message, ...optionalParams);
  };

  /**
   * The `console.info()` function.
   *
   * @param message the message
   * @param optionalParams the optional parameters
   */
  public readonly info = (message: string, ...optionalParams: unknown[]): void => {
    console.info(message, ...optionalParams);
  };

  /**
   * The `console.warn()` function.
   *
   * @param message the message
   * @param optionalParams the optional parameters
   */
  public readonly warn = (message: string, ...optionalParams: unknown[]): void => {
    console.warn(message, ...optionalParams);
  };

  /**
   * The `console.time()` function.
   *
   * @param label the unique label
   */
  public readonly time = (label: string): void => {
    console.time(label);
  };

  /**
   * The `console.timeEnd()` function.
   *
   * @param label the unique label
   */
  public readonly timeEnd = (label: string): void => {
    console.timeEnd(label);
  };

  /**
   * The `console.error()` function.
   *
   * @param message the message
   * @param optionalParams the optional parameters
   */
  public readonly error = (message: string, ...optionalParams: unknown[]): void => {
    console.error(message, ...optionalParams);
  };
}
