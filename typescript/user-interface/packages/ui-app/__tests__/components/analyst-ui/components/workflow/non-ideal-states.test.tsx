import type { TimeRange } from '@gms/common-model/lib/common/types';
import type { Workflow } from '@gms/common-model/lib/workflow/types';
import { WithNonIdealStates } from '@gms/ui-core-components';
import type {
  OperationalTimePeriodConfigurationQuery,
  ProcessingAnalystConfigurationQuery,
  StageIntervalList,
  UseQueryStateResult
} from '@gms/ui-state';
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';
import { Provider } from 'react-redux';

import {
  workflowIntervalQueryNonIdealStates,
  workflowQueryNonIdealStates
} from '../../../../../src/ts/components/analyst-ui/components/workflow/non-ideal-states';
import type { WorkflowPanelProps } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-panel';
import { WorkflowPanel } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-panel';
import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';
import { useQueryStateResult } from '../../../../__data__/test-util-data';
import { glContainer } from './gl-container';
import * as WorkflowDataTypes from './workflow-data-types';

const store = getStore();

const intervalQuery: UseQueryStateResult<StageIntervalList> = cloneDeep(useQueryStateResult);

const workflowQuery: UseQueryStateResult<Workflow> = cloneDeep(useQueryStateResult);

const processingAnalystConfigurationQuery: ProcessingAnalystConfigurationQuery = cloneDeep(
  useQueryStateResult
);
processingAnalystConfigurationQuery.isError = false;
processingAnalystConfigurationQuery.isLoading = false;

const operationalTimePeriodConfigurationQuery: OperationalTimePeriodConfigurationQuery = cloneDeep(
  useQueryStateResult
);
operationalTimePeriodConfigurationQuery.data = {
  operationalPeriodStart: 1000,
  operationalPeriodEnd: 2000
};

const timeRange: TimeRange = {
  startTimeSecs: 1000,
  endTimeSecs: 2000
};

describe('workflow non-ideal-states', () => {
  it('workflow query exists', () => {
    expect(workflowQueryNonIdealStates).toBeDefined();
  });

  it('workflow query error matches snapshot', () => {
    const NonIdealElement = WithNonIdealStates<WorkflowPanelProps>(
      [...workflowQueryNonIdealStates],
      WorkflowPanel
    );
    workflowQuery.isError = true;
    workflowQuery.isLoading = false;
    intervalQuery.isError = false;
    intervalQuery.isLoading = false;

    operationalTimePeriodConfigurationQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isError = false;
    const component = render(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <NonIdealElement
            workflowQuery={workflowQuery}
            workflowIntervalQuery={intervalQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    expect(component).toMatchSnapshot();
  });

  it('workflow query loading matches snapshot', () => {
    const NonIdealElement = WithNonIdealStates<WorkflowPanelProps>(
      [...workflowQueryNonIdealStates],
      WorkflowPanel
    );
    workflowQuery.isError = false;
    workflowQuery.isLoading = true;
    intervalQuery.isError = false;
    intervalQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isError = false;
    const component = render(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <NonIdealElement
            workflowQuery={workflowQuery}
            workflowIntervalQuery={intervalQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    expect(component).toMatchSnapshot();
  });

  it('workflow interval query empty matches snapshot', () => {
    const NonIdealElement = WithNonIdealStates<WorkflowPanelProps>(
      [...workflowIntervalQueryNonIdealStates],
      WorkflowPanel
    );
    workflowQuery.isError = false;
    workflowQuery.isLoading = false;
    intervalQuery.isError = false;
    intervalQuery.isLoading = false;
    intervalQuery.data = undefined;
    operationalTimePeriodConfigurationQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isError = false;
    const component = render(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <NonIdealElement
            workflowQuery={workflowQuery}
            workflowIntervalQuery={intervalQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    expect(component).toMatchSnapshot();
  });

  it('workflow interval query error matches snapshot', () => {
    const NonIdealElement = WithNonIdealStates<WorkflowPanelProps>(
      [...workflowIntervalQueryNonIdealStates],
      WorkflowPanel
    );
    workflowQuery.isError = false;
    workflowQuery.isLoading = false;
    intervalQuery.isError = true;
    intervalQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isError = false;
    const component = render(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <NonIdealElement
            workflowQuery={workflowQuery}
            workflowIntervalQuery={intervalQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    expect(component).toMatchSnapshot();
  });

  it('workflow interval query loading matches snapshot', () => {
    const NonIdealElement = WithNonIdealStates<WorkflowPanelProps>(
      [...workflowIntervalQueryNonIdealStates],
      WorkflowPanel
    );
    workflowQuery.isError = false;
    workflowQuery.isLoading = false;
    intervalQuery.isError = false;
    intervalQuery.isLoading = true;
    operationalTimePeriodConfigurationQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isError = false;
    const component = render(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <NonIdealElement
            workflowQuery={workflowQuery}
            workflowIntervalQuery={intervalQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            cleanupWorkflowIntervalQuery={jest.fn()}
          />
        </Provider>
      </BaseDisplay>
    );
    expect(component).toMatchSnapshot();
  });

  it('workflow interval query reloading matches snapshot', () => {
    const NonIdealElement = WithNonIdealStates<
      React.PropsWithChildren<WorkflowPanelProps & { readonly hasFetchedInitialIntervals: boolean }>
    >([...workflowIntervalQueryNonIdealStates], WorkflowPanel);
    workflowQuery.isError = false;
    workflowQuery.isLoading = false;
    workflowQuery.data = WorkflowDataTypes.workflow;
    intervalQuery.isError = false;
    intervalQuery.isLoading = true;

    const intervalQueryResultMap: StageIntervalList = [];
    intervalQueryResultMap.push({
      name: WorkflowDataTypes.interactiveStage.name,
      value: [WorkflowDataTypes.interactiveAnalysisStageInterval]
    });
    intervalQuery.data = intervalQueryResultMap;

    operationalTimePeriodConfigurationQuery.isLoading = false;
    operationalTimePeriodConfigurationQuery.isError = false;
    const component = render(
      <BaseDisplay glContainer={glContainer}>
        <Provider store={store}>
          <NonIdealElement
            workflowQuery={workflowQuery}
            workflowIntervalQuery={intervalQuery}
            operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
            processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
            timeRange={timeRange}
            hasFetchedInitialIntervals
            cleanupWorkflowIntervalQuery={jest.fn()}
          >
            <div>content</div>
          </NonIdealElement>
        </Provider>
      </BaseDisplay>
    );
    expect(component).toMatchSnapshot();
  });

  it('workflow interval query exists', () => {
    expect(workflowIntervalQueryNonIdealStates).toBeDefined();
  });

  it('workflow interval query matches snapshot', () => {
    expect(workflowIntervalQueryNonIdealStates).toBeDefined();
  });
});
