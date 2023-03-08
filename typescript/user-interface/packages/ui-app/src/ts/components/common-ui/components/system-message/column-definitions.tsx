import type { StringValueGetter } from '@gms/ui-core-components';

import { SystemMessageCellRenderer } from './system-message-cell-renderer';
import type { SystemMessageColumnDefinition } from './types';

/** flag to enable or disable debug of columns - shows all columns */
const DEBUG_COLUMNS = false;

/**
 * The header template for the ag-grid columns.
 * This templates removes the sort order from displaying in the column headers.
 */
const headerTemplate = `
  <div class="ag-cell-label-container" role="presentation">
      <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>
      <div ref="eLabel" class="ag-header-cell-label" role="presentation">
          <span ref="eText" class="ag-header-cell-text" role="columnheader"></span>
          <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>
          <!-- <span ref="eSortOrder" class="ag-header-icon ag-sort-order"></span> -->
          <span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon"></span>
          <span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon"></span>
          <span ref="eSortNone" class="ag-header-icon ag-sort-none-icon"></span>
      </div>
  </div>
`;

/**
 * The default column definition settings.
 */
export const defaultColumnDefinition: SystemMessageColumnDefinition = {
  cellRendererFramework: SystemMessageCellRenderer,
  disableStaticMarkupForHeaderComponentFramework: true,
  disableStaticMarkupForCellRendererFramework: true,
  sortable: true,
  filter: false,
  headerComponentParams: { template: headerTemplate }
};

/**
 * The id column definition.
 */
const idColumnDef = (): SystemMessageColumnDefinition => ({
  colId: 'id',
  field: 'id',
  headerName: 'ID',
  headerTooltip: 'ID',
  width: 10,
  hide: !DEBUG_COLUMNS
});

/**
 * The timestamp column definition.
 *
 * @param valueGetter the value getter (number) for retrieving the time value from the system message
 */
const timestampColumnDef = (valueGetter: StringValueGetter): SystemMessageColumnDefinition => ({
  colId: 'time',
  headerName: 'Timestamp',
  headerTooltip: 'Timestamp',
  sort: 'asc',
  width: 200,
  valueGetter,
  cellRendererFramework: SystemMessageCellRenderer
});

/**
 * The category column definition.
 *
 * @param valueGetter the value getter (string) for retrieving the category value from the system message
 */
const categoryColumnDef = (valueGetter: StringValueGetter): SystemMessageColumnDefinition => ({
  colId: 'category',
  headerName: 'Category',
  headerTooltip: 'Category',
  width: 110,
  valueGetter,
  cellRendererFramework: SystemMessageCellRenderer,
  comparator: (a, b) => a?.localeCompare(b)
});

/**
 * The subcategory column definition.
 *
 * @param valueGetter the value getter (string) for retrieving the subcategory value from the system message
 */
const subCategoryColumnDef = (valueGetter: StringValueGetter): SystemMessageColumnDefinition => ({
  colId: 'subCategory',
  headerName: 'Subcategory',
  headerTooltip: 'Subcategory',
  width: 150,
  valueGetter,
  cellRendererFramework: SystemMessageCellRenderer,
  comparator: (a, b) => a?.localeCompare(b)
});

/**
 * The severity column definition.
 *
 * @param valueGetter the value getter (string) for retrieving the severity value from the system message
 */
const severityColumnDef = (valueGetter: StringValueGetter): SystemMessageColumnDefinition => ({
  colId: 'severity',
  headerName: 'Severity',
  headerTooltip: 'Severity',
  width: 105,
  valueGetter,
  cellRendererFramework: SystemMessageCellRenderer,
  comparator: (a, b) => a?.localeCompare(b)
});

/**
 * The message column definition.
 *
 * @param valueGetter the value getter (string) for retrieving the message value from the system message
 */
const messageColumnDef = (valueGetter: StringValueGetter): SystemMessageColumnDefinition => ({
  colId: 'message',
  headerName: 'Message',
  headerTooltip: 'Message',
  flex: 1,
  minWidth: 200,
  valueGetter,
  cellRendererFramework: SystemMessageCellRenderer,
  comparator: (a, b) => a?.localeCompare(b)
});

/**
 * Builds the column definitions.
 *
 * @param timeValueGetter the value getter (number) for retrieving the time value from the system message
 * @param categoryValueGetter the value getter (string) for retrieving the category value from the system message
 * @param subCategoryValueGetter the value getter (string) for retrieving the subcategory value from the system message
 * @param severityValueGetter the value getter (string) for retrieving the severity value from the system message
 * @param messageValueGetter the value getter (string) for retrieving the message value from the system message
 */
export const buildColumnDefs = (
  // indexValueGetter: NumberValueGetter,
  timeValueGetter: StringValueGetter,
  categoryValueGetter: StringValueGetter,
  subCategoryValueGetter: StringValueGetter,
  severityValueGetter: StringValueGetter,
  messageValueGetter: StringValueGetter
): SystemMessageColumnDefinition[] => {
  const columnDefinitions: SystemMessageColumnDefinition[] = [];
  columnDefinitions.push(idColumnDef());
  columnDefinitions.push(timestampColumnDef(timeValueGetter));
  columnDefinitions.push(categoryColumnDef(categoryValueGetter));
  columnDefinitions.push(subCategoryColumnDef(subCategoryValueGetter));
  columnDefinitions.push(severityColumnDef(severityValueGetter));
  columnDefinitions.push(messageColumnDef(messageValueGetter));
  return columnDefinitions;
};
