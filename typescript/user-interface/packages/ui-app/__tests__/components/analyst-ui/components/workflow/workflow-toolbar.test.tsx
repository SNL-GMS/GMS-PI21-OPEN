import { WorkflowTypes } from '@gms/common-model';
import {
  FORTY_FIVE_DAYS_IN_SECONDS,
  MILLISECONDS_IN_SECOND,
  SECONDS_IN_HOUR
} from '@gms/common-util';
import type { OperationalTimePeriodConfigurationQuery } from '@gms/ui-state';
import { getStore, setOpenInterval } from '@gms/ui-state';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import { WorkflowToolbar } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-toolbar';
import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { glContainer } from './gl-container';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

const TEN_SECONDS_MS = 10000;

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const MOCK_TIME = 1606818240000;

const operationalTimePeriodConfigurationQuery: OperationalTimePeriodConfigurationQuery = cloneDeep(
  useQueryStateResult
);

operationalTimePeriodConfigurationQuery.data = {
  operationalPeriodStart: FORTY_FIVE_DAYS_IN_SECONDS,
  operationalPeriodEnd: 0
};

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
      data: {
        leadBufferDuration: 900,
        lagBufferDuration: 900,
        maximumOpenAnythingDuration: 7200,
        workflow: {
          panSingleArrow: 86400,
          panDoubleArrow: 604800
        }
      }
    })),
    useGetOperationalTimePeriodConfigurationQuery: jest.fn(
      () => operationalTimePeriodConfigurationQuery
    ),
    useGetProcessingStationGroupNamesConfigurationQuery: jest.fn(() => ({
      data: { stationGroupNames: ['mockStationGroup', 'mockStationGroup'] }
    }))
  };
});

describe('Workflow Toolbar', () => {
  const onPan = jest.fn();

  const storeDefault = getStore();

  const store = getStore();
  store.dispatch(
    setOpenInterval(
      {
        startTimeSecs: MOCK_TIME / MILLISECONDS_IN_SECOND - SECONDS_IN_HOUR,
        endTimeSecs: MOCK_TIME / MILLISECONDS_IN_SECOND
      },
      {
        name: 'Station Group',
        effectiveAt: 0,
        description: ''
      },
      'Al1',
      ['Event Review'],
      WorkflowTypes.AnalysisMode.SCAN
    ) as any
  );

  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(MOCK_TIME);
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  it('is exported', () => {
    expect(WorkflowToolbar).toBeDefined();
  });

  it('mocks date correctly', () => {
    expect(new Date().getTimezoneOffset()).toEqual(0);
    expect(Date.now()).toEqual(MOCK_TIME);
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={storeDefault}>
        <BaseDisplay glContainer={glContainer}>
          <WorkflowToolbar onPan={onPan} />
        </BaseDisplay>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('matches default value snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <BaseDisplay glContainer={glContainer}>
          <WorkflowToolbar onPan={onPan} />
        </BaseDisplay>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it(
    'handle buttons clicks',
    () => {
      const component = Enzyme.mount(
        <Provider store={storeDefault}>
          <BaseDisplay glContainer={glContainer}>
            <WorkflowToolbar onPan={onPan} />
          </BaseDisplay>
        </Provider>
      );

      jest.runAllTimers();

      const buttons = component.find('button');

      const doubleLeftArrowItem = buttons.find({ 'data-cy': 'workflow-doubleLeftArrowItem' });
      expect(doubleLeftArrowItem.props().title).toMatchInlineSnapshot(
        `"Pan the workflow to the left by 7 days (Shift + ←)"`
      );
      doubleLeftArrowItem.simulate('click');
      expect(onPan).toHaveBeenCalledTimes(1);

      const singleLeftArrowItem = buttons.find({ 'data-cy': 'workflow-singleLeftArrowItem' });
      expect(singleLeftArrowItem.props().title).toMatchInlineSnapshot(
        `"Pan the workflow to the left by 1 day (←)"`
      );
      singleLeftArrowItem.simulate('click');
      expect(onPan).toHaveBeenCalledTimes(2);

      const singleRightArrowItem = buttons.find({ 'data-cy': 'workflow-singleRightArrowItem' });
      expect(singleRightArrowItem.props().title).toMatchInlineSnapshot(
        `"Pan the workflow to the right by 1 day (→)"`
      );
      singleRightArrowItem.simulate('click');
      expect(onPan).toHaveBeenCalledTimes(3);

      const doubleRightArrowItem = buttons.find({ 'data-cy': 'workflow-doubleRightArrowItem' });
      expect(doubleRightArrowItem.props().title).toMatchInlineSnapshot(
        `"Pan the workflow to the right by 7 days (Shift + →)"`
      );
      doubleRightArrowItem.simulate('click');
      expect(onPan).toHaveBeenCalledTimes(4);

      const openAnythingItem = buttons.find({ 'data-cy': 'workflow-openAnythingButtonItem' });
      expect(openAnythingItem.props().title).toMatchInlineSnapshot(`"Open anything"`);

      component.update();
    },
    TEN_SECONDS_MS
  );
});
