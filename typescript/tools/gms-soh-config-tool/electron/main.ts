import { app, BrowserWindow, ipcMain, } from 'electron';
import * as path from 'path';
import * as isDev from 'electron-is-dev';
import installExtension, {
  REACT_DEVELOPER_TOOLS,
  REDUX_DEVTOOLS,
} from 'electron-devtools-installer';
import * as notification from './notification';
import * as io from './io';

export let mainWindow: BrowserWindow | null = null;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1678,
    height: 1080,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: true
    },
  });

  mainWindow.webContents.session.webRequest.onBeforeSendHeaders(
    (details, callback) => {
      callback({ requestHeaders: { Origin: '*', ...details.requestHeaders } });
    }
  );

  mainWindow.webContents.session.webRequest.onHeadersReceived(
    (details, callback) => {
      callback({
        responseHeaders: {
          ...details.responseHeaders,
          'access-control-allow-origin': ['*'],
        },
      });
    }
  );

  if (isDev) {
    mainWindow.loadURL('http://localhost:3000/index.html');
  } else {
    // 'build/index.html'
    mainWindow.loadURL(`file://${__dirname}/../index.html`);
  }

  mainWindow.on('closed', () => (mainWindow = null));

  // Hot Reloading
  if (isDev) {
    // 'node_modules/.bin/electronPath'
    require('electron-reload')(__dirname, {
      electron: path.join(
        __dirname,
        '..',
        '..',
        'node_modules',
        '.bin',
        'electron'
      ),
      forceHardReset: true,
      hardResetMethod: 'exit',
    });
  }

  // DevTools
  installExtension([REDUX_DEVTOOLS, REACT_DEVELOPER_TOOLS])
    .then((name: string) => console.log(`Added Extension:  ${name}`))
    .catch((err: string) => console.log('An error occurred: ', err));

  if (isDev) {
    mainWindow.webContents.openDevTools();
  }
}

app.name = 'SOH Config Tool';

app.on('ready', createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (mainWindow === null) {
    createWindow();
  }
});

/************************/

// listen to file(s) add event
ipcMain.handle('app:on-file-add', (event, args) => {
  if (!args?.data && !args?.fileName) {
    notification.message(
      `Error: Bad arguments for file. Args are invalid: ${args}`
    );
    return;
  }

  io.addFile(args.data, args.fileName);
});

ipcMain.handle('app:get-dir-path', (event, args) => io.getDirPath());

ipcMain.handle('app:get-app-setting', (event, args) =>
  io.getAppSetting(args.settingKey)
);

ipcMain.handle('app:get-app-settings', () => io.getAppSettings());

ipcMain.handle('app:set-app-settings', (event, args) =>
  io.setAppSettings(args.settings)
);

ipcMain.handle('app:load-file', (event, args) => io.loadFile(args.filePath));

ipcMain.handle('app:delete-file', (event, args) =>
  io.deleteFile(args.directory, args.filePath)
);

ipcMain.handle('app:get-file-path', () => io.getFilePath());

ipcMain.handle('app:load-config-from-dir', (event, args) =>
  io.loadConfigFromDir(args.dirName)
);
