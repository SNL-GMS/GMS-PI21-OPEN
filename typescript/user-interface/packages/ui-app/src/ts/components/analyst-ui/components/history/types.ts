import type { CacheTypes, CommonTypes, LegacyEventTypes, WorkflowTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import React from 'react';

import type { HistoryEntryAction } from './history-stack/types';

/**
 * Mutations used by the History component
 */
export interface HistoryMutations {
  undoHistory: (args: any) => Promise<void>;
  redoHistory: (args: any) => Promise<void>;
  undoHistoryById: (args: any) => Promise<void>;
  redoHistoryById: (args: any) => Promise<void>;
  undoEventHistory: (args: any) => Promise<void>;
  redoEventHistory: (args: any) => Promise<void>;
  undoEventHistoryById: (args: any) => Promise<void>;
  redoEventHistoryById: (args: any) => Promise<void>;
}

/**
 * History Redux Props
 */
export interface HistoryReduxProps {
  // Passed in from golden-layout
  glContainer?: GoldenLayout.Container;
  historyActionInProgress: number;
  openEventId: string;
  currentTimeInterval: CommonTypes.TimeRange;
  analysisMode: WorkflowTypes.AnalysisMode;
  keyPressActionQueue: AnalystWorkspaceTypes.AnalystKeyAction[];
  historyQuery: any;
  setKeyPressActionQueue(actions: AnalystWorkspaceTypes.AnalystKeyAction[]): void;
  incrementHistoryActionInProgress();
  decrementHistoryActionInProgress();
}

/**
 * History Props
 */
export type HistoryProps = HistoryReduxProps & HistoryMutations;

/**
 * State for the HistoryComponent
 */
export interface HistoryState {
  actionIntentPointer: HistoryEntryPointer;
}
/**
 * History Panel Props
 * optional non-ideal-state element to render in the children history-stacks
 */
export interface HistoryPanelProps {
  nonIdealState?: NonIdealHistoryState;
}

/**
 * Keeps track of which HistoryEntryAction is being target for an undo/redo action
 * and provides a check function isIncluded for checking if other actions would be
 * included based on whether the target is an event or global undo/redo.
 */
export interface HistoryEntryPointer {
  entryId: string;
  entryType: HistoryEntryAction;
  isEventMode: boolean;
  isIncluded?(entry: CacheTypes.History): boolean;
  isChangeIncluded(change: CacheTypes.HistoryChange): boolean;
}

/**
 * The type for the data used by history context
 */
export interface HistoryContextData {
  glContainer: GoldenLayout.Container;
  historyList: CacheTypes.History[];
  historyActionIntent: HistoryEntryPointer;
  openEventId: string;
  eventsInTimeRange: LegacyEventTypes.Event[];
  historyActionInProgress: number;
  redoEventHistory(quantity?: number): void;
  redoEventHistoryById(id: string): void;
  redoHistory(quantity?: number): void;
  redoHistoryById(id: string): void;
  setHistoryActionIntent(pointer: HistoryEntryPointer): void;
  undoEventHistory(quantity?: number): void;
  undoEventHistoryById(id: string): void;
  undoHistory(quantity?: number): void;
  undoHistoryById(id: string): void;
}

/**
 * History Context to provide access to these high-level props to the History-Component tree.
 * Here, we instantiate the Context variable and set up the defaults for the history context
 */
export const HistoryContext: React.Context<HistoryContextData> = React.createContext<
  HistoryContextData
>({
  glContainer: undefined,
  openEventId: undefined,
  eventsInTimeRange: [],
  undoEventHistory: undefined,
  undoEventHistoryById: undefined,
  redoEventHistory: undefined,
  redoEventHistoryById: undefined,
  undoHistory: undefined,
  undoHistoryById: undefined,
  redoHistory: undefined,
  redoHistoryById: undefined,
  historyActionIntent: undefined,
  setHistoryActionIntent: undefined,
  historyActionInProgress: 0,
  historyList: []
});

export interface NonIdealHistoryState {
  loadingEvent: JSX.Element;
  loadingHistory: JSX.Element;
}
