import { UILogger } from '@gms/ui-util';
// eslint-disable-next-line import/no-extraneous-dependencies
import { BrowserWindow } from 'electron';

import { SERVER_URL } from './constants';

const logger = UILogger.create('GMS_LOG_ELECTRON', process.env.GMS_LOG_ELECTRON);

/**
 * Options used to create a popout-window
 */
export interface PopoutOptions {
  /**
   * The popout bounds
   */
  bounds?: {
    /**
     * The x bounds
     */
    x: number;
    /**
     * The y bounds
     */
    y: number;
  };
  /**
   * The popout configuration
   */
  config: {
    /**
     * The component
     */
    component: string;
    /**
     * The component name
     */
    componentName: 'lm-react-component';
    /**
     * Is closable flag
     */
    isClosable: true;
    /**
     * Reorder enabled flag
     */
    reorderEnabled: true;
    /**
     * The title
     */
    title: string;
    /**
     * The type
     */
    type: 'component';
  }[];
  /**
   * The popout title
   */
  title: string;
  /**
   * The popout url
   */
  url: string;
}

/**
 * Create a popout-window with the given configuration
 *
 * @param options options
 * @param nextWindow next window
 */
export function createPopoutWindow(
  options: PopoutOptions,
  nextWindow: Electron.BrowserWindow
): Electron.BrowserWindow {
  const onShow = (window: Electron.BrowserWindow, opts: PopoutOptions): void => {
    const currentWindow = window;

    // nextWindow.setBounds({
    //     height: options.bounds.height,
    //     width: options.bounds.width,
    //     x: options.bounds.x,
    //     y: options.bounds.y,
    // });

    logger.debug(`Opening popout window ${opts.url.substring(opts.url.indexOf('#/'))}`);

    // set popout config on the window so that it can pop itself back in correctly.
    // TODO figure out how to do this properly in electron (set metadata on a BrowserWindow)
    // eslint-disable-next-line prefer-destructuring, @typescript-eslint/no-explicit-any, no-param-reassign
    (currentWindow.webContents as any).popoutConfig = options.config[0];

    currentWindow.webContents.send('load-path', opts.url.substring(opts.url.indexOf('#/')));
  };

  const currentWindowToShow = nextWindow;
  currentWindowToShow.setTitle(options.title);
  currentWindowToShow.on('show', () => onShow(nextWindow, options));
  currentWindowToShow.show();

  // eslint-disable-next-line no-param-reassign
  nextWindow = new BrowserWindow({
    autoHideMenuBar: true,
    show: false,
    backgroundColor: '#182026',
    webPreferences: { nodeIntegration: true }
  });
  nextWindow.setMenuBarVisibility(false);

  // eslint-disable-next-line
  nextWindow.loadURL(`${SERVER_URL}/#/loading`);

  return nextWindow;
}
