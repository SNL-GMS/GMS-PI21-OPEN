import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { isCustomToolbarItem } from '../toolbar-item/custom-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a CustomItem specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const CustomOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isCustomToolbarItem(item) ? (
    <MenuItem
      key={menuKey}
      text={item.menuLabel ?? item.label}
      disabled={item.disabled}
      icon={item.icon}
    >
      {item.element}
    </MenuItem>
  ) : null;
