import { IanDisplays } from '@gms/common-model/lib/displays/types';
import type GoldenLayout from '@gms/golden-layout';
import { WithNonIdealStates } from '@gms/ui-core-components';
import {
  useCleanupStageIntervalsByIdAndTimeQuery,
  useGetProcessingAnalystConfigurationQuery,
  useOperationalTimePeriodConfiguration,
  useStageIntervalsByIdAndTimeQuery,
  useWorkflowQuery
} from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import * as React from 'react';

import { AnalystNonIdealStates } from '~analyst-ui/common/non-ideal-states';
import { BaseDisplay } from '~common-ui/components/base-display';

import type { WorkflowPanelProps } from './workflow-panel';
import { WorkflowPanel } from './workflow-panel';

const logger = UILogger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);

export interface WorkflowComponentProps {
  // passed in from golden-layout
  readonly glContainer?: GoldenLayout.Container;
}

export const WorkflowPanelOrNonIdealState = WithNonIdealStates<WorkflowPanelProps>(
  [
    ...AnalystNonIdealStates.processingAnalystConfigNonIdealStateDefinitions,
    ...AnalystNonIdealStates.operationalTimePeriodConfigNonIdealStateDefinitions
  ],
  WorkflowPanel
);

export function WorkflowComponent(props: WorkflowComponentProps) {
  logger.debug(`Rendering WorkflowComponent`, props);
  const { glContainer } = props;
  const processingAnalystConfigurationQuery = useGetProcessingAnalystConfigurationQuery();

  const {
    operationalTimePeriodConfigurationQuery,
    timeRange
  } = useOperationalTimePeriodConfiguration();

  const workflowQuery = useWorkflowQuery();

  const stageNames = React.useMemo(
    () => (workflowQuery.isSuccess ? workflowQuery.data?.stages.map(stage => stage.name) : []),
    [workflowQuery.isSuccess, workflowQuery.data?.stages]
  );

  const workflowIntervalQuery = useStageIntervalsByIdAndTimeQuery(stageNames, timeRange);

  const cleanupWorkflowIntervalQuery = useCleanupStageIntervalsByIdAndTimeQuery(
    stageNames,
    timeRange
  );

  return (
    <BaseDisplay
      glContainer={glContainer}
      className="workflow-display-window gms-body-text"
      tabName={IanDisplays.WORKFLOW}
    >
      <WorkflowPanelOrNonIdealState
        glContainer={glContainer}
        timeRange={timeRange}
        processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
        operationalTimePeriodConfigurationQuery={operationalTimePeriodConfigurationQuery}
        workflowQuery={workflowQuery}
        workflowIntervalQuery={workflowIntervalQuery}
        cleanupWorkflowIntervalQuery={cleanupWorkflowIntervalQuery}
      />
    </BaseDisplay>
  );
}
