/* eslint-disable @typescript-eslint/no-empty-interface */
import type { RowNode as AgRowNode } from 'ag-grid-community';

/** Row Interface */
export interface Row {
  id: string;
}

/** Wrapper interface class around ag-grid interface `RowNode` */
export interface RowNode extends AgRowNode {}
