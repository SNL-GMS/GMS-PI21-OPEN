/* eslint-disable react/destructuring-assignment */
import { Button, Classes, Label } from '@blueprintjs/core';
import { DATE_TIME_FORMAT } from '@gms/common-util';
import uniqueId from 'lodash/uniqueId';
import moment from 'moment';
import React from 'react';

import { DateRangePopup } from '../date-range-popup';
import type { DateRangePickerProps, DateRangePickerState } from './types';

/**
 * Allows the selection of a time range
 * consisting of a start and end time as input
 * on a single widget.
 * Protects against cases where startTime > endTime
 * Basically a <DateRangeInput /> with an apply button
 */
export class DateRangePicker extends React.PureComponent<
  DateRangePickerProps,
  DateRangePickerState
> {
  /** the default props for the DateRangePicker */
  // eslint-disable-next-line react/static-property-placement
  public static readonly defaultProps = {
    format: DATE_TIME_FORMAT
  };

  /** the date time picker label */
  private static readonly dateTimePickerLabel: string = 'Time Range:';

  /** a unique id for the component */
  private readonly id: string = uniqueId();

  /** constructor */
  public constructor(props: DateRangePickerProps) {
    super(props);
    this.state = {
      startTimeMs: props.startTimeMs,
      endTimeMs: props.endTimeMs,
      isPopupOpen: false
    };
  }

  /**
   * Formats a date for the date range picker.
   *
   * @param date the date to format
   * @returns the formatted date/time string
   */
  private readonly formatDate = (date: Date): string =>
    moment(date).utc().format(this.props.format);

  private readonly onPopupClose = () => {
    this.setState({ isPopupOpen: false });
  };

  private readonly onPopupOpen = () => {
    this.setState({ isPopupOpen: true });
  };

  private readonly onApply = (startTimeMs: number, endTimeMs: number) => {
    this.setState({ startTimeMs, endTimeMs, isPopupOpen: false });
    if (this.props.onApply) {
      this.props.onApply(startTimeMs, endTimeMs);
    }
  };

  /**
   * React component lifecycle.
   */
  public render(): JSX.Element {
    return (
      <div id={`date-range-picker-id-${this.id}`} className="date-range-picker">
        <Label className={`${Classes.LABEL} ${Classes.INLINE} date-range-picker__label`}>
          {`${DateRangePicker.dateTimePickerLabel} ${this.formatDate(
            new Date(this.state.startTimeMs)
          )} to ${this.formatDate(new Date(this.state.endTimeMs))}`}
        </Label>
        <DateRangePopup
          startTimeMs={this.state.startTimeMs}
          endTimeMs={this.state.endTimeMs}
          isOpen={this.state.isPopupOpen}
          title="Choose Date Range?"
          onClose={this.onPopupClose}
          // Pass through props
          format={this.props.format}
          durations={this.props.durations}
          minStartTimeMs={this.props.minStartTimeMs}
          maxEndTimeMs={this.props.maxEndTimeMs}
          maxSelectedRangeMs={this.props.maxSelectedRangeMs}
          onNewInterval={this.props.onNewInterval}
          onApply={this.onApply}
        />
        <div>
          <Button
            className="date-range-picker__apply-button"
            data-cy="date-range-picker-edit"
            onClick={this.onPopupOpen}
          >
            Edit
          </Button>
        </div>
      </div>
    );
  }
}
