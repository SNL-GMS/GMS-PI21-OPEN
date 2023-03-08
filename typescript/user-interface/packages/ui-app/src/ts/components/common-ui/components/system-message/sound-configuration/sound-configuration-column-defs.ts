import type { ColumnDefinition } from '@gms/ui-core-components';

import {
  NotificationsStatusCellRenderer,
  SoundConfigurationDropdownRenderer
} from './sound-configuration-cell-renderer';
import type { SoundConfigurationRow } from './types';

export const columnDefs: ColumnDefinition<
  SoundConfigurationRow,
  unknown,
  string | number,
  unknown,
  unknown
>[] = [
  {
    headerName: '',
    field: 'hasNotificationStatusError',
    width: 40,
    minWidth: 40,
    resizable: false,
    suppressMovable: true,
    cellRendererFramework: NotificationsStatusCellRenderer,
    sortable: false,
    suppressMenu: true
  },
  {
    headerName: 'Sound',
    field: 'sound',
    cellRendererFramework: SoundConfigurationDropdownRenderer
  },
  {
    headerName: 'Category',
    field: 'category'
  },
  {
    headerName: 'Subcategory',
    field: 'subcategory'
  },
  {
    headerName: 'Severity',
    field: 'severity'
  },
  {
    headerName: 'Message',
    field: 'message',
    flex: 1,
    minWidth: 300
  }
];
