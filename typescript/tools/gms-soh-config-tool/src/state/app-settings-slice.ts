import { createSlice } from '@reduxjs/toolkit';
import { windowAPI } from '../electron-util';
import { useAppDispatch } from './react-redux-hooks';

export interface AppSettingsState {
  error: string | undefined;
  targetDir: string | undefined;
  processingStationGroupFilePath: string | undefined;
  serviceURL: string | undefined;
  supportedMonitorTypesFilePath: string | undefined
}

export const AppSettingsKeys = [
  'error',
  'targetDir',
  'processingStationGroupFilePath',
  'serviceURL',
  'supportedMonitorTypesFilePath',
] as const;

export const isAppSettingsKey = (
  key: string
): key is keyof AppSettingsState => {
  return AppSettingsKeys.includes(key as keyof AppSettingsState);
};

console.debug(`Default targetDir: ${windowAPI.electronAPI.defaultPaths.targetDir}`);
console.debug(`Default processingStationGroupFilePath: ${windowAPI.electronAPI.defaultPaths.processingStationGroupFilePath}`);
console.debug(`Default supportedMonitorTypesFilePath: ${windowAPI.electronAPI.defaultPaths.supportedMonitorTypesFilePath}`);

export const appSettingsSlice = createSlice({
  name: 'app-settings',
  initialState: {
    error: undefined,
    targetDir: windowAPI.electronAPI.defaultPaths.targetDir,
    processingStationGroupFilePath: windowAPI.electronAPI.defaultPaths.processingStationGroupFilePath,
    serviceURL: '',
    supportedMonitorTypesFilePath: windowAPI.electronAPI.defaultPaths.supportedMonitorTypesFilePath,
  },
  reducers: {
    setError(state, action) {
      state.error = action.payload;
    },
    setTargetDir(state, action) {
      state.targetDir = action.payload;
    },
    setProcessingStationGroupFilePath(state, action) {
      state.processingStationGroupFilePath = action.payload;
    },
    setServiceURL(state, action) {
      state.serviceURL = action.payload;
    },
    setAppSettings(_state, action) {
      return action.payload;
    },
    setSupportedMonitorTypesFilePath(state, action) {
      state.targetDir = action.payload;
    },
  },
});

export const {
  setError,
  setTargetDir,
  setProcessingStationGroupFilePath,
  setServiceURL,
  setAppSettings,
  setSupportedMonitorTypesFilePath,
} = appSettingsSlice.actions;

export const useSetError = () => {
  const dispatch = useAppDispatch();
  return (message: string) => dispatch(setError(message));
};
