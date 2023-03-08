import { ContextMenu, MenuItem } from '@blueprintjs/core';
import React from 'react';

import { IntervalPicker } from '../../interval-picker';
import { isIntervalPickerToolbarItem } from '../toolbar-item/interval-picker-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for an IntervalPicker specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const IntervalPickerOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isIntervalPickerToolbarItem(item) ? (
    <MenuItem
      text={item.menuLabel ?? item.label}
      icon={item.icon}
      key={menuKey}
      disabled={item.disabled}
    >
      <IntervalPicker
        renderStacked
        startDate={item.startDate}
        endDate={item.endDate}
        shortFormat={item.shortFormat}
        onNewInterval={(startDate, endDate) => item.onChange(startDate, endDate)}
        onApply={(startDate: Date, endDate: Date) => {
          item.onApplyButton(startDate, endDate);
          ContextMenu.hide();
        }}
        defaultIntervalInHours={item.defaultIntervalInHours}
      />
    </MenuItem>
  ) : null;
