import { WorkflowTypes } from '@gms/common-model';

const iaStage: WorkflowTypes.InteractiveAnalysisStage = {
  name: 'name',
  mode: WorkflowTypes.StageMode.INTERACTIVE,
  duration: 50,
  activities: [
    {
      name: 'name',
      analysisMode: WorkflowTypes.AnalysisMode.EVENT_REVIEW,
      stationGroup: {
        name: 'ALL_1',
        effectiveAt: 1000,
        description: 'Hi'
      }
    }
  ]
};

const autoStage: WorkflowTypes.AutomaticProcessingStage = {
  name: 'name',
  mode: WorkflowTypes.StageMode.AUTOMATIC,
  duration: 5000,
  sequences: [
    {
      name: 'name',
      steps: []
    }
  ]
};

export const workflow: WorkflowTypes.Workflow = {
  name: 'workflow',
  stages: [iaStage, autoStage]
};
