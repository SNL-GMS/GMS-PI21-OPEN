import React from 'react';

import { IntervalPicker } from '../../interval-picker';
import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isIntervalPickerToolbarItem(
  object: unknown
): object is IntervalPickerToolbarItemProps {
  return (
    (object as IntervalPickerToolbarItemProps).startDate !== undefined &&
    (object as IntervalPickerToolbarItemProps).endDate !== undefined &&
    (object as IntervalPickerToolbarItemProps).defaultIntervalInHours !== undefined
  );
}

/**
 * Properties to pass to the {@link IntervalPickerToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface IntervalPickerToolbarItemProps extends ToolbarItemBase {
  /** start date for the interval */
  startDate: Date;

  /** end date for the interval */
  endDate: Date;

  /** default interval time - in hours */
  defaultIntervalInHours: number;

  /** display in short format - default is no */
  shortFormat?: boolean;

  /** callback when the interval is changed/selected */
  onChange(startDate: Date, endDate: Date);

  /** callback when the interval's apply button is clicked */
  onApplyButton(startDate: Date, endDate: Date);
}

/**
 * Represents an interval selector used within a toolbar
 *
 * @param intervalPickerItem the intervalPickerItem to display {@link IntervalPickerToolbarItem}
 */
// eslint-disable-next-line react/function-component-definition
export const IntervalPickerToolbarItem: React.FC<IntervalPickerToolbarItemProps> = ({
  startDate: start,
  endDate: end,
  defaultIntervalInHours,
  shortFormat,
  onChange,
  onApplyButton,
  style,
  cyData
}: IntervalPickerToolbarItemProps): ToolbarItemElement => {
  const handleNewInterval = (startDate: Date, endDate: Date) => {
    onChange(startDate, endDate);
  };

  const handleApply = (startDate: Date, endDate: Date) => {
    onApplyButton(startDate, endDate);
  };

  return (
    <div className="toolbar-button--capsule" style={style ?? {}}>
      <IntervalPicker
        shortFormat={shortFormat}
        startDate={start}
        endDate={end}
        onNewInterval={handleNewInterval}
        onApply={handleApply}
        defaultIntervalInHours={defaultIntervalInHours}
        data-cy={cyData}
      />
    </div>
  );
};
