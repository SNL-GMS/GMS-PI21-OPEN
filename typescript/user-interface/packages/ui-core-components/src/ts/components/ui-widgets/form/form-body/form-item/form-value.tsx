/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable react/destructuring-assignment */
import classNames from 'classnames';
import React from 'react';

import { Widget } from '../../../widgets';
import type { WidgetData } from '../../../widgets/types';

/**
 * FormValue Props
 */
export interface FormValueProps {
  value: WidgetData;
  widthPx: number;
  itemKey: string;
  className?: string;

  // data field for cypress testing
  'data-cy'?: string;

  onValue(valueKey: string, payload: any);
  onHoldChange(valueKey: string, holdStatus: boolean);
}

/*
 * FormValue State
 */

export interface FormValueState {
  currentValue: any;
  isOnHold: boolean;
}

/**
 * FormValue component.
 */
export class FormValue extends React.Component<FormValueProps, FormValueState> {
  private constructor(props) {
    super(props);
    this.state = {
      currentValue: this.props.value.defaultValue,
      isOnHold: false
    };
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className={classNames('form-value', this.props.className)}>
        <Widget
          {...this.props.value}
          data-cy={this.props['data-cy']}
          onMaybeValue={this.onMaybeInput}
          onValidStatus={this.onHoldStatus}
          isValid={!this.state.isOnHold}
        />
      </div>
    );
  }

  public getHoldStatus = (): boolean => this.state.isOnHold;

  public getCurrentValue = (): any => this.state.currentValue;

  private readonly onMaybeInput = (input: any | undefined) => {
    if (input !== undefined) {
      this.setState({ currentValue: input });
      this.props.onValue(this.props.itemKey, input);
    }
  };

  private readonly onHoldStatus = (hold: boolean) => {
    this.setState({ isOnHold: hold });
    this.props.onHoldChange(this.props.itemKey, hold);
  };
}
