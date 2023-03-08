import { doTagsMatch } from '@gms/common-util';
import type { RangeTuple, SearchResult } from '@gms/ui-util';
import { vanillaSearch } from '@gms/ui-util';
import isEqualWith from 'lodash/isEqualWith';
import uniqBy from 'lodash/uniqBy';

import type { Command } from './types';

/**
 * calls the function associated with the command
 *
 * @param commandAction the command to perform
 */
export const executeCommand = (commandAction: Command): void => {
  commandAction.action();
};

/**
 * Checks if a search term is a match for a command.
 * It's considered a match if any words in the search term match a command's display text or tags
 *
 * @param element a CommandAction
 * @param term a string to check against the command action
 */
export const commandMatchesSearchTerm: (element: Command, term: string) => boolean = (
  element: Command,
  term: string
) =>
  element &&
  (element.displayText?.toLowerCase().includes(term.toLowerCase()) ||
    doTagsMatch(element.searchTags, term.toLowerCase()) ||
    element.commandType?.toString()?.toLowerCase()?.includes(term.toLowerCase()));

/**
 * Intended for use with fuzzy search results
 * Returns a list of indices within a search result string that should be highlighted.
 *
 * @param result Search results
 */
export const getRangesToHighlight: (result: SearchResult<Command>) => RangeTuple[] = (
  result: SearchResult<Command>
) => {
  if (result.matches) {
    const displayTextMatches = [...result.matches]?.find(match => match.key === 'displayText');
    return displayTextMatches && [...displayTextMatches.indices];
  }
  return [];
};

/**
 * Create an ID from a command action.
 * Assumes that the union of command type and display text is unique.
 *
 * @param ca a command action from which to generate an id
 */
export const getCommandId = (ca: Command): string => `${ca.commandType}-${ca.displayText}`;

/**
 * gets a unique id from the command within a search result.
 *
 * @param value a search result
 */
export const getCommandIdFromSearchResult = (value: SearchResult<Command>): string =>
  getCommandId(value.item);

/**
 * Searches commands for any matches in a list of provided strings.
 *
 * @param searchTerms a list of strings to search for
 * @param commandActions a list of command actions to search
 * @returns an array of search results for which a match was found
 */
export const searchCommandsForTerms: (
  searchTerms: string[],
  commandActions: Command[]
) => SearchResult<Command>[] = (searchTerms: string[], commandActions: Command[]) =>
  uniqBy(
    searchTerms?.reduce<SearchResult<Command>[]>(
      (results: SearchResult<Command>[], term: string) => [
        ...results,
        ...vanillaSearch(commandActions, term, commandMatchesSearchTerm)
      ],
      []
    ),
    getCommandIdFromSearchResult
  );

/**
 * Compares two lists of CommandActions and tells if it changes.
 * Comparison uses command type and display text.
 *
 * @param prevCommandList the previous command list
 * @param nextCommandList the new command list
 */
export const hasCommandListChanged = (
  prevCommandList: Command[],
  nextCommandList: Command[]
): boolean =>
  prevCommandList.length !== nextCommandList.length ||
  !isEqualWith(
    prevCommandList,
    nextCommandList,
    (prev: Command, next: Command) =>
      prev.commandType === next.commandType && prev?.displayText === next?.displayText
  );
