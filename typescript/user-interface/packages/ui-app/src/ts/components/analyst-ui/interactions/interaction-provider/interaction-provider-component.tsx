/* eslint-disable react/destructuring-assignment */
import React from 'react';

import type { InteractionCallbacks, InteractionProviderProps } from './types';
import { InteractionContext } from './types';

/**
 * Provides one implementation of redux capabilities and provides them to child components via a context
 */
export function InteractionProvider({
  children,
  currentTimeInterval,
  commandPaletteIsVisible,
  setCommandPaletteVisibility,
  undoHistory,
  incrementHistoryActionInProgress,
  decrementHistoryActionInProgress,
  redoHistory
}: React.PropsWithChildren<InteractionProviderProps>) {
  const isIntervalOpened = currentTimeInterval !== undefined && currentTimeInterval !== null;
  const undo = React.useCallback(() => {
    if (isIntervalOpened && undoHistory) {
      incrementHistoryActionInProgress();
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      undoHistory({ variables: { numberOfItems: 1 } })
        .then(() => {
          decrementHistoryActionInProgress();
        })
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        .catch(() => {});
    }
  }, [
    decrementHistoryActionInProgress,
    incrementHistoryActionInProgress,
    isIntervalOpened,
    undoHistory
  ]);

  /**
   * Redo a history change.
   *
   * @param count the number of changes to redo
   */
  const redo = React.useCallback(() => {
    if (isIntervalOpened && redoHistory) {
      incrementHistoryActionInProgress();
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      redoHistory({ variables: { numberOfItems: 1 } })
        .then(() => {
          decrementHistoryActionInProgress();
        })
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        .catch(() => {});
    }
  }, [
    decrementHistoryActionInProgress,
    incrementHistoryActionInProgress,
    isIntervalOpened,
    redoHistory
  ]);

  const toggleCommandPaletteVisibility = React.useCallback(() => {
    setCommandPaletteVisibility(!commandPaletteIsVisible);
  }, [commandPaletteIsVisible, setCommandPaletteVisibility]);

  const interactionContextData: InteractionCallbacks = React.useMemo(
    () => ({
      isListenerAttached: false,
      toggleCommandPaletteVisibility,
      undo,
      redo
    }),
    [redo, toggleCommandPaletteVisibility, undo]
  );

  return (
    <InteractionContext.Provider value={interactionContextData}>
      {children}
    </InteractionContext.Provider>
  );
}
