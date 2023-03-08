import type { CommonTypes, SignalDetectionTypes } from '@gms/common-model';
import { WorkflowTypes } from '@gms/common-model';
import {
  isAutomaticProcessingStage,
  isAutomaticProcessingStageInterval,
  isInteractiveAnalysisStage,
  isProcessingSequenceInterval
} from '@gms/common-model/lib/workflow/types';
import { epochSecondsNow, toDate } from '@gms/common-util';
import type {
  AppDispatch,
  FindEventsByAssociatedSignalDetectionHypothesesProps,
  StageIntervalList,
  UpdateActivityIntervalStatusMutationFunc,
  UpdateActivityIntervalStatusParams,
  UpdateStageIntervalStatusMutationFunc,
  UpdateStageIntervalStatusParams
} from '@gms/ui-state';
import {
  setClosedInterval,
  setOpenInterval,
  useAppDispatch,
  useAppSelector,
  useGetSignalDetections,
  useInterval,
  useUpdateActivityIntervalStatusMutation,
  useUpdateStageIntervalStatusMutation,
  useWorkflowQuery
} from '@gms/ui-state';
import * as d3 from 'd3';

import { PIXELS_PER_SECOND } from './constants';

/**
 * Calculates the width for the given start and end times.
 *
 * @param startTime the start time
 * @param endTime the end time
 * @returns the width
 */
export const calculateWidth = (startTime: number, endTime: number): number => {
  return (endTime - startTime) * PIXELS_PER_SECOND;
};

/**
 * Returns the scales and total for the provided time range.
 * The scaleWidth can be used to determine the placement of an interval.
 * The scale can be used to determine the placement of ticks for the axis.
 *
 * @param timeRange the time range
 * @returns The scales and the total width
 */
export const getScaleForTimeRange = (
  timeRange: CommonTypes.TimeRange
): {
  scaleAxis: d3.ScaleTime<number, number>;
  scaleToPosition: d3.ScaleLinear<number, number>;
  scaleToTime: d3.ScaleLinear<number, number>;
  totalWidth: number;
} => {
  const totalWidth = timeRange ? calculateWidth(timeRange.startTimeSecs, timeRange.endTimeSecs) : 0;

  // apply a slight margin to account for the tick offset and to align with the day boundary
  const margin = 1.5;
  const scaleAxis = d3
    .scaleUtc()
    .domain([toDate(timeRange?.startTimeSecs), toDate(timeRange?.endTimeSecs)])
    .range([0 + margin, totalWidth + margin]);

  const scaleToPosition = d3
    .scaleLinear()
    .domain([timeRange?.startTimeSecs, timeRange?.endTimeSecs])
    .range([0, totalWidth]);

  const scaleToTime = d3
    .scaleLinear()
    .domain([0, totalWidth])
    .range([timeRange?.startTimeSecs, timeRange?.endTimeSecs]);

  return { scaleAxis, scaleToPosition, scaleToTime, totalWidth };
};

/**
 * Determines if a `stage interval` should be rendered as a percent bar.
 *
 * Auto Network is the only automatic stage mode where the cells should not be percent bars
 * Auto Network time chunks are 5min so if stage mode is automatic and duration is more than
 * 5min it's not a Auto Network cell and should be shown as a percent bar
 * Also, the status of the interval should be IN_PROGRESS
 *
 * @param interval stage interval
 * @returns boolean
 */
export const isStageIntervalPercentBar = (interval: WorkflowTypes.StageInterval): boolean => {
  const fiveMinInSeconds = 300;
  return (
    interval.endTime - interval.startTime > fiveMinInSeconds &&
    interval.stageMode !== WorkflowTypes.StageMode.INTERACTIVE &&
    interval.status === WorkflowTypes.IntervalStatus.IN_PROGRESS
  );
};

/**
 * Retrieves the percentComplete value of an AutomaticProcessingStageInterval
 *
 * @param interval
 * @returns the percentComplete for the AutomaticProcessingStageInterval, or 0
 */
export const getPercentComplete = (
  interval: WorkflowTypes.StageInterval,
  workflow: WorkflowTypes.Workflow
): number => {
  if (isAutomaticProcessingStageInterval(interval) && interval.sequenceIntervals.length > 0) {
    const intervalStage: WorkflowTypes.Stage = workflow.stages.find(
      stage => stage.name === interval.intervalId.definitionId.name
    );

    if (isAutomaticProcessingStage(intervalStage)) {
      const stageIndex = intervalStage.sequences[0].steps.findIndex(
        step => step.name === interval.sequenceIntervals[0].lastExecutedStepName
      );

      if (stageIndex === -1) {
        return 0;
      }
      return (stageIndex / intervalStage.sequences[0].steps.length) * 100;
    }
  }
  if (isProcessingSequenceInterval(interval)) {
    return interval.percentComplete;
  }
  return 0;
};

/**
 * Closes a stage
 *
 * @param userName current username
 * @param startTimeSecs open time range start time secs
 * @param openIntervalName open interval name
 * @param analystStageMutation mutation to close the stage
 */
export const closeStage = async (
  userName: string,
  startTimeSecs: number,
  openIntervalName: string,
  analystStageMutation: UpdateStageIntervalStatusMutationFunc
): Promise<void> => {
  const args: UpdateStageIntervalStatusParams = {
    stageIntervalId: {
      startTime: startTimeSecs,
      definitionId: {
        name: openIntervalName
      }
    },
    status: WorkflowTypes.IntervalStatus.NOT_COMPLETE,
    userName,
    time: epochSecondsNow()
  };
  await analystStageMutation(args);
};

export const getStageName = (
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
): string => {
  if (WorkflowTypes.isActivityInterval(interval)) {
    return interval.stageName;
  }
  return interval.name;
};

export const closeOpenInterval = async (
  dispatch: AppDispatch,
  userName: string,
  stageName: string,
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval,
  status: WorkflowTypes.IntervalStatus,
  openIntervalName: string,
  startTimeSecs: number,
  analystStageMutation: UpdateStageIntervalStatusMutationFunc
): Promise<boolean> => {
  // Close the open stage and/or activities since user is discarding changes and opening another interval
  if (
    status === WorkflowTypes.IntervalStatus.IN_PROGRESS &&
    openIntervalName &&
    (openIntervalName !== stageName || interval.startTime !== startTimeSecs)
  ) {
    await closeStage(userName, startTimeSecs, openIntervalName, analystStageMutation);
    return false;
  }
  return true;
};

export const setIntervalStatus = (
  dispatch: AppDispatch,
  workflow: WorkflowTypes.Workflow,
  userName: string,
  status: WorkflowTypes.IntervalStatus,
  openIntervalName: string,
  openActivityNames: string[],
  startTimeSecs: number,
  activityMutation: UpdateActivityIntervalStatusMutationFunc,
  analystStageMutation: UpdateStageIntervalStatusMutationFunc
) => async (
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
): Promise<void> => {
  let interactiveAnalysisStage: WorkflowTypes.InteractiveAnalysisStage;
  let activities: WorkflowTypes.Activity[];

  const stageName = getStageName(interval);
  const stage: WorkflowTypes.Stage = workflow.stages.find(s => s.name === stageName);

  const isActivityInOpenStageInterval = await closeOpenInterval(
    dispatch,
    userName,
    stageName,
    interval,
    status,
    openIntervalName,
    startTimeSecs,
    analystStageMutation
  );

  if (isInteractiveAnalysisStage(stage)) {
    interactiveAnalysisStage = stage;
  }

  if (WorkflowTypes.isStageInterval(interval)) {
    const args: UpdateStageIntervalStatusParams = {
      stageIntervalId: {
        startTime: interval.startTime,
        definitionId: {
          name: interval.name
        }
      },
      status,
      userName,
      time: epochSecondsNow() // what should this time be?
    };
    await analystStageMutation(args);
    activities = interactiveAnalysisStage.activities;
  }

  if (WorkflowTypes.isActivityInterval(interval)) {
    const args: UpdateActivityIntervalStatusParams = {
      activityIntervalId: {
        startTime: interval.startTime,
        definitionId: {
          name: interval.name
        }
      },
      stageIntervalId: {
        startTime: interval.startTime,
        definitionId: {
          name: interval.stageName
        }
      },
      status,
      userName,
      time: epochSecondsNow() // what should this time be?
    };
    await activityMutation(args);
    activities = [interactiveAnalysisStage.activities.find(act => act.name === interval.name)];
  }

  // Get the station group from the Workflow Stage
  // As guidance the first activity is the open that is supposed to be used
  const { stationGroup, analysisMode } = activities[0];
  // With a success mutation dispatch to Redux
  switch (status) {
    case WorkflowTypes.IntervalStatus.IN_PROGRESS:
      dispatch(
        setOpenInterval(
          {
            startTimeSecs: interval.startTime,
            endTimeSecs: interval.endTime
          },
          stationGroup,
          interactiveAnalysisStage.name,
          isActivityInOpenStageInterval
            ? [...activities.map(activity => activity.name), ...openActivityNames]
            : activities.map(activity => activity.name),
          analysisMode
        )
      );
      break;
    case WorkflowTypes.IntervalStatus.NOT_COMPLETE:
      dispatch(setClosedInterval(activities[0].name, WorkflowTypes.isStageInterval(interval)));
      break;
    default:
  }
};

/**
 * Hook to construct the props for the useFindEventsByAssociatedSignalDetectionHypothesesQuery hook
 *
 * @param openIntervalName the interval currently open
 */
export const useGetFindEventsByAssociatedSignalDetectionHypothesesProps = (
  openIntervalName: string
): FindEventsByAssociatedSignalDetectionHypothesesProps => {
  const isSynced = useAppSelector(
    state => state.app.signalDetections.displayedSignalDetectionConfiguration.syncWaveform
  );
  const [interval] = useInterval(isSynced);
  const signalDetections = useGetSignalDetections(interval);
  const hypothesesSet = new Set<SignalDetectionTypes.SignalDetectionHypothesis>();
  signalDetections.data.forEach(signalDetection => {
    signalDetection.signalDetectionHypotheses.forEach(signalDetectionHypothesis =>
      hypothesesSet.add(signalDetectionHypothesis)
    );
  });

  const signalDetectionHypotheses = Array.from(hypothesesSet);
  return { signalDetectionHypotheses, stageId: { name: openIntervalName } };
};

export const useSetInterval = (
  status: WorkflowTypes.IntervalStatus.NOT_COMPLETE | WorkflowTypes.IntervalStatus.IN_PROGRESS
): ((interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval) => Promise<void>) => {
  const dispatch = useAppDispatch();
  const workflow = useWorkflowQuery();
  const userName = useAppSelector(state => state.app.userSession.authenticationStatus.userName);
  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const openActivityNames = useAppSelector(state => state.app.workflow.openActivityNames);
  const startTimeSecs = useAppSelector(state => state.app.workflow.timeRange.startTimeSecs);
  const [activityMutation] = useUpdateActivityIntervalStatusMutation();
  const [analystStageMutation] = useUpdateStageIntervalStatusMutation();
  return setIntervalStatus(
    dispatch,
    workflow.data,
    userName,
    status,
    openIntervalName,
    openActivityNames,
    startTimeSecs,
    activityMutation,
    analystStageMutation
  );
};

/**
 * Opens an interval and updates the redux state to reflect a state with open interval
 */
export const useSetOpenInterval = (): ((
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
) => Promise<void>) => {
  return useSetInterval(WorkflowTypes.IntervalStatus.IN_PROGRESS);
};

/**
 * Closes an interval and updates the redux state to reflect a state with no open interval
 */
export const useCloseInterval = (): ((
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
) => Promise<void>) => {
  return useSetInterval(WorkflowTypes.IntervalStatus.NOT_COMPLETE);
};

/**
 * Retrieves the TimeRange for the provided intervals; i.e. the earliest start time and the latest end time.
 *
 * @param stageIntervals the stage intervals
 * @returns the time range for the intervals
 */
export const getTimeRangeForIntervals = (
  stageIntervals: StageIntervalList
): CommonTypes.TimeRange => {
  const timeRange: CommonTypes.TimeRange = {
    startTimeSecs: Infinity,
    endTimeSecs: -Infinity
  };

  if (stageIntervals) {
    stageIntervals.forEach(s =>
      s.value.forEach(interval => {
        timeRange.startTimeSecs =
          interval.startTime <= timeRange.startTimeSecs
            ? interval.startTime
            : timeRange.startTimeSecs;

        timeRange.endTimeSecs =
          interval.endTime >= timeRange.endTimeSecs ? interval.endTime : timeRange.endTimeSecs;
      })
    );
  }

  if (timeRange.startTimeSecs === Infinity || timeRange.endTimeSecs === -Infinity) {
    return {
      startTimeSecs: undefined,
      endTimeSecs: undefined
    };
  }
  return timeRange;
};

/**
 * Create a unique key based on the selected Workflow Interval
 */
export const useWorkflowIntervalUniqueId = (): string => {
  const currentInterval = useAppSelector(state => state.app.workflow.timeRange);

  const analysisMode = useAppSelector(state => state.app.workflow.analysisMode);

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  return `currentIntervalStartTime ${currentInterval.startTimeSecs} analysisMode ${analysisMode} openIntervalName ${openIntervalName}`;
};
