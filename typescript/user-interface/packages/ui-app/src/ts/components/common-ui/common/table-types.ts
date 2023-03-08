import type { ColumnDefinition } from '@gms/ui-core-components';
import { TableCellRendererFramework } from '@gms/ui-core-components';

/**
 * Constants
 */
export const INVALID_CELL_TEXT = 'Unknown';
export const smallCellWidthPx = 105;
export const medCellWidthPx = 200;
export const largeCellWidthPx = 300;

/**
 * @returns returns the default column definition for a table
 */
export function defaultColumnDefinition<T>(): ColumnDefinition<
  T,
  unknown,
  unknown,
  unknown,
  unknown
> {
  return {
    headerClass: 'table-header-cell',
    width: smallCellWidthPx,
    sortable: true,
    filter: true,
    resizable: true,
    disableStaticMarkupForHeaderComponentFramework: false,
    disableStaticMarkupForCellRendererFramework: true,
    sortingOrder: ['asc', 'desc', null],
    cellRendererFramework: TableCellRendererFramework,
    lockVisible: false,
    minWidth: 30
  };
}
