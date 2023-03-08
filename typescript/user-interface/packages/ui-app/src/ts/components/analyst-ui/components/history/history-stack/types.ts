import type { CacheTypes } from '@gms/common-model';

/**
 * HistoryStack State
 */
export interface HistoryStackState {
  isEventMode: boolean;
}

/**
 * The types of history entry actions, namely undo and redo.
 */
export enum HistoryEntryAction {
  undo = 'Undo',
  redo = 'Redo'
}

export type HistoryEntryComponentData = HistoryInteractionHandlers & HistoryEntryDisplayFlags;

type IntermediateHistoryEntryProps = HistoryEntryComponentData & CacheTypes.History;

/**
 * The data for a single history list element
 */
export interface HistoryEntryProps extends IntermediateHistoryEntryProps {
  message: string;
}

/**
 * HistoryStack Props
 */
export interface HistoryStackProps {
  historyActions: Map<string, (id: string) => void>;
  isIncluded(summary: CacheTypes.History | CacheTypes.HistoryChange): boolean;
}

export interface HistoryInteractionHandlers {
  handleAction?(e: React.MouseEvent<HTMLDivElement, MouseEvent>): void;
  handleMouseEnter?(e: React.MouseEvent<HTMLDivElement, MouseEvent>): void;
  handleMouseOut?(e: React.MouseEvent<HTMLDivElement, MouseEvent>): void;
  handleKeyDown?(e: React.KeyboardEvent<HTMLDivElement>): void;
  handleKeyUp?(e: React.KeyboardEvent<HTMLDivElement>): void;
}

export interface HistoryEntryDisplayFlags {
  isAffected?: boolean;
  isAssociated?: boolean;
  isCompleted?: boolean;
  isInConflict?: boolean;
  isOrphaned?: boolean;
  isEventReset?: boolean;
  isChild?: boolean;
  entryType?: HistoryEntryAction;
}

export interface MultipleHistoryEntryProps extends CacheTypes.History {
  description: string;
  historySummary: CacheTypes.History;
  historyEntryChildren: GenericHistoryEntryProps[];
}

export interface GenericHistoryEntryProps extends HistoryEntryComponentData {
  message: string;
}
