import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { isSwitchToolbarItem } from '../toolbar-item/switch-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a Switch specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const SwitchOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isSwitchToolbarItem(item) ? (
    <MenuItem
      text={item.menuLabel ?? item.label}
      icon={item.icon}
      key={menuKey}
      disabled={item.disabled}
      onClick={() => item.onChange(!item.switchValue)}
    />
  ) : null;
