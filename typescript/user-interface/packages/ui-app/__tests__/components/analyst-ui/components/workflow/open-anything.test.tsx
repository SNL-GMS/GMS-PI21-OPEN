/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable jest/no-commented-out-tests */

import { WorkflowTypes } from '@gms/common-model';
import { FORTY_FIVE_DAYS_IN_SECONDS } from '@gms/common-util';
import type {
  OperationalTimePeriodConfigurationQuery,
  ProcessingAnalystConfigurationQuery
} from '@gms/ui-state';
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import { OpenAnythingDialog } from '../../../../../src/ts/components/analyst-ui/components/workflow/open-anything-dialog';
import { WorkflowContext } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-context';
import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { glContainer } from './gl-container';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const MOCK_TIME = 1609506000000;

const processingAnalystConfigurationQuery: ProcessingAnalystConfigurationQuery = cloneDeep(
  useQueryStateResult
);

const operationalTimePeriodConfigurationQuery: OperationalTimePeriodConfigurationQuery = cloneDeep(
  useQueryStateResult
);

operationalTimePeriodConfigurationQuery.data = {
  operationalPeriodStart: FORTY_FIVE_DAYS_IN_SECONDS,
  operationalPeriodEnd: 0
};

jest.mock('@gms/ui-state', () => ({
  // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
  ...(jest.requireActual('@gms/ui-state') as any),
  useWorkflowQuery: jest.fn(() => ({
    ...cloneDeep(useQueryStateResult),
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
      maximumOpenAnythingDuration: 100
    }
  })),
  useGetOperationalTimePeriodConfigurationQuery: jest.fn(
    () => operationalTimePeriodConfigurationQuery
  ),
  useGetProcessingStationGroupNamesConfigurationQuery: jest.fn(() => ({
    data: { stationGroupNames: ['mockStationGroup'] }
  })),
  useGetStationGroupsByNamesQuery: jest.fn(() => ({
    data: [{ name: 'mockStationGroup' }, { name: 'mockStationGroup' }]
  }))
}));

describe('Open Anything Dialog', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(MOCK_TIME);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('is exported', () => {
    expect(OpenAnythingDialog).toBeDefined();
  });

  it('has the correct mocked time', () => {
    expect(new Date().getTimezoneOffset()).toEqual(0);
    expect(Date.now()).toEqual(MOCK_TIME);
  });

  it('matches snapshot', () => {
    let resultsRenderer = render(
      <OpenAnythingDialog isVisible={false} onCancel={jest.fn()} onOpen={jest.fn()} />
    );
    expect(resultsRenderer.container).toMatchSnapshot();

    resultsRenderer = render(
      <OpenAnythingDialog isVisible onCancel={jest.fn()} onOpen={jest.fn()} />
    );
    expect(resultsRenderer.container).toMatchSnapshot();
  });
  // TODO: disabled pending jest upgrade

  const store = getStore();

  it('default values are called', () => {
    const openAnythingConfirmationPrompt = jest.fn();
    const onOpen = jest.fn();
    const component = Enzyme.mount(
      <Provider store={store}>
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt
          }}
        >
          <BaseDisplay glContainer={glContainer}>
            <OpenAnythingDialog isVisible onCancel={jest.fn()} onOpen={onOpen} />
          </BaseDisplay>
        </WorkflowContext.Provider>
      </Provider>
    );

    jest.runAllTimers();

    const openButton = component.find('button[data-cy="date-picker-apply-button"]').first();
    openButton.simulate('click');

    expect(onOpen).toBeCalled();
    expect(openAnythingConfirmationPrompt).toBeCalledWith({
      openIntervalName: 'mockStage',
      stationGroup: {
        name: 'mockStationGroup'
      },
      timeRange: {
        endTimeSecs: 1609506000,
        startTimeSecs: 1609505900
      }
    });
  });

  // TODO Unskip tests
  it.skip('values change', () => {
    const openAnythingConfirmationPrompt = jest.fn();
    const onOpen = jest.fn();
    const component = Enzyme.mount(
      <Provider store={store}>
        <WorkflowContext.Provider
          value={{
            staleStartTime: 1,
            allActivitiesOpenForSelectedInterval: false,
            openConfirmationPrompt: jest.fn(),
            closeConfirmationPrompt: jest.fn(),
            openAnythingConfirmationPrompt
          }}
        >
          <BaseDisplay glContainer={glContainer}>
            <OpenAnythingDialog isVisible onCancel={jest.fn()} onOpen={onOpen} />
          </BaseDisplay>
        </WorkflowContext.Provider>
      </Provider>
    );

    expect(component).toMatchSnapshot();
    jest.runAllTimers();

    component.find('DateRangePopup').prop('onNewInterval')(1602930240000, 1602930250000);
    component.find('DropDown[title="Select processing stage"]').prop('onMaybeValue')('mockStage3');
    component.find('DropDown[title="Select station group"]').prop('onMaybeValue')(
      'mockStationGroup2'
    );

    jest.runAllTimers();

    const openButton = component.find('button[data-cy="date-picker-apply-button"]').first();
    openButton.simulate('click');

    expect(component).toMatchSnapshot();

    expect(openAnythingConfirmationPrompt).toBeCalledWith({
      openIntervalName: 'mockStage3',
      stationGroup: {
        name: 'mockStationGroup'
      },
      timeRange: {
        endTimeSecs: 1602930250,
        startTimeSecs: 1602930240
      }
    });
    expect(onOpen).toBeCalled();
  });
});
