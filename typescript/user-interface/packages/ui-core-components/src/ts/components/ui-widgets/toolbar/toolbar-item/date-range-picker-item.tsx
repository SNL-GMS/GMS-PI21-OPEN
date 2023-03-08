import type { DateTimeFormat } from '@gms/common-util';
import React from 'react';

import type { DateRangePickerTypes } from '../../date-range-picker';
import { DateRangePicker } from '../../date-range-picker';
import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isDateRangePickerToolbarItem(
  object: unknown
): object is DateRangePickerToolbarItemProps {
  return (
    (object as DateRangePickerToolbarItemProps).startTimeMs !== undefined &&
    (object as DateRangePickerToolbarItemProps).endTimeMs !== undefined &&
    (object as DateRangePickerToolbarItemProps).format !== undefined
  );
}

/**
 * Properties to pass to the {@link DateRangePickerToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface DateRangePickerToolbarItemProps extends ToolbarItemBase {
  /** start time - in milliseconds post Jan 1, 1970 */
  startTimeMs: number;

  /** end time - in milliseconds post Jan 1, 1970 */
  endTimeMs: number;

  /** format - @see DateTimeFormat */
  format: DateTimeFormat;

  /** duration - @see DateRangePickerTypes.DurationOption */
  durations?: DateRangePickerTypes.DurationOption[];

  /** Minimum time for the date picker to start - in milliseconds */
  minStartTimeMs?: number;

  /** Maximum time for the date picker to end - in milliseconds */
  maxEndTimeMs?: number;

  /** callback when the date picker value changes */
  onChange(startTimeMs: number, endTimeMs: number);

  /** callback when the date picker's apply button is clicked */
  onApplyButton(startTimeMs: number, endTimeMs: number);
}

/**
 * Represents a date picker item used within a toolbar
 *
 * @param dateRangePicker the datePickerItem to display {@link DateRangePickerItem}
 */
// eslint-disable-next-line react/function-component-definition
export const DateRangePickerToolbarItem: React.FC<DateRangePickerToolbarItemProps> = ({
  startTimeMs: startMs,
  endTimeMs: endMs,
  format,
  durations,
  minStartTimeMs,
  maxEndTimeMs,
  onChange,
  onApplyButton,
  style
}: DateRangePickerToolbarItemProps): ToolbarItemElement => {
  const handleNewInterval = (startTimeMs: number, endTimeMs: number) => {
    onChange(startTimeMs, endTimeMs);
  };

  const handleApply = (startTimeMs: number, endTimeMs: number) => {
    onApplyButton(startTimeMs, endTimeMs);
  };

  return (
    <div className="toolbar-button--capsule" style={style ?? {}}>
      <DateRangePicker
        startTimeMs={startMs}
        endTimeMs={endMs}
        format={format}
        durations={durations}
        minStartTimeMs={minStartTimeMs}
        maxEndTimeMs={maxEndTimeMs}
        onNewInterval={handleNewInterval}
        onApply={handleApply}
      />
    </div>
  );
};
