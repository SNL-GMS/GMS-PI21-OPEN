/* eslint-disable prefer-regex-literals */
/* eslint-disable react/destructuring-assignment */
import { Button } from '@blueprintjs/core';
import { DatePicker } from '@blueprintjs/datetime';
import { IconNames } from '@blueprintjs/icons';
import {
  dateToString,
  ISO_DATE_TIME_FORMAT,
  ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  stringToDate
} from '@gms/common-util';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';

import type { TimePickerTypes } from '.';

/**
 * TimePicker component that lets you enter time in ISO format.
 */

class TimePicker extends React.Component<
  TimePickerTypes.TimePickerProps,
  TimePickerTypes.TimePickerState
> {
  private timePickerRef: HTMLDivElement;

  private timeoutHandle: ReturnType<typeof setTimeout>;

  /*
    A constructor
    */
  private constructor(props) {
    super(props);
    this.timeoutHandle = undefined;
    this.state = {
      isValid: true,
      showDatePicker: false,
      displayString: this.props.shortFormat
        ? dateToString(this.props.date, ISO_DATE_TIME_FORMAT)
        : dateToString(this.props.date, ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION),
      // eslint-disable-next-line react/no-unused-state
      hasHold: this.props.hasHold ? this.props.hasHold : false,
      datePickerOnBottom: false
    };
  }

  public componentWillUnmount(): void {
    clearTimeout(this.timeoutHandle);
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        className="time-picker"
        ref={ref => {
          if (ref !== null) {
            this.timePickerRef = ref;
          }
        }}
      >
        <textarea
          value={this.state.displayString}
          className={
            this.state.isValid && !this.props.hasHold
              ? 'time-picker__input'
              : 'time-picker__input--invalid'
          }
          style={{
            width: this.props.shortFormat ? '152px' : '240px'
          }}
          // eslint-disable-next-line @typescript-eslint/no-magic-numbers
          cols={27}
          rows={1}
          // When focus leaves element, unsets holds and widget will display last valid date entered
          onBlur={e => {
            e.stopPropagation();
            this.setState({
              displayString: this.props.shortFormat
                ? dateToString(this.props.date, ISO_DATE_TIME_FORMAT)
                : dateToString(
                    this.props.date,
                    ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
                  ),
              isValid: true
            });

            const timeout = 200;
            if (this.props.setHold) {
              this.timeoutHandle = setTimeout(() => {
                if (this.props.setHold) {
                  this.props.setHold(false);
                }
              }, timeout);
            }
          }}
          onKeyDown={e => {
            if (e.nativeEvent.code === 'Enter') {
              if (this.props.onEnter) {
                this.props.onEnter();
              }
              e.preventDefault();
            }
          }}
          onChange={e => {
            // Attempts to create new date from parsed string
            const regex = this.props.shortFormat
              ? new RegExp(/^\d\d\d\d-\d\d-\d\dT\d\d:\d\d/, 'g')
              : new RegExp(/^\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d.\d\d\d\d\d\d/, 'g');

            const validStringFormat = regex.test(e.target.value);
            const newDate = stringToDate(e.target.value);
            // If the date is valid
            // eslint-disable-next-line no-restricted-globals
            if (validStringFormat && !isNaN(newDate.getTime())) {
              this.setState({ isValid: true, displayString: e.target.value }, () => {
                this.props.onMaybeDate(newDate);
              });
            } else {
              // If the date is not valid
              this.setState({ isValid: false, displayString: e.target.value });
              this.props.onMaybeDate(undefined);
              if (this.props.setHold) {
                this.props.setHold(true);
              }
            }
          }}
        />
        {this.state.showDatePicker ? (
          <DatePicker
            className={
              this.state.datePickerOnBottom
                ? 'time-picker__date-picker time-picker__date-picker--bottom'
                : 'time-picker__date-picker'
            }
            value={this.props.date}
            onChange={(inputDate, isUserChange) => {
              // Creates new date from state
              // Updates new date with values from date picker
              if (isUserChange) {
                document.body.removeEventListener('click', this.hideDatePickerOnClick);
                document.body.removeEventListener('keydown', this.hideDatePickerOnKeydown);
                const newDate = cloneDeep(this.props.date);
                newDate.setDate(inputDate.getDate());
                newDate.setMonth(inputDate.getMonth());
                newDate.setFullYear(inputDate.getFullYear());
                this.props.onMaybeDate(newDate);
                if (this.props.setHold) {
                  this.props.setHold(false);
                }
                this.setState({
                  isValid: true,
                  showDatePicker: false,
                  displayString: this.props.shortFormat
                    ? dateToString(newDate, ISO_DATE_TIME_FORMAT)
                    : dateToString(newDate, ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
                });
              }
            }}
          />
        ) : null}
        {this.props.datePickerEnabled ? (
          <Button
            icon={IconNames.CALENDAR}
            onClick={() => {
              this.setState(prevState => ({ showDatePicker: !prevState.showDatePicker }));
            }}
            className={
              this.state.showDatePicker
                ? 'time-picker__date-picker-button time-picker__date-picker-button--active'
                : 'time-picker__date-picker-button'
            }
          />
        ) : null}
      </div>
    );
  }

  // eslint-disable-next-line react/sort-comp
  public componentDidUpdate(
    prevProps: TimePickerTypes.TimePickerProps,
    prevState: TimePickerTypes.TimePickerState
  ): void {
    // We only check the date picker's position when it's created
    if (!prevState.showDatePicker && this.state.showDatePicker) {
      this.repositionDatePicker();
      document.body.addEventListener('click', this.hideDatePickerOnClick);
      document.body.addEventListener('keydown', this.hideDatePickerOnKeydown);
    }
    if (prevProps.date.valueOf() !== this.props.date.valueOf()) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        isValid: true,
        displayString: this.props.shortFormat
          ? dateToString(this.props.date, ISO_DATE_TIME_FORMAT)
          : dateToString(this.props.date, ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
      });
    }
  }

  private readonly hideDatePickerOnKeydown = (e: any): void => {
    if (e.nativeEvent.code === 'Escape') {
      this.hideDatePickerOnClick(e);
    }
  };

  private readonly hideDatePickerOnClick = (e: any): void => {
    let parent = e.target.parentNode;
    let hideDatePicker = true;
    while (parent && hideDatePicker) {
      if (parent.classList && parent.classList.contains('time-picker__date-picker')) {
        hideDatePicker = false;
      }
      parent = parent.parentNode;
    }

    if (hideDatePicker) {
      document.body.removeEventListener('click', this.hideDatePickerOnClick);
      document.body.removeEventListener('keydown', this.hideDatePickerOnKeydown);
      e.stopPropagation();
      this.setState({ showDatePicker: false });
    }
  };

  private readonly repositionDatePicker = () => {
    if (this.timePickerRef && this.state.showDatePicker) {
      const MIN_HEIGHT_OF_DATE_PICKER_PX = 233;

      const elemRect = this.timePickerRef.getBoundingClientRect();
      let container = this.timePickerRef.parentElement;

      // If the time picker is in a normal div, then we use the golden layout component
      // to decide if it's off screen
      if (!container) {
        return;
      }
      while (container.className !== 'lm_content') {
        container = container.parentElement;
        if (!container) {
          break;
        }
      }
      // Otherwise, we use the document body [occurs if time picker is in context menu]
      if (!container) {
        container = document.body;
      }
      const containerRect = container.getBoundingClientRect();

      if (elemRect.top - MIN_HEIGHT_OF_DATE_PICKER_PX < containerRect.top) {
        this.setState({ datePickerOnBottom: true });
      }
    }
  };
}
export { TimePicker };
