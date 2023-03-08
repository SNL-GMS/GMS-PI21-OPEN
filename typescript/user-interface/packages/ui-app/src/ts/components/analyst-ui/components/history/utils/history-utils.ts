import { CacheTypes } from '@gms/common-model';

import { messageConfig } from '~analyst-ui/config/message-config';

import { HistoryEntryAction } from '../history-stack/types';
import type { HistoryEntryPointer } from '../types';

/**
 * Gets the number of history entry entries that can be undone
 *
 * @param historyList the list to check
 * @returns the number of entry entries that can be undone
 */
export function getNumberOfUndos(historyList: CacheTypes.History[]): number {
  // eslint-disable-next-line @typescript-eslint/no-use-before-define
  return getNumberOfEntriesOfType(historyList, HistoryEntryAction.undo);
}

/**
 * Gets the number of history entry entries that can be redone
 *
 * @param historyList the list to check
 * @returns the number of entry entries that can be redone
 */
export function getNumberOfRedos(historyList: CacheTypes.History[]): number {
  // eslint-disable-next-line @typescript-eslint/no-use-before-define
  return getNumberOfEntriesOfType(historyList, HistoryEntryAction.redo);
}

/**
 * Gets the number of undos or redos in a history entry list
 *
 * @param historyList The list of History Summaries to check
 * @param entryType Are we checking for an undo or a redo
 * @returns the number of entry entries of the given type
 */
function getNumberOfEntriesOfType(
  historyList: CacheTypes.History[],
  entryType: HistoryEntryAction
): number {
  return historyList.reduce(
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    (accum: number, entry) => (canPerformAction(entry.changes, entryType) ? accum + 1 : accum),
    0
  );
}

/* eslint-disable @typescript-eslint/indent */
/**
 * Check if a history entry action would affect an event
 *
 * @param entry the entry entry to check
 * @param eventId the open event id to check against
 * @return true if the history entry entry affects the provided event id
 */
export const doesChangeAffectEvent = (change: CacheTypes.HistoryChange, eventId: string): boolean =>
  change && eventId && change.eventId === eventId;

/**
 * Check if a history entry action would affect an event
 *
 * @param entry the entry entry to check
 * @param eventId the open event id to check against
 * @return true if the history entry entry affects the provided event id
 */
export const doesEntryAffectEvent = (entry: CacheTypes.History, eventId: string): boolean =>
  entry.changes.reduce(
    (doesMatch: boolean, change) => doesMatch || change.eventId === eventId,
    false
  );

/**
 * Gets the last index that matches the type and the isIncluded check provided
 *
 * @param historyList a list of History entries to check
 * @param eventId the event ID to check against
 */
export function getIndexOfLastIncludedOfType(
  historyList: CacheTypes.History[],
  isIncluded: (entry: CacheTypes.History) => boolean,
  entryType: HistoryEntryAction
): number | undefined {
  if (historyList === undefined || isIncluded === undefined || !entryType) {
    return undefined;
  }
  let lastIndex = historyList.length - 1;
  while (
    lastIndex >= 0 &&
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    (getHistoryActionType(historyList[lastIndex]) !== entryType ||
      !isIncluded(historyList[lastIndex]))
  ) {
    // eslint-disable-next-line no-plusplus
    lastIndex--;
  }
  return lastIndex;
}

export function getLastIncludedOfType(
  historyList: CacheTypes.History[],
  isIncluded: (entry: CacheTypes.History) => boolean,
  entryType: HistoryEntryAction
): CacheTypes.History {
  return historyList !== undefined && isIncluded && entryType
    ? historyList[getIndexOfLastIncludedOfType(historyList, isIncluded, entryType)]
    : undefined;
}

export function getNextOrderedRedo(historyList: CacheTypes.History[]): CacheTypes.History {
  if (!historyList) {
    return undefined;
  }
  return historyList.find(entry => entry.redoPriorityOrder === 1);
}

/**
 * Check if a list contains a change that can have the action performed
 *
 * @param changes a list of history changes to check
 * @param entryType are we looking to see if we can undo or redo?
 * @returns whether you can perform an undo or redo action on the list of changes provided
 */
export function canPerformAction(
  changes: CacheTypes.HistoryChange[],
  entryType: HistoryEntryAction
): boolean {
  if (!changes || !entryType) {
    return undefined;
  }
  return changes.reduce(
    (accum, change) =>
      entryType === HistoryEntryAction.undo ? accum || change.active : accum || !change.active,
    false
  );
}

/**
 * Determines if a history entry is an undo or a redo action. If both, returns undo.
 *
 * @param entry a History to check
 * @returns HistoryEntryAction.undo or HistoryEntryAction.redo
 */
export function getHistoryActionType(entry: CacheTypes.History): HistoryEntryAction {
  return getNumberOfUndos([entry]) > 0 ? HistoryEntryAction.undo : HistoryEntryAction.redo;
}

/**
 * Find a history entry with the passed in ID
 *
 * @param historyList an array of History objects
 * @param historyId the ID to find
 * @returns the index of the entry, or -1 if not found
 */
export function getHistoryIndex(historyList: CacheTypes.History[], historyId: string): number {
  if (!historyList || !historyId) {
    return -1;
  }
  let foundIndex = -1;
  historyList.forEach((entry, index) => {
    if (entry.id === historyId) {
      foundIndex = index;
    }
  });
  return foundIndex;
}

/**
 * Will a history change be undone/redone by an action? This is used to
 * highlight the history entries that will be affected by an undo/redo
 * action.
 *
 * @param change the change which is being targeted (hovered)
 * @param index the index of the entry which is being targeted (hovered over)
 * @param hoverEffectIntent contains info on which action is targeted and how to filter entries
 * @param historyList the list of history entries to check
 * @returns whether the history entry would be undone/redone by an action
 */
export function isAffected(
  change: CacheTypes.HistoryChange,
  index: number,
  hoverEffectIntent: HistoryEntryPointer,
  historyList: CacheTypes.History[]
): boolean {
  const entryType = change.active ? HistoryEntryAction.undo : HistoryEntryAction.redo;
  if (
    hoverEffectIntent === undefined ||
    hoverEffectIntent.entryId === undefined ||
    hoverEffectIntent.entryType === undefined ||
    entryType !== hoverEffectIntent.entryType ||
    !hoverEffectIntent.isChangeIncluded(change)
  ) {
    return false;
  }
  const hoverIndex = getHistoryIndex(historyList, hoverEffectIntent.entryId);
  return entryType === HistoryEntryAction.undo ? hoverIndex <= index : hoverIndex >= index;
}

/**
 * Filters a list of HistoryChanges based on the map of historyFilters in message config.
 * Use to remove undesired messages that are returned by the gateway.
 *
 * @param historyEntry a history entry
 * @returns updated history entry with changes filtered out
 */
export function filterIrrelevantChanges(historyEntry: CacheTypes.History): CacheTypes.History {
  if (!historyEntry) {
    return undefined;
  }
  const { changes } = historyEntry;
  const filteredChanges = changes.filter(change => {
    const shouldExclude = messageConfig.historyFilters.get(
      change.hypothesisChangeInformation.userAction
    );
    return change && change.hypothesisChangeInformation && !shouldExclude;
  });

  return {
    id: historyEntry.id,
    changes: filteredChanges,
    description: historyEntry.description,
    redoPriorityOrder: historyEntry.redoPriorityOrder
  };
}

/**
 * Prepares the history entry to display for a create event action. This will
 * filter out event creation changes and use the event creation user action
 * as the history description.
 *
 * @param changes a create event history change item
 * @returns updated history with the event change filtered out and the top level description updated
 */
export function formatCreateEventHistory(historyEntry: CacheTypes.History): CacheTypes.History {
  if (!historyEntry) {
    return undefined;
  }
  const { changes } = historyEntry;
  let entryDescription = historyEntry.description;
  const filteredChanges = changes.filter(change => {
    // If the change was an event creation - filter it out and use the user action as the top level history change
    const isCreateEventChange = change.hypothesisChangeInformation.userAction.includes(
      CacheTypes.UserActionDescription.CREATE_EVENT
    );
    if (isCreateEventChange) {
      entryDescription = change.hypothesisChangeInformation.userAction;
    }
    return !isCreateEventChange;
  });

  return {
    id: historyEntry.id,
    changes: filteredChanges,
    description: entryDescription,
    redoPriorityOrder: historyEntry.redoPriorityOrder
  };
}
