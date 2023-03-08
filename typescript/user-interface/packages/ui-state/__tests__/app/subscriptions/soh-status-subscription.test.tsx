// eslint-disable-next-line workspaces/require-dependency, import/no-extraneous-dependencies
import { stationAndStationGroupSohStatus } from '@gms/ui-app/__tests__/__data__/data-acquisition-ui/soh-overview-data';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';

import { withReduxProvider } from '../../../src/ts/app/redux-provider';
import {
  addLatestSohMessages,
  addSohForStation,
  checkToSendSohStatusChanges,
  getLatestStationAndGroupSoh,
  initializeSohStatusBuffering
} from '../../../src/ts/app/subscription/soh-status-buffer';
import type { SohStatusSubscriptionProps } from '../../../src/ts/app/subscription/soh-status-subscription-wrapper';
import {
  SohStatusSubscriptionComponent,
  wrapSohStatusSubscription
} from '../../../src/ts/app/subscription/soh-status-subscription-wrapper';
import { sohConfiguration } from '../../__data__/soh-configuration-query-data';
import { sohStatus } from '../../__data__/soh-status-data';
import { useQueryStateResult } from '../../__data__/test-util-data';

const MOCK_TIME = 10000000;
global.Date.now = jest.fn(() => MOCK_TIME);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

const sohConfigurationQuery: any = useQueryStateResult;
sohConfigurationQuery.data = sohConfiguration;

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    // useGetSohConfigurationQuery: jest.fn(() => sohConfigurationQuery),
    dataAcquisitionSlice: {
      ...actual.dataAcquisitionSlice,
      reducers: {
        ...actual.dataAcquisitionSlice.endpoints,
        setSohStatus: jest.fn()
      }
    },
    ssamControlApiSlice: {
      ...actual.ssamControlApiSlice,
      endpoints: {
        ...actual.ssamControlApiSlice.endpoints,
        getSohConfiguration: {
          ...actual.ssamControlApiSlice.endpoints.getSohConfiguration,
          select: jest.fn(() => jest.fn(() => sohConfigurationQuery)),
          initiate: jest.fn(() => jest.fn(() => sohConfigurationQuery))
        }
      }
    }
  };
});

jest.mock('../../../src/ts/app/api/system-event-gateway/system-event-gateway-api-slice', () => {
  const actual = jest.requireActual(
    '../../../src/ts/app/api/system-event-gateway/system-event-gateway-api-slice'
  );
  return {
    ...actual,
    useClientLogMutation: () => [jest.fn()]
  };
});

describe('soh-status-subscription-wrapper', () => {
  const reduxProps: Partial<SohStatusSubscriptionProps> = {
    sohConfigurationQuery,
    selectedStationIds: [],
    sohStatus
  };
  const sys = new SohStatusSubscriptionComponent(reduxProps as SohStatusSubscriptionProps);
  it('should have defined members', () => {
    expect(wrapSohStatusSubscription).toBeDefined();
  });

  it('updates store', () => {
    sys.updateSohMessagesInRedux(false, [stationAndStationGroupSohStatus]);
    expect(sys.componentWillUnmount()).toBeUndefined();
  });

  it('set stale timer', () => {
    const anySys: any = sys;
    expect(anySys.setStaleTimer()).toBeUndefined();
  });

  it('mount soh subscription', () => {
    // Haven't figured out how to snapshot since the 'requestId' changes
    // each time run
    const Wrapper = withReduxProvider(wrapSohStatusSubscription(React.Fragment, {}));
    const sohMessages: any = Enzyme.mount(<Wrapper />);
    expect(sohMessages).toBeDefined();
  });
});

describe('soh-status-buffer tests', () => {
  it('check initial state with no data', () => {
    expect(checkToSendSohStatusChanges).toBeDefined();
    expect(() => checkToSendSohStatusChanges()).not.toThrowError();
    expect(getLatestStationAndGroupSoh).toBeDefined();
    const sohStatusResult = getLatestStationAndGroupSoh();
    expect(sohStatusResult.stationGroups).toHaveLength(0);
    expect(sohStatusResult.stationSoh).toHaveLength(0);
  });

  it('check state with data added', () => {
    // will not add undefined
    expect(() => addSohForStation(undefined)).not.toThrowError();
    expect(() =>
      addSohForStation(sohStatus.stationAndStationGroupSoh.stationSoh[0])
    ).not.toThrowError();
    // ignores duplicates
    expect(() =>
      addSohForStation(sohStatus.stationAndStationGroupSoh.stationSoh[0])
    ).not.toThrowError();
    expect(getLatestStationAndGroupSoh()).toMatchSnapshot();
  });

  it('checkToSendSohStatusChanges', () => {
    expect(checkToSendSohStatusChanges).toBeDefined();
    expect(() => checkToSendSohStatusChanges()).not.toThrowError();
  });

  it('initializeSohStatusBuffering', () => {
    const updateSohMessageFn = jest.fn();
    expect(initializeSohStatusBuffering).toBeDefined();
    expect(() => initializeSohStatusBuffering(updateSohMessageFn)).not.toThrowError();
  });

  it('consumeUiStationSohKafkaMessages', () => {
    expect(addLatestSohMessages).toBeDefined();
    expect(() => addLatestSohMessages([sohStatus.stationAndStationGroupSoh])).not.toThrowError();

    // now try as update response
    const updateResponse = cloneDeep(sohStatus.stationAndStationGroupSoh);
    updateResponse.isUpdateResponse = true;
    expect(() => addLatestSohMessages([updateResponse])).not.toThrowError();
  });
});
