import type {
  CellRendererParams,
  ColumnDefinition,
  Row,
  TooltipParams
} from '@gms/ui-core-components';
import React from 'react';

import { digitPrecision } from '../fk-util';

/** Interface that defines a cell of data */
export interface DataCell {
  value: number;
  uncertainty: number | undefined;
}

/** Interface for defining a row in the properties table */
export interface PropertiesRow extends Row {
  id: string;
  description: string;
  peak: DataCell | undefined;
  predicted: DataCell | undefined;
  selected: DataCell | undefined;
  residual: DataCell | undefined;
}

/** Returns true if the params passed in are for the Fstat/Power/Residual cell */
const isFstatOrPowerResidualCell = (params: any) =>
  params.colDef.headerName === 'Residual' &&
  (params.data.id === 'Fstat' || params.data.id === 'Power');

/**
 * Number formatter
 *
 * @param value Number to format
 * @param uncertainty Uncertainty value ot format
 */
const formatValueUncertaintyPair = (value: number, uncertainty: number): string =>
  `${value.toFixed(digitPrecision)} (\u00B1 ${uncertainty.toFixed(digitPrecision)})`;

const labelCellRenderer: React.FunctionComponent<any> = props =>
  props.data.id === 'Slowness' ? (
    <span style={{ position: 'relative', top: '-0.4em' }}>
      {props.value} (<sup>s</sup>/<sub>°</sub>)
    </span>
  ) : (
    props.value
  );

/** Formats the table data cells */
const formatCell = (params: CellRendererParams<DataCell, any, DataCell, any, any>): string => {
  if (isFstatOrPowerResidualCell(params)) {
    return 'N/A';
  }
  return params.value && params.value.value ? params.value.value.toFixed(digitPrecision) : '-';
};

/** Formats the table data tooltips */
const formatTooltip = (params: TooltipParams): any =>
  // eslint-disable-next-line no-nested-ternary
  params.value && params.value.value && params.value.uncertainty
    ? formatValueUncertaintyPair(params.value.value, params.value.uncertainty)
    : params.value && params.value.value
    ? params.value.value.toFixed(digitPrecision)
    : '-';

/** Custom comparator to compare data cells */
const dataCellComparator = (valueA: DataCell, valueB: DataCell): number =>
  valueA.value - valueB.value;

/** Hard-coded columns for table */
// TODO define the generic types for the column definition
export const columnDefs: ColumnDefinition<DataCell, any, DataCell, any, any>[] = [
  {
    headerName: '',
    field: 'description',
    width: 100,
    resizable: false,
    sortable: false,
    filter: false,
    cellRendererFramework: labelCellRenderer
  },
  {
    headerName: 'Peak',
    field: 'peak',
    width: 80,
    cellStyle: { 'text-align': 'right' },
    resizable: true,
    sortable: true,
    filter: false,
    tooltipValueGetter: formatTooltip,
    // TODO should just implement formatValue
    cellRenderer: formatCell,
    comparator: dataCellComparator
  },
  {
    headerName: 'Predicted',
    field: 'predicted',
    width: 80,
    cellStyle: { 'text-align': 'right' },
    resizable: true,
    sortable: true,
    filter: false,
    tooltipValueGetter: formatTooltip,
    cellRenderer: formatCell,
    comparator: dataCellComparator,
    hide: true
  },
  {
    headerName: 'Selected',
    field: 'selected',
    cellStyle: { 'text-align': 'right' },
    width: 80,
    resizable: true,
    sortable: true,
    filter: false,
    tooltipValueGetter: formatTooltip,
    cellRenderer: formatCell,
    comparator: dataCellComparator
  },
  {
    headerName: 'Residual',
    field: 'residual',
    cellStyle: params =>
      isFstatOrPowerResidualCell(params)
        ? { 'text-align': 'right', color: 'grey' }
        : { 'text-align': 'right' },
    width: 85,
    resizable: true,
    sortable: true,
    filter: false,
    tooltipValueGetter: formatTooltip,
    cellRenderer: formatCell,
    comparator: dataCellComparator
  }
];
