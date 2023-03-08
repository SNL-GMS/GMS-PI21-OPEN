import type { CommonTypes, WorkflowTypes } from '@gms/common-model';
import React from 'react';

import type { HistoryMutations } from '~analyst-ui/components/history/types';

/**
 * The interaction provider redux props.
 */
export interface InteractionProviderReduxProps {
  analysisMode: WorkflowTypes.AnalysisMode;
  openEventId: string;
  currentTimeInterval: CommonTypes.TimeRange;
  historyActionInProgress: number;
  commandPaletteIsVisible: boolean;
  setCommandPaletteVisibility(visibility: boolean): void;
  incrementHistoryActionInProgress();
  decrementHistoryActionInProgress();
}

/**
 * The interaction provider props.
 */
export type InteractionProviderProps = InteractionProviderReduxProps & HistoryMutations;

/**
 * The interaction provider callbacks.
 */
export interface InteractionCallbacks {
  isListenerAttached: boolean;
  toggleCommandPaletteVisibility(): void;
  undo(count: number): void;
  redo(count: number): void;
}

/**
 * The interaction context.
 */
export const InteractionContext = React.createContext<InteractionCallbacks>(undefined);
