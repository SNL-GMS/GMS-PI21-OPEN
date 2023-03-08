import { Timer } from '@gms/common-util';
import * as React from 'react';

/**
 * a class to apply to highlighted elements by default if no other class is provided.
 */
const defaultHighlightedClass = 'is-highlighted';

/**
 * a set to search over, of type T
 */
export type SearchSet<T> = readonly T[];

/**
 * A single search result of search type T
 */
export interface SearchResult<T> {
  item: T;
  refIndex: number;
  score?: number;
  matches?: readonly SearchResultMatch[];
}

// Denotes the start/end indices of a match
//                        start    end
//                          ↓       ↓
export type RangeTuple = [number, number];

/**
 * A matching search result. Indices show which segments were a match for fuzzy search.
 * refIndex is this result's index in the search set.
 */
export interface SearchResultMatch {
  indices: readonly RangeTuple[];
  key?: string;
  refIndex?: number;
  value?: string;
}

/**
 * Exposes an API for managing selected search results.
 */
export interface SelectionManager<T> {
  selectPrevious(): void;
  selectNext(): void;
  resetSelection(): void;
  getSelectedResult(): SearchResult<T>;
}

/**
 * Helper hook that uses a provided search function to search over a set of type T
 *
 * @param searchFn
 * @returns an array containing these results in this order:
 * * an array of search results
 * * a setSearchTerm function
 * * a getSearchTerm function
 */
export function useSearch<T>(
  searchFn: (searchTerm: string) => SearchResult<T>[]
): [SearchResult<T>[], React.Dispatch<React.SetStateAction<string>>, () => string] {
  const [searchTerm, setSearchTerm] = React.useState(null);
  const getSearchTerm = React.useCallback(() => searchTerm, [searchTerm]);
  if (searchTerm) {
    const searchResults = searchFn(searchTerm);
    return [searchResults, setSearchTerm, getSearchTerm];
  }
  return [undefined, setSearchTerm, getSearchTerm];
}

/**
 * Searches a set of type T for a search term, and returns an array of search results.
 *
 * @param set A SearchSet of type T over which to search
 * @param searchTerm a search term to search for
 * @param matches a function that determines if the search string matches a given element of type T.
 * @returns an array of search results of type T that matched
 */
export function vanillaSearch<T>(
  set: SearchSet<T>,
  searchTerm: string,
  matches: (element: T, term: string) => boolean
): SearchResult<T>[] {
  if (set && searchTerm && matches) {
    Timer.start(`search for: ${searchTerm}`);
    const results: SearchResult<T>[] = [];
    set.forEach((element, refIndex) => {
      if (matches(element, searchTerm)) {
        results.push({
          item: element,
          refIndex
        });
      }
    });
    Timer.end(`search for: ${searchTerm}`);
    return results;
  }
  return undefined;
}

/**
 * A hook that manages searching.
 *
 * @param set a search set over which to search
 * @param matches a function to determine if a search term matches a string.
 * @returns an array containing the following in this order:
 * * An array containing the results of the search.
 * * A setter function to set the search term, which will update the results returned.
 * * A getter function that will return the currently used search term.
 */
export function useVanillaSearch<T>(
  set: SearchSet<T>,
  matches: (element: T, term: string) => boolean
): [SearchResult<T>[], React.Dispatch<React.SetStateAction<string>>, () => string] {
  return useSearch<T>((searchTerm: string) => vanillaSearch(set, searchTerm, matches));
}

/**
 * A helper function used to constrain a selected index within set of available search results.
 * For example, wrapSelectedIndices(4, 0, 3) will return 0, because 4 is out of bounds (0 to 3), wrapping
 * the index back to the start.
 *
 * @param i the index we are trying to wrap
 * @param start the lower bound, inclusive
 * @param end the upper bound, inclusive
 */
export const wrapSelectedIndices = (i, start, end) => {
  if (i > end) {
    return start;
  }
  if (i < start) {
    return end;
  }
  return i;
};

/**
 * Provides an API for managing a selected result for a set of search results.
 * Keeps track of which element is selected, and exposes functions to select previous, next,
 * reset the selection (to index 0), and to get the currently selected result.
 *
 * @param results the search results for which we want to manage a selected result.
 * @returns a selection manager for search results of type T.
 */
export function useSearchResultSelectionManager<T>(
  results: SearchResult<T>[]
): SelectionManager<T> {
  const [selectedIndex, setSelectedIndex] = React.useState(0);
  const selectNext = () =>
    setSelectedIndex(wrapSelectedIndices(selectedIndex + 1, 0, results.length - 1));
  const selectPrevious = () =>
    setSelectedIndex(wrapSelectedIndices(selectedIndex - 1, 0, results.length - 1));
  const getSelectedResult = () => results[selectedIndex];
  const resetSelection = () => setSelectedIndex(0);
  return { selectPrevious, selectNext, resetSelection, getSelectedResult };
}

/**
 * Returns a Span with highlight spans wrapping each indicated range.
 *
 * @param text the string to highlight
 * @param reverseSortedRanges a list of ranges, from last to first, that should be highlighted.
 * @param highlightedClass the class to apply to the spans that wrap the highlighted sub-strings
 */
export const highlightRanges = (
  text: string,
  reverseSortedRanges: RangeTuple[],
  highlightedClass: string = defaultHighlightedClass
): JSX.Element | string => {
  if (reverseSortedRanges.length === 0) {
    return text;
  }
  const mutableRanges = [...reverseSortedRanges];
  const range = mutableRanges.shift();
  const prefix = text.substring(0, range[0]);
  const suffix = text.substring(range[1] + 1);
  return (
    <>
      {highlightRanges(prefix, mutableRanges)}
      <span className={highlightedClass}>{text.substring(range[0], range[1] + 1)}</span>
      {suffix}
    </>
  );
};
