/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable react/destructuring-assignment */
import { CacheTypes, LegacyEventTypes } from '@gms/common-model';
import React from 'react';

import type { HistoryContextData } from '../types';
import { HistoryContext } from '../types';
import {
  doesChangeAffectEvent,
  doesEntryAffectEvent,
  filterIrrelevantChanges,
  formatCreateEventHistory,
  getHistoryActionType,
  getIndexOfLastIncludedOfType,
  isAffected
} from '../utils/history-utils';
import { HiddenHistoryEntry, HistoryEntry } from './history-entry';
import { HistoryStackRow } from './history-stack-row';
import { MultipleHistoryEntry } from './multiple-history-entry';
import type { HistoryEntryDisplayFlags, HistoryStackProps, HistoryStackState } from './types';
import { HistoryEntryAction } from './types';

/**
 * A History Stack is a list of undo/redo actions.
 * Requires a HistoryContext object containing the list of History data.
 */

export class HistoryStack extends React.Component<HistoryStackProps, HistoryStackState> {
  // eslint-disable-next-line react/static-property-placement
  public static contextType: React.Context<HistoryContextData> = HistoryContext;

  // eslint-disable-next-line react/static-property-placement
  public declare context: React.ContextType<typeof HistoryContext>;

  /**
   * Renders the component.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className="list list--history">
        {this.generateHistoryEntries(this.context.historyList)}
      </div>
    );
  }

  /**
   * Check if an event has been marked completed
   *
   * @param id the event ID to check
   * @returns whether the event referenced by the ID is complete
   */
  private isEventCompleted(id: string): boolean {
    const theEvent = this.context.eventsInTimeRange.find(event => event.id === id);
    return theEvent && theEvent.status === LegacyEventTypes.EventStatus.Complete;
  }

  /**
   * For each change in this entry, create an array of the flags for showing
   * various states such as color, highlighting, entry type
   *
   * @param change the HistoryChange to use for generating the flags
   * @param index the index where this entry appears in the list
   */
  private generateHistoryEntryDisplayFlags(
    change: CacheTypes.HistoryChange,
    index: number
  ): HistoryEntryDisplayFlags {
    if (change === undefined || change.active === undefined) {
      return undefined;
    }
    const changeIsAffected =
      change &&
      isAffected(change, index, this.context.historyActionIntent, this.context.historyList);
    return change
      ? {
          isAffected: changeIsAffected,
          isAssociated: doesChangeAffectEvent(change, this.context.openEventId),
          isCompleted: this.isEventCompleted(change.eventId),
          isInConflict: change.conflictCreated,
          isOrphaned: change.eventId === null || change.eventId === undefined,
          isEventReset:
            this.context.historyActionIntent &&
            this.context.historyActionIntent.isEventMode &&
            changeIsAffected,
          entryType: change.active ? HistoryEntryAction.undo : HistoryEntryAction.redo
        }
      : undefined;
  }

  /**
   * getHandleMouseEnter is a higher order function that takes a history entry and returns a
   * handler which updates the historyActionIntent so that it reflects which entry is being
   * targeted by a hover or a possible button press.
   *
   * @param entry a History entry for which to generate the mouse enter handler
   * @returns a MouseEvent handler which updates the historyActionIntent
   */
  // eslint-disable-next-line react/sort-comp
  private getHandleMouseEnter(entry: CacheTypes.History) {
    return (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
      this.context.setHistoryActionIntent({
        entryId: entry.id,
        entryType: getHistoryActionType(entry),
        isEventMode: e.ctrlKey || e.metaKey,
        isChangeIncluded:
          // eslint-disable-next-line no-nested-ternary
          e.ctrlKey || e.metaKey
            ? doesEntryAffectEvent(entry, this.context.openEventId)
              ? change => doesChangeAffectEvent(change, this.context.openEventId)
              : () => false
            : () => true
      });
    };
  }

  /**
   * getHandleAction is a higher order function which takes an entry and returns a mouseEvent
   * handler which triggers the correct action based on the entry which is provided
   *
   * @param entry a History entry for which we will create a function which handles the
   * action (undo or redo)
   * @returns a function which handles a mouseEvent and calls the appropriate undo/redo action
   * for the entry to which this is bound
   */
  private getHandleAction(entry: CacheTypes.History) {
    return (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
      let performAction;

      // Determine if we are performing event-level or global undo/redo
      if (e.ctrlKey || e.metaKey) {
        e.preventDefault();
        // eslint-disable-next-line no-nested-ternary
        performAction = this.context.historyActionIntent.isIncluded(entry)
          ? getHistoryActionType(entry) === HistoryEntryAction.undo
            ? () => {
                this.context.undoEventHistoryById(entry.id);
              }
            : () => {
                this.context.redoEventHistoryById(entry.id);
              }
          : undefined;
      } else {
        performAction = this.props.historyActions.get(getHistoryActionType(entry));
      }

      if (performAction) {
        performAction(entry.id);
      }
    };
  }

  /**
   * A simple higher order function that returns a function which
   * resets the history action intent to an undefined state
   */
  private getGenerateMouseOut() {
    return () => {
      this.context.setHistoryActionIntent(undefined);
    };
  }

  /**
   * getHandleKeyDown is a higher order function which generates a function bound to an entry
   * that checks for a meta keypress and toggles the event-revision mode appropriately
   *
   * @param entry A history entry for which to generate a keydown handler function
   * @returns a function for the entry which updates the historyActionIntent to appropriately
   * reflect whether the meta key is held down, which should toggle the event-revision mode
   */
  private getHandleKeyDown(entry: CacheTypes.History) {
    return (e: React.KeyboardEvent<HTMLDivElement>) => {
      if (e.ctrlKey || e.metaKey) {
        const { historyActionIntent } = this.context;
        this.context.setHistoryActionIntent({
          entryId: historyActionIntent ? historyActionIntent.entryId : undefined,
          entryType: historyActionIntent ? historyActionIntent.entryType : undefined,
          isEventMode: true,
          isChangeIncluded:
            // eslint-disable-next-line no-nested-ternary
            e.ctrlKey || e.metaKey
              ? doesEntryAffectEvent(entry, this.context.openEventId)
                ? change => doesChangeAffectEvent(change, this.context.openEventId)
                : () => false
              : () => true
        });
      }
    };
  }

  /**
   * @returns a function that accepts a keyboard event and resets the historyActionIntent
   * to a non-event-reset state.
   */
  private getHandleKeyUp() {
    return (e: React.KeyboardEvent<HTMLDivElement>) => {
      const { historyActionIntent } = this.context;
      if (!e.ctrlKey && !e.metaKey) {
        this.context.setHistoryActionIntent({
          entryId: historyActionIntent ? historyActionIntent.entryId : undefined,
          entryType: historyActionIntent ? historyActionIntent.entryType : undefined,
          isEventMode: false,
          isChangeIncluded: change => this.props.isIncluded(change)
        });
      }
    };
  }

  /**
   * Takes an array of history entry data in and returns an array of HistoryStackRows
   * that contain an undo or redo HistoryEntry.
   *
   * @param entries an array of history entry data to use to generate the
   * list of history entries
   */
  private generateHistoryEntries(entries: CacheTypes.History[]) {
    const nextToRedoIndex = entries.findIndex(ent => ent.redoPriorityOrder === 1);
    const nextToUndoIndex = getIndexOfLastIncludedOfType(
      this.context.historyList,
      this.props.isIncluded,
      HistoryEntryAction.undo
    );
    const areUndoRedoAdjacent = nextToUndoIndex === nextToRedoIndex - 1;

    // Generate our stack of entries by mapping over all entries and creating their components
    return entries.map((historyEntry, index) => {
      const areUndoRedoJoined =
        nextToRedoIndex >= 0 && nextToUndoIndex === nextToRedoIndex && index === nextToRedoIndex;

      let entry = filterIrrelevantChanges(historyEntry);
      const isCreateEventEntry =
        entry.description === CacheTypes.UserActionDescription.CREATE_EVENT;
      if (isCreateEventEntry) {
        entry = formatCreateEventHistory(entry);
      }
      const relevantChanges = entry.changes;

      // Callback functions for this entry's interactions
      const interactionHandlers = {
        handleAction: this.getHandleAction(entry),
        handleMouseEnter: this.getHandleMouseEnter(entry),
        handleMouseOut: this.getGenerateMouseOut(),
        handleKeyDown: this.getHandleKeyDown(entry),
        handleKeyUp: this.getHandleKeyUp()
      };

      // Return statement for the map over all entries
      return (
        <React.Fragment key={`row:${entry.id}`}>
          <HistoryStackRow
            isFirstRow={index === 0}
            undoTarget={index === nextToUndoIndex}
            redoTarget={index === nextToRedoIndex}
            areUndoRedoAdjacent={areUndoRedoAdjacent}
            areUndoRedoJoined={areUndoRedoJoined}
          >
            {/* eslint-disable-next-line no-nested-ternary */}
            {this.props.isIncluded(entry) ? (
              relevantChanges.length > 1 || isCreateEventEntry ? (
                <MultipleHistoryEntry
                  {...entry}
                  historySummary={entry}
                  historyEntryChildren={relevantChanges.map(change => ({
                    message: change.hypothesisChangeInformation.userAction,
                    ...this.generateHistoryEntryDisplayFlags(change, index)
                  }))}
                  {...interactionHandlers}
                />
              ) : (
                <HistoryEntry
                  message={
                    relevantChanges[0]
                      ? relevantChanges[0].hypothesisChangeInformation.userAction
                      : 'undefined'
                  }
                  {...entry}
                  {...this.generateHistoryEntryDisplayFlags(
                    relevantChanges ? relevantChanges[0] : undefined,
                    index
                  )}
                  key={entry.id}
                  {...interactionHandlers}
                />
              )
            ) : (
              <HiddenHistoryEntry
                message={relevantChanges[0].hypothesisChangeInformation.userAction}
              />
            )}
          </HistoryStackRow>
        </React.Fragment>
      );
    });
  }
}
