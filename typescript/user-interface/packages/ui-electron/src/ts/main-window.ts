// eslint-disable-next-line import/no-extraneous-dependencies
import { BrowserWindow } from 'electron';

import { SERVER_URL } from './constants';

/**
 * Create a BrowserWindow which will be the primary window (containing the analyst workspace)
 */
export function createMainWindow(): BrowserWindow {
  const window = new BrowserWindow({
    width: 1500,
    height: 900,
    title: 'GMS / Interactive Analysis',
    webPreferences: { nodeIntegration: true },
    backgroundColor: '#182026'
  });

  // eslint-disable-next-line
  window.loadURL(SERVER_URL);

  return window;
}
