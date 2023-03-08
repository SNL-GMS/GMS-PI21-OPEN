/* eslint-disable react/destructuring-assignment */
import * as React from 'react';

import { ButtonCheckboxEntry } from './button-checkbox-entry';
import { CheckboxEntry } from './checkbox-entry';
import { ElementCheckboxEntry } from './element-checkbox-entry';
import { IconCheckboxEntry } from './icon-checkbox-entry';
import type { CheckboxListEntry, SimpleCheckboxListProps, SimpleCheckboxListState } from './types';
import {
  isCheckboxListEntryButton,
  isCheckboxListEntryElement,
  isCheckboxListEntryIcon
} from './types';

/**
 * Creates a list of checkboxes with a label and optional color
 */
export class SimpleCheckboxList extends React.Component<
  SimpleCheckboxListProps,
  SimpleCheckboxListState
> {
  public constructor(props: SimpleCheckboxListProps) {
    super(props);
    this.state = {
      checkboxEntriesMap: {}
    };
  }

  // ***************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * React lifecycle method that triggers on mount, populates the map state class variable
   */
  public componentDidMount(): void {
    const tempCheckboxEntriesMap: Record<string, CheckboxListEntry> = {};
    this.props.checkBoxListEntries.forEach(entry => {
      tempCheckboxEntriesMap[entry.name] = entry;
    });
    this.setState({ checkboxEntriesMap: tempCheckboxEntriesMap });
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className="checkbox-list__body">
        {this.props.checkBoxListEntries.map(entry => {
          const isChecked = this.state.checkboxEntriesMap[entry.name]
            ? this.state.checkboxEntriesMap[entry.name].isChecked
            : entry.isChecked;
          if (isCheckboxListEntryButton(entry)) {
            return (
              <ButtonCheckboxEntry
                key={entry.name}
                entry={entry}
                isChecked={isChecked}
                onChange={this.updateCheckboxEntriesMap}
              />
            );
          }
          if (isCheckboxListEntryIcon(entry)) {
            return (
              <IconCheckboxEntry
                key={entry.name}
                entry={entry}
                isChecked={isChecked}
                onChange={this.updateCheckboxEntriesMap}
              />
            );
          }
          if (isCheckboxListEntryElement(entry)) {
            return (
              <ElementCheckboxEntry
                key={entry.name}
                entry={entry}
                isChecked={isChecked}
                onChange={this.updateCheckboxEntriesMap}
              />
            );
          }
          return (
            <CheckboxEntry
              key={entry.name}
              entry={entry}
              isChecked={isChecked}
              onChange={this.updateCheckboxEntriesMap}
            />
          );
        })}
      </div>
    );
  }
  // ***************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ***************************************

  /**
   * Updates the state and triggers a on change call back to the parent
   *
   * @param entryName name of the text for the checkbox
   * @returns void
   */
  private readonly updateCheckboxEntriesMap = (entryName: string): void => {
    const entry = this.state.checkboxEntriesMap[entryName];
    entry.isChecked = !entry.isChecked;
    this.props.onChange(entryName);
    this.setState(prevState => {
      const { checkboxEntriesMap } = prevState;
      checkboxEntriesMap[entryName] = entry;
      return { checkboxEntriesMap };
    });
  };
}
