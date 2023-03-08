/* eslint-disable react/destructuring-assignment */
import { Menu, MenuDivider, MenuItem } from '@blueprintjs/core';
import React from 'react';

import type { HistoryListProps } from './types';

// Default length of history list, overridden by props.listLength
const DEFAULT_LIST_LENGTH = 7;

/**
 * Accepts a start and end time as input
 * Protects against cases where startTime > endTime
 * Basically two <TimePicker/>'s put in a div with an enter button
 */

export class HistoryList extends React.Component<HistoryListProps, unknown> {
  private selfRef: HTMLDivElement;

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const listLength = this.props.listLength ? this.props.listLength : DEFAULT_LIST_LENGTH;
    const sortedItems = this.props.items.filter((item, index) => index < listLength);
    sortedItems.sort((a, b) => b.index - a.index);

    return (
      <div
        ref={ref => {
          if (ref) {
            this.selfRef = ref;
          }
        }}
      >
        <Menu>
          {this.props.preferredItems ? (
            <>
              {this.props.preferredItems.map(item => (
                <MenuItem key={item.id} text={item.label} onClick={e => this.onClick(e, item.id)} />
              ))}
              <MenuDivider />
            </>
          ) : null}
          {sortedItems.map(item => (
            <MenuItem key={item.id} text={item.label} onClick={e => this.onClick(e, item.id)} />
          ))}
        </Menu>
      </div>
    );
  }

  public componentDidMount(): void {
    if (this.selfRef) {
      this.selfRef.focus();
    }
  }

  private readonly onClick = (event: React.MouseEvent, itemId: string) => {
    this.props.onSelect(itemId);
  };
}
