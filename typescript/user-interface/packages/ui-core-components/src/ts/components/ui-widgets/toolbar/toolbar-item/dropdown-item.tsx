import React from 'react';

import { DropDown } from '../../drop-down';
import type { ToolbarItemBase, ToolbarItemElement } from '../types';

/**
 * Type guard, for use when rendering overflow menu items.
 */
export function isDropdownToolbarItem(object: unknown): object is DropdownToolbarItemProps {
  return (object as DropdownToolbarItemProps).dropdownOptions !== undefined;
}

/**
 * Properties to pass to the {@link DropdownToolbarItem}
 *
 * @see {@link ToolbarItemBase} for base properties.
 */
export interface DropdownToolbarItemProps extends ToolbarItemBase {
  /** list of dropdown options that are selectable */
  dropdownOptions: any;

  /** list of text displayable options - one per dropdown option */
  dropdownText?: any;

  /** list of disabled options that should be disabled */
  disabledDropdownOptions?: any;

  /** item that is initially selected */
  value: any;

  custom?: boolean;

  /** should the label be displayed in front of the item list */
  displayLabel?: boolean;

  /** callback when the value of the list changes */
  onChange(value: any);
}

/**
 * Represents a group of static items to display/select within a toolbar
 *
 * @param dropdownItem the dropdownItem to display {@link DropdownItem}
 */
// eslint-disable-next-line react/function-component-definition
export const DropdownToolbarItem: React.FC<DropdownToolbarItemProps> = ({
  dropdownOptions,
  dropdownText,
  disabledDropdownOptions,
  value: dropdownValue,
  custom,
  displayLabel,
  onChange,
  style,
  disabled,
  widthPx,
  tooltip,
  label,
  cyData
}: DropdownToolbarItemProps): ToolbarItemElement => {
  return (
    <div style={style ?? {}}>
      <DropDown
        onMaybeValue={onChange}
        value={dropdownValue}
        custom={custom}
        dropDownItems={dropdownOptions}
        dropdownText={dropdownText}
        disabledDropdownOptions={disabledDropdownOptions}
        disabled={disabled}
        widthPx={widthPx}
        title={tooltip}
        data-cy={cyData}
        displayLabel={displayLabel}
        label={label}
      />
    </div>
  );
};
