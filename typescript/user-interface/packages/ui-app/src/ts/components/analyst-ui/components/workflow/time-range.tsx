import { DATE_FORMAT, secondsToString } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import React from 'react';

const logger = UILogger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);
export interface TimeRangeProps {
  startTime?: number;
  endTime?: number;
}

export class TimeRange extends React.Component<TimeRangeProps> {
  /** the internal start time in epoch seconds */
  private internalStartTime: number;

  /** the internal end time in epoch seconds */
  private internalEndTime: number;

  /** the internal start date string */
  private internalStartTimeStr: string;

  /** the internal end date string */
  private internalEndTimeStr: string;

  public constructor(props: TimeRangeProps) {
    super(props);
    const { startTime, endTime } = props;
    this.internalStartTime = startTime;
    this.internalStartTimeStr = secondsToString(this.internalStartTime, DATE_FORMAT);
    this.internalEndTime = endTime;
    this.internalEndTimeStr = secondsToString(this.internalEndTime, DATE_FORMAT);
  }

  /**
   * React lifecycle determines if the component should update based on the next props passed in.
   *
   * @param nextProps the new props
   * @returns boolean
   */
  public shouldComponentUpdate(nextProps: TimeRangeProps): boolean {
    const { startTime: nextStartProps, endTime: nextEndProps } = nextProps;
    const { startTime, endTime } = this.props;
    const shouldUpdate = startTime !== nextStartProps || endTime !== nextEndProps;
    if (shouldUpdate) {
      return this.internalUpdate(nextStartProps, nextEndProps);
    }
    return false;
  }

  /**
   * Updates the internal state of the component only when necessary.
   *
   * @param startTime the start time to update
   * @param endTime the end time to update
   * @returns returns true if an update occurred; false otherwise
   */
  private internalUpdate(startTime: number, endTime: number): boolean {
    const timesChanged = this.internalStartTime !== startTime || this.internalEndTime !== startTime;
    // check if the internal times have changed and update those times if necessary
    if (timesChanged) {
      this.internalStartTime = startTime;
      this.internalEndTime = endTime;

      const startStr = secondsToString(this.internalStartTime, DATE_FORMAT);
      const endStr = secondsToString(this.internalEndTime, DATE_FORMAT);

      const shouldUpdate =
        this.internalStartTimeStr !== startStr || this.internalEndTimeStr !== endStr;

      // check if the internal date strings have changed and update those strings if necessary
      if (shouldUpdate) {
        this.internalStartTimeStr = startStr;
        this.internalEndTimeStr = endStr;
        // only return `true` if the start or end date strings changed, i.e. the month, day, or year changed
        return true;
      }
    }
    return false;
  }

  public update(startTime: number, endTime: number): void {
    if (this.internalUpdate(startTime, endTime)) {
      logger.debug(
        `Force updating TimeRange`,
        this.internalStartTime,
        this.internalStartTimeStr,
        this.internalEndTime,
        this.internalEndTimeStr
      );
      // the internal state changed; force render the component
      this.forceUpdate();
    }
  }

  public render(): JSX.Element {
    logger.debug(`Rendering TimeRange`, this.props);

    return (
      <div className="workflow-time-range">
        <span className="time-range-left">{this.internalStartTimeStr}</span>
        <span className="time-range-right">{this.internalEndTimeStr}</span>
      </div>
    );
  }
}
