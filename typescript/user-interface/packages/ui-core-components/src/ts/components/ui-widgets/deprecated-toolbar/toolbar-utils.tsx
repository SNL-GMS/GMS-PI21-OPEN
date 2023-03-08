import React from 'react';

import type { PopoverButton } from '../popover-button';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { ToolbarItemRenderer } from './toolbar-item';
// !FIX ESLINT CIRCULAR DEPENDENCY
// eslint-disable-next-line import/no-cycle
import { ToolbarMenuItemRenderer } from './toolbar-menu-item';
import type { ToolbarItem } from './types';

/**
 * Takes a toolbarItem and wraps it in the rendered menu item
 *
 * @param item the toolbar item
 * @param hasIssue boolean
 * @param menuKey rank
 * @returns rendered item
 */
export const renderMenuItem = (
  item: ToolbarItem,
  hasIssue: boolean,
  menuKey: string
): JSX.Element => (
  <ToolbarMenuItemRenderer item={item} hasIssue={hasIssue} menuKey={menuKey} key={menuKey} />
);

/**
 * Generate the items for the toolbar
 *
 * @param item Item to be rendered as widget
 * @param popoverButtonMap
 * @param hasIssue rendered item has an issue to indicate
 * @returns the rendered item
 */
export const renderItem = (
  item: ToolbarItem,
  popoverButtonMap: Map<number, PopoverButton>,
  hasIssue = false
): JSX.Element => (
  <ToolbarItemRenderer
    addToPopoverMap={(key, val) => popoverButtonMap.set(key, val)}
    item={item}
    key={item.rank}
    hasIssue={hasIssue}
  />
);

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
/**
 * Build a list of left and right menu items for the toolbar
 *
 * @param itemsRight
 * @param itemsLeft
 * @param rightIndicesToOverflow
 * @param leftIndicesToOverflow
 * @returns the jsx elements that are in the overflow menu. *
 */
export const getOverflowMenuItems = (
  itemsRight: ToolbarItem[],
  itemsLeft: ToolbarItem[],
  rightIndicesToOverflow: number[],
  leftIndicesToOverflow: number[]
): JSX.Element[] => {
  const sortedItemsRight = [...itemsRight].sort((a, b) => a.rank - b.rank);
  const sortedItemsLeft = [...itemsLeft].sort((a, b) => a.rank - b.rank);
  const overflowItemsRight = sortedItemsRight.filter(
    (item, index) => rightIndicesToOverflow.indexOf(index) >= 0
  );
  const overflowItemsLeft = sortedItemsLeft.filter(
    (item, index) => leftIndicesToOverflow.indexOf(index) >= 0
  );
  return [
    ...overflowItemsRight.map(item =>
      renderMenuItem(item, item.hasIssue, item.label ?? Number(item.rank).toString())
    ),
    ...overflowItemsLeft.map(item =>
      renderMenuItem(item, item.hasIssue, item.label ?? Number(item.rank).toString())
    )
  ];
};
