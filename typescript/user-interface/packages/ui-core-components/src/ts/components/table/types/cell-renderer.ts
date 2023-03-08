/* eslint-disable @typescript-eslint/no-empty-interface */
import type { ICellRendererParams as AgICellRendererParams, IComponent } from 'ag-grid-community';

import type { ColumnApi } from './column-api';
import type { ColumnDefinition } from './column-definition';
import type { RowNode } from './row-node';
import type { TableApi } from './table-api';

/** Wrapper interface class around ag-grid interface `ICellRendererParams` */
export interface CellRendererParams<
  RowDataType,
  ContextDataType,
  CellValueType,
  CellRendererParamsType,
  HeaderRendererParamsType
> extends AgICellRendererParams {
  children?: React.ReactNode;

  /** value to be rendered */
  value: CellValueType;

  /** value to be rendered formatted */
  valueFormatted: any;

  /** the rows data */
  data: RowDataType;

  /** row rows row node */
  node: RowNode;

  /** the cells column definition */
  colDef: ColumnDefinition<
    RowDataType,
    ContextDataType,
    CellValueType,
    CellRendererParamsType,
    HeaderRendererParamsType
  >;

  /** the grid API */
  api: TableApi;

  /** grid column API */
  columnApi: ColumnApi;

  /** the grid's context */
  context: ContextDataType;

  /** convenience function to get most recent up to date value */
  getValue(): CellValueType;

  /** convenience to set the value */
  setValue(value: CellValueType): void;

  /** convenience to format a value using the columns formatter */
  formatValue(value: CellValueType): any;
}

export interface CellRenderer {
  /**
   * Get the cell to refresh. Return true if successful.
   * Return false if not (or you don't have refresh logic),
   * then the grid will refresh the cell for you.
   */
  refresh(params: any): boolean;
}

export interface CellRendererComp<
  RowDataType,
  ContextDataType,
  CellValueType,
  CellRendererParamsType,
  HeaderRendererParamsType
> extends CellRenderer,
    IComponent<
      CellRendererParams<
        RowDataType,
        ContextDataType,
        CellValueType,
        CellRendererParamsType,
        HeaderRendererParamsType
      >
    > {}

export interface CellRendererFunc<
  RowDataType,
  ContextDataType,
  CellValueType,
  CellRendererParamsType,
  HeaderRendererParamsType
> {
  (
    params: CellRendererParams<
      RowDataType,
      ContextDataType,
      CellValueType,
      CellRendererParamsType,
      HeaderRendererParamsType
    >
  ): HTMLElement | string;
}
