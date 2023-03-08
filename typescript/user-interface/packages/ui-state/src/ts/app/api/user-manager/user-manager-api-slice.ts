import { UserProfileTypes } from '@gms/common-model';
import { IS_MODE_SOH } from '@gms/common-util';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';
import type { Draft } from 'immer';
import produce from 'immer';
import cloneDeep from 'lodash/cloneDeep';

import type { UseQueryStateResult } from '../../query';
import { config } from './endpoint-configuration';

/**
 * Set layout args:
 * The arguments needed to update a layout.
 * Takes a layout configuration input and optional parameter which defines
 * if we should save as a default, and if so, what default layout mode we should overwrite.
 */
export interface SetLayoutArgs {
  /**
   * The layout configuration and metadata like name, modes supported.
   */
  workspaceLayoutInput: UserProfileTypes.UserLayout;

  /**
   * The layout name, used for updating the query cache.
   */
  defaultLayoutName?: UserProfileTypes.DefaultLayoutNames;

  /**
   * The default layout type (are we changing the SOH or IAN default),
   * or undefined if we are not saving as a default.
   */
  saveAsDefaultLayoutOfType?: UserProfileTypes.DefaultLayoutNames;
}

/**
 * Converts a UI User Profile to its COI version.
 *
 * @param profile the profile to convert
 * @returns the converted profile
 */
export const convertUserProfileToCOI = (
  profile: UserProfileTypes.UserProfile
): UserProfileTypes.UserProfileCOI => {
  // we use cloneDeep to ensure that changes to the new layout don't mutate the old one. Without it, the
  // workspaces are the same, and changes to a layout could change the original user profile.
  return produce(
    {
      audibleNotifications: undefined,
      defaultAnalystLayoutName: undefined,
      defaultSohLayoutName: undefined,
      currentTheme: undefined,
      userId: undefined,
      workspaceLayouts: undefined
    },
    (draft: Draft<UserProfileTypes.UserProfileCOI>) => {
      draft.audibleNotifications = cloneDeep(profile.audibleNotifications);
      draft.defaultAnalystLayoutName = cloneDeep(profile.defaultAnalystLayoutName);
      draft.currentTheme = cloneDeep(profile.currentTheme);
      draft.defaultSohLayoutName = cloneDeep(profile.defaultSohLayoutName);
      draft.userId = cloneDeep(profile.userId);
      draft.workspaceLayouts = cloneDeep(profile.workspaceLayouts);
    }
  );
};

/**
 * Creates a new user profile that includes the new/updated layout
 *
 * @param currentProfile the current user profile
 * @param setLayoutInput the user input
 */
export const createNewProfileFromSetLayoutInput = (
  currentProfile: UserProfileTypes.UserProfile,
  setLayoutInput: SetLayoutArgs
): UserProfileTypes.UserProfileCOI => {
  const layouts: UserProfileTypes.UserLayout[] = produce(currentProfile.workspaceLayouts, draft => {
    const index = draft.findIndex(wl => wl.name === setLayoutInput.workspaceLayoutInput.name);
    if (index !== -1) draft.splice(index, 1);
    draft.push(setLayoutInput.workspaceLayoutInput);
  });

  return produce(convertUserProfileToCOI(currentProfile), draft => {
    draft.workspaceLayouts = layouts;
    if (setLayoutInput.saveAsDefaultLayoutOfType !== undefined) {
      if (
        setLayoutInput.saveAsDefaultLayoutOfType ===
        UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT
      ) {
        draft.defaultAnalystLayoutName = setLayoutInput.workspaceLayoutInput.name;
      }

      if (
        setLayoutInput.saveAsDefaultLayoutOfType === UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT
      ) {
        draft.defaultSohLayoutName = setLayoutInput.workspaceLayoutInput.name;
      }
    }
  });
};

/**
 * Create, update, and delete notifications from a list.
 * pass an empty string for the filename to delete a notification
 *
 * @param audibleNotifications the original list of notifications
 * @param updatedNotifications the notifications to update. If the same
 * system message definition is found in the @param audibleNotifications, then this
 * will overwrite it.
 */
export const updateAudibleNotification = (
  audibleNotifications: UserProfileTypes.AudibleNotification[],
  updatedNotifications: UserProfileTypes.AudibleNotification[]
): UserProfileTypes.AudibleNotification[] => {
  const newNotificationMap = audibleNotifications ? cloneDeep(audibleNotifications) : [];
  updatedNotifications?.forEach(notification => {
    const indexToRemove = newNotificationMap.findIndex(
      n => n.notificationType === notification.notificationType
    );
    if (indexToRemove === -1) {
      newNotificationMap.push(notification);
    } else if (notification.fileName) {
      newNotificationMap.splice(indexToRemove, 1, notification);
    } else {
      newNotificationMap.splice(indexToRemove, 1);
    }
  });
  return newNotificationMap;
};

/**
 * The user manager api reducer slice.
 */
export const userManagerApiSlice = createApi({
  reducerPath: 'userManagerApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.user.baseUrl
  }),
  tagTypes: ['UserProfile'],
  endpoints(build) {
    return {
      /**
       * defines the user profile query
       */
      getUserProfile: build.query<UserProfileTypes.UserProfile, void>({
        query: () => ({ requestConfig: config.user.services.getUserProfile.requestConfig }),
        transformResponse: (
          responseData: UserProfileTypes.UserProfileCOI
        ): UserProfileTypes.UserProfile => {
          return {
            ...responseData,
            defaultLayoutName: IS_MODE_SOH
              ? responseData.defaultSohLayoutName
              : responseData.defaultAnalystLayoutName
          };
        },
        providesTags: ['UserProfile']
      }),

      /**
       * defines the updateActivityIntervalStatus mutation
       */
      setUserProfile: build.mutation<void, UserProfileTypes.UserProfileCOI>({
        query: (data: UserProfileTypes.UserProfileCOI) => ({
          requestConfig: {
            ...config.user.services.setUserProfile.requestConfig,
            data
          }
        }),
        // invalidate the user profile tag to force a re-query
        invalidatesTags: () => ['UserProfile']
      })
    };
  }
});

export const { useGetUserProfileQuery, useSetUserProfileMutation } = userManagerApiSlice;

export type UserProfileQuery = UseQueryStateResult<UserProfileTypes.UserProfile>;

export type SetUserProfileMutation = ReturnType<typeof useSetUserProfileMutation>;

const setUserProfileMutation: SetUserProfileMutation = undefined;

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
export type SetUserProfileMutationFunc = typeof setUserProfileMutation[0];
