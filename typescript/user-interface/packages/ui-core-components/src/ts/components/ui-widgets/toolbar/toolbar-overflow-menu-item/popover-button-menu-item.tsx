import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { isPopoverButtonToolbarItem } from '../toolbar-item/popover-button-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a PopoverButton specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const PopoverButtonOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isPopoverButtonToolbarItem(item) ? (
    <MenuItem
      text={item.menuLabel ?? item.label}
      icon={item.icon}
      key={menuKey}
      disabled={item.disabled}
    >
      {item.popoverContent}
    </MenuItem>
  ) : null;
