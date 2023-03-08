import type { UserProfileTypes } from '@gms/common-model';
import { AVAILABLE_SOUND_FILES } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import Axios from 'axios';
import includes from 'lodash/includes';
import sortBy from 'lodash/sortBy';
import uniq from 'lodash/uniq';
import { toast } from 'react-toastify';

import { userPreferences } from '~components/common-ui/config/user-preferences';

import type { SoundConfigurationRow } from './types';
import { ALL_CATEGORIES, ALL_SEVERITIES, ALL_SUBCATEGORIES } from './types';

const logger = UILogger.create('GMS_LOG_SYSTEM_MESSAGE', process.env.GMS_LOG_SYSTEM_MESSAGE);

const BASE_SOUNDS_PATH = userPreferences.baseSoundsPath;

/**
 * Determine which sound files are missing, if any.
 *
 * @returns the missing sound files; empty array if no files are missing
 */
export const getMissingSounds = async (filenames: string[]): Promise<string[]> =>
  Promise.all<string>(
    sortBy<string>(uniq<string>(filenames)).map(async soundFileName =>
      Axios.get(`${BASE_SOUNDS_PATH}${String(soundFileName)}`)
        .then(() => undefined)
        .catch(() => soundFileName)
    )
  ).then(response => response.filter(r => r !== undefined));

/**
 * Validate the available sound files. Toast an error message for
 * each sound file that is not loaded properly (not found).
 */
export const validateAvailableSounds = (): void => {
  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  getMissingSounds(AVAILABLE_SOUND_FILES)
    .then(missingSoundFiles => {
      missingSoundFiles.forEach(soundFileName => {
        toast.error(userPreferences.soundFileNotFound(soundFileName));
        logger.error(userPreferences.soundFileNotFound(`${BASE_SOUNDS_PATH}${soundFileName}`));
      });
    })
    .catch(error => logger.error(error));
};

/**
 * Validate the configured audible notifications against the available sounds.
 * Toast an error message for each sound file that is configured for an audible notifications but not available.
 */
export const validateConfiguredAudibleNotifications = (
  audibleNotifications: UserProfileTypes.AudibleNotification[]
): void => {
  const configuredSoundFiles = sortBy<string>(
    uniq<string>(audibleNotifications?.map(notification => notification.fileName))
  );

  getMissingSounds(configuredSoundFiles)
    .then(missingSoundFiles => {
      const soundFilesInError = configuredSoundFiles.filter(sound =>
        includes(missingSoundFiles, sound)
      );
      soundFilesInError.forEach(soundFileName => {
        toast.error(userPreferences.configuredAudibleNotificationFileNotFound(soundFileName));
        logger.error(
          userPreferences.configuredAudibleNotificationFileNotFound(
            `${BASE_SOUNDS_PATH}${soundFileName}`
          )
        );
      });
    })
    .catch(error => logger.error(error));
};

/**
 * @returns the filenames of the sound files that are in the /sounds directory
 * Relative path only. For example /sounds/example.mp3 would be returned as "example.mp3"
 */
export const getAvailableSounds = (): {
  [key: string]: string;
} => {
  const soundFileNames: string[] = AVAILABLE_SOUND_FILES;
  const obj: { [key: string]: string } = {};
  let soundsMap = soundFileNames.reduce((prev, curr) => {
    // eslint-disable-next-line no-param-reassign
    prev[curr] = curr;
    return prev;
  }, obj);
  soundsMap = { default: 'None', ...soundsMap };
  return soundsMap;
};

/**
 * Filter rows based on severity, category, and subcategory
 *
 * @param selectedSeverity - selected severity
 * @param rows - The rows to be filtered
 * @param selectedCategory - Selected category
 * @param selectedSubcategory - Selected subcategory
 *
 * @returns - Filtered sound configuration rows
 */
export const filterRowsByType = (
  selectedSeverity: string,
  rows: SoundConfigurationRow[],
  selectedCategory: string,
  selectedSubcategory: string
): SoundConfigurationRow[] => {
  let newRows = [...rows];

  if (selectedSeverity !== ALL_SEVERITIES) {
    newRows = newRows.filter(row => row.severity === selectedSeverity);
  }
  if (selectedCategory !== ALL_CATEGORIES) {
    newRows = newRows.filter(row => row.category === selectedCategory);
  }
  if (selectedSubcategory !== ALL_SUBCATEGORIES) {
    newRows = newRows.filter(row => row.subcategory === selectedSubcategory);
  }
  return newRows;
};
