/* eslint-disable react/destructuring-assignment */
import { Checkbox } from '@blueprintjs/core';
import React from 'react';

import type { CheckboxListProps, CheckboxListState } from './types';

/**
 * Renders a list of checkboxes that can be scrolled through. The list is filtered
 * for matches starting with string typed in by user.
 */
export class CheckboxSearchList extends React.Component<CheckboxListProps, CheckboxListState> {
  private constructor(props) {
    super(props);
    this.state = {
      currentFilter: ''
    };
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        className="checkbox-search-list"
        style={{
          height: this.props.maxHeightPx ? `${this.props.maxHeightPx}px` : '200px'
        }}
      >
        <input
          className="checkbox-search-list__search"
          type="search"
          placeholder="Search input"
          tabIndex={0}
          onChange={e => {
            this.onFilterInput(e);
            e.stopPropagation();
          }}
          onKeyDown={e => {
            e.stopPropagation();
          }}
          // eslint-disable-next-line jsx-a11y/no-autofocus
          autoFocus
          value={this.state.currentFilter}
        />
        {this.props.items.map(item =>
          item.name.toLowerCase().startsWith(this.state.currentFilter.toLowerCase()) ? (
            <div className="checkbox-search-list-item" key={`checkbox-search-list${item.id}`}>
              <Checkbox
                checked={item.checked}
                onChange={() => {
                  this.props.onCheckboxChecked(item.id, !item.checked);
                }}
                data-cy-station-checkbox={item.name}
              />
              <span className="checkbox-search-list-item__label">{item.name}</span>
            </div>
          ) : null
        )}
      </div>
    );
  }

  private readonly onFilterInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({ currentFilter: e.currentTarget.value });
  };
}
