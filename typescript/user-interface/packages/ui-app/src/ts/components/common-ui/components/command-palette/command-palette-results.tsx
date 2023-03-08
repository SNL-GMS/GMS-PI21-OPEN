/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { Timer } from '@gms/common-util';
import type { RangeTuple, SearchResult } from '@gms/ui-util';
import { classList, highlightRanges, isElementOutOfView, useScrollIntoView } from '@gms/ui-util';
import orderBy from 'lodash/orderBy';
import * as React from 'react';

import { CommandPaletteContext } from './command-palette-context';
import type { Command, CommandPaletteResultListProps, CommandPaletteResultProps } from './types';
import { scrollOptions } from './types';
import { getRangesToHighlight } from './utils';

/**
 * Creates a string with spans that enclose highlighted portions of the string.
 */
const SearchResultWithHighlights: React.FunctionComponent<{
  text: string;
  rangesToHighlight: RangeTuple[];
  // eslint-disable-next-line react/function-component-definition
}> = ({ text, rangesToHighlight }) => {
  const reverseSortedRanges = orderBy(rangesToHighlight, range => range[0], 'desc');
  return <span>{highlightRanges(text, reverseSortedRanges)}</span>;
};

/**
 * A command palette result element in the list.
 * Creates a <li> element. If selected, scrolls to ensure that the selected element is visible.
 */
// eslint-disable-next-line react/function-component-definition
const CommandPaletteResult: React.FunctionComponent<CommandPaletteResultProps> = props => {
  const context = React.useContext(CommandPaletteContext);
  const listElementRef = useScrollIntoView<HTMLLIElement>(
    (element: HTMLLIElement) => props.isSelected && isElementOutOfView(element),
    scrollOptions
  );
  const commandAction = props.searchResult.item;
  return (
    // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-noninteractive-element-interactions
    <li
      ref={ref => {
        listElementRef.current = ref;
      }}
      className={classList({
        'command-palette__result': true,
        'command-palette__result--selected': props.isSelected
      })}
      onClick={() => {
        commandAction.action();
        context.setCommandPaletteVisibility(false);
      }}
      key={commandAction.displayText}
    >
      <SearchResultWithHighlights
        text={commandAction.displayText ?? commandAction.commandType}
        rangesToHighlight={getRangesToHighlight(props.searchResult)}
      />
    </li>
  );
};

/**
 * @returns a command palette result list element that is either selected, or not.
 */
// eslint-disable-next-line react/display-name
const createCommandPaletteResultWithSelection = (selectedResult: SearchResult<Command>) =>
  // eslint-disable-next-line func-names, react/display-name
  function (searchResult: SearchResult<Command>) {
    return searchResult && searchResult.item && searchResult.item.commandType && selectedResult ? (
      <CommandPaletteResult
        key={searchResult.refIndex}
        searchResult={searchResult}
        isSelected={searchResult.refIndex === selectedResult.refIndex}
      />
    ) : null;
  };

/**
 * Creates a list of command palette results.
 */
// eslint-disable-next-line react/function-component-definition
export const CommandPaletteResultList: React.FunctionComponent<CommandPaletteResultListProps> = ({
  searchResults,
  selectedResult
}) => {
  Timer.start('Create command palette result list');
  const createCommandPaletteResult = createCommandPaletteResultWithSelection(selectedResult);
  const results = searchResults.map(createCommandPaletteResult);
  Timer.end('Create command palette result list');
  return (
    <div
      className={classList({
        'command-palette__results-wrapper': true
      })}
    >
      <ul
        className={classList({
          'command-palette__result-list': true
        })}
      >
        {...results}
      </ul>
    </div>
  );
};
