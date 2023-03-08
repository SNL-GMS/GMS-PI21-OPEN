import * as path from 'path';
import * as fs from 'fs-extra';
import * as Store from 'electron-store';
import { flatten } from 'lodash';

// local dependencies
import * as notification from './notification';
import { dialog } from 'electron';
import { mainWindow } from './main';

export interface PersistedAppSettings {
  targetDir: string | null;
  processingStationGroupFilePath: string | null;
  serviceURL: string | null;
  supportedMonitorTypesFilePath: string | null;
}

const appStoreSchema: Record<keyof PersistedAppSettings, any> = {
  targetDir: {
    type: ['string', 'null'],
  },
  processingStationGroupFilePath: {
    type: ['string', 'null'],
  },
  serviceURL: {
    type: ['string', 'null'],
  },
  supportedMonitorTypesFilePath: {
    type: ['string', 'null'],
  },
} as const; // needed to get the types right for the schema

const appStore = new Store<PersistedAppSettings>({ schema: appStoreSchema });

const isKeyOfPersistedAppSettings = (
  keyToCheck: string
): keyToCheck is keyof PersistedAppSettings => {
  return Object.keys(appStoreSchema).includes(keyToCheck);
};

// get application directory
let targetDir = appStore.get('targetDir');

export const getDirPath = async () => {
  if (!mainWindow) {
    return;
  }
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openDirectory'],
  });
  if (result.canceled) {
    return undefined;
  }
  if (!result.filePaths) {
    notification.error('Invalid directory');
    return;
  }
  if (result.filePaths.length > 1) {
    notification.message(
      `Only one directory supported. Using the first directory chosen`
    );
  }
  return result.filePaths[0];
};

export const setAppSettings = (settings: Partial<PersistedAppSettings>) => {
  const settingsToSave: PersistedAppSettings = {} as PersistedAppSettings;
  Object.keys(appStoreSchema).forEach((paramName) => {
    if (isKeyOfPersistedAppSettings(paramName)) {
      settingsToSave[paramName] = settings[paramName] ?? null;
    } else {
      notification.error(`Invalid key in settings: ${paramName}`);
    }
  });
  appStore.set(settingsToSave);
  targetDir = settingsToSave.targetDir;
};

export const getAppSetting = (settingKey: keyof PersistedAppSettings) =>
  appStore.get(settingKey);

export const getAppSettings = () => {
  return Object.keys(appStoreSchema).reduce<PersistedAppSettings>(
    (settingsObj: PersistedAppSettings, settingKey: string) => {
      if (isKeyOfPersistedAppSettings(settingKey)) {
        settingsObj[settingKey] = appStore.get(settingKey);
        return settingsObj;
      } else {
        throw new Error(
          `App Settings schema does not have a key ${settingKey}.`
        );
      }
    },
    {} as PersistedAppSettings
  );
};

/****************************/

// get the list of files
export const getFiles = (dir = targetDir) => {
  if (!dir) {
    return null;
  }
  const files = fs.readdirSync(dir);

  return files.map((filename: string) => {
    const filePath = path.resolve(dir, filename);
    const fileStats = fs.statSync(filePath);

    return {
      name: filename,
      path: filePath,
      size: Number(fileStats.size / 1000).toFixed(1), // kb
    };
  });
};

/****************************/

// add file
export const addFile = (data: any, fileName: string) => {
  if (!targetDir) {
    notification.error(`Error adding ${fileName}, directory not set.`);
    return;
  }
  // ensure `targetDir` exists
  fs.ensureDirSync(targetDir);

  const filePath = path.resolve(targetDir, fileName);

  fs.outputJsonSync(filePath, data, { spaces: 2 });

  notification.filesAdded(fileName);
};

// open a file
export const loadFile = (filePath: string) => {
  // open a file using default application
  if (fs.pathExistsSync(filePath)) {
    return fs.readJsonSync(filePath);
  } else {
    notification.message(`File not found, cannot read file ${filePath}`);
  }
};

// delete a file
export const deleteFile = (directory: string, fileName: string) => {
  const dirPath = `${targetDir}/${directory}`;
  const filePath = path.resolve(dirPath, fileName);
  if (fs.pathExistsSync(filePath)) {
    fs.unlink(filePath);
  } else {
    notification.message(`File not found, cannot delete file ${fileName}`);
  }
};

export const getFilePath = async (): Promise<string | null> => {
  if (!mainWindow) {
    return null;
  }
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile'],
  });
  if (result.canceled) {
    return null;
  }
  if (!result.filePaths) {
    notification.message('Error: Invalid directory');
    return null;
  }
  if (result.filePaths.length > 1) {
    notification.message(
      `Only one file supported. Using the first file chosen`
    );
  }
  return result.filePaths[0];
};

export const loadConfigFromDir = (configDir: string) => {
  if (!targetDir) {
    return undefined;
  }
  const dirPath = `${targetDir}/${configDir}`;
  const files = fs.readdirSync(`${targetDir}/${configDir}`);

  return flatten(
    files
      .filter((filename: string) => {
        return filename.endsWith('.json');
      })
      .map((filename: string) => {
        console.log({ filename });
        const filePath = path.resolve(dirPath, filename);
        console.log({ filePath });
        return loadFile(filePath);
      })
  );
};
