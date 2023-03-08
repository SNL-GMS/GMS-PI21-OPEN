import type { CommonTypes, WorkflowTypes } from '@gms/common-model';

export interface OpenAnythingInterval {
  readonly timeRange: CommonTypes.TimeRange;
  readonly stationGroup: WorkflowTypes.StationGroup;
  readonly openIntervalName: string;
}
