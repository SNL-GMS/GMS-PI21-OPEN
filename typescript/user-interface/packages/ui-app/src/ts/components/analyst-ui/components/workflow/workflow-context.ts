import type { WorkflowTypes } from '@gms/common-model';
import * as React from 'react';

import type { OpenAnythingInterval } from './types';

export interface WorkflowContextData {
  readonly staleStartTime: number;
  readonly allActivitiesOpenForSelectedInterval: boolean;
  readonly closeConfirmationPrompt: (
    interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
  ) => void;
  readonly openConfirmationPrompt: (
    interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
  ) => void;
  readonly openAnythingConfirmationPrompt: (interval: OpenAnythingInterval) => void;
}

/**
 * The audible notification context
 */
export const WorkflowContext: React.Context<WorkflowContextData> = React.createContext<
  WorkflowContextData
>(undefined);
