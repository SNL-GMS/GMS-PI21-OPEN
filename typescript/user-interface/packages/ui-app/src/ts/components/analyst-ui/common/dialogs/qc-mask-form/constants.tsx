import {
  DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION,
  secondsToString
} from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import React from 'react';

import type { QcMaskHistoryRow } from '../types';

/**
 * Column definitions for the overlapping mask table.
 */
// TODO define the generic types for the column definition
export const MASK_HISTORY_COLUMN_DEFINITIONS: ColumnDefinition<
  QcMaskHistoryRow,
  any,
  any,
  any,
  any
>[] = [
  {
    headerName: '',
    field: 'color',
    cellStyle: { 'text-align': 'left', 'vertical-align': 'middle' },
    width: 30,
    // eslint-disable-next-line react/display-name
    cellRendererFramework: e => (
      <div
        style={{
          height: '10px',
          width: '20px',
          backgroundColor: e.data.color.toString(),
          marginTop: '4px'
        }}
      />
    )
  },
  {
    headerName: 'Category',
    field: 'category',
    cellStyle: { 'text-align': 'left' },
    width: 130
  },
  {
    headerName: 'Type',
    field: 'type',
    cellStyle: { 'text-align': 'left' },
    width: 130
  },
  {
    headerName: 'Start time',
    field: 'startTime',
    cellStyle: { 'text-align': 'left' },
    width: 170,
    valueFormatter: e =>
      secondsToString(e.data.startTime, DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
  },
  {
    headerName: 'End time',
    field: 'endTime',
    cellStyle: { 'text-align': 'left' },
    width: 170,
    valueFormatter: e =>
      secondsToString(e.data.endTime, DATE_TIME_FORMAT_WITH_FRACTIONAL_SECOND_PRECISION)
  },
  {
    headerName: 'Rationale',
    field: 'rationale',
    cellStyle: { 'text-align': 'left' },
    width: 300
  }
];
