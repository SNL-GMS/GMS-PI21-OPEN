/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable jsx-a11y/no-noninteractive-tabindex */
import { Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { CommonTypes, WorkflowTypes } from '@gms/common-model';
import {
  isActivityInterval,
  isInteractiveAnalysisStage
} from '@gms/common-model/lib/workflow/types';
import {
  MILLISECONDS_IN_DAY,
  MILLISECONDS_IN_HALF_SECOND,
  MILLISECONDS_IN_SECOND,
  MILLISECONDS_IN_WEEK,
  startOfHour
} from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import { ModalPrompt, WithNonIdealStates } from '@gms/ui-core-components';
import type {
  CleanupStageIntervalsByIdAndTimeQuery,
  OperationalTimePeriodConfigurationQueryProps,
  ProcessingAnalystConfigurationQueryProps,
  StageIntervalsByIdAndTimeQuery,
  WorkflowQuery
} from '@gms/ui-state';
import {
  setOpenInterval,
  useAppDispatch,
  useAppSelector,
  useUpdateStageIntervalStatusMutation
} from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import min from 'lodash/min';
import throttle from 'lodash/throttle';
import React, { useState } from 'react';

import { AnalystNonIdealStates } from '~analyst-ui/common/non-ideal-states';
import { messageConfig } from '~analyst-ui/config/message-config';
import { useBaseDisplaySize } from '~common-ui/components/base-display/base-display-hooks';

import {
  workflowIntervalQueryNonIdealStates,
  workflowQueryNonIdealStates
} from './non-ideal-states';
import type { OpenAnythingInterval } from './types';
import type { WorkflowContextData } from './workflow-context';
import { WorkflowContext } from './workflow-context';
import type { WorkflowTableProps } from './workflow-table';
import { WorkflowTable } from './workflow-table';
import { WorkflowToolbar } from './workflow-toolbar';
import {
  closeStage,
  getTimeRangeForIntervals,
  useCloseInterval,
  useSetOpenInterval
} from './workflow-util';

const logger = UILogger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);

export type WorkflowPanelProps = ProcessingAnalystConfigurationQueryProps &
  OperationalTimePeriodConfigurationQueryProps & {
    readonly glContainer?: GoldenLayout.Container;
    readonly timeRange: CommonTypes.TimeRange;
    workflowQuery: WorkflowQuery;
    workflowIntervalQuery: StageIntervalsByIdAndTimeQuery;
    cleanupWorkflowIntervalQuery: CleanupStageIntervalsByIdAndTimeQuery;
  };

/**
 * onKeyDown function to determine hot key input and pan position
 * !NOTE use https://blueprintjs.com/docs/#core/hooks/use-hotkeys when blueprint latest is updated
 *
 * @param event react mouse event
 * @param onPan pan function which will update scroll position
 */
export const panWithHotKey = (
  event: React.KeyboardEvent<HTMLDivElement>,
  onPan: (seconds: number) => void
): void => {
  const timeToPan = event.shiftKey
    ? MILLISECONDS_IN_WEEK / MILLISECONDS_IN_SECOND
    : MILLISECONDS_IN_DAY / MILLISECONDS_IN_SECOND;
  switch (event.key) {
    case 'D':
    case 'd':
    case 'ArrowRight':
      onPan(timeToPan);
      event.stopPropagation();
      break;
    case 'A':
    case 'a':
    case 'ArrowLeft':
      onPan(-timeToPan);
      event.stopPropagation();
      break;
    default:
  }
};

const useWorkflowPanelState = () => {
  const [isConfirmationPromptVisible, setConfirmationPromptVisible] = useState(false);
  const [popupInterval, setPopupInterval] = useState<
    WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval | OpenAnythingInterval
  >(null);

  const isOpenAnything = (
    object: Partial<
      WorkflowTypes.ActivityInterval & WorkflowTypes.StageInterval & OpenAnythingInterval
    >
  ): object is OpenAnythingInterval => {
    return (
      object &&
      object.stationGroup !== undefined &&
      object.timeRange !== undefined &&
      object.openIntervalName !== undefined
    );
  };

  return {
    isConfirmationPromptVisible,
    setConfirmationPromptVisible,
    isOpenAnything,
    popupInterval,
    setPopupInterval
  };
};

const useWorkflowPanel = (): {
  isConfirmationPromptVisible: boolean;
  onCancelPrompt: () => void;
  showConfirmationPrompt: (
    interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
  ) => Promise<void>;
  showOpenAnythingConfirmationPrompt: (interval: OpenAnythingInterval) => void;
  onConfirmationPrompt: () => Promise<void>;
} => {
  const {
    isConfirmationPromptVisible,
    setConfirmationPromptVisible,
    isOpenAnything,
    popupInterval,
    setPopupInterval
  } = useWorkflowPanelState();

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const startTimeSecs = useAppSelector(state => state.app.workflow.timeRange.startTimeSecs);
  const endTimeSecs = useAppSelector(state => state.app.workflow.timeRange.endTimeSecs);

  const userName = useAppSelector(state => state.app.userSession.authenticationStatus.userName);
  const openInterval = useSetOpenInterval();
  const [stageMutation] = useUpdateStageIntervalStatusMutation();

  const dispatch = useAppDispatch();

  const openAnything = React.useCallback(
    (interval: OpenAnythingInterval): void => {
      dispatch(
        setOpenInterval(
          interval.timeRange,
          interval.stationGroup,
          interval.openIntervalName,
          [],
          null
        )
      );
    },
    [dispatch]
  );

  const onCancelPrompt = (): void => {
    setPopupInterval(undefined);
    setConfirmationPromptVisible(false);
  };

  const showConfirmationPrompt = React.useCallback(
    async (
      interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
    ): Promise<void> => {
      let stageName = interval.name;
      if (isActivityInterval(interval)) {
        stageName = interval.stageName;
      }

      // If nothing is open, no need to prompt
      if (!openIntervalName) {
        return openInterval(interval);
      }

      // If open but opening something in the same time range and stage interval no need to prompt
      if (
        openIntervalName === stageName &&
        startTimeSecs === interval.startTime &&
        endTimeSecs === interval.endTime
      ) {
        return openInterval(interval);
      }
      setPopupInterval(interval);
      setConfirmationPromptVisible(true);
      return null;
    },
    [
      endTimeSecs,
      openInterval,
      openIntervalName,
      setConfirmationPromptVisible,
      setPopupInterval,
      startTimeSecs
    ]
  );

  const showOpenAnythingConfirmationPrompt = React.useCallback(
    (interval: OpenAnythingInterval): void => {
      if (endTimeSecs || startTimeSecs) {
        setPopupInterval(interval);
        setConfirmationPromptVisible(true);
        return null;
      }
      return openAnything(interval);
    },
    [endTimeSecs, openAnything, setConfirmationPromptVisible, setPopupInterval, startTimeSecs]
  );

  const onConfirmationPrompt = async (): Promise<void> => {
    setConfirmationPromptVisible(false);
    if (isOpenAnything(popupInterval)) {
      await closeStage(userName, startTimeSecs, openIntervalName, stageMutation);
      openAnything(popupInterval);
    } else {
      await openInterval(popupInterval);
    }
  };

  return {
    isConfirmationPromptVisible,
    onCancelPrompt,
    showConfirmationPrompt,
    showOpenAnythingConfirmationPrompt,
    onConfirmationPrompt
  };
};

type WorkflowPanelTableProps = WorkflowTableProps & {
  // ? The linter thinks the values are not used; however they are used by the non-ideal state checks
  readonly glContainer?: GoldenLayout.Container;
  readonly tableRef: React.MutableRefObject<WorkflowTable>;
  readonly hasFetchedInitialIntervals: boolean;
  workflowQuery: WorkflowQuery;
  workflowIntervalQuery: StageIntervalsByIdAndTimeQuery;
};

function WorkflowPanelTable(props: WorkflowPanelTableProps) {
  const {
    glContainer,
    widthPx,
    heightPx,
    workflow,
    stageIntervals,
    timeRange,
    tableRef,
    staleStartTime
  } = props;

  return (
    <WorkflowTable
      ref={ref => {
        tableRef.current = ref;
      }}
      glContainer={glContainer}
      widthPx={widthPx}
      heightPx={heightPx}
      timeRange={timeRange}
      workflow={workflow}
      stageIntervals={stageIntervals}
      staleStartTime={staleStartTime}
    />
  );
}

const WorkflowTableOrNonIdealState = WithNonIdealStates<WorkflowPanelTableProps>(
  [
    ...AnalystNonIdealStates.processingAnalystConfigNonIdealStateDefinitions,
    ...AnalystNonIdealStates.operationalTimePeriodConfigNonIdealStateDefinitions,
    ...workflowQueryNonIdealStates,
    ...workflowIntervalQueryNonIdealStates
  ],
  WorkflowPanelTable
);

/**
 * Component to render the workflow toolbar and workflow table.
 * It uses a workflow query which returns workflow and stage intervals
 */
export function WorkflowPanel(props: WorkflowPanelProps) {
  logger.debug(`Rendering WorkflowPanel`, props);

  const {
    glContainer,
    workflowQuery,
    workflowIntervalQuery,
    operationalTimePeriodConfigurationQuery,
    cleanupWorkflowIntervalQuery
  } = props;

  const { data: workflow } = workflowQuery;
  const { data: stageIntervals } = workflowIntervalQuery;

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const openActivityNames = useAppSelector(state => state.app.workflow.openActivityNames);
  const openStartTime = useAppSelector(
    state => state.app.workflow.timeRange.startTimeSecs || Infinity
  );

  const tableRef = React.useRef<WorkflowTable>(undefined);

  const timeRangeForIntervals = React.useMemo(() => getTimeRangeForIntervals(stageIntervals), [
    stageIntervals
  ]);

  const closeInterval = useCloseInterval();

  const {
    isConfirmationPromptVisible,
    onCancelPrompt,
    showConfirmationPrompt,
    showOpenAnythingConfirmationPrompt,
    onConfirmationPrompt
  } = useWorkflowPanel();

  const onPan = (seconds: number): void => {
    if (tableRef && tableRef.current) {
      tableRef.current.panBy(seconds);
    }
  };

  // track if the workflow interval query is refetching; only show the non ideal state on the initial fetch
  const hasFetchedInitialIntervals = React.useRef(false);
  React.useEffect(() => {
    if (workflowIntervalQuery.isSuccess) {
      hasFetchedInitialIntervals.current = true;
    }
  }, [workflowIntervalQuery.isSuccess]);

  const viewableMinStartTime = tableRef?.current?.getViewableMinStartTime() || Infinity;

  const operationalDuration = React.useMemo(
    () =>
      operationalTimePeriodConfigurationQuery.data.operationalPeriodStart -
      operationalTimePeriodConfigurationQuery.data.operationalPeriodEnd,
    [
      operationalTimePeriodConfigurationQuery.data.operationalPeriodEnd,
      operationalTimePeriodConfigurationQuery.data.operationalPeriodStart
    ]
  );

  const operationalStartTime = React.useMemo(
    () => timeRangeForIntervals.endTimeSecs - operationalDuration,
    [operationalDuration, timeRangeForIntervals.endTimeSecs]
  );

  // determine the minimum stale boundary for cleanup
  const staleCleanUpBoundary = React.useMemo(
    () => startOfHour(min([viewableMinStartTime, openStartTime, operationalStartTime]) ?? 0),
    [openStartTime, operationalStartTime, viewableMinStartTime]
  );

  // run clean up of stale data ONLY if the following changes:
  // stale clean up func, stale clean up boundary changes, interval data
  React.useEffect(() => {
    cleanupWorkflowIntervalQuery(staleCleanUpBoundary);
  }, [cleanupWorkflowIntervalQuery, staleCleanUpBoundary, stageIntervals]);

  const openStage = workflow?.stages.find(stage => stage.name === openIntervalName);

  const allActivitiesOpenForSelectedInterval = React.useMemo(
    () =>
      isInteractiveAnalysisStage(openStage) &&
      openStage?.activities.length === openActivityNames.length,
    [openActivityNames.length, openStage]
  );

  const [widthPx, heightPx] = useBaseDisplaySize();

  // Memoized to avoid context re-rendering
  const closeConfirmationPrompt = React.useCallback(
    async (interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval) =>
      closeInterval(interval),
    [closeInterval]
  );

  // Memoized to avoid context re-rendering
  const openConfirmationPrompt = React.useCallback(
    async (interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval) =>
      showConfirmationPrompt(interval),
    [showConfirmationPrompt]
  );

  // Memoized to avoid context re-rendering
  const openAnythingConfirmationPrompt = React.useCallback(
    (interval: OpenAnythingInterval) => showOpenAnythingConfirmationPrompt(interval),
    [showOpenAnythingConfirmationPrompt]
  );

  // Memoized to avoid context re-rendering
  const workflowContextData: WorkflowContextData = React.useMemo(
    () => ({
      staleStartTime: operationalStartTime,
      allActivitiesOpenForSelectedInterval,
      closeConfirmationPrompt,
      openConfirmationPrompt,
      openAnythingConfirmationPrompt
    }),
    [
      allActivitiesOpenForSelectedInterval,
      closeConfirmationPrompt,
      openAnythingConfirmationPrompt,
      openConfirmationPrompt,
      operationalStartTime
    ]
  );

  return (
    <WorkflowContext.Provider value={workflowContextData}>
      <div
        className="workflow-panel"
        data-cy="workflow-panel"
        onKeyDown={throttle(e => {
          panWithHotKey(e, onPan);
        }, MILLISECONDS_IN_HALF_SECOND / 4)}
        onMouseEnter={throttle(e => {
          e.currentTarget.focus();
        })}
        onMouseLeave={throttle(e => {
          e.currentTarget.blur();
        })}
        tabIndex={0}
      >
        <WorkflowToolbar onPan={throttle(onPan, MILLISECONDS_IN_HALF_SECOND / 4)} />
        <WorkflowTableOrNonIdealState
          tableRef={tableRef}
          glContainer={glContainer}
          workflowQuery={workflowQuery}
          workflowIntervalQuery={workflowIntervalQuery}
          widthPx={widthPx}
          heightPx={heightPx}
          timeRange={timeRangeForIntervals}
          workflow={workflow}
          stageIntervals={stageIntervals}
          hasFetchedInitialIntervals={hasFetchedInitialIntervals.current}
          staleStartTime={operationalStartTime}
        />
        <ModalPrompt
          actionText={messageConfig.tooltipMessages.workflowConfirmation.discardText}
          actionCallback={onConfirmationPrompt}
          optionalCallback={onCancelPrompt}
          cancelText={messageConfig.tooltipMessages.workflowConfirmation.cancelText}
          cancelButtonCallback={onCancelPrompt}
          onCloseCallback={onCancelPrompt}
          isOpen={isConfirmationPromptVisible}
          title={messageConfig.tooltipMessages.workflowConfirmation.title}
          actionTooltipText={messageConfig.tooltipMessages.workflowConfirmation.discardTooltip}
          cancelTooltipText={messageConfig.tooltipMessages.workflowConfirmation.cancelTooltip}
        >
          <div className="interval-confirmation-contents">
            <div className="interval-confirmation-text">
              <div className="interval-confirmation-header">
                {messageConfig.tooltipMessages.workflowConfirmation.header}
              </div>
              <div className="interval-confirmation-paragraph">
                {messageConfig.tooltipMessages.workflowConfirmation.text}
              </div>
            </div>
            <Icon icon={IconNames.ERROR} className="interval-confirmation-icon" iconSize={48} />
          </div>
        </ModalPrompt>
      </div>
    </WorkflowContext.Provider>
  );
}
