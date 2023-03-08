/* eslint-disable react/destructuring-assignment */
import { IconNames } from '@blueprintjs/icons';
import type { CacheTypes } from '@gms/common-model';
import { DeprecatedToolbar, DeprecatedToolbarTypes } from '@gms/ui-core-components';
import React from 'react';

import { messageConfig } from '~analyst-ui/config/message-config';

import { HistoryStack } from './history-stack/history-stack';
import { HistoryEntryAction } from './history-stack/types';
import type { HistoryContextData, HistoryPanelProps } from './types';
import { HistoryContext } from './types';
import {
  getLastIncludedOfType,
  getNextOrderedRedo,
  getNumberOfRedos,
  getNumberOfUndos
} from './utils/history-utils';

const MARGIN_FOR_TOOLBAR_PX = 14;

/**
 * Higher order function that generates a handler for mouse enter over the provided history entry
 *
 * @param context the history context which exposes the setHistoryActionIntent function and the history list
 * @param isIncluded a function to determine if the events/changes are included in the intended action
 * @returns a function which sets the target and conditions for the historyActionIntent (what should happen on
 * hover)
 */
export function getHandleUndoMouseEnter(
  context: HistoryContextData,
  isIncluded: (entry: CacheTypes.History | CacheTypes.HistoryChange) => boolean
) {
  return (): void => {
    const actionIntentTarget = getLastIncludedOfType(
      context.historyList,
      isIncluded,
      HistoryEntryAction.undo
    );
    if (actionIntentTarget) {
      context.setHistoryActionIntent({
        entryId: actionIntentTarget.id,
        entryType: HistoryEntryAction.undo,
        isChangeIncluded: isIncluded,
        isEventMode: false
      });
    }
  };
}

/**
 * handleRedoMouseEnter is a higher order function which generates the function that
 * sets the action intent for hovering over the redo button
 *
 * @param context the context which exposes the settHistoryActionIntent function and the
 * history list
 * @returns a function which sets the action intent to target the next redoable action
 */
export function getHandleRedoMouseEnter(context: HistoryContextData) {
  return (): void => {
    const actionIntentTarget = getNextOrderedRedo(context.historyList);
    if (actionIntentTarget) {
      context.setHistoryActionIntent({
        entryId: actionIntentTarget.id,
        entryType: HistoryEntryAction.redo,
        isEventMode: false,
        isChangeIncluded: change =>
          actionIntentTarget.changes.reduce(
            (accum, parentChange) => accum || parentChange.id === change.id,
            false
          )
      });
    }
  };
}

/**
 * History Panel holds the Event history stack and the global history stack side-by-side
 *
 * @param props the history panel props including a non-ideal state which is passed
 * to the panel's children
 */
// eslint-disable-next-line react/function-component-definition
export const HistoryPanel: React.FunctionComponent<HistoryPanelProps> = () => {
  const context = React.useContext(HistoryContext);
  const { historyList } = context;
  const isIncluded = (): boolean => true;

  // When historyList changes, this will run before re-render
  React.useEffect(() => {
    // scroll to the bottom
    const containerHeight =
      context && context.glContainer ? context.glContainer.height : window.innerHeight;
    document.querySelectorAll('.history-panel').forEach((panel: HTMLElement) => {
      if (!panel.scroll) return;
      panel.scroll({ top: panel.scrollHeight - containerHeight, left: 0, behavior: 'smooth' });
    });
    // !FIX ESLINT DO NOT USE REACT HOOK HOOKS IN CONDITIONAL
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [historyList]);

  const undo = (quantity = 1) => {
    context.undoHistory(quantity);
  };
  const redo = (quantity = 1) => {
    context.redoHistory(quantity);
  };

  // A mapping of history actions types (undo/redo) to functions that call mutations
  const globalHistoryActions: Map<string, (id: string) => void> = new Map([
    [
      HistoryEntryAction.undo,
      (id: string) => {
        context.undoHistoryById(id);
      }
    ],
    [
      HistoryEntryAction.redo,
      (id: string) => {
        context.redoHistoryById(id);
      }
    ]
  ]);

  const undoButton: DeprecatedToolbarTypes.ButtonItem = {
    rank: 1,
    type: DeprecatedToolbarTypes.ToolbarItemType.Button,
    tooltip: messageConfig.tooltipMessages.history.undoButtonAction,
    label: 'Undo',
    icon: IconNames.UNDO,
    widthPx: 80,
    onClick: () => {
      undo();
    },
    onMouseEnter: getHandleUndoMouseEnter(context, isIncluded),
    onMouseOut: () => context.setHistoryActionIntent(undefined),
    disabled:
      context.historyList === undefined ||
      getNumberOfUndos(context.historyList.filter(isIncluded)) === 0
  };

  const redoButton: DeprecatedToolbarTypes.ButtonItem = {
    rank: 2,
    type: DeprecatedToolbarTypes.ToolbarItemType.Button,
    tooltip: messageConfig.tooltipMessages.history.redoButtonAction,
    label: 'Redo',
    icon: IconNames.REDO,
    widthPx: 80,
    onClick: () => {
      redo();
    },
    onMouseEnter: getHandleRedoMouseEnter(context),
    onMouseOut: () => context.setHistoryActionIntent(undefined),
    disabled:
      context.historyList === undefined ||
      getNumberOfRedos(context.historyList.filter(isIncluded)) === 0
  };

  const buttonGroup: DeprecatedToolbarTypes.ButtonGroupItem = {
    rank: 1,
    type: DeprecatedToolbarTypes.ToolbarItemType.ButtonGroup,
    tooltip: 'Undo or Redo',
    label: 'Undo or Redo',
    buttons: [undoButton, redoButton]
  };

  const maybeSpinner: DeprecatedToolbarTypes.LoadingSpinnerItem = {
    rank: 1,
    type: DeprecatedToolbarTypes.ToolbarItemType.LoadingSpinner,
    tooltip: 'undo or redo in progress',
    label: '',
    itemsToLoad: context.historyActionInProgress,
    hideTheWordLoading: true,
    hideOutstandingCount: true,
    onlyShowIcon: true
  };

  return (
    <div className="history-panel" data-cy="history">
      <div className="list-toolbar-wrapper">
        <DeprecatedToolbar
          toolbarWidthPx={
            context && context.glContainer
              ? context.glContainer.width - MARGIN_FOR_TOOLBAR_PX
              : window.innerWidth - MARGIN_FOR_TOOLBAR_PX
          }
          itemsRight={[buttonGroup]}
          itemsLeft={[maybeSpinner]}
        />
      </div>
      <HistoryStack historyActions={globalHistoryActions} isIncluded={isIncluded} />
    </div>
  );
};
