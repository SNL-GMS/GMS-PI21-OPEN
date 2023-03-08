/* eslint-disable react/destructuring-assignment */
/*
 * Accepts a start and end time as input
 * Protects against cases where startTime > endTime
 * Basically two <TimePicker/>'s put in a div with an enter button
 */
import { Button } from '@blueprintjs/core';
import React from 'react';

import { TimePicker } from '../time-picker';
import type { IntervalPickerProps, IntervalPickerState } from './types';

// Length of an hour is milliseconds
const HOUR_IN_MS = 3600000;
export class IntervalPicker extends React.Component<IntervalPickerProps, IntervalPickerState> {
  /*
    A constructor
    */
  private constructor(props) {
    super(props);
    this.state = {
      // eslint-disable-next-line react/no-unused-state
      startDateHold: false,
      // eslint-disable-next-line react/no-unused-state
      endDateHold: false
    };
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const dateOrderingHold = this.props.startDate.getTime() >= this.props.endDate.getTime();
    return (
      <div className={this.props.renderStacked ? 'interval-picker--stacked' : 'interval-picker'}>
        <div
          className={
            this.props.renderStacked
              ? 'interval-picker__input_column'
              : 'interval-picker__input-row'
          }
        >
          <div
            className={
              this.props.renderStacked
                ? 'interval-picker__input interval-picker__input--stacked'
                : 'interval-picker__input interval-picker__input--flat'
            }
          >
            <div className="interval-picker__time-picker-label">Start Time:</div>
            <TimePicker
              date={this.props.startDate}
              datePickerEnabled
              onMaybeDate={this.onMaybeStartDate}
              setHold={this.onStartHold}
              hasHold={dateOrderingHold}
              onEnter={this.onEnter}
              shortFormat={this.props.shortFormat}
            />
          </div>
          <div
            className={
              this.props.renderStacked
                ? 'interval-picker__input'
                : 'interval-picker__input interval-picker__input--right'
            }
          >
            <div className="interval-picker__time-picker-label">End Time:</div>
            <TimePicker
              date={this.props.endDate}
              datePickerEnabled
              onMaybeDate={this.onMaybeEndDate}
              setHold={this.onEndHold}
              hasHold={dateOrderingHold}
              onEnter={this.onEnter}
              shortFormat={this.props.shortFormat}
            />
          </div>
          <div
            className={
              this.props.renderStacked
                ? 'interval-picker__enter-button interval-picker__enter-button--stacked'
                : 'interval-picker__enter-button interval-picker__enter-button--flat'
            }
          >
            <Button onClick={this.onApply}>Apply</Button>
          </div>
        </div>
      </div>
    );
  }

  private readonly onMaybeStartDate = (maybeDate: Date | undefined) => {
    if (maybeDate) {
      // If a default interval is set, sets the end date so many hours into the future
      if (this.props.defaultIntervalInHours) {
        const newEndDate = new Date(
          maybeDate.valueOf() + HOUR_IN_MS * this.props.defaultIntervalInHours
        );
        this.props.onNewInterval(maybeDate, newEndDate);
      } else {
        this.props.onNewInterval(maybeDate, this.props.endDate);
      }
    }
  };

  private readonly onMaybeEndDate = (maybeDate: Date | undefined) => {
    if (maybeDate) {
      this.props.onNewInterval(this.props.startDate, maybeDate);
    }
  };

  private readonly onStartHold = (hold: boolean) => {
    // eslint-disable-next-line react/no-unused-state
    this.setState({ startDateHold: hold });
  };

  private readonly onEndHold = (hold: boolean) => {
    // eslint-disable-next-line react/no-unused-state
    this.setState({ endDateHold: hold });
  };

  private readonly onEnter = () => {
    if (this.props.startDate.getTime() <= this.props.endDate.getTime()) {
      this.onApply();
    }
  };

  private readonly onApply = () => {
    if (this.props.startDate.getTime() >= this.props.endDate.getTime()) {
      if (this.props.onInvalidInterval) {
        this.props.onInvalidInterval('Start Time must be less then End Time');
      }
    } else if (this.props.onApply) {
      this.props.onApply(this.props.startDate, this.props.endDate);
    }
  };
}
