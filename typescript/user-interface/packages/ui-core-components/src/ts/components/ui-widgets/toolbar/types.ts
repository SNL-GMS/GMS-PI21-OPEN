import type { IconName } from '@blueprintjs/core';
import type React from 'react';

import type { PopoverButton } from '../popover-button';

/**
 * Alias for a {@link JSX.Element} that is a ToolbarItem
 *
 * @example <LabelValueToolbarItem {...props}/>
 */
export type ToolbarItemElement = React.ReactElement<
  ToolbarItemBase,
  string | React.JSXElementConstructor<unknown>
>;

/**
 * react props for the toolbar
 */
export interface ToolbarProps {
  /** width of the toolbar */
  toolbarWidthPx: number;
  /**
   * account toolbar width for the padding of parent container.
   *
   * effective toolbar width will be {@link ToolbarProps.toolbarWidthPx toolbarWidthPx} -
   * {@link ToolbarProps.parentContainerPaddingPx parentContainerPaddingPx}
   */
  parentContainerPaddingPx: number;
  /** left-aligned toolbar items */
  itemsLeft: ToolbarItemElement[];
  /** right-aligned toolbar items */
  itemsRight?: ToolbarItemElement[];
  /** min white space to preserve at the end of the toolbar */
  minWhiteSpacePx?: number;
  /** space in pixels between elements */
  spaceBetweenItemsPx?: number;
  /** is the toolbar hidden */
  hidden?: boolean;
  /** icon to display when the size is smaller than can be displayed */
  overflowIcon?: IconName;
}
/**
 * react toolbar state
 */
export interface ToolbarState {
  /** indices of the right toolbar items that are in overflow */
  rightIndicesToOverflow: number[];
  /** indices of the left toolbar items that are in overflow */
  leftIndicesToOverflow: number[];
  /** white space allotment to preserve between left and right toolbar items */
  whiteSpaceAllotmentPx: number;
  /** flag for checking size */
  checkSizeOnNextDidMountOrDidUpdate: boolean;
}

/**
 * base type for a toolbar item
 */
export interface ToolbarItemBase {
  key: string | number;
  /** the label to be shown */
  label?: string;
  /** the tooltip to be shown */
  tooltip?: string;
  tooltipForIssue?: string;
  /** css style for the item */
  style?: React.CSSProperties;
  /** width - in pixels */
  widthPx?: number;
  /** right side label */
  labelRight?: string;
  /** is this item disabled */
  disabled?: boolean;
  /** icon to display */
  icon?: IconName;
  /** show only the icon? */
  onlyShowIcon?: boolean;
  /** menu label */
  menuLabel?: string;
  /** cypress testing data */
  cyData?: string;
  hasIssue?: boolean;
  /** If true, use default ian styling. Defaults to false (SOH). */
  ianApp?: boolean;
  /** callback for mouse enter */
  onMouseEnter?(): void;
  /** callback for mouse out */
  onMouseOut?(): void;
  /** which side of the toolbar is the item on (LEFT or RIGHT) */
  itemSide?: string;
  popoverButtonMap?: Map<number, PopoverButton>;
  /** className */
  className?: string;
}

/**
 * type for a min/max
 */
export interface MinMax {
  /** minimum number */
  min: number;
  /** maximum number */
  max: number;
}
