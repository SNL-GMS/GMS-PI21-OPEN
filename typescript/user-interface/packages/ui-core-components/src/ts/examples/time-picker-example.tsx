/* eslint-disable react/destructuring-assignment */
/**
 * Example of using the form that actually accepts input
 */

import {
  dateToString,
  ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
} from '@gms/common-util';
import React from 'react';

import { TimePicker } from '../components';

interface TimePickerExampleState {
  date: Date;
  hold: boolean;
}
/**
 * Example displaying how to use the Table component.
 */
export class TimePickerExample extends React.Component<unknown, TimePickerExampleState> {
  public constructor(props: unknown) {
    super(props);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers, react/no-unused-state
    this.state = { date: new Date(1182038443000), hold: false };
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
        <TimePicker onMaybeDate={this.onMaybeDate} date={this.state.date} datePickerEnabled />
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Date: ${dateToString(
            this.state.date,
            ISO_DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION
          )}`}
          <br />
        </div>
      </div>
    );
  }

  private readonly onMaybeDate = (date: Date | undefined) => {
    if (date !== undefined) {
      // eslint-disable-next-line react/no-unused-state
      this.setState({ date, hold: false });
    } else {
      // eslint-disable-next-line react/no-unused-state
      this.setState({ hold: true });
    }
  };
}
