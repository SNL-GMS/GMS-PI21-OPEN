import type { CommonTypes, WorkflowTypes } from '@gms/common-model';

export interface WorkflowState {
  timeRange: CommonTypes.TimeRange;
  stationGroup: WorkflowTypes.StationGroup;
  openIntervalName: string; // e.x AL1
  openActivityNames: string[]; // e.x Event Review
  analysisMode: WorkflowTypes.AnalysisMode;
}
