/* eslint-disable @typescript-eslint/ban-types */
/**
 * ! Here is where types are defined so that when using the table component have access to more specific types
 * ! that are generic
 */

import type { CellRendererParams } from './cell-renderer';
import type { ColumnDefinition, ValueGetterParams } from './column-definition';

/**
 * Column definition - value is of type string
 */
export type StringColumnDefinition = ColumnDefinition<
  { id: string },
  {},
  string,
  CellRendererParams<any, any, any, any, any>,
  {}
>;

/**
 * Column definition - value is of type number
 */
export type NumberColumnDefinition = ColumnDefinition<
  { id: string },
  {},
  number,
  CellRendererParams<any, any, any, any, any>,
  {}
>;

/**
 * Column value getter params - value is of type string
 */
export type StringValueGetterParams = ValueGetterParams<
  { id: string },
  {},
  string,
  CellRendererParams<any, any, any, any, any>,
  {}
>;

/**
 * Column value getter - value is of type string
 */
export type StringValueGetter = (params: StringValueGetterParams) => string;

/**
 * Column value getter params - value is of type number
 */
export type NumberValueGetterParams = ValueGetterParams<
  { id: string },
  {},
  number,
  CellRendererParams<any, any, any, any, any>,
  {}
>;
/**
 * Column value getter - value is of type number
 */
export type NumberValueGetter = (params: NumberValueGetterParams) => number;

/**
 * Cell renderer params - value is of type string
 */
export type StringCellRendererParams = CellRendererParams<
  { id: string },
  {},
  string,
  CellRendererParams<any, any, any, any, any>,
  {}
>;

/**
 * Cell renderer params - value is of type number
 */
export type NumberCellRendererParams = CellRendererParams<
  { id: string },
  {},
  number,
  CellRendererParams<any, any, any, any, any>,
  {}
>;

// export type TooltipValueGetterParams =
