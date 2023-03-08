import produce, { Draft } from 'immer';
import { windowAPI } from '../electron-util';
import { isAppSettingsKey } from '../state/app-settings-slice';
import { useAppSettings } from '../state/settings-hooks';
import { useOnce } from '../util/custom-hooks';

export const usePersistentSettings = () => {
  const [settings, updateSettings] = useAppSettings();
  /**
   * Updates the settings in the app data based on the settings loaded from disk on start.
   */
  const loadSettings = async () => {
    const s = await windowAPI.electronAPI.getAppSettings();
    console.log('Load settings', JSON.stringify(s));
      updateSettings(
        produce(settings, (draft: Draft<any>) => {
          console.dir(settings);
          Object.keys(s).forEach((settingName) => {
            // the persisted settings will be undefined if never set previously; keep default
            if (s[settingName] !== undefined) {
              draft[settingName] = s[settingName];
            }
          });
        })
      );    
  };
  useOnce(loadSettings);

  /**
   * Updates the settings on disk if the settings change in the UI
   */
  const maybeSaveSettings = async () => {
    const oldSettings: Record<string, unknown> =
      await windowAPI.electronAPI.getAppSettings();
    let hasChanged = false;
    const settingsToSave = produce(
      oldSettings,
      (draft: Draft<Record<string, unknown>>) => {
        Object.keys(oldSettings).forEach((settingName) => {
          if (!isAppSettingsKey(settingName)) {
            throw new Error(
              `Cannot set App Setting with key of ${settingName}. Invalid key.`
            );
          }
          if (
            settings[settingName] &&
            draft[settingName] !== settings[settingName]
          ) {
            draft[settingName] = settings[settingName];
            hasChanged = true;
          }
        });
      }
    );
    if (hasChanged) {
      console.log('Save settings', JSON.stringify(settingsToSave));
      await windowAPI.electronAPI.setAppSettings(settingsToSave);
    }
  };
  maybeSaveSettings();
};
