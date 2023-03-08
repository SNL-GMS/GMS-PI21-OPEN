import { getStore } from '@gms/ui-state';
import { processingAnalystConfiguration } from '@gms/ui-state/__tests__/__data__/processing-analyst-configuration';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import { EventsPanel } from '../../../../../src/ts/components/analyst-ui/components/events/events-panel';
import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { glContainer } from '../workflow/gl-container';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});

const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const timeRange = { startTimeSecs: 1609500000, endTimeSecs: 1609506000 };

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);
processingAnalystConfigurationQuery.data = processingAnalystConfiguration;

describe('Event Panel', () => {
  const storeDefault = getStore();

  it('is exported', () => {
    expect(EventsPanel).toBeDefined();
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={storeDefault}>
        <BaseDisplay glContainer={glContainer}>
          <EventsPanel
            timeRange={timeRange}
            stageName="stageName"
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
          />
        </BaseDisplay>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
