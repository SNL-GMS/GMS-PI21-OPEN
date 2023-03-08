import type { CommonTypes } from '@gms/common-model';
import { getStore, workflowSlice } from '@gms/ui-state';
import { processingAnalystConfiguration } from '@gms/ui-state/__tests__/__data__/processing-analyst-configuration';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import { Events } from '../../../../../src/ts/components/analyst-ui/components/events';
import { EventsComponent } from '../../../../../src/ts/components/analyst-ui/components/events/events-component';
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

const operationalTimeRange: CommonTypes.TimeRange = {
  startTimeSecs: 0,
  endTimeSecs: 2000
};
const operationalTimePeriodConfigurationQuery = cloneDeep(useQueryStateResult);
operationalTimePeriodConfigurationQuery.data = operationalTimeRange;

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => processingAnalystConfigurationQuery),
    useGetOperationalTimePeriodConfigurationQuery: jest.fn(
      () => operationalTimePeriodConfigurationQuery
    )
  };
});

describe('Event component', () => {
  const store = getStore();

  it('is exported', () => {
    expect(Events).toBeDefined();
    expect(EventsComponent).toBeDefined();
  });

  it('matches snapshot', () => {
    store.dispatch(workflowSlice.actions.setTimeRange(timeRange));
    const { container } = render(
      <Provider store={store}>
        <BaseDisplay glContainer={glContainer}>
          <Events />
        </BaseDisplay>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
