import type { CommonTypes } from '@gms/common-model';
import type { DateTimeFormat } from '@gms/common-util';
import { epochSecondsNow } from '@gms/common-util';
import { DeprecatedToolbarTypes } from '@gms/ui-core-components';
import { useInterval } from '@gms/ui-util';
import * as React from 'react';

const buildTimePicker = (
  label: string,
  tooltip: string,
  internalStartTimeMs: number,
  internalEndTimeMs: number,
  setInternalInterval: (startTimeMs: number, endTimeMs: number) => void,
  setInterval: (startTimeMs: number, endTimeMs: number) => void,
  rank: number,
  durations?: DeprecatedToolbarTypes.DateRangePickerTypes.DurationOption[],
  minStartTimeMs?: number,
  maxEndTimeMs?: number,
  format?: DateTimeFormat
): DeprecatedToolbarTypes.DateRangePickerItem => ({
  label,
  tooltip,
  type: DeprecatedToolbarTypes.ToolbarItemType.DateRangePicker,
  rank,
  durations,
  startTimeMs: internalStartTimeMs,
  endTimeMs: internalEndTimeMs,
  format,
  minStartTimeMs,
  maxEndTimeMs,
  onChange: (s: number, e: number) => {
    setInternalInterval(s, e);
  },
  onApplyButton: (s: number, e: number) => {
    setInterval(s, e);
  }
});

/**
 * Creates a time picker for the toolbar.
 * If the interval provided is undefined or its times are undefined, it defaults
 * to using now - 1hr
 *
 * @param interval the default time range in seconds
 * @param label the label to display in the overflow menu
 * @param tooltip the tooltip to display on hover over the picker
 * @param rank the position in the toolbar. Must be unique
 * @param durations an optional list of duration options to appear as "shortcuts:" preset ranges of time with corresponding labels
 * @param maxSelectedDurationSec an optional number of seconds which constrains the maximum amount of time selectable - disabled
 * @param operationalTimePeriod the min and max times allowed (in seconds). Dates outside of this range will be grayed out, and times outside this range
 * @param format (optional) the date and time format to be used by the time picker
 * @returns an array containing a time range and time picker in the format: [TimeRange, DeprecatedToolbarTypes.ToolbarItem]
 */
export const useTimePicker = (
  interval: CommonTypes.TimeRange,
  label: string,
  tooltip: string,
  rank: number,
  durations?: DeprecatedToolbarTypes.DateRangePickerTypes.DurationOption[],
  operationalTimePeriod?: CommonTypes.TimeRange,
  format?: DateTimeFormat
): [CommonTypes.TimeRange, DeprecatedToolbarTypes.DateRangePickerItem] => {
  let intervalToUse = interval;
  if (!intervalToUse || !intervalToUse.startTimeSecs || !intervalToUse.endTimeSecs) {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    const SECS_IN_AN_HOUR = 60 * 60;
    intervalToUse = {
      startTimeSecs: epochSecondsNow(),
      endTimeSecs: epochSecondsNow() - SECS_IN_AN_HOUR
    };
  }

  // internal interval state
  // manages the internal state of the interval picker - this allows the user to be able to change
  // the interval without firing an event, i.e. querying the historical data
  const [internalStartTimeMs, internalEndTimeMs, setInternalInterval] = useInterval(
    intervalToUse.startTimeSecs * 1000,
    intervalToUse.endTimeSecs * 1000
  );

  // the interval state
  // this is the `true` state of the interval picker - this is set when the apply button is pressed
  const [startTimeMs, endTimeMs, setInterval] = useInterval(
    intervalToUse.startTimeSecs * 1000,
    intervalToUse.endTimeSecs * 1000
  );

  const intervalSelector = React.useMemo(
    () =>
      buildTimePicker(
        label,
        tooltip,
        internalStartTimeMs,
        internalEndTimeMs,
        setInternalInterval,
        setInterval,
        rank,
        durations,
        operationalTimePeriod?.startTimeSecs !== undefined
          ? operationalTimePeriod.startTimeSecs * 1000
          : undefined,
        operationalTimePeriod?.endTimeSecs !== undefined
          ? operationalTimePeriod.endTimeSecs * 1000
          : undefined,
        format
      ),
    [
      label,
      tooltip,
      internalStartTimeMs,
      internalEndTimeMs,
      setInternalInterval,
      setInterval,
      rank,
      durations,
      operationalTimePeriod?.startTimeSecs,
      operationalTimePeriod?.endTimeSecs,
      format
    ]
  );
  return [{ startTimeSecs: startTimeMs / 1000, endTimeSecs: endTimeMs / 1000 }, intervalSelector];
};
