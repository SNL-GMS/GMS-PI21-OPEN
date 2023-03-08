import { SystemMessageTypes, UserProfileTypes } from '@gms/common-model';
import axios from 'axios';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';

import type { SetLayoutArgs } from '../../../src/ts/app/api/user-manager/user-manager-api-slice';
import {
  useSetAudibleNotifications,
  useSetLayout,
  useSetThemeInUserProfile
} from '../../../src/ts/app/hooks/user-manager-hooks';
import { getStore } from '../../../src/ts/app/store';

const currentProfile: UserProfileTypes.UserProfile = {
  audibleNotifications: [],
  defaultAnalystLayoutName: 'testLayout',
  defaultSohLayoutName: UserProfileTypes.DefaultLayoutNames.SOH_LAYOUT,
  currentTheme: 'GMS Dark Mode',
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

const audibleNotification: UserProfileTypes.AudibleNotification = {
  fileName: 'test',
  notificationType: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
  defaultLayoutName: UserProfileTypes.DefaultLayoutNames.ANALYST_LAYOUT
};

jest.mock('../../../src/ts/app/api/user-manager/user-manager-api-slice', () => {
  const actual = jest.requireActual('../../../src/ts/app/api/user-manager/user-manager-api-slice');
  return {
    ...actual,
    useGetUserProfileQuery: jest.fn(() => ({
      data: currentProfile
    }))
  };
});

jest.mock('axios');
describe('user manager hooks', () => {
  beforeEach(() => {
    (axios.get as jest.Mock).mockResolvedValue({
      status: 200,
      statusText: 'OK',
      data: {},
      headers: [],
      config: {}
    });
  });
  it('exists', () => {
    expect(useSetLayout).toBeDefined();
    expect(useSetAudibleNotifications).toBeDefined();
    expect(useSetThemeInUserProfile).toBeDefined();
  });

  it('can set layout', async () => {
    const store = getStore();

    let setLayout: (args: SetLayoutArgs) => Promise<void>;

    function Component() {
      setLayout = useSetLayout();
      React.useEffect(() => {
        setLayout(setLayoutInput).catch(e => {
          throw new Error(e);
        });
      }, []);
      return <>{JSON.stringify('set layout')}</>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    expect(async () => {
      await setLayout(setLayoutInput);
    }).not.toThrow();

    await setLayout(setLayoutInput);
  });

  it('can set audible notifications', async () => {
    const store = getStore();

    let setAudibleNotifications: (
      audibleNotifications: UserProfileTypes.AudibleNotification[]
    ) => Promise<void>;

    function Component() {
      setAudibleNotifications = useSetAudibleNotifications();
      React.useEffect(() => {
        setAudibleNotifications([audibleNotification]).catch(e => {
          throw new Error(e);
        });
      }, []);
      return <>{JSON.stringify('set audible notifications')}</>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    expect(async () => {
      await setAudibleNotifications([audibleNotification]);
    }).not.toThrow();

    await setAudibleNotifications([audibleNotification]);
  });

  it('can set user theme', async () => {
    const store = getStore();

    let setCurrentTheme: (currentTheme: string) => Promise<void>;

    function Component() {
      setCurrentTheme = useSetThemeInUserProfile();
      React.useEffect(() => {
        setCurrentTheme('GMS Dark Theme').catch(e => {
          throw new Error(e);
        });
      }, []);
      return <>{JSON.stringify('set current theme')}</>;
    }

    expect(
      create(
        <Provider store={store}>
          <Component />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    expect(async () => {
      await setCurrentTheme('GMS Dark Theme');
    }).not.toThrow();

    await setCurrentTheme('GMS Dark Theme');
  });
});
