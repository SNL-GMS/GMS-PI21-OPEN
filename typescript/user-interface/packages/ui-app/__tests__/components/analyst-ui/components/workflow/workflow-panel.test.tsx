import { FORTY_FIVE_DAYS_IN_SECONDS } from '@gms/common-util';
import type { PromptProps } from '@gms/ui-core-components/lib/components/dialog/types';
import type {
  OperationalTimePeriodConfigurationQuery,
  ProcessingAnalystConfigurationQuery,
  StageIntervalList,
  StageIntervalsByIdAndTimeQuery,
  WorkflowQuery
} from '@gms/ui-state';
import { getStore, workflowSlice } from '@gms/ui-state';
import * as Enzyme from 'enzyme';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import {
  panWithHotKey,
  WorkflowPanel
} from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-panel';
import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { glContainer } from './gl-container';
import * as WorkflowDataTypes from './workflow-data-types';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

const timeRange = { startTimeSecs: 1609500000, endTimeSecs: 1609506000 };
const MOCK_TIME = 1609506000000;

const store = getStore();
store.dispatch(workflowSlice.actions.setTimeRange(timeRange));

const intervalQueryResult: StageIntervalList = [];
intervalQueryResult.push({
  name: WorkflowDataTypes.interactiveStage.name,
  value: [WorkflowDataTypes.interactiveAnalysisStageInterval]
});

const intervalQuery: StageIntervalsByIdAndTimeQuery = cloneDeep(useQueryStateResult);
intervalQuery.data = intervalQueryResult;

const workflowQuery: WorkflowQuery = cloneDeep(useQueryStateResult);
workflowQuery.data = WorkflowDataTypes.workflow;

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

const mockOpenInterval = jest.fn();
const mockCloseInterval = jest.fn();

jest.mock('../../../../../src/ts/components/analyst-ui/components/workflow/workflow-util', () => ({
  // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
  ...(jest.requireActual(
    '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-util'
  ) as any),
  useSetOpenInterval: jest.fn(() => {
    return mockOpenInterval;
  }),
  useCloseInterval: jest.fn(() => {
    return mockCloseInterval;
  })
}));

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetProcessingAnalystConfigurationQuery: jest.fn(() => ({
      ...processingAnalystConfigurationQuery,
      data: {
        mOpenAnythingDuration: 7200
      }
    })),
    useGetOperationalTimePeriodConfigurationQuery: jest.fn(
      () => operationalTimePeriodConfigurationQuery
    ),
    useGetProcessingStationGroupNamesConfigurationQuery: jest.fn(() => ({
      data: { stationGroupNames: [WorkflowDataTypes.stationGroupName] }
    }))
  };
});

describe('Workflow Panel', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(MOCK_TIME);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('is exported', () => {
    expect(WorkflowPanel).toBeDefined();
  });

  it('shallow mounts', () => {
    const shallow = Enzyme.shallow(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <WorkflowPanel
            glContainer={glContainer}
            workflowIntervalQuery={intervalQuery}
            workflowQuery={workflowQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );

    expect(shallow).toMatchSnapshot();
    const workflowPanel = shallow.find('WorkflowPanel');
    expect(workflowPanel).toBeDefined();
  });

  it('can handle panWithHotKey', () => {
    const stopProp = jest.fn();
    const eventWithShiftRight: any = {
      shiftKey: true,
      key: 'ArrowRight',
      stopPropagation: stopProp
    };
    const pan = jest.fn();
    panWithHotKey(eventWithShiftRight, pan);

    const eventRight: any = {
      shiftKey: false,
      key: 'ArrowRight',
      stopPropagation: stopProp
    };
    panWithHotKey(eventRight, pan);

    const eventWithShiftLeft: any = {
      shiftKey: true,
      key: 'ArrowLeft',
      stopPropagation: stopProp
    };
    panWithHotKey(eventWithShiftLeft, pan);

    const eventLeft: any = {
      shiftKey: false,
      key: 'ArrowLeft',
      stopPropagation: stopProp
    };
    panWithHotKey(eventLeft, pan);

    expect(pan).toHaveBeenCalledTimes(4);
    expect(stopProp).toHaveBeenCalledTimes(4);
    stopProp.mockClear();

    // Verify that the onKeyDown event calls panWithHotKey
    const component = Enzyme.mount(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <WorkflowPanel
            glContainer={glContainer}
            workflowIntervalQuery={intervalQuery}
            workflowQuery={workflowQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );

    component.find('div[className="workflow-panel"]').props().onKeyDown(eventLeft);
    expect(stopProp).toHaveBeenCalledTimes(1);
  });

  it('confirmation panel exists with discard and cancel buttons', () => {
    const realUseState = React.useState;
    jest.spyOn(React, 'useState').mockImplementationOnce(() => realUseState(true as unknown));

    const shallow = Enzyme.shallow(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <WorkflowPanel
            glContainer={glContainer}
            workflowIntervalQuery={intervalQuery}
            workflowQuery={workflowQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    const confirmationDialogueDiscardButton = shallow
      .find('button')
      .find({ text: 'Discard changes' });
    expect(confirmationDialogueDiscardButton).toBeDefined();

    const confirmationDialogueCancelButton = shallow.find('button').find({ text: 'Cancel' });
    expect(confirmationDialogueCancelButton).toBeDefined();
  });

  it('full mounts', () => {
    const component = Enzyme.mount(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <WorkflowPanel
            glContainer={glContainer}
            workflowIntervalQuery={intervalQuery}
            workflowQuery={workflowQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );

    const workflowPanel = component.find('WorkflowPanel');
    expect(workflowPanel).toBeDefined();
  });

  it('confirmation panel closes and sets the interval for opening an interval', () => {
    const component = Enzyme.mount(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <WorkflowPanel
            glContainer={glContainer}
            workflowIntervalQuery={intervalQuery}
            workflowQuery={workflowQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    const confirmationPrompt = component.find('ModalPrompt[title="Warning"]');
    (confirmationPrompt.props() as PromptProps).actionCallback();
    expect(mockOpenInterval).toBeCalledWith(null);
    expect(component).toBeDefined();
  });

  it('mouse events work', () => {
    const component = Enzyme.mount(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <WorkflowPanel
            glContainer={glContainer}
            workflowIntervalQuery={intervalQuery}
            workflowQuery={workflowQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );

    const mockEventFocus = jest.fn();
    const mockEventBlur = jest.fn();

    const mockMouseEvent: any = {
      currentTarget: {
        blur: mockEventBlur,
        focus: mockEventFocus
      }
    };
    component.find('div[className="workflow-panel"]').props().onMouseEnter(mockMouseEvent);
    expect(mockEventFocus).toHaveBeenCalled();

    component.find('div[className="workflow-panel"]').props().onMouseLeave(mockMouseEvent);
    expect(mockEventBlur).toHaveBeenCalled();
  });
});
