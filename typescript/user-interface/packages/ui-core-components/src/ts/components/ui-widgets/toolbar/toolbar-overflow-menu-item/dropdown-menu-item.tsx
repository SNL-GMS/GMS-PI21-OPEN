import { MenuItem } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';

import { isDropdownToolbarItem } from '../toolbar-item/dropdown-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a Dropdown specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const DropdownOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) => {
  if (isDropdownToolbarItem(item)) {
    return (
      <MenuItem
        text={item.menuLabel ?? item.label}
        icon={item.icon}
        key={menuKey}
        disabled={item.disabled}
      >
        {item.dropdownOptions
          ? Object.keys(item.dropdownOptions).map(ekey => (
              <MenuItem
                text={item.dropdownOptions[ekey]}
                disabled={
                  item.disabledDropdownOptions
                    ? item.disabledDropdownOptions.indexOf(item.dropdownOptions[ekey]) > -1
                    : false
                }
                key={ekey}
                onClick={() => item.onChange(item.dropdownOptions[ekey])}
                icon={item.value === item.dropdownOptions[ekey] ? IconNames.TICK : undefined}
              />
            ))
          : null}
      </MenuItem>
    );
  }
  return null;
};
