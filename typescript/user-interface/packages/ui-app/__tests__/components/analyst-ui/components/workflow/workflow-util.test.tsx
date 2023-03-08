/* eslint-disable @typescript-eslint/no-magic-numbers */
import { WorkflowTypes } from '@gms/common-model';
import type { StageIntervalList, WorkflowState } from '@gms/ui-state';
import { getStore } from '@gms/ui-state';

import {
  getPercentComplete,
  getTimeRangeForIntervals,
  isStageIntervalPercentBar,
  setIntervalStatus,
  useCloseInterval,
  useSetOpenInterval
} from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-util';
import { renderReduxHook } from '../../../../utils/render-hook-util';
import * as WorkflowDataTypes from './workflow-data-types';

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

const dispatch = jest.fn();
const store = getStore();

describe('Workflow Util', () => {
  const workflow: WorkflowState = {
    analysisMode: undefined,
    openActivityNames: ['1'],
    openIntervalName: 'test',
    stationGroup: undefined,
    timeRange: { startTimeSecs: 1, endTimeSecs: 2 }
  };

  it('can determine cell percent bar', () => {
    const interactiveStageInterval = isStageIntervalPercentBar(
      WorkflowDataTypes.interactiveAnalysisStageInterval
    );
    expect(interactiveStageInterval).toBeFalsy();

    const automaticStageInterval = isStageIntervalPercentBar(
      WorkflowDataTypes.automaticProcessingStageInterval
    );
    expect(automaticStageInterval).toBeTruthy();

    expect(
      isStageIntervalPercentBar((WorkflowDataTypes.processingSequenceInterval as unknown) as any)
    ).toBeTruthy();
  });

  it('handles setIntervalStatus with activity', async () => {
    const activityMutation = jest.fn();
    const analystStageMutation = jest.fn();
    const results = setIntervalStatus(
      dispatch,
      WorkflowDataTypes.workflow,
      'Joe Blow',
      WorkflowTypes.IntervalStatus.IN_PROGRESS,
      workflow.openIntervalName,
      workflow.openActivityNames,
      workflow.timeRange.startTimeSecs,
      activityMutation,
      analystStageMutation
    );
    expect(results).toBeDefined();
    expect(results).toMatchSnapshot();
    await results(WorkflowDataTypes.activityInterval);
    expect(activityMutation).toHaveBeenCalled();
    expect(analystStageMutation).toHaveBeenCalledTimes(1);
  });

  it('handles setIntervalStatus with stage', async () => {
    const activityMutation = jest.fn();
    const analystStageMutation = jest.fn();
    const results = setIntervalStatus(
      dispatch,
      WorkflowDataTypes.workflow,
      'Joe Blow',
      WorkflowTypes.IntervalStatus.NOT_COMPLETE,
      workflow.openIntervalName,
      workflow.openActivityNames,
      workflow.timeRange.startTimeSecs,
      activityMutation,
      analystStageMutation
    );
    expect(results).toBeDefined();
    expect(results).toMatchSnapshot();
    await results(WorkflowDataTypes.interactiveAnalysisStageInterval);
    expect(analystStageMutation).toHaveBeenCalled();
    expect(activityMutation).toHaveBeenCalledTimes(0);
  });

  it('handles useSetOpenInterval Hook', () => {
    const openInterval = renderReduxHook(store, () => useSetOpenInterval());
    expect(openInterval).toMatchSnapshot();
  });

  it('handles useCloseInterval Hook', () => {
    const closeInterval = renderReduxHook(store, () => useCloseInterval());
    expect(closeInterval).toMatchSnapshot();
  });

  it('handles getTimeRangeForIntervals', () => {
    const stageIntervals: StageIntervalList = [];

    let timeRange = getTimeRangeForIntervals(stageIntervals);

    expect(timeRange).toBeDefined();
    expect(timeRange.startTimeSecs).toBeUndefined();
    expect(timeRange.endTimeSecs).toBeUndefined();

    stageIntervals.push({
      name: WorkflowDataTypes.interactiveStage.name,
      value: [WorkflowDataTypes.interactiveAnalysisStageInterval]
    });

    timeRange = getTimeRangeForIntervals(stageIntervals);

    expect(timeRange.startTimeSecs).toEqual(
      WorkflowDataTypes.interactiveAnalysisStageInterval.startTime
    );
    expect(timeRange.endTimeSecs).toEqual(
      WorkflowDataTypes.interactiveAnalysisStageInterval.endTime
    );
  });

  it('gets the percentComplete for an AutomaticProcessingStageInterval', () => {
    // Check that function exists
    expect(getPercentComplete).toBeDefined();
    // Check that function returns a percent complete based on the workflow stage
    expect(
      getPercentComplete(
        WorkflowDataTypes.automaticProcessingStageInterval,
        WorkflowDataTypes.workflow
      ).toFixed(2)
    ).toEqual('33.33');
    // Check that the function returns the percentComplete of a ProcessingSequenceInterval
    expect(
      getPercentComplete(
        (WorkflowDataTypes.processingSequenceInterval as unknown) as WorkflowTypes.StageInterval,
        WorkflowDataTypes.workflow
      )
    ).toEqual(WorkflowDataTypes.PERCENT_COMPLETE);
    // Check that the function returns 0 for a non-automatic StageInterval
    expect(
      getPercentComplete(
        WorkflowDataTypes.interactiveAnalysisStageInterval,
        WorkflowDataTypes.workflow
      )
    ).toEqual(0);
  });
});
