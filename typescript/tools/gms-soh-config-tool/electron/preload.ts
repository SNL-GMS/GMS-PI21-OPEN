import { contextBridge, ipcRenderer } from 'electron';
import path = require('path');
import { PersistedAppSettings } from './io';
import * as fs from 'fs-extra';

function getUserHome(): string {
  return process.env[(process.platform === 'win32') ? 'USERPROFILE' : 'HOME'] || '';
}

const SOH_CONFIG_TOOL = 'SOH Config UI';
const SOH_CONFIGS = 'soh-configs';

const sohConfigToolDir = path.join(getUserHome(), SOH_CONFIG_TOOL);
const sohConfigurationDir = path.join(sohConfigToolDir, SOH_CONFIGS);

console.debug(`User SOH Config UI directory: ${sohConfigToolDir}`);

if (!fs.existsSync(sohConfigToolDir)){
  fs.mkdirSync(sohConfigToolDir);
}

if (!fs.existsSync(sohConfigurationDir)){
  fs.mkdirSync(sohConfigurationDir);
}

// copy over any new or missing data files; do not overwrite
const defaultConfigs = path.join(process.resourcesPath, SOH_CONFIGS);
if (fs.existsSync(defaultConfigs)) {
  fs.copySync(defaultConfigs, sohConfigurationDir, {overwrite: false});
}

const targetDir = path.join(sohConfigurationDir, `/processing`);
const processingStationGroupFilePath = path.join(
      sohConfigurationDir,
      `/station-reference/stationdata/processing-station-group.json`
    );
const supportedMonitorTypesFilePath = path.join(
      sohConfigurationDir,
      `/supported-monitor-types.json`
    );


const electronAPI = {
  saveDataAsFile: (data: any, fileName: string) =>
    ipcRenderer.invoke('app:on-file-add', { data, fileName }),
  getDirPath: () => ipcRenderer.invoke('app:get-dir-path'),
  setAppSettings: (settings: Partial<PersistedAppSettings>) =>
    ipcRenderer.invoke('app:set-app-settings', { settings }),
  getAppSetting: (settingKey: string) =>
    ipcRenderer.invoke('app:get-app-setting', { settingKey }),
  getAppSettings: () => ipcRenderer.invoke('app:get-app-settings'),
  loadFile: (filePath: string) =>
    ipcRenderer.invoke('app:load-file', { filePath }),
  deleteFile: (directory: string, filePath: string) =>
    ipcRenderer.invoke('app:delete-file', { directory, filePath }),
  getFilePath: () => ipcRenderer.invoke('app:get-file-path'),
  loadConfigFromDir: (dirName: string) =>
    ipcRenderer.invoke('app:load-config-from-dir', { dirName }),
  defaultPaths: {
    targetDir,
    processingStationGroupFilePath,
    supportedMonitorTypesFilePath
  },
};

export type electronAPIType = typeof electronAPI;

contextBridge.exposeInMainWorld('electronAPI', electronAPI);
