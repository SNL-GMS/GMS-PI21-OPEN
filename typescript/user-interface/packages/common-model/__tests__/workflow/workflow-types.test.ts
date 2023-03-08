import * as WorkflowTypes from '../../src/ts/workflow/types';

const workflowName = 'gms';
const ONE_HOUR_IN_SECONDS = 3600;
const FIVE_MINUTES_IN_SECONDS = 300;
const ONE_MINUTE_IN_SECONDS = 60;
const EFFECTIVE_TIME = 1622048927;
const START_TIME = 1622053587;
const END_TIME = START_TIME + ONE_HOUR_IN_SECONDS;
const PROCESSING_START_TIME = END_TIME + FIVE_MINUTES_IN_SECONDS;
const PROCESSING_END_TIME = PROCESSING_START_TIME + ONE_MINUTE_IN_SECONDS;
const STORAGE_TIME = PROCESSING_END_TIME + ONE_MINUTE_IN_SECONDS;
const MODIFICATION_TIME = STORAGE_TIME + ONE_MINUTE_IN_SECONDS;
const PERCENT_AVAILABLE = 100;
const COMMENT = 'interval example';
const PERCENT_COMPLETE = 50;
const LAST_EXECUTED_STEP_NAME = 'last-step';
const stageDuration = ONE_HOUR_IN_SECONDS;
const interactiveStageName = 'AL1';
const processingStageName = 'Auto Network';

const interactiveStageMode: WorkflowTypes.StageMode = WorkflowTypes.StageMode.INTERACTIVE;
const automaticStageMode: WorkflowTypes.StageMode = WorkflowTypes.StageMode.AUTOMATIC;

const interactiveStage: WorkflowTypes.Stage = {
  name: interactiveStageName,
  mode: interactiveStageMode,
  duration: stageDuration
};

const processingStepName = 'AAA';
const processingStepName2 = 'BBB';
const processingStepName3 = 'CCC';

const processingStep: WorkflowTypes.ProcessingStep = {
  name: processingStepName
};
const processingStep2: WorkflowTypes.ProcessingStep = {
  name: processingStepName2
};
const processingStep3: WorkflowTypes.ProcessingStep = {
  name: processingStepName3
};

const processingSteps: WorkflowTypes.ProcessingStep[] = [
  processingStep,
  processingStep2,
  processingStep3
];

const processingSequence: WorkflowTypes.ProcessingSequence = {
  name: 'Auto Post-AL1 Seq',
  steps: processingSteps
};

const automaticProcessingStage: WorkflowTypes.AutomaticProcessingStage = {
  name: processingStageName,
  mode: automaticStageMode,
  duration: stageDuration,
  sequences: [processingSequence]
};

const workflowAnalysisMode: WorkflowTypes.AnalysisMode = WorkflowTypes.AnalysisMode.EVENT_REVIEW;

const stationGroupName = 'station-group';
const stationGroupDescription = 'station group';
const workflowStationGroup: WorkflowTypes.StationGroup = {
  effectiveAt: EFFECTIVE_TIME,
  name: stationGroupName,
  description: stationGroupDescription
};

const workflowActivity: WorkflowTypes.Activity = {
  name: 'Event Review',
  analysisMode: workflowAnalysisMode,
  stationGroup: workflowStationGroup
};

const workflowActivities: WorkflowTypes.Activity[] = [workflowActivity];

const interactiveAnalysisStage: WorkflowTypes.InteractiveAnalysisStage = {
  name: interactiveStageName,
  mode: interactiveStageMode,
  duration: stageDuration,
  activities: workflowActivities
};

const workflow: WorkflowTypes.Workflow = {
  name: workflowName,
  stages: [interactiveAnalysisStage, automaticProcessingStage]
};

const status: WorkflowTypes.IntervalStatus = WorkflowTypes.IntervalStatus.IN_PROGRESS;

const stageMetricsEventCount = 21;
const stageMetricsAssociatedDetectionCount = 34;
const stageMetricsUnAssociatedSignalDetectionCount = 55;
const stageMetricsMaxMagnitude = 8;

const stageMetrics: WorkflowTypes.StageMetrics = {
  eventCount: stageMetricsEventCount,
  associatedSignalDetectionCount: stageMetricsAssociatedDetectionCount,
  unassociatedSignalDetectionCount: stageMetricsUnAssociatedSignalDetectionCount,
  maxMagnitude: stageMetricsMaxMagnitude
};

const processingSequenceInterval: WorkflowTypes.ProcessingSequenceInterval = {
  intervalId: { startTime: START_TIME, definitionId: { name: processingStageName } },
  name: 'Auto Network Seq',
  stageName: processingStageName,
  status,
  endTime: END_TIME,
  startTime: START_TIME,
  processingStartTime: PROCESSING_START_TIME,
  processingEndTime: PROCESSING_END_TIME,
  storageTime: STORAGE_TIME,
  modificationTime: MODIFICATION_TIME,
  percentAvailable: PERCENT_AVAILABLE,
  comment: COMMENT,
  percentComplete: PERCENT_COMPLETE,
  lastExecutedStepName: LAST_EXECUTED_STEP_NAME
};

const analysts = ['larry', 'moe', 'curly'];

const activityInterval: WorkflowTypes.ActivityInterval = {
  intervalId: { startTime: START_TIME, definitionId: { name: processingStageName } },
  name: 'Event Review',
  status,
  endTime: END_TIME,
  startTime: START_TIME,
  processingStartTime: PROCESSING_START_TIME,
  processingEndTime: PROCESSING_END_TIME,
  storageTime: STORAGE_TIME,
  modificationTime: MODIFICATION_TIME,
  percentAvailable: PERCENT_AVAILABLE,
  comment: COMMENT,
  activeAnalysts: analysts,
  stageName: interactiveStageName
};

const automaticProcessingStageInterval: WorkflowTypes.AutomaticProcessingStageInterval = {
  intervalId: { startTime: START_TIME, definitionId: { name: processingStageName } },
  name: processingStageName,
  status,
  endTime: END_TIME,
  startTime: START_TIME,
  processingStartTime: PROCESSING_START_TIME,
  processingEndTime: PROCESSING_END_TIME,
  storageTime: STORAGE_TIME,
  modificationTime: MODIFICATION_TIME,
  percentAvailable: PERCENT_AVAILABLE,
  comment: COMMENT,
  stageMetrics,
  stageMode: automaticStageMode,
  sequenceIntervals: [processingSequenceInterval]
};

const interactiveAnalysisStageInterval: WorkflowTypes.InteractiveAnalysisStageInterval = {
  intervalId: { startTime: START_TIME, definitionId: { name: processingStageName } },
  name: interactiveStageName,
  status,
  endTime: END_TIME,
  startTime: START_TIME,
  processingStartTime: PROCESSING_START_TIME,
  processingEndTime: PROCESSING_END_TIME,
  storageTime: STORAGE_TIME,
  modificationTime: MODIFICATION_TIME,
  percentAvailable: PERCENT_AVAILABLE,
  comment: COMMENT,
  stageMetrics,
  stageMode: interactiveStageMode,
  activityIntervals: [activityInterval]
};

describe('Workflow Type Definitions', () => {
  it('expect StageMode to be defined', () => {
    expect(WorkflowTypes.StageMode).toBeDefined();
    expect(WorkflowTypes.StageMode.INTERACTIVE).toBe('INTERACTIVE');
    expect(WorkflowTypes.StageMode.AUTOMATIC).toBe('AUTOMATIC');
    expect(interactiveStageMode).toBeDefined();
  });

  it('expect Stage to be defined', () => {
    expect(interactiveStage).toBeDefined();
    expect(interactiveStage.duration).toBe(stageDuration);
    expect(interactiveStage.name).toEqual(interactiveStageName);
    expect(interactiveStage.mode).toEqual(interactiveStageMode);
  });

  it('expect ProcessingStep to be defined', () => {
    expect(processingStep).toBeDefined();
    expect(processingStep.name).toEqual(processingStepName);
    expect(processingStepName2).toBeDefined();
    expect(processingStepName3).toBeDefined();
  });

  it('expect ProcessingSequence to be defined', () => {
    expect(processingSequence).toBeDefined();
    expect(processingSequence.name).toEqual('Auto Post-AL1 Seq');
    expect(processingSequence.steps).toHaveLength(3);
  });

  it('expect AutomaticProcessingStage to be defined', () => {
    expect(automaticProcessingStage).toBeDefined();
    expect(automaticProcessingStage.sequences).toHaveLength(1);
  });

  it('expect AnalysisMode to be defined', () => {
    expect(workflowAnalysisMode).toBeDefined();
    expect(workflowAnalysisMode).toEqual(WorkflowTypes.AnalysisMode.EVENT_REVIEW);
  });

  it('expect StationGroup to be defined', () => {
    expect(workflowStationGroup).toBeDefined();
    expect(workflowStationGroup.name).toEqual(stationGroupName);
    expect(workflowStationGroup.description).toEqual(stationGroupDescription);
  });

  it('expect Activity to be defined', () => {
    expect(workflowActivity).toBeDefined();
  });

  it('expect InteractiveAnalysisStage to be defined', () => {
    expect(interactiveAnalysisStage).toBeDefined();
  });

  it('expect Workflow to be defined', () => {
    expect(workflow).toBeDefined();
  });

  it('expect IntervalStatus to be defined', () => {
    expect(status).toBeDefined();
    expect(status).toEqual(WorkflowTypes.IntervalStatus.IN_PROGRESS);
  });

  it('expect StageMetrics to be defined', () => {
    expect(stageMetrics).toBeDefined();
  });

  it('expect Interactive StageInterval to be defined', () => {
    expect(interactiveAnalysisStageInterval).toBeDefined();
    expect(interactiveAnalysisStageInterval.endTime).toEqual(END_TIME);
  });

  it('expect Automatic StageInterval to be defined', () => {
    expect(automaticProcessingStageInterval).toBeDefined();
    expect(automaticProcessingStageInterval.endTime).toEqual(END_TIME);
  });

  it('expect ProcessingSequenceInterval to be defined', () => {
    expect(processingSequenceInterval).toBeDefined();
    expect(processingSequenceInterval.percentComplete).toEqual(PERCENT_COMPLETE);
  });

  it('expect ActivityInterval to be defined', () => {
    expect(activityInterval).toBeDefined();
    expect(activityInterval.activeAnalysts).toHaveLength(3);
  });

  it('expect AutomaticProcessingStageInterval to be defined', () => {
    expect(automaticProcessingStageInterval).toBeDefined();
    expect(automaticProcessingStageInterval.sequenceIntervals).toHaveLength(1);
  });

  it('expect InteractiveAnalysisStageInterval to be defined', () => {
    expect(interactiveAnalysisStageInterval).toBeDefined();
    expect(interactiveAnalysisStageInterval.activityIntervals).toHaveLength(1);
  });

  it('expect isInteractiveAnalysisStageInterval to be correct', () => {
    expect(WorkflowTypes.isInteractiveAnalysisStageInterval).toBeDefined();
    expect(
      WorkflowTypes.isInteractiveAnalysisStageInterval(interactiveAnalysisStageInterval)
    ).toBeTruthy();
    expect(
      WorkflowTypes.isInteractiveAnalysisStageInterval(automaticProcessingStageInterval)
    ).toBeFalsy();
  });

  it('expect isAutomaticProcessingStageInterval to be correct', () => {
    expect(WorkflowTypes.isAutomaticProcessingStageInterval).toBeDefined();
    expect(
      WorkflowTypes.isAutomaticProcessingStageInterval(automaticProcessingStageInterval)
    ).toBeTruthy();
    expect(
      WorkflowTypes.isAutomaticProcessingStageInterval(interactiveAnalysisStageInterval)
    ).toBeFalsy();
  });

  it('expect isProcessingSequenceInterval to be correct', () => {
    expect(WorkflowTypes.isProcessingSequenceInterval).toBeDefined();
    expect(WorkflowTypes.isProcessingSequenceInterval(processingSequenceInterval)).toBeTruthy();
    expect(
      WorkflowTypes.isProcessingSequenceInterval(automaticProcessingStageInterval)
    ).toBeFalsy();
  });

  it('functions are exported and check stage', () => {
    expect(WorkflowTypes.isAutomaticProcessingStage).toBeDefined();
    expect(WorkflowTypes.isInteractiveAnalysisStage).toBeDefined();
    const oneHourInSeconds = 3600;
    const twoHoursInSeconds = 7200;
    const aStage: WorkflowTypes.Stage = {
      name: 'astage',
      mode: WorkflowTypes.StageMode.AUTOMATIC,
      duration: twoHoursInSeconds
    };
    const iStage: WorkflowTypes.Stage = {
      name: 'istage',
      mode: WorkflowTypes.StageMode.INTERACTIVE,
      duration: oneHourInSeconds
    };

    const aStageCall = WorkflowTypes.isInteractiveAnalysisStage(aStage);
    expect(aStageCall).toBeFalsy();

    const iStageCall = WorkflowTypes.isInteractiveAnalysisStage(iStage);
    expect(iStageCall).toBeTruthy();

    const aStageCall2 = WorkflowTypes.isAutomaticProcessingStage(aStage);
    expect(aStageCall2).toBeTruthy();

    const iStageCall2 = WorkflowTypes.isAutomaticProcessingStage(iStage);
    expect(iStageCall2).toBeFalsy();

    const aIntervalCall = WorkflowTypes.isActivityInterval(activityInterval);
    expect(aIntervalCall).toBeTruthy();

    const aStageInterval = WorkflowTypes.isStageInterval(interactiveAnalysisStageInterval);
    expect(aStageInterval).toBeTruthy();
  });
});
