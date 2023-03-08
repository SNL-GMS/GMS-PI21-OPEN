// eslint-disable-next-line import/no-extraneous-dependencies
import { BrowserWindow } from 'electron';
// eslint-disable-next-line import/no-extraneous-dependencies
import * as storage from 'electron-json-storage';
import debounce from 'lodash/debounce';
import isEqual from 'lodash/isEqual';

const layoutStorageKey = 'user-layout';

/**
 *
 * @param nextPopout - a handle to the next popout window, which should be ignored during persistence.
 */
const persistLayout = nextPopout => {
  const windows = BrowserWindow.getAllWindows();
  storage.set(
    layoutStorageKey,
    windows
      .map(window => {
        // ignore the next popout window
        if (isEqual(window, nextPopout)) return undefined;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const { popoutConfig } = window.webContents as any;
        const bounds = window.getBounds();
        const url = window.webContents.getURL();
        const title = popoutConfig?.title ?? window.getTitle();
        return {
          url,
          bounds,
          title,
          popoutConfig
        };
      })
      .filter(config => config !== undefined)
  );
};

// only allow for state saves every 500ms, since these events can fire pretty often
// eslint-disable-next-line
const persistLayoutDebounce: ((nextPopout: BrowserWindow) => void) & {
  cancel(): void;
  flush(): void;
} = debounce(
  (nextPopout: Electron.BrowserWindow) => {
    persistLayout(nextPopout);
  },
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  500
);

export { persistLayoutDebounce as persistLayout };

/**
 * Load the layout. layout will be empty if there is no value set for the storage key.
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function loadLayout(): Promise<any> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return new Promise<any>((resolve, reject) => {
    storage.get(layoutStorageKey, (error, layout) => {
      if (error) {
        reject(error);
      } else {
        resolve(layout);
      }
    });
  });
}

/**
 * Clear the layout
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function clearLayout(): Promise<void> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return new Promise<void>((resolve, reject) => {
    storage.clear(error => {
      if (error) {
        reject(error);
      } else {
        resolve();
      }
    });
  });
}
