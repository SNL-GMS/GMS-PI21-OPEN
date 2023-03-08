import { WorkflowTypes } from '@gms/common-model';

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
export const PERCENT_COMPLETE = 50;
export const LAST_EXECUTED_STEP_NAME = 'last-step';
const stageDuration = ONE_HOUR_IN_SECONDS;
const interactiveStageName = 'AL1';
const processingStageName = 'Auto Network';

export const interactiveStageMode: WorkflowTypes.StageMode = WorkflowTypes.StageMode.INTERACTIVE;
export const automaticStageMode: WorkflowTypes.StageMode = WorkflowTypes.StageMode.AUTOMATIC;

export const interactiveStage: WorkflowTypes.Stage = {
  name: interactiveStageName,
  mode: interactiveStageMode,
  duration: stageDuration
};

export const processingStage: WorkflowTypes.Stage = {
  name: processingStageName,
  mode: automaticStageMode,
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

export const automaticProcessingStage: WorkflowTypes.AutomaticProcessingStage = {
  name: processingStageName,
  mode: automaticStageMode,
  duration: stageDuration,
  sequences: [processingSequence]
};

const workflowAnalysisMode: WorkflowTypes.AnalysisMode = WorkflowTypes.AnalysisMode.EVENT_REVIEW;

export const stationGroupName = 'station-group';
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

export const interactiveAnalysisStage: WorkflowTypes.InteractiveAnalysisStage = {
  name: interactiveStageName,
  mode: interactiveStageMode,
  duration: stageDuration,
  activities: workflowActivities
};

export const workflow: WorkflowTypes.Workflow = {
  name: workflowName,
  stages: [interactiveAnalysisStage, automaticProcessingStage]
};

export const status: WorkflowTypes.IntervalStatus = WorkflowTypes.IntervalStatus.IN_PROGRESS;
export const notStartedStatus: WorkflowTypes.IntervalStatus.NOT_STARTED =
  WorkflowTypes.IntervalStatus.NOT_STARTED;
export const notCompleteStatus: WorkflowTypes.IntervalStatus.NOT_COMPLETE =
  WorkflowTypes.IntervalStatus.NOT_COMPLETE;
export const completeStatus: WorkflowTypes.IntervalStatus.COMPLETE =
  WorkflowTypes.IntervalStatus.COMPLETE;

export const stageMetricsEventCount = 21;
const stageMetricsAssociatedDetectionCount = 34;
const stageMetricsUnAssociatedSignalDetectionCount = 55;
const stageMetricsMaxMagnitude = 8;

const stageMetrics: WorkflowTypes.StageMetrics = {
  eventCount: stageMetricsEventCount,
  associatedSignalDetectionCount: stageMetricsAssociatedDetectionCount,
  unassociatedSignalDetectionCount: stageMetricsUnAssociatedSignalDetectionCount,
  maxMagnitude: stageMetricsMaxMagnitude
};

export const stageIntervalAuto: WorkflowTypes.StageInterval = {
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
  stageMode: automaticStageMode
};

export const processingSequenceInterval: WorkflowTypes.ProcessingSequenceInterval = {
  intervalId: { startTime: START_TIME, definitionId: { name: processingStageName } },
  name: 'Auto Network Seq',
  stageName: processingStageName,
  status,
  startTime: START_TIME,
  endTime: END_TIME,
  processingStartTime: PROCESSING_START_TIME,
  processingEndTime: PROCESSING_END_TIME,
  storageTime: STORAGE_TIME,
  modificationTime: MODIFICATION_TIME,
  percentAvailable: PERCENT_AVAILABLE,
  comment: COMMENT,
  percentComplete: PERCENT_COMPLETE,
  lastExecutedStepName: LAST_EXECUTED_STEP_NAME
};

export const analysts = ['larry', 'moe', 'curly'];

export const activityInterval: WorkflowTypes.ActivityInterval = {
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

export const automaticProcessingStageInterval: WorkflowTypes.AutomaticProcessingStageInterval = {
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

export const interactiveAnalysisStageInterval: WorkflowTypes.InteractiveAnalysisStageInterval = {
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
