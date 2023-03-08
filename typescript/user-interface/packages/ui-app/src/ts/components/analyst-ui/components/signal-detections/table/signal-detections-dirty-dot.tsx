import type { CellRendererParams } from '@gms/ui-core-components';
import { getColumnPosition, TableCellRenderer } from '@gms/ui-core-components';
import React from 'react';

import type { SignalDetectionRow } from '../types';

/**
 * Cell renderer to render the dirty dot column
 */
export const DirtyDotCellRenderer: React.FunctionComponent<CellRendererParams<
  SignalDetectionRow,
  unknown,
  unknown,
  unknown,
  unknown
  // eslint-disable-next-line react/function-component-definition
>> = (props: CellRendererParams<SignalDetectionRow, unknown, unknown, unknown, unknown>) => {
  const value = '';
  return (
    <TableCellRenderer
      data-col-position={getColumnPosition<SignalDetectionRow>(props)}
      value={value}
      isNumeric={false}
      tooltipMsg={value}
    />
  );
};
