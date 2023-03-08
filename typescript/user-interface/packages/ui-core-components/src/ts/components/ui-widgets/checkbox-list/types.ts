import type { Position } from '@blueprintjs/core';
import type { IconName } from '@blueprintjs/icons';
import type Immutable from 'immutable';
import type React from 'react';

export interface CheckboxListProps {
  checkboxEnum: any;
  enumToCheckedMap: Immutable.Map<any, boolean>;
  enumKeysToDisplayStrings?: Immutable.Map<string, string>;
  enumToColorMap?: Immutable.Map<any, string>;
  enumKeysToDividerMap?: Immutable.Map<string, boolean>;
  enumKeysToLabelMap?: Immutable.Map<string, string>;
  onChange(map: Immutable.Map<any, boolean>);
}

export type CheckboxEntry =
  | CheckboxListEntry
  | CheckboxListEntryIcon
  | CheckboxListEntryButton
  | CheckboxListEntryElement;

export interface SimpleCheckboxListProps {
  checkBoxListEntries: CheckboxEntry[];
  onChange?(name: string): void;
}

export interface SimpleCheckboxListState {
  checkboxEntriesMap: Record<string, CheckboxListEntry>;
}

export interface CheckboxListEntry {
  name: string;
  isChecked: boolean;
  color?: string;
  headerTitle?: string;
  divider?: boolean;
}

export interface CheckboxListEntryIcon extends CheckboxListEntry {
  iconName: IconName;
  iconColor?: string;
}

export interface CheckboxListEntryElement extends CheckboxListEntry {
  element: JSX.Element;
}

export interface CheckboxListEntryButton extends CheckboxListEntry {
  iconButton: IconButton;
}

export interface IconButton {
  iconName: IconName;
  popover?: {
    position: Position;
    usePortal?: boolean;
    minimal?: boolean;
    content?: string | JSX.Element;
  };
  onClick?(event: React.MouseEvent<HTMLButtonElement>): void;
}

/**
 * Determines if entry is a button
 *
 * @param entry checkbox entry
 * @returns a boolean
 */
export const isCheckboxListEntryButton = (entry: unknown): entry is CheckboxListEntryButton =>
  (entry as CheckboxListEntryButton).iconButton !== undefined;

/**
 * Determines if entry is a Icon
 *
 * @param entry checkbox entry
 * @returns a boolean
 */
export const isCheckboxListEntryIcon = (entry: unknown): entry is CheckboxListEntryIcon =>
  (entry as CheckboxListEntryIcon).iconName !== undefined;

/**
 * Determines if entry is an element
 *
 * @param entry checkbox entry
 * @returns a boolean
 */
export const isCheckboxListEntryElement = (entry: unknown): entry is CheckboxListEntryElement =>
  (entry as CheckboxListEntryElement).element !== undefined;
