import { uuid } from '@gms/common-util';
import type { SystemMessageDefinitionQuery } from '@gms/ui-state';
import { getStore } from '@gms/ui-state';
import { waitForComponentToPaint } from '@gms/ui-state/__tests__/test-util';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import * as React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../src/ts/components/common-ui/components/base-display';
import { SystemMessage } from '../../../../src/ts/components/common-ui/components/system-message/system-message-component';
import type { SystemMessageProps } from '../../../../src/ts/components/common-ui/components/system-message/types';
import { systemMessageDefinitions } from '../../../__data__/common-ui/system-message-definition-data';
import { useQueryStateResult } from '../../../__data__/test-util-data';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';
let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

const systemMessageDefinitionsQuery: SystemMessageDefinitionQuery = cloneDeep(useQueryStateResult);
systemMessageDefinitionsQuery.data = systemMessageDefinitions;

const baseDisplayContextData = {
  glContainer: { width: 150, height: 150 } as any,
  widthPx: 150,
  heightPx: 150
};

const systemMessageProps: SystemMessageProps = {
  systemMessageDefinitionsQuery,
  addSystemMessages: jest.fn(),
  clearAllSystemMessages: jest.fn(),
  systemMessagesState: {
    lastUpdated: 0,
    latestSystemMessages: [],
    systemMessages: [],
    isSoundEnabled: false
  }
};

describe('System Message Component', () => {
  const store = getStore();

  const systemMessage = Enzyme.shallow(
    <Provider store={store}>
      <BaseDisplayContext.Provider value={baseDisplayContextData}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <SystemMessage {...systemMessageProps} />
      </BaseDisplayContext.Provider>
    </Provider>
  );
  it('should be defined', async () => {
    // we gotta wait for the use state
    await waitForComponentToPaint(systemMessage);
    systemMessage.update();
    expect(SystemMessage).toBeDefined();
    expect(systemMessage).toBeDefined();
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <BaseDisplayContext.Provider value={baseDisplayContextData}>
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <SystemMessage {...systemMessageProps} />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
