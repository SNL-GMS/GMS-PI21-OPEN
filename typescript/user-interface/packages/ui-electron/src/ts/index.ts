import { getElectron, isElectron, UILogger } from '@gms/ui-util';
// eslint-disable-next-line import/no-extraneous-dependencies
import { app, BrowserWindow, ipcMain } from 'electron';
import delay from 'lodash/delay';
import includes from 'lodash/includes';
import isEmpty from 'lodash/isEmpty';
// eslint-disable-next-line import/no-extraneous-dependencies
import process from 'process';

import { SERVER_URL } from './constants';
import { createMainWindow } from './main-window';
import { clearLayout, loadLayout, persistLayout } from './persist-layout';
import type { PopoutOptions } from './popout-window';
import { createPopoutWindow } from './popout-window';

const logger = UILogger.create('GMS_LOG_ELECTRON', process.env.GMS_LOG_ELECTRON);

// for debugging purposes, add context menu for inspecting elements;
/* eslint-disable */
// require('electron-context-menu')({});
/* eslint-enable */

logger.info(`Process is running on electron: ${isElectron()}`);
logger.info(`Required electron successfully: ${getElectron() !== undefined}`);
logger.info(`Setting up Redux store for electron`);
// TODO: Investigate building the store before electron
// removing the store from the main thread of electron because
// we do not have window before starting electron
// getStore(); // set up redux in the main process
logger.info(`Completed setup for Redux store for electron`);

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow: Electron.BrowserWindow;
// eslint-disable-next-line import/no-mutable-exports
export let nextPopoutWindow: Electron.BrowserWindow;

/**
 * Load & rehydrate a saved configuration
 *
 * @param layout layout
 */
function loadSavedConfiguration(layout) {
  layout.forEach(windowLayout => {
    if (windowLayout === null) return;
    const { bounds, url, title, popoutConfig } = windowLayout;
    const window = new BrowserWindow({
      title,
      x: bounds.x,
      y: bounds.y,
      height: bounds.height,
      width: bounds.width,
      backgroundColor: '#182026',
      webPreferences: { nodeIntegration: true }
    });

    // eslint-disable-next-line
    window.loadURL(url);

    if (popoutConfig) {
      window.setMenuBarVisibility(false);

      delay(() => {
        // if the HTML tag <title> is defined in the HTML file loaded by loadURL();
        // the `title` property will be ignored. force update
        window.setTitle(title);
        // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      }, 1000);

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (window.webContents as any).popoutConfig = popoutConfig;
    } else {
      mainWindow = window;
    }
  });
}

/**
 * Set up the application.
 * Load layout and re-hydrate stored layout, or initialize a blank layout
 */
async function initialize() {
  // eslint-disable-next-line @typescript-eslint/dot-notation, dot-notation
  const [, , ...argv] = process['argv'];
  if (includes(argv, '--clear')) {
    await clearLayout();
  }

  const layout = await loadLayout();
  if (isEmpty(layout)) {
    mainWindow = createMainWindow();
  } else {
    loadSavedConfiguration(layout);
  }

  mainWindow.on('close', () => {
    app.quit();
  });

  nextPopoutWindow = new BrowserWindow({
    autoHideMenuBar: true,
    show: false,
    backgroundColor: '#182026',
    webPreferences: { nodeIntegration: true }
  });
  nextPopoutWindow.setMenuBarVisibility(false);

  // eslint-disable-next-line
  nextPopoutWindow.loadURL(`${SERVER_URL}/#/loading`);

  // pop-out events from the main window will broadcast on this channel.
  ipcMain.on('popout-window', (event, args) => {
    nextPopoutWindow = createPopoutWindow(args, nextPopoutWindow);
  });

  // pop-in events from pop-out windows will broadcast on this channel.
  ipcMain.on('popin-window', (event, args) => {
    // fire a popin-resolve event to the main window
    mainWindow.webContents.send('popin-window-resolve', args);
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  ipcMain.on('state-changed', (event, args) => {
    persistLayout(nextPopoutWindow);
  });
}

/**
 * Create a pop-out window
 *
 * @param options options
 */
export function popout(options: PopoutOptions): void {
  nextPopoutWindow = createPopoutWindow(options, nextPopoutWindow);
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', initialize);

app.on('browser-window-created', (event, window) => {
  if (SERVER_URL.includes('localhost') && process.env.ELECTRON_DEV_TOOLS) {
    window.webContents.openDevTools();
  }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  window.on('move', e => {
    if (nextPopoutWindow) {
      persistLayout(nextPopoutWindow);
    }
  });
});

app.on('window-all-closed', () => {
  app.quit();
});
