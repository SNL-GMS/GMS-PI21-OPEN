import type { NonIdealStateDefinition } from '@gms/ui-core-components';
import { nonIdealStateWithNoSpinner, nonIdealStateWithSpinner } from '@gms/ui-core-components';
import type { StageIntervalsByIdAndTimeQuery, WorkflowQuery } from '@gms/ui-state';

export const workflowQueryNonIdealStates: NonIdealStateDefinition<unknown>[] = [
  {
    condition: (props: { workflowQuery: WorkflowQuery }): boolean => {
      return props.workflowQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Workflow Data')
  },
  {
    condition: (props: { workflowQuery: WorkflowQuery }): boolean => {
      return props.workflowQuery?.isError;
    },
    element: nonIdealStateWithSpinner('Error', 'Problem Loading Workflow Data')
  }
];

export const workflowIntervalQueryNonIdealStates: NonIdealStateDefinition<unknown>[] = [
  {
    condition: (props: {
      readonly hasFetchedInitialIntervals: boolean;
      workflowIntervalQuery: StageIntervalsByIdAndTimeQuery;
    }): boolean => {
      // only show the loading state if the on the initial fetch of the interval data
      return !props.hasFetchedInitialIntervals && props.workflowIntervalQuery?.isLoading;
    },
    element: nonIdealStateWithSpinner('Loading', 'Workflow Interval Data')
  },
  {
    condition: (props: { workflowIntervalQuery: StageIntervalsByIdAndTimeQuery }): boolean => {
      return (
        !props.workflowIntervalQuery?.isLoading &&
        !props.workflowIntervalQuery?.isError &&
        (!props.workflowIntervalQuery.data || props.workflowIntervalQuery.data.length === 0)
      );
    },
    element: nonIdealStateWithNoSpinner('No data', 'Stage intervals returned empty')
  },
  {
    condition: (props: { workflowIntervalQuery: StageIntervalsByIdAndTimeQuery }): boolean => {
      return props.workflowIntervalQuery?.isError;
    },
    element: nonIdealStateWithNoSpinner('Error', 'Problem Loading Workflow Interval Data')
  }
];
