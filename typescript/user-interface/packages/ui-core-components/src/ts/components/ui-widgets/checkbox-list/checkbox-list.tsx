/* eslint-disable react/destructuring-assignment */
import { Checkbox, Classes, H6, MenuDivider } from '@blueprintjs/core';
import type Immutable from 'immutable';
import React from 'react';

import type { CheckboxListProps } from './types';

interface CheckboxListState {
  enumToCheckedMap: Immutable.Map<any, boolean>;
}

export const NULL_CHECKBOX_COLOR_SWATCH = 'NULL_CHECKBOX_COLOR_SWATCH';

export class CheckboxList extends React.Component<CheckboxListProps, CheckboxListState> {
  public constructor(props: CheckboxListProps) {
    super(props);
    this.state = {
      enumToCheckedMap: props.enumToCheckedMap
    };
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className="checkbox-list__body">
        {Object.keys(this.props.checkboxEnum).map(key => (
          <React.Fragment key={`${key}rf`}>
            {this.checkboxLabelString(key) ? (
              <H6 className={Classes.HEADING}> {this.checkboxLabelString(key)}</H6>
            ) : undefined}
            <div className="checkbox-list__row" key={key}>
              <div className="checkbox-list__box-and-label">
                <Checkbox
                  className="checkbox-list__checkbox"
                  data-cy={`checkbox-list-${
                    // eslint-disable-next-line no-restricted-globals
                    isNaN(key as any) ? String(key) : this.props.checkboxEnum[key]
                  }`}
                  checked={this.state.enumToCheckedMap.get(this.props.checkboxEnum[key])}
                  onChange={() => this.onChange(key)}
                >
                  <div className="checkbox-list__label">
                    {this.props.enumKeysToDisplayStrings
                      ? this.props.enumKeysToDisplayStrings.get(key)
                      : this.props.checkboxEnum[key]}
                  </div>
                  {this.props.enumToColorMap ? (
                    <div
                      className={`checkbox-list__legend-box${
                        this.shouldRenderNoColorSwatch(key) ? ' null-color-swatch' : ''
                      }`}
                      style={{
                        backgroundColor: !this.shouldRenderNoColorSwatch(key)
                          ? this.props.enumToColorMap.get(key)
                          : undefined
                      }}
                    />
                  ) : null}
                </Checkbox>
              </div>
            </div>
            {this.shouldRenderDivider(key) ? <MenuDivider /> : undefined}
          </React.Fragment>
        ))}
      </div>
    );
  }

  private readonly onChange = (key: string) => {
    this.setState(
      prevState => ({
        enumToCheckedMap: prevState.enumToCheckedMap.set(
          this.props.checkboxEnum[key],
          !prevState.enumToCheckedMap.get(this.props.checkboxEnum[key])
        )
      }),
      () => {
        this.props.onChange(this.state.enumToCheckedMap);
      }
    );
  };

  private readonly shouldRenderNoColorSwatch = (key: string): boolean => {
    if (this.props.enumToColorMap) {
      return this.props.enumToColorMap.get(key) === NULL_CHECKBOX_COLOR_SWATCH;
    }
    return false;
  };

  private readonly shouldRenderDivider = (key: string): boolean => {
    if (this.props.enumKeysToDividerMap) {
      return this.props.enumKeysToDividerMap.get(key);
    }
    return false;
  };

  private readonly checkboxLabelString = (key: string): boolean | string => {
    if (this.props.enumKeysToLabelMap) {
      return this.props.enumKeysToLabelMap.get(key);
    }
    return false;
  };
}
