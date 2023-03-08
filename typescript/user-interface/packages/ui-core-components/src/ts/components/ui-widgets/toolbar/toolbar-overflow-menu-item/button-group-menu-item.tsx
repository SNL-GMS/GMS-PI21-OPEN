import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { isButtonGroupToolbarItem } from '../toolbar-item/button-group-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a ButtonGroup specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const ButtonGroupOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isButtonGroupToolbarItem(item) ? (
    <MenuItem text={item.menuLabel ?? item.label} icon={item.icon} key={menuKey}>
      {item.buttons.map(button => (
        <MenuItem
          text={button.menuLabel ?? button.label}
          icon={button.icon}
          key={button.key}
          disabled={button.disabled}
          onClick={() => button.onButtonClick()}
        />
      ))}
    </MenuItem>
  ) : null;
