import { defaultTo, setDecimalPrecision } from '@gms/common-util';
import type { RowNode } from '@gms/ui-core-components/lib/components';
import { EventFilters, EventsColumn } from '@gms/ui-state';
import Immutable from 'immutable';

import { messageConfig } from '~analyst-ui/config/message-config';

import { userPreferences } from '../config/user-preferences';
import { INVALID_CELL_TEXT } from './table-types';

/**
 * Returns the height of a row, based on the user preferences, plus a border.
 * This helps get around a linter bug that doesn't see types for values in preferences
 */
export const getRowHeightWithBorder: () => number = () => {
  const defaultBorderSize = 4;
  const rowHeight: number = userPreferences.tableRowHeightPx;
  return rowHeight + defaultBorderSize;
};

/**
 * Returns the height of a row, based on the user preferences, plus a border.
 * This helps get around a linter bug that doesn't see types for values in preferences
 */
export const getHeaderHeight: () => number = () => {
  const extraHeight = 12;
  const rowHeight: number = userPreferences.tableHeaderHeightPx;
  return rowHeight * 2 + extraHeight;
};

/**
 * Returns the height of a multi row header.
 * This helps get around a linter bug that doesn't see types for values in preferences
 */
export const getMultiLineHeaderHeight: (numLines: number) => number = (numLines: number) => {
  const extraHeight = 12;
  const rowHeight: number = userPreferences.tableHeaderHeightPx;
  return rowHeight * numLines + extraHeight;
};

/**
 * This function allows our tables to correctly sort columns that hold numeric values in strings
 *
 * @param valueA The first value in the cells to be compared. Typically sorts are done on these values only.
 * @param valueB The second value in the cells to be compared. Typically sorts are done on these values only.
 */
export function numericStringComparator(valueA: string, valueB: string): number {
  const adjustedA =
    !valueA || valueA === INVALID_CELL_TEXT || Number.isNaN(parseFloat(valueA))
      ? -Number.MAX_VALUE
      : parseFloat(valueA);
  const adjustedB =
    !valueB || valueB === INVALID_CELL_TEXT || Number.isNaN(parseFloat(valueB))
      ? -Number.MAX_VALUE
      : parseFloat(valueB);
  if (adjustedA === adjustedB) return 0;
  return adjustedA > adjustedB ? 1 : -1;
}

/**
 * This function allows our tables to correctly sort columns without caring about case
 *
 * @param valueA The first value in the cells to be compared. Typically sorts are done on these values only.
 * @param valueB The second value in the cells to be compared. Typically sorts are done on these values only.
 */
export function caseInsensitiveComparator(valueA: string, valueB: string): number {
  return valueA.toLowerCase().localeCompare(valueB.toLowerCase());
}

/**
 * This function allows our tables to correctly sort columns that format values down to a single decimal place
 *
 * @param valueA The first value in the cells to be compared. Typically sorts are done on these values only.
 * @param valueB The second value in the cells to be compared. Typically sorts are done on these values only.
 */
export function singleDecimalComparator(valueA: number, valueB: number): number {
  const adjustedA = setDecimalPrecision(valueA, 1);
  const adjustedB = setDecimalPrecision(valueB, 1);
  if (adjustedA === adjustedB) return 0;
  return adjustedA > adjustedB ? 1 : -1;
}

/**
 * Returns @param num with a maximum of three decimal places, rounded
 * to be displayed in tables
 * Returns invalidCellText if passed null or undefined (which is currently 'Unknown')
 *
 * @param num
 * @returns the num maximum of three decimal places, rounded, as a string
 */
export function formatNumberForDisplayMaxThreeDecimalPlaces(num: number): string {
  if (!num && num !== 0) return messageConfig.invalidCellText;
  return parseFloat(num.toFixed(3)).toString();
}

/**
 * Returns @param num with exactly three decimal places, rounded,
 * will pad zeroes as decimal places. Warning: the resulting number's formatting does not take into consideration significant figures,
 * nor do any trailing zeroes imply that any number of significant figures are present. This is purely decorative as per UX guidance
 * to be displayed in tables
 * Returns invalidCellText if passed null or undefined (which is currently 'Unknown')
 *
 * @param num
 * @returns the num with exactly three decimal places
 */
export function formatNumberForDisplayFixedThreeDecimalPlaces(num: number): string {
  return defaultTo(setDecimalPrecision(num, 3), messageConfig.invalidCellText);
}

/**
 * Takes a string input, and makes sure there is something to display
 * If there is nothing, returns invalidCellText, which is currently: 'Unknown'
 * If there is text, returns the text unchanged
 *
 * @param text
 */
export function getTableCellStringValue(text: string): string {
  return defaultTo(text, messageConfig.invalidCellText);
}

/**
 * Given a row node, updates selection to match the provided boolean {@param selected}
 *
 * @param rowNode
 * @param selected
 */
export function setRowNodeSelection(rowNode: RowNode, selected: boolean): RowNode {
  rowNode.setSelected(selected);
  return rowNode;
}

/**
 * Takes a column definition {@link Immutable.Map} and converts it back to a {@link Record}.
 */
export const convertMapToObject = (
  columnArguments: Immutable.Map<string, boolean>
): Record<string, boolean> => {
  const newObject: Record<string, boolean> = {} as Record<string, boolean>;
  columnArguments.forEach((value: boolean, key: string) => {
    newObject[key] = value;
  });

  return newObject;
};

/**
 * Takes the column definition records from redux and converts it to a {@link Immutable.Map}.
 */
export const convertObjectToEventsColumnMap = (
  columnArguments: Record<string, boolean>
): Immutable.Map<EventsColumn, boolean> => {
  const notableValues = [...Object.keys(columnArguments)];
  return Immutable.Map<EventsColumn, boolean>([
    ...Object.values(EventsColumn)
      .filter(v => notableValues.includes(v))
      .map<[EventsColumn, boolean]>(v => [v, columnArguments[v]])
  ]);
};

/**
 * Takes the column definition records from redux and converts it to a {@link Immutable.Map}.
 */
export const convertObjectToEventFiltersMap = (
  columnArguments: Record<string, boolean>
): Immutable.Map<EventFilters, boolean> => {
  const notableValues = [...Object.keys(columnArguments)];
  return Immutable.Map<EventFilters, boolean>([
    ...Object.values(EventFilters)
      .filter(v => notableValues.includes(v))
      .map<[EventFilters, boolean]>(v => [v, columnArguments[v]])
  ]);
};
