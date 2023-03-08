import type { UserProfileTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';
import React from 'react';

import type { SetLayoutArgs } from '../api/user-manager';
import {
  createNewProfileFromSetLayoutInput,
  updateAudibleNotification,
  useGetUserProfileQuery,
  useSetUserProfileMutation
} from '../api/user-manager';

const logger = UILogger.create('GMS_LOG_USER_MANAGER_API', process.env.GMS_LOG_USER_MANAGER_API);

/**
 * Hook that provides a function (mutation) for setting the user layout.
 *
 * @returns a mutation function for setting the layout
 */
export const useSetLayout = (): ((args: SetLayoutArgs) => Promise<void>) => {
  const userProfileQuery = useGetUserProfileQuery();
  const [setUserProfileMutation] = useSetUserProfileMutation();
  const userProfile: UserProfileTypes.UserProfile = userProfileQuery?.data;

  return React.useCallback(
    async (args: SetLayoutArgs) => {
      const updatedProfile: UserProfileTypes.UserProfileCOI = createNewProfileFromSetLayoutInput(
        userProfile,
        args
      );
      await setUserProfileMutation(updatedProfile).catch(error =>
        logger.error('Failed to set layout', error)
      );
    },
    [userProfile, setUserProfileMutation]
  );
};

/**
 * Hook that provides a function (mutation) for setting the audible notifications.
 *
 * @returns a mutation function for setting the audible notifications
 */
export const useSetAudibleNotifications = (): ((
  audibleNotifications: UserProfileTypes.AudibleNotification[]
) => Promise<void>) => {
  const userProfileQuery = useGetUserProfileQuery();
  const [setUserProfileMutation] = useSetUserProfileMutation();
  const userProfile: UserProfileTypes.UserProfile = userProfileQuery?.data;

  return React.useCallback(
    async (audibleNotifications: UserProfileTypes.AudibleNotification[]) => {
      const updatedProfile: UserProfileTypes.UserProfileCOI = {
        ...userProfile,
        audibleNotifications: updateAudibleNotification(
          userProfile.audibleNotifications,
          audibleNotifications
        )
      };
      await setUserProfileMutation(updatedProfile).catch(error =>
        logger.error('Failed to set audible notifications', error)
      );
    },
    [userProfile, setUserProfileMutation]
  );
};

/**
 * Returns a mutation function that sets the theme in the user profile to be the string provided.
 *
 * @throws if the mutation fails
 */
export const useSetThemeInUserProfile = (): ((themeName: string) => Promise<void>) => {
  const userProfileQuery = useGetUserProfileQuery();
  const userProfile: UserProfileTypes.UserProfile = userProfileQuery?.data;
  const [setUserProfileMutation] = useSetUserProfileMutation();
  return async (themeName: string) => {
    const newUserProfile = produce(userProfile, draft => {
      draft.currentTheme = themeName;
    });
    await setUserProfileMutation(newUserProfile).catch(error => {
      logger.error('Failed to set theme in user profile', error);
    });
  };
};
