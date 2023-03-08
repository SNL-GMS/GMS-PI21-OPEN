/* eslint-disable @typescript-eslint/no-empty-interface */
import type { AgEvent, Column as AgColumn } from 'ag-grid-community';

import type { ColumnApi } from './column-api';
import type { ColumnDefinition } from './column-definition';
import type { RowNode } from './row-node';
import type { TableApi } from './table-api';

export interface AgGridEvent extends AgEvent {
  api: TableApi;
  columnApi: ColumnApi;
}

export interface RowEvent<RowDataType> extends AgGridEvent {
  node: RowNode;
  data: RowDataType;
  rowIndex: number;
  rowPinned: string;
  context: any;
  // TODO verify the event type
  event?: MouseEvent | null;
}

export interface CellEvent<RowDataType, ContextDataType, CellValueType>
  extends RowEvent<RowDataType> {
  column: AgColumn;
  colDef: ColumnDefinition<RowDataType, ContextDataType, CellValueType, unknown, unknown>;
  value: CellValueType;
}

/** Wrapper interface class around ag-grid interface `CellContextMenuEvent` */
export interface CellContextMenuEvent<RowDataType, ContextDataType, CellValueType>
  extends CellEvent<RowDataType, ContextDataType, CellValueType> {}

/** Wrapper interface class around ag-grid interface `CellValueChangedEvent` */
export interface CellValueChangedEvent<RowDataType, ContextDataType, CellValueType>
  extends CellEvent<RowDataType, ContextDataType, CellValueType> {}

/** Wrapper interface class around ag-grid interface `CellDoubleClickedEvent` */
export interface CellDoubleClickedEvent<RowDataType, ContextDataType, CellValueType>
  extends CellEvent<RowDataType, ContextDataType, CellValueType> {}

/** Wrapper interface class around ag-grid interface `CellClickedEvent` */
export interface CellClickedEvent<RowDataType, ContextDataType, CellValueType>
  extends CellEvent<RowDataType, ContextDataType, CellValueType> {}

/** Wrapper interface class around ag-grid interface `RowClickedEvent` */
export interface RowClickedEvent<RowDataType, ContextDataType, CellValueType>
  extends CellEvent<RowDataType, ContextDataType, CellValueType> {}

/** Wrapper interface class around ag-grid interface `RowSelectedEvent` */
export interface RowSelectedEvent<RowDataType, ContextDataType, CellValueType>
  extends CellEvent<RowDataType, ContextDataType, CellValueType> {}

/** Wrapper interface class around ag-grid interface `GridReadyEvent` */
export interface GridReadyEvent extends AgGridEvent {}

/** Wrapper interface class around ag-grid interface `RowDataUpdatedEvent` */
export interface RowDataUpdatedEvent extends AgGridEvent {}

/** Wrapper interface class around ag-grid interface `SortChangedEvent` */
export interface SortChangedEvent extends AgGridEvent {}

/** Wrapper interface class around ag-grid interface `BodyScrollEvent` */
export interface BodyScrollEvent extends AgGridEvent {
  direction: string;
  left: number;
  top: number;
}

/** Wrapper interface class around ag-grid interface `PaginationChangedEvent` */
export interface PaginationChangedEvent extends AgGridEvent {
  animate?: boolean;
  keepRenderedRows?: boolean;
  newData?: boolean;
  newPage: boolean;
}
