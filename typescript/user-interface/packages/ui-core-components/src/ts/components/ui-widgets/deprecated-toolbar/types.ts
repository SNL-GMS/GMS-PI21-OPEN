import type { IconName } from '@blueprintjs/core';
import type { DateTimeFormat } from '@gms/common-util';
import type Immutable from 'immutable';
import type React from 'react';

import { DateRangePickerTypes } from '../date-range-picker';

/**
 * react props for the toolbar
 */
export interface ToolbarProps {
  itemsRight: DeprecatedToolbarItemBase[];
  toolbarWidthPx: number;
  itemsLeft?: DeprecatedToolbarItemBase[];
  minWhiteSpacePx?: number;
  spaceBetweenItemsPx?: number;
  hidden?: boolean;
  overflowIcon?: IconName;
}
/**
 * react toolbar state
 */
export interface ToolbarState {
  rightIndicesToOverflow: number[];
  leftIndicesToOverflow: number[];
  whiteSpaceAllotmentPx: number;
  checkSizeOnNextDidMountOrDidUpdate: boolean;
}

/**
 * All the toolbar items that can be created
 */
export type ToolbarItem =
  | NumericInputItem
  | DropdownItem
  | IntervalPickerItem
  | DateRangePickerItem
  | PopoverItem
  | SwitchItem
  | ButtonItem
  | ButtonGroupItem
  | LabelValueItem
  | CheckboxDropdownItem
  | LoadingSpinnerItem
  | CustomItem;

/**
 * base type for a toolbar item
 */
export interface DeprecatedToolbarItemBase {
  label?: string;
  tooltip: string;
  tooltipForIssue?: string;
  type: ToolbarItemType;
  rank: number;
  style?: React.CSSProperties;
  widthPx?: number;
  labelRight?: string;
  disabled?: boolean;
  icon?: IconName;
  onlyShowIcon?: boolean;
  menuLabel?: string;
  cyData?: string;
  hasIssue?: boolean;
  ianApp?: boolean; // If true, use default ian styling. defaults to false (SoH)
  onMouseEnter?(): void;
  onMouseOut?(): void;
}

/**
 * type for the numeric item toolbar item
 */
interface NumericInputItem extends DeprecatedToolbarItemBase {
  value: number;
  minMax: MinMax;
  step?: number;
  onChange(value: number);
}

/**
 * type for the dropdown item toolbar item
 */
interface DropdownItem extends DeprecatedToolbarItemBase {
  dropdownOptions?: any;
  dropdownText?: any;
  value?: any;
  custom?: boolean;
  displayLabel?: boolean;
  onChange(value: any);
}

/**
 * type for the interval picker item toolbar item
 */
interface IntervalPickerItem extends DeprecatedToolbarItemBase {
  startDate: Date;
  endDate: Date;
  defaultIntervalInHours: number;
  shortFormat?: boolean;
  onChange(startDate: Date, endDate: Date);
  onApplyButton(startDate: Date, endDate: Date);
}

/**
 * type for the date range picker item toolbar item
 */
interface DateRangePickerItem extends DeprecatedToolbarItemBase {
  startTimeMs: number;
  endTimeMs: number;
  format: DateTimeFormat;
  durations?: DateRangePickerTypes.DurationOption[];
  minStartTimeMs?: number;
  maxEndTimeMs?: number;
  onChange(startTimeMs: number, endTimeMs: number);
  onApplyButton(startTimeMs: number, endTimeMs: number);
}

/**
 * type for the popover item toolbar item
 */
interface PopoverItem extends DeprecatedToolbarItemBase {
  popoverContent: JSX.Element;
  onPopoverDismissed();
}

/**
 * type for the switch item toolbar item
 */
interface SwitchItem extends DeprecatedToolbarItemBase {
  value: boolean;
  onChange(value: boolean);
}

/**
 * type for the button item toolbar item
 */
interface ButtonItem extends DeprecatedToolbarItemBase {
  onClick();
}

/**
 * type for the button group item toolbar item
 */
interface ButtonGroupItem extends DeprecatedToolbarItemBase {
  buttons: ButtonItem[];
}

/**
 * type for the label value item toolbar item
 */
interface LabelValueItem extends DeprecatedToolbarItemBase {
  value: string | JSX.Element;
  valueColor?: string;
  styleForValue?: React.CSSProperties;
}

/**
 * type for the checkbox dropdown item toolbar item
 */
interface CheckboxDropdownItem extends DeprecatedToolbarItemBase {
  values: Immutable.Map<any, boolean>;
  colors?: Immutable.Map<any, string>;
  enumKeysToDisplayStrings?: Immutable.Map<string, string>;
  enumOfKeys: any;
  onChange(value: any): void;
  onPopUp?(ref?: HTMLDivElement): void;
  onPopoverDismissed?(): void;
}

/**
 * type for the loading spinner item toolbar item
 */
interface LoadingSpinnerItem extends DeprecatedToolbarItemBase {
  itemsToLoad: number;
  itemsLoaded?: number;
  hideTheWordLoading?: boolean;
  hideOutstandingCount?: boolean;
}

/**
 * type for the custom item toolbar item
 */
interface CustomItem extends DeprecatedToolbarItemBase {
  element?: JSX.Element;
}

/**
 * type for a min/max
 */
export interface MinMax {
  min: number;
  max: number;
}

/**
 * enum for all the toolbar items
 */
export enum ToolbarItemType {
  Switch = 'Switch',
  Popover = 'Popover',
  Dropdown = 'Dropdown',
  NumericInput = 'NumericInput',
  Button = 'Button',
  IntervalPicker = 'IntervalPicker',
  DateRangePicker = 'DateRangePicker',
  ButtonGroup = 'ButtonGroup',
  LabelValue = 'LabelValue',
  CheckboxList = 'CheckboxList',
  LoadingSpinner = 'LoadingSpinner',
  CustomItem = 'CustomItem'
}

export {
  ButtonGroupItem,
  ButtonItem,
  CheckboxDropdownItem,
  CustomItem,
  DateRangePickerItem,
  DateRangePickerTypes,
  DropdownItem,
  IntervalPickerItem,
  LabelValueItem,
  LoadingSpinnerItem,
  NumericInputItem,
  PopoverItem,
  SwitchItem
};
