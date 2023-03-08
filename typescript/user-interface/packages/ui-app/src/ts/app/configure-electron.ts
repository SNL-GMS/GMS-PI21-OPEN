import { getElectron } from '@gms/ui-util';

/**
 * Configures Electron.
 */
export const configureElectron = (): void => {
  // electron instance; undefined if not running in electron
  const electron = getElectron();
  if (electron !== undefined && electron.ipcRenderer !== undefined) {
    electron.ipcRenderer.on('load-path', (event, newHash: string) => {
      window.location.hash = newHash;
    });
  }
};
