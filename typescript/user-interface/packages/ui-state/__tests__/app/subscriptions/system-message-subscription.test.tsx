import { SystemMessageTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import * as React from 'react';

import { withReduxProvider } from '../../../src/ts/app/redux-provider';
import type { SystemMessageState } from '../../../src/ts/app/state/system-message/types';
import {
  bufferSystemMessages,
  checkToUpdateSystemMessages,
  initializeSystemMessageBuffering,
  SystemMessageSubscriptionComponent,
  wrapSystemMessageSubscription
} from '../../../src/ts/app/subscription/system-message-subscription';

const MOCK_TIME = 10000000;
global.Date.now = jest.fn(() => MOCK_TIME);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';
let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

const mockUpdateSystemMessagesInRedux = jest.fn();

const systemMessage0 = {
  id: '0',
  category: SystemMessageTypes.SystemMessageCategory.SOH,
  message: 'test',
  severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
  time: 5,
  subCategory: SystemMessageTypes.SystemMessageSubCategory.STATION,
  type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED
};
const systemMessage1 = {
  id: '1',
  category: SystemMessageTypes.SystemMessageCategory.SOH,
  message: 'test',
  severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
  time: 5,
  subCategory: SystemMessageTypes.SystemMessageSubCategory.STATION,
  type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED
};

describe('System Message Component', () => {
  it('Can initialize system buffering', () => {
    expect(initializeSystemMessageBuffering).toBeDefined();
    expect(() =>
      initializeSystemMessageBuffering(mockUpdateSystemMessagesInRedux, 5)
    ).not.toThrowError();
  });

  it('Can check add new system messages and check to update', () => {
    expect(() => checkToUpdateSystemMessages()).not.toThrowError();
    expect(() => bufferSystemMessages([systemMessage0, systemMessage1])).not.toThrowError();
    expect(() => checkToUpdateSystemMessages()).not.toThrowError();
  });

  it('should be defined', () => {
    expect(wrapSystemMessageSubscription).toBeDefined();
  });

  const Wrapper = withReduxProvider(wrapSystemMessageSubscription(React.Fragment, {}));

  const systemMessageDisplay: any = Enzyme.mount(<Wrapper />);

  it('matches snapshot', () => {
    expect(systemMessageDisplay).toMatchSnapshot();
  });

  it('updates store', () => {
    const addSystemMessages = jest.fn();
    const clearExpiredSystemMessages = jest.fn();
    const clearSystemMessages = jest.fn();
    const clearAllSystemMessages = jest.fn();

    const systemMessagesState: SystemMessageState = {
      lastUpdated: 100,
      latestSystemMessages: [],
      systemMessages: [systemMessage0],
      isSoundEnabled: false
    };

    const sys = new SystemMessageSubscriptionComponent({
      addSystemMessages,
      clearAllSystemMessages,
      clearExpiredSystemMessages,
      clearSystemMessages,
      systemMessagesState
    });
    sys.updateSystemMessagesInRedux([], 0);
    sys.updateSystemMessagesInRedux([], 1000);

    expect(clearExpiredSystemMessages).not.toBeCalled();
    expect(clearSystemMessages).not.toBeCalled();
    expect(clearAllSystemMessages).not.toBeCalled();

    sys.updateSystemMessagesInRedux([systemMessage1], 0);

    expect(clearExpiredSystemMessages).not.toBeCalled();
    expect(clearSystemMessages).not.toBeCalled();
    expect(clearAllSystemMessages).not.toBeCalled();

    sys.updateSystemMessagesInRedux([systemMessage0], 0);

    expect(clearExpiredSystemMessages).not.toBeCalled();
    expect(clearSystemMessages).not.toBeCalled();
    expect(clearAllSystemMessages).not.toBeCalled();

    expect(sys.componentWillUnmount()).toBeUndefined();
  });
});
