import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { CheckboxList } from '../../checkbox-list';
import { isCheckboxDropdownToolbarItem } from '../toolbar-item/checkbox-dropdown-item';
import type { ToolbarOverflowMenuItemProps } from './types';

/**
 * ToolbarItem component for a CheckboxDropdown specifically in the overflow menu.
 */
// eslint-disable-next-line react/function-component-definition
export const CheckboxDropdownOverflowMenuToolbarItem: React.FC<ToolbarOverflowMenuItemProps> = ({
  item,
  menuKey
}: ToolbarOverflowMenuItemProps) =>
  isCheckboxDropdownToolbarItem(item) ? (
    <MenuItem
      text={item.menuLabel ?? item.label}
      icon={item.icon}
      key={menuKey}
      disabled={item.disabled}
    >
      <CheckboxList
        enumToCheckedMap={item.values}
        enumToColorMap={item.colors}
        enumKeysToDisplayStrings={item.enumKeysToDisplayStrings}
        checkboxEnum={item.enumOfKeys}
        onChange={value => item.onChange(value)}
      />
    </MenuItem>
  ) : null;
