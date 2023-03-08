import { MenuItem } from '@blueprintjs/core';
import React from 'react';

import { isButtonGroupToolbarItem } from './toolbar-item/button-group-item';
import { isButtonToolbarItem } from './toolbar-item/button-item';
import { isCheckboxDropdownToolbarItem } from './toolbar-item/checkbox-dropdown-item';
import { isCustomToolbarItem } from './toolbar-item/custom-item';
import { isDateRangePickerToolbarItem } from './toolbar-item/date-range-picker-item';
import { isDropdownToolbarItem } from './toolbar-item/dropdown-item';
import { isIntervalPickerToolbarItem } from './toolbar-item/interval-picker-item';
import { isLabelValueToolbarItem } from './toolbar-item/label-value-item';
import { isLoadingSpinnerToolbarItem } from './toolbar-item/loading-spinner-item';
import { isNumericInputToolbarItem } from './toolbar-item/numeric-input-item';
import { isPopoverButtonToolbarItem } from './toolbar-item/popover-button-item';
import { isSwitchToolbarItem } from './toolbar-item/switch-item';
import { ButtonGroupOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/button-group-menu-item';
import { ButtonOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/button-menu-item';
import { CheckboxDropdownOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/checkbox-dropdown-menu-item';
import { CustomOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/custom-menu-item';
import { DateRangePickerOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/date-range-picker-menu-item';
import { DropdownOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/dropdown-menu-item';
import { IntervalPickerOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/interval-picker-menu-item';
import { LabelValueOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/label-value-menu-item';
import { LoadingSpinnerOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/loading-spinner-menu-item';
import { NumericOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/numeric-input-menu-item';
import { PopoverButtonOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/popover-button-menu-item';
import { SwitchOverflowMenuToolbarItem } from './toolbar-overflow-menu-item/switch-menu-item';
import type { ToolbarOverflowMenuItemProps } from './toolbar-overflow-menu-item/types';
import type { ToolbarItemBase, ToolbarItemElement } from './types';

/**
 * Function signature type for the type guard functions
 * provided to{@link toolbarItemMap}
 */
type ToolbarItemTypeGuard = (item: ToolbarItemBase) => boolean;

/**
 * Used by {@link renderOverflowMenuItem} to evaluate the type of the
 * passed {@link ToolbarItemElement}
 */
const toolbarItemMap = new Map<ToolbarItemTypeGuard, React.FC<ToolbarOverflowMenuItemProps>>([
  [isButtonToolbarItem, ButtonOverflowMenuToolbarItem],
  [isButtonGroupToolbarItem, ButtonGroupOverflowMenuToolbarItem],
  [isCheckboxDropdownToolbarItem, CheckboxDropdownOverflowMenuToolbarItem],
  [isCustomToolbarItem, CustomOverflowMenuToolbarItem],
  [isDateRangePickerToolbarItem, DateRangePickerOverflowMenuToolbarItem],
  [isDropdownToolbarItem, DropdownOverflowMenuToolbarItem],
  [isIntervalPickerToolbarItem, IntervalPickerOverflowMenuToolbarItem],
  [isLabelValueToolbarItem, LabelValueOverflowMenuToolbarItem],
  [isLoadingSpinnerToolbarItem, LoadingSpinnerOverflowMenuToolbarItem],
  [isNumericInputToolbarItem, NumericOverflowMenuToolbarItem],
  [isPopoverButtonToolbarItem, PopoverButtonOverflowMenuToolbarItem],
  [isSwitchToolbarItem, SwitchOverflowMenuToolbarItem]
]);

/**
 * "Intercepts" a {@link ToolbarItemElement} and instead returns the corresponding
 * Overflow MenuItem component.
 */
export const renderOverflowMenuItem = (
  item: ToolbarItemElement,
  menuKey: string | number
): JSX.Element => {
  const { props: toolbarItem } = item;
  let menuItemComponent: React.ReactElement<ToolbarOverflowMenuItemProps> | undefined;

  /** Prevents .forEach from unnecessarily iterating after a matching component has been found. */
  let componentFound = false;
  toolbarItemMap.forEach((OverflowMenuToolbarItem, typeGuard) => {
    if (typeGuard(toolbarItem) && !componentFound) {
      componentFound = true;
      menuItemComponent = (
        <OverflowMenuToolbarItem key={menuKey} item={toolbarItem} menuKey={menuKey} />
      );
    }
  });

  return componentFound ? menuItemComponent : <MenuItem />;
};

/**
 * Gets the widthpx of all the ref combined using getBoundingClientRect
 *
 * @param toolbarItemRefs
 * @returns width px
 */
export const getSizeOfItems = (toolbarItemRefs: HTMLElement[]): number => {
  return toolbarItemRefs.length > 0
    ? toolbarItemRefs
        .map(ref => ref.getBoundingClientRect().width)
        .reduce((accumulator: number, currentValue: number) => accumulator + currentValue)
    : 0;
};
/**
 *  Calculate the total width of all rendered items
 *
 * @param toolbarItemLeftRefs
 * @param toolbarItemRightRefs
 * @param whiteSpaceAllotmentPx amount of whitespace to reserve
 * @returns total with of all rendered items
 */
export const getSizeOfAllRenderedItems = (
  toolbarItemLeftRefs: HTMLElement[],
  toolbarItemRightRefs: HTMLElement[],
  whiteSpaceAllotmentPx: number
): number => {
  return (
    getSizeOfItems(toolbarItemLeftRefs) +
    whiteSpaceAllotmentPx +
    getSizeOfItems(toolbarItemRightRefs)
  );
};
