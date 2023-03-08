import type { TimeRange } from '../common/types';

// ***************************************
// Subscriptions
// ***************************************

export interface QcMasksCreatedSubscription {
  qcMasksCreated: QcMask[];
}

// ***************************************
// Queries
// ***************************************

export interface QcMasksByChannelNameQueryArgs {
  timeRange: TimeRange;
  channelNames: string[];
}

// ***************************************
// Model
// ***************************************

export interface QcMaskVersion {
  startTime: number;
  endTime: number;
  category: string;
  type: string;
  rationale: string;
  version: string;
  channelSegmentIds: string[];
}

export interface QcMask {
  id: string;
  channelName: string;
  currentVersion: QcMaskVersion;
  qcMaskVersions: QcMaskVersion[];
}
