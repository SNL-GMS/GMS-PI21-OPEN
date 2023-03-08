import type { DateTimeFormat } from '@gms/common-util';

/** Defines a trend */
export interface DurationOption {
  /** the description of the trend ... i.e. `last 24 hours` */
  description: string;
  /** the number of milliseconds */
  value: number;
}

// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface DateRangePickerProps {
  /** the start time in epoch milliseconds */
  startTimeMs: number;
  /** the end time in epoch milliseconds */
  endTimeMs: number;
  /** the format of the date time */
  format: DateTimeFormat;
  /** the duration options of the shortcut menu */
  durations?: DurationOption[];
  /** the minimum start time in epoch milliseconds */
  minStartTimeMs?: number;
  /** the maximum end time in epoch milliseconds */
  maxEndTimeMs?: number;
  /** the maximum time range in epoch milliseconds */
  maxSelectedRangeMs?: number;
  /** the onNewInterval event handler called onChange and onApply (Apply Button) */
  onNewInterval(startTimeMs: number, endTimeMS: number);
  /** the onApply event handler called onApply (Apply Button) */
  onApply?(startTimeMS: number, endTimeMS: number);
}

export interface DateRangePickerState {
  /** the start time in epoch milliseconds */
  startTimeMs: number;
  /** the end time in epoch milliseconds */
  endTimeMs: number;
  /** the open status of the popup */
  isPopupOpen: boolean;
}
