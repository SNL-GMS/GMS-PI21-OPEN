import type { ILogger } from '@gms/common-util';
import type { Logger } from 'log4javascript';
import { getLogger, Level, PopUpAppender } from 'log4javascript';

/**
 * A Log4JavascriptLogger implementation.
 */
export class Log4JavascriptLogger implements ILogger {
  /** The singleton instance of the logger */
  private static instance: Log4JavascriptLogger;

  /** The pop up appender  */
  private readonly popUpAppender: PopUpAppender;

  /** The log4javascript logger */
  private readonly logger: Logger;

  /** Number of max messages stored before purge begins */
  private readonly maxMessages: number = 1000;

  /** returns the logger instance */
  public static Instance(): Log4JavascriptLogger {
    if (!Log4JavascriptLogger.instance) {
      Log4JavascriptLogger.instance = new Log4JavascriptLogger();
    }
    return Log4JavascriptLogger.instance;
  }

  private constructor() {
    // create a Pop Up Appender with default options
    this.popUpAppender = new PopUpAppender();

    // change the desired configuration options
    this.popUpAppender.setNewestMessageAtTop(true);
    this.popUpAppender.setComplainAboutPopUpBlocking(true);
    this.popUpAppender.setUseOldPopUp(true);
    this.popUpAppender.setReopenWhenClosed(true);
    this.popUpAppender.setScrollToLatestMessage(true);
    this.popUpAppender.setFocusPopUp(false);
    this.popUpAppender.setInitiallyMinimized(true);
    this.popUpAppender.setMaxMessages(this.maxMessages);
    this.popUpAppender.hide();

    // initialize the logger
    this.logger = getLogger('logger');
    this.logger.addAppender(this.popUpAppender);
    this.logger.setLevel(Level.ALL);
  }

  /**
   * Logs to the log4javascript logger
   *
   * @param level the log level
   * @param messages the messages to log
   */
  private readonly log = (level: Level, message: string, ...optionalParams: unknown[]): void => {
    this.logger.log(level, [message, ...optionalParams]);
  };

  /**
   * Shows the log pop out window.
   */
  public readonly showLogPopUpWindow = (): void => {
    this.popUpAppender.show();
  };

  /**
   * Debug log
   *
   * @param message type string message to be logged
   * @param messages
   */
  public readonly debug = (message: string, ...optionalParams: unknown[]): void => {
    this.log(Level.DEBUG, message, ...optionalParams);
  };

  /**
   * Info log
   *
   * @param message type string message to be logged
   * @param messages
   */
  public readonly info = (message: string, ...optionalParams: unknown[]): void => {
    this.log(Level.INFO, message, ...optionalParams);
  };

  /**
   * Warning log
   *
   * @param message type string message to be logged
   * @param messages
   */
  public readonly warn = (message: string, ...optionalParams: unknown[]): void => {
    this.log(Level.WARN, message, ...optionalParams);
  };

  /**
   * Error log
   *
   * @param message type string message to be logged
   * @param messages
   */
  public readonly error = (message: string, ...optionalParams: unknown[]): void => {
    this.log(Level.ERROR, message, ...optionalParams);
  };
}
