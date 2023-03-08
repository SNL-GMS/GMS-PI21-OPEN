import { WorkflowTypes } from '@gms/common-model';
import { FORTY_FIVE_DAYS_IN_SECONDS, MILLISECONDS_IN_SECOND, sleep } from '@gms/common-util';
import type { OperationalTimePeriodConfigurationQuery } from '@gms/ui-state';
import { analystActions, getStore, workflowActions } from '@gms/ui-state';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { act } from 'react-dom/test-utils';
import { Provider } from 'react-redux';

import {
  WorkflowComponent,
  WorkflowPanelOrNonIdealState
} from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-component';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { glContainer } from './gl-container';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

const store = getStore();

const operationalTimePeriodConfigurationQuery: OperationalTimePeriodConfigurationQuery = cloneDeep(
  useQueryStateResult
);
operationalTimePeriodConfigurationQuery.isSuccess = true;
operationalTimePeriodConfigurationQuery.data = {
  operationalPeriodStart: FORTY_FIVE_DAYS_IN_SECONDS,
  operationalPeriodEnd: 0
};

const processingAnalystConfigurationQuery = cloneDeep(useQueryStateResult);

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useWorkflowQuery: jest.fn(() => ({
      data: {
        stages: [
          {
            name: 'mockStage',
            mode: WorkflowTypes.StageMode.INTERACTIVE,
            activities: [{ stationGroup: { name: 'mockStationGroup' } }]
          },
          {
            name: 'mockStage2',
            mode: WorkflowTypes.StageMode.AUTOMATIC,
            activities: [{ stationGroup: { name: 'mockStationGroup2' } }]
          },
          {
            name: 'mockStage3',
            mode: WorkflowTypes.StageMode.INTERACTIVE,
            activities: [{ stationGroup: { name: 'mockStationGroup3' } }]
          }
        ]
      }
    })),
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
      ...processingAnalystConfigurationQuery,
      data: {
        maximumOpenAnythingDuration: 7200,
        currentIntervalDuration: 1000,
        currentIntervalEndTime: 2000
      }
    })),
    useGetOperationalTimePeriodConfigurationQuery: jest.fn(
      () => operationalTimePeriodConfigurationQuery
    ),
    useOperationalTimePeriodConfiguration: jest.fn(() => {
      return {
        timeRange: {
          startTimeSecs: 10000,
          endTimeSecs: 20000
        },
        operationalTimePeriodConfigurationQuery: {
          data: {
            operationalPeriodStart: 10000,
            operationalPeriodEnd: 20000
          },
          ...operationalTimePeriodConfigurationQuery
        }
      };
    })
  };
});

describe('Workflow Component', () => {
  it('is exported', () => {
    expect(WorkflowComponent).toBeDefined();
    expect(WorkflowPanelOrNonIdealState).toBeDefined();
  });

  it('matches snapshot', async () => {
    store.dispatch(analystActions.setEffectiveNowTime(Date.now() / MILLISECONDS_IN_SECOND));
    store.dispatch(workflowActions.setTimeRange({ startTimeSecs: 10000, endTimeSecs: 20000 }));
    store.dispatch(workflowActions.setOpenIntervalName('test'));
    await act(async () => {
      const waitDurationMs = 200;
      await sleep(waitDurationMs);
    });

    const { container } = render(
      <Provider store={store}>
        <WorkflowComponent glContainer={glContainer} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
