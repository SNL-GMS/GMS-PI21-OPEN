/* eslint-disable react/destructuring-assignment */
import { HTMLSelect } from '@blueprintjs/core';
import kebabCase from 'lodash/kebabCase';
import React from 'react';

import type { WidgetTypes } from '..';
import type { DropDownProps } from './types';

/**
 * Drop Down menu
 */
const UNSELECTABLE_CUSTOM_VALUE = 'UNSELECTED_CUSTOM_VALUE';

export class DropDown extends React.Component<DropDownProps, WidgetTypes.WidgetState> {
  private constructor(props) {
    super(props);
    this.state = {
      // eslint-disable-next-line react/no-unused-state
      value: this.props.value,
      // eslint-disable-next-line react/no-unused-state
      isValid: true
    };
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const minWidth = `${this.props.widthPx}px`;
    const altStyle = {
      minWidth,
      width: minWidth
    };
    const kebabLabel = kebabCase(this.props.label);
    return (
      <div className="dropdown-container">
        {this.props.displayLabel && this.props.label && (
          <span className="dropdown-label">
            {this.props.label.length !== 0 ? `${this.props.label}: ` : ''}
          </span>
        )}
        <span className="dropdown-selector" data-cy={`${kebabLabel}-dropdown`}>
          <HTMLSelect
            title={`${this.props.title}`}
            disabled={this.props.disabled}
            style={this.props.widthPx !== undefined ? altStyle : undefined}
            className={this.props.className}
            onChange={e => {
              const input = e.target.value;
              if (this.props.custom && input === UNSELECTABLE_CUSTOM_VALUE) {
                return;
              }
              this.props.onMaybeValue(input);
            }}
            data-cy={this.props['data-cy']}
            value={this.props.custom ? UNSELECTABLE_CUSTOM_VALUE : this.props.value}
          >
            {this.createDropdownItems(
              this.props.dropDownItems,
              this.props.dropdownText,
              this.props.disabledDropdownOptions
            )}
            {this.props.custom ? (
              <option key={UNSELECTABLE_CUSTOM_VALUE} value={UNSELECTABLE_CUSTOM_VALUE}>
                Custom
              </option>
            ) : null}
          </HTMLSelect>
        </span>
      </div>
    );
  }

  /**
   * Creates the HTML for the dropdown items for the type input
   *
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly createDropdownItems = (
    enumOfOptions: any,
    dropdownText: any,
    disabledDropdownOptions: any
  ): JSX.Element[] => {
    const items: any[] = [];
    Object.keys(enumOfOptions).forEach(type => {
      items.push(
        <option
          key={type}
          value={enumOfOptions[type]}
          // If a disabledDropdownOptions is passed it, disable any options that exist in the array
          disabled={
            disabledDropdownOptions
              ? disabledDropdownOptions.indexOf(enumOfOptions[type]) > -1
              : false
          }
        >
          {dropdownText ? dropdownText[type] : enumOfOptions[type]}
        </option>
      );
    });
    return items;
  };
}
