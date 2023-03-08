/* eslint-disable react/destructuring-assignment */
/**
 * Example of using the form that actually accepts input
 */

import {
  dateToString,
  ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
} from '@gms/common-util';
import React from 'react';

import { IntervalPicker } from '../components';

interface IntervalPickerExampleState {
  startDate: Date;
  endDate: Date;
}
/**
 * Example displaying how to use the Table component.
 */
export class IntervalPickerExample extends React.Component<unknown, IntervalPickerExampleState> {
  public constructor(props: unknown) {
    super(props);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    this.state = { startDate: new Date(1182038443000), endDate: new Date(1182124843000) };
  }

  /**
   * React render method
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        className="ag-dark"
        style={{
          flex: '1 1 auto',
          position: 'relative',
          width: '700px'
        }}
      >
        <IntervalPicker
          onNewInterval={this.onSubmit}
          startDate={this.state.startDate}
          endDate={this.state.endDate}
          onInvalidInterval={this.onInvalidInterval}
        />
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Start Date: ${dateToString(
            this.state.startDate,
            ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
          )}`}
          <br />
          {`End Date: ${dateToString(
            this.state.endDate,
            ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
          )}`}
        </div>
      </div>
    );
  }

  private readonly onSubmit = (interval: any) => {
    this.setState({ startDate: interval.startDate, endDate: interval.endDate });
  };

  // eslint-disable-next-line class-methods-use-this
  private readonly onInvalidInterval = (message: string) => {
    // eslint-disable-next-line no-alert
    alert(message);
  };
}
