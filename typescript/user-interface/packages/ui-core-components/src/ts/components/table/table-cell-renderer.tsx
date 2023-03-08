/* eslint-disable react/prop-types */
import * as StringUtils from '@gms/common-util';
import { classList, getDataAttributesFromProps } from '@gms/ui-util';
import * as React from 'react';

import type { CellRendererParams } from './types/cell-renderer';

/**
 * Props for generic table cell renderer
 */
export interface TableCellRendererProps {
  cellValueClassName?: string;
  children?: JSX.Element | JSX.Element[] | string | React.ReactNode;
  className?: string;
  heightCSS?: string;
  isDate?: boolean;
  isNumeric?: boolean;
  leftChild?: JSX.Element;
  shouldCenterText?: boolean;
  tooltipMsg?: string;
  value?: string;
}

/**
 * Cell renderer for table cells. Accepts classes,
 * data-attributes, and a flag to indicate if it is numeric
 */
// eslint-disable-next-line react/function-component-definition
const TableCellComponent: React.FunctionComponent<TableCellRendererProps> = (
  props: TableCellRendererProps
) => {
  const dataAttributes = getDataAttributesFromProps(props);
  const {
    cellValueClassName,
    children,
    className,
    heightCSS,
    isDate,
    isNumeric,
    leftChild,
    shouldCenterText,
    tooltipMsg,
    value
  } = props;
  return (
    <div
      style={{
        height: heightCSS || '36px',
        overflow: 'hidden'
      }}
      data-cy="table-cell"
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...dataAttributes}
      className={`table-cell ${className ?? ''}`}
      title={tooltipMsg || value}
    >
      {leftChild}
      {value ? (
        <div
          className={classList(
            {
              'table-cell__value--date': isDate,
              'table-cell__value--numeric': isNumeric,
              'table-cell__value--center': shouldCenterText
            },
            `table-cell__value ${cellValueClassName ?? ''}`
          )}
          data-cy="table-cell__value"
        >
          <span>{value}</span>
        </div>
      ) : null}
      {children}
    </div>
  );
};

/**
 * Cell renderer for table cells. Accepts classes,
 * data-attributes, and a flag to indicate if it is numeric
 */
export const TableCellRenderer = React.memo(TableCellComponent);

/**
 * Generic column position method used to get the column position.
 *
 * @param props generic cell renderer props
 * @returns column position
 */
export function getColumnPosition<T>(
  props: CellRendererParams<T, unknown, unknown, unknown, unknown>
): number | 'first' | 'last' {
  const index = props.columnApi
    ?.getAllDisplayedColumns()
    ?.findIndex(c => c.getColId() === (props.colDef.colId ?? props.colDef.field));
  let retValue: number | 'first' | 'last' = index;
  if (index === 0) {
    retValue = 'first';
  } else if (index === (props.columnApi?.getAllDisplayedColumns()?.length ?? -Infinity) - 1) {
    retValue = 'last';
  }
  return retValue;
}

/**
 * Generic table cell renderer framework used for general renderer in column definitions
 *
 * @param props generic cell renderer props
 * @returns JSX.Element of what to render
 */
export function TableCellRendererFramework<T>(
  props: CellRendererParams<T, unknown, any, unknown, unknown>
): JSX.Element {
  const { valueFormatted, value, colDef } = props;
  const tooltipMsg =
    colDef && colDef.tooltipValueGetter
      ? colDef.tooltipValueGetter({ ...props, location: 'cell' })
      : undefined;
  return (
    <TableCellRenderer
      data-col-position={getColumnPosition<T>(props)}
      isDate={StringUtils.isDate(value?.toString())}
      isNumeric={StringUtils.isNumeric(value?.toString())}
      tooltipMsg={tooltipMsg ?? valueFormatted ?? value}
      value={valueFormatted ?? value}
    />
  );
}
