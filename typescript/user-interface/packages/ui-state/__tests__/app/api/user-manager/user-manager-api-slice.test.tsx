/* eslint-disable jest/expect-expect */
import { SystemMessageTypes, UserProfileTypes } from '@gms/common-model';
import produce from 'immer';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';

import type { SetLayoutArgs } from '../../../../src/ts/app/api/user-manager';
import {
  convertUserProfileToCOI,
  createNewProfileFromSetLayoutInput,
  updateAudibleNotification,
  useGetUserProfileQuery,
  userManagerApiSlice,
  useSetUserProfileMutation
} from '../../../../src/ts/app/api/user-manager';
import { expectQueryHookToMakeAxiosRequest } from '../query-test-util';

const currentProfile: UserProfileTypes.UserProfile = {
  audibleNotifications: [],
  defaultAnalystLayoutName: 'testLayout',
  defaultSohLayoutName: UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT,
  currentTheme: 'GMS Dark Theme',
  userId: 'fooman',
  workspaceLayouts: [
    {
      layoutConfiguration: 'abc123',
      name: UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT,
      supportedUserInterfaceModes: [UserProfileTypes.UserMode.IAN]
    }
  ]
};

const setLayoutInput: SetLayoutArgs = {
  defaultLayoutName: UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT,
  workspaceLayoutInput: {
    layoutConfiguration: 'xyz123',
    name: 'newLayout',
    supportedUserInterfaceModes: [UserProfileTypes.UserMode.IAN, UserProfileTypes.UserMode.SOH]
  }
};

describe('User Manager API Slice', () => {
  it('provides', () => {
    expect(useGetUserProfileQuery).toBeDefined();
    expect(useSetUserProfileMutation).toBeDefined();
    expect(convertUserProfileToCOI).toBeDefined();
    expect(createNewProfileFromSetLayoutInput).toBeDefined();
    expect(updateAudibleNotification).toBeDefined();
    expect(userManagerApiSlice).toBeDefined();
  });

  it('can convertUserProfileToCOI', () => {
    const clonedCurrentProfile = cloneDeep(currentProfile);

    const result = convertUserProfileToCOI(clonedCurrentProfile);
    expect(JSON.stringify(currentProfile)).toEqual(JSON.stringify(clonedCurrentProfile));
    expect(JSON.stringify(currentProfile.audibleNotifications)).toEqual(
      JSON.stringify(result.audibleNotifications)
    );
    expect(JSON.stringify(currentProfile.defaultAnalystLayoutName)).toEqual(
      JSON.stringify(result.defaultAnalystLayoutName)
    );
    expect(JSON.stringify(currentProfile.defaultSohLayoutName)).toEqual(
      JSON.stringify(result.defaultSohLayoutName)
    );
    expect(JSON.stringify(currentProfile.userId)).toEqual(JSON.stringify(result.userId));
    expect(JSON.stringify(currentProfile.workspaceLayouts)).toEqual(
      JSON.stringify(result.workspaceLayouts)
    );
  });

  it('can createNewProfileFromSetLayoutInput', () => {
    const clonedCurrentProfile = cloneDeep(currentProfile);
    const clonedSetLayoutInput = cloneDeep(setLayoutInput);

    let newProfile = createNewProfileFromSetLayoutInput(clonedCurrentProfile, clonedSetLayoutInput);

    expect(JSON.stringify(currentProfile)).toEqual(JSON.stringify(clonedCurrentProfile));
    expect(JSON.stringify(setLayoutInput)).toEqual(JSON.stringify(clonedSetLayoutInput));
    expect(newProfile.workspaceLayouts).toHaveLength(
      clonedCurrentProfile.workspaceLayouts.length + 1
    );

    newProfile = createNewProfileFromSetLayoutInput(
      clonedCurrentProfile,
      produce(clonedSetLayoutInput, draft => {
        draft.workspaceLayoutInput.name = undefined;
      })
    );
    expect(newProfile.workspaceLayouts).toHaveLength(
      clonedCurrentProfile.workspaceLayouts.length + 1
    );

    newProfile = createNewProfileFromSetLayoutInput(
      clonedCurrentProfile,
      produce(clonedSetLayoutInput, draft => {
        draft.saveAsDefaultLayoutOfType = UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT;
      })
    );
    expect(newProfile.workspaceLayouts).toHaveLength(
      clonedCurrentProfile.workspaceLayouts.length + 1
    );

    newProfile = createNewProfileFromSetLayoutInput(
      clonedCurrentProfile,
      produce(clonedSetLayoutInput, draft => {
        draft.saveAsDefaultLayoutOfType = UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT;
        draft.workspaceLayoutInput.name = undefined;
      })
    );
    expect(newProfile.workspaceLayouts).toHaveLength(
      clonedCurrentProfile.workspaceLayouts.length + 1
    );

    newProfile = createNewProfileFromSetLayoutInput(
      clonedCurrentProfile,
      produce(clonedSetLayoutInput, draft => {
        draft.saveAsDefaultLayoutOfType = UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT;
      })
    );
    expect(newProfile.workspaceLayouts).toHaveLength(
      clonedCurrentProfile.workspaceLayouts.length + 1
    );

    newProfile = createNewProfileFromSetLayoutInput(
      clonedCurrentProfile,
      produce(clonedSetLayoutInput, draft => {
        draft.saveAsDefaultLayoutOfType = UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT;
        draft.workspaceLayoutInput.name = undefined;
      })
    );
    expect(newProfile.workspaceLayouts).toHaveLength(
      clonedCurrentProfile.workspaceLayouts.length + 1
    );

    newProfile = createNewProfileFromSetLayoutInput(
      clonedCurrentProfile,
      produce(clonedSetLayoutInput, draft => {
        draft.workspaceLayoutInput.name = clonedCurrentProfile.workspaceLayouts[0].name;
      })
    );
    expect(newProfile.workspaceLayouts).toHaveLength(currentProfile.workspaceLayouts.length);
  });

  it('can update audible notifications', () => {
    const audibleNotification1: UserProfileTypes.AudibleNotification = {
      fileName: 'file1',
      notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
      defaultLayoutName: UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT
    };

    const audibleNotification2: UserProfileTypes.AudibleNotification = {
      fileName: 'file2',
      notificationType: SystemMessageTypes.SystemMessageType.STATION_NEEDS_ATTENTION,
      defaultLayoutName: UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT
    };

    const audibleNotification3: UserProfileTypes.AudibleNotification = {
      fileName: 'file3',
      notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
      defaultLayoutName: UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT
    };

    const audibleNotification4: UserProfileTypes.AudibleNotification = {
      fileName: undefined,
      notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
      defaultLayoutName: UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT
    };

    expect(
      updateAudibleNotification(
        [audibleNotification1, audibleNotification2],
        [audibleNotification3]
      )
    ).toEqual([audibleNotification3, audibleNotification2]);

    expect(updateAudibleNotification([audibleNotification1], [audibleNotification2])).toEqual([
      audibleNotification1,
      audibleNotification2
    ]);

    expect(
      updateAudibleNotification(
        [audibleNotification1, audibleNotification2],
        [audibleNotification4]
      )
    ).toEqual([audibleNotification2]);

    expect(
      updateAudibleNotification(undefined, [audibleNotification1, audibleNotification2])
    ).toEqual([audibleNotification1, audibleNotification2]);

    expect(
      updateAudibleNotification([audibleNotification1, audibleNotification2], undefined)
    ).toEqual([audibleNotification1, audibleNotification2]);
  });

  it('successfully updates an empty list of audible notifications when given a new notification', () => {
    const old: UserProfileTypes.AudibleNotification[] = [];
    const add: UserProfileTypes.AudibleNotification[] = [
      {
        fileName: 'test1.file',
        notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
      },
      {
        fileName: 'test2.file',
        notificationType: SystemMessageTypes.SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED
      }
    ];
    const notifications = updateAudibleNotification(old, add);
    expect(notifications).toEqual(add);
  });
  it('successfully updates an filled list of audible notifications when given a new notification', () => {
    const old: UserProfileTypes.AudibleNotification[] = [
      {
        fileName: 'test1.file',
        notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
      },
      {
        fileName: 'test2.file',
        notificationType: SystemMessageTypes.SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED
      }
    ];
    const add: UserProfileTypes.AudibleNotification[] = [
      {
        fileName: 'test3.file',
        notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
      }
    ];
    const notifications = updateAudibleNotification(old, add);
    expect(notifications.find(n => n.fileName === 'test1.file')).toBeFalsy();
    expect(notifications.find(n => n.fileName === 'test2.file')).toBeTruthy();
    expect(notifications.find(n => n.fileName === 'test3.file')).toBeTruthy();
  });
  it('successfully removes an audible notification from a list', () => {
    const old: UserProfileTypes.AudibleNotification[] = [
      {
        fileName: 'test1.file',
        notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
      },
      {
        fileName: 'test2.file',
        notificationType: SystemMessageTypes.SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED
      }
    ];
    const remove: UserProfileTypes.AudibleNotification[] = [
      {
        fileName: '',
        notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
      }
    ];
    const notifications = updateAudibleNotification(old, remove);
    expect(notifications.find(n => n.fileName === 'test1.file')).toBeFalsy();
    expect(notifications.find(n => n.fileName === 'test2.file')).toBeTruthy();
  });

  it('hook queries for user profile', async () => {
    await expectQueryHookToMakeAxiosRequest(useGetUserProfileQuery);
  });

  it('set user profile', async () => {
    const useMutation = () => {
      const [mutation] = useSetUserProfileMutation();

      const result = React.useRef<any>();
      React.useEffect(() => {
        result.current = mutation(undefined);
      }, [mutation]);

      return <div>{result.current}</div>;
    };
    await expectQueryHookToMakeAxiosRequest(useMutation);
  });
});
