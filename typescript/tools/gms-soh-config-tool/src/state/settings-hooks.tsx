import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { AppSettingsState, setAppSettings } from './app-settings-slice';
import { AppState } from './store';

export const useAppSettings = (): [
  AppSettingsState,
  (settings: AppSettingsState) => void
] => {
  const settingsData = useSelector((state: AppState) => state['app-settings']);
  const dispatch = useDispatch();
  const updateAppSettings = React.useCallback(
    (settings: AppSettingsState) => {
      dispatch(setAppSettings(settings));
    },
    [dispatch]
  );
  return [settingsData, updateAppSettings];
};
