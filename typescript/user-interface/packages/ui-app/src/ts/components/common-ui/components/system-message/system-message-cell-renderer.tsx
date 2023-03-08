/* eslint-disable react/destructuring-assignment */
import { TableCellRenderer } from '@gms/ui-core-components';
import * as React from 'react';

import type { SystemMessageCellRendererParams } from './types';

function MessageCellRenderer(props: SystemMessageCellRendererParams) {
  if (!props.data) {
    return null;
  }
  const index = props.columnApi
    .getAllDisplayedColumns()
    .findIndex(c => c.getColId() === props.colDef.colId);
  const columnPosition =
    // eslint-disable-next-line no-nested-ternary
    index === 0
      ? 'first'
      : index === props.columnApi.getAllDisplayedColumns().length - 1
      ? 'last'
      : index;
  return (
    <TableCellRenderer
      className="system-cell"
      data-severity={props.data.severity}
      data-col-position={columnPosition}
      value={props.valueFormatted ?? props.value}
    />
  );
}

type SystemMessageCellRendererMemoProps = Readonly<
  React.PropsWithChildren<SystemMessageCellRendererParams>
>;

const MemoizedSystemMessageCellRenderer = React.memo(
  MessageCellRenderer,
  (prevProps: SystemMessageCellRendererMemoProps, nextProps: SystemMessageCellRendererMemoProps) =>
    prevProps.data?.id === nextProps.data?.id
);

export function SystemMessageCellRenderer(props: SystemMessageCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <MemoizedSystemMessageCellRenderer {...props} />
  );
}
