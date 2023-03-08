import { Logger } from '@gms/common-util';
import { getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { setAppAuthenticationStatus } from '@gms/ui-state/lib/app/state/operations';
import { render } from '@testing-library/react';
import * as React from 'react';

import { App } from '../../../src/ts/app/ui-soh-app/app';
// eslint-disable-next-line import/namespace
import * as Index from '../../../src/ts/app/ui-soh-app/index';
import { useQueryStateResult } from '../../__data__/test-util-data';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

jest.mock('../../../src/ts/components/data-acquisition-ui/components/soh-map', () => {
  return { SOHMap: () => logger.debug('map') };
});

jest.mock('@gms/common-util', () => {
  const actual = jest.requireActual('@gms/common-util');
  return {
    ...actual,
    GMS_UI_MODE: 'soh',
    IS_MODE_SOH: true,
    IS_MODE_IAN: false,
    IS_MODE_LEGACY: false
  };
});

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    SohStatusSubscription: { wrapSohStatusSubscription: (p: any) => p },
    ssamControlApiSlice: {
      ...actual.ssamControlApiSlice,
      endpoints: {
        ...actual.ssamControlApiSlice.endpoints,
        getSohConfiguration: {
          ...actual.ssamControlApiSlice.endpoints.getSohConfiguration,
          select: jest.fn(() =>
            jest.fn(() => ({
              ...useQueryStateResult,
              data: sohConfiguration
            }))
          )
        }
      }
    }
  };
});

describe('SOH App', () => {
  const store = getStore();
  store.dispatch(
    setAppAuthenticationStatus({
      authenticated: true,
      authenticationCheckComplete: true,
      failedToConnect: false,
      userName: 'username'
    }) as any
  );

  it('exists', () => {
    expect(Index).toBeDefined();
  });

  it('matches a snapshot', () => {
    const { container } = render(<App />);
    expect(container).toMatchSnapshot();
  });
});
