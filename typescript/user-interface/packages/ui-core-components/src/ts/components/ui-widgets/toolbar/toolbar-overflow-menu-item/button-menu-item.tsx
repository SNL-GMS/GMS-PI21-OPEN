import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { isButtonToolbarItem } from '../toolbar-item/button-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a Button specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const ButtonOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isButtonToolbarItem(item) ? (
    <MenuItem
      key={menuKey}
      text={item.menuLabel ?? item.label}
      title={item.tooltip}
      icon={item.icon}
      disabled={item.disabled}
      onClick={() => item.onButtonClick()}
    />
  ) : null;
