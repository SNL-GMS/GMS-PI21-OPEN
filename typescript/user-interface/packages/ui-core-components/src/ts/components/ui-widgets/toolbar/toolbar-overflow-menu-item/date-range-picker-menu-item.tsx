import { ContextMenu, MenuItem } from '@blueprintjs/core';
import React from 'react';

import { DateRangePicker } from '../../date-range-picker';
import { isDateRangePickerToolbarItem } from '../toolbar-item/date-range-picker-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a DateRangePicker specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const DateRangePickerOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isDateRangePickerToolbarItem(item) ? (
    <MenuItem
      text={item.menuLabel ?? item.label}
      icon={item.icon}
      key={menuKey}
      disabled={item.disabled}
    >
      <div className="date-range-picker__menu-popover">
        <DateRangePicker
          startTimeMs={item.startTimeMs}
          endTimeMs={item.endTimeMs}
          format={item.format}
          durations={item.durations}
          minStartTimeMs={item.minStartTimeMs}
          maxEndTimeMs={item.maxEndTimeMs}
          onNewInterval={(startTimeMs: number, endTimeMs: number) =>
            item.onChange(startTimeMs, endTimeMs)
          }
          onApply={(startTimeMs: number, endTimeMs: number) => {
            item.onApplyButton(startTimeMs, endTimeMs);
            ContextMenu.hide();
          }}
        />
      </div>
    </MenuItem>
  ) : null;
