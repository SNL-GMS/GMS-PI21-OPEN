/* eslint-disable react/destructuring-assignment */
import { Timer } from '@gms/common-util';
import { classList, useSearchResultSelectionManager, useVanillaSearch } from '@gms/ui-util';
import uniqBy from 'lodash/uniqBy';
import React from 'react';

import { CommandPaletteContext } from './command-palette-context';
import { CommandPaletteInput } from './command-palette-input';
import { CommandPaletteResultList } from './command-palette-results';
import type { Command, SearchableCommandPaletteProps } from './types';
import { commandMatchesSearchTerm, executeCommand } from './utils';

/**
 * The search manager. Manages the state for the search. Determines what comparison algorithm to use.
 * Creates a form with the input field, which it listens to, and the results list.
 */
export function CommandPaletteSearchManager(props: SearchableCommandPaletteProps) {
  Timer.start('Command palette search manager setup');
  /**
   * command palette visibility
   */
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { commandPaletteIsVisible, setCommandPaletteVisibility } = React.useContext(
    CommandPaletteContext
  );

  /**
   * Provides search results, and getters and setters for the search term.
   * Uses the commandMatchesSearchTerm function to determine a match
   */
  const [searchResults, setSearchTerm, getSearchTerm] = useVanillaSearch<Command>(
    props.commandActions,
    commandMatchesSearchTerm
  );

  /**
   * Take the search results, or the default results provided, and removes duplicate matches.
   * Create a selection manager for these search results to keep track of which is selected.
   */
  const unfilteredResults = searchResults ?? props.defaultSearchResults ?? [];
  const commandResults = uniqBy(unfilteredResults, result => result.refIndex);
  const selectionManager = useSearchResultSelectionManager(commandResults);

  // Resets defaults for search term and which element is selected
  const cleanUp = () => {
    setSearchTerm('');
    selectionManager.resetSelection();
  };

  // if we unmount, run the clean up function.
  // !FIX ESLINT Validate and check REACT HOOK dependencies
  // eslint-disable-next-line react-hooks/exhaustive-deps
  React.useEffect(() => cleanUp, []);

  // if the command palette closes, run the clean up function.
  // !FIX ESLINT Validate and check REACT HOOK dependencies
  // eslint-disable-next-line react-hooks/exhaustive-deps
  React.useEffect(cleanUp, [commandPaletteIsVisible]);

  Timer.end('Command palette search manager setup');
  return (
    // eslint-disable-next-line jsx-a11y/no-static-element-interactions
    <div
      className="overlay__contents command-palette-overlay__contents"
      onKeyDown={(e: React.KeyboardEvent) => {
        e.stopPropagation();
      }}
    >
      <form
        className={classList({
          'command-palette': true
        })}
        data-cy="command-palette"
        onSubmit={(e: React.FormEvent<HTMLFormElement>) => {
          e.preventDefault();
          // eslint-disable-next-line no-unused-expressions
          selectionManager.getSelectedResult() &&
            executeCommand(selectionManager.getSelectedResult().item);
          setCommandPaletteVisibility(false);
        }}
      >
        <CommandPaletteInput
          getSearchTerm={getSearchTerm}
          setSearchTerm={setSearchTerm}
          selectionManager={selectionManager}
        />
        <CommandPaletteResultList
          searchResults={commandResults}
          selectedResult={selectionManager.getSelectedResult()}
        />
      </form>
    </div>
  );
}
