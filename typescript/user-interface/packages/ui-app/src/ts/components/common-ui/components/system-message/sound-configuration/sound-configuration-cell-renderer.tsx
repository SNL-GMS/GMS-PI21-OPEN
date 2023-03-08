/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import type { ColumnDefinition } from '@gms/ui-core-components';
import { DropDown, TableCellRenderer } from '@gms/ui-core-components';
import cloneDeep from 'lodash/cloneDeep';
import * as React from 'react';

import { userPreferences } from '~components/common-ui/config/user-preferences';

import type { SoundConfigurationRendererParams, SoundConfigurationRow } from './types';

export type SoundConfigurationColumnDefinition = ColumnDefinition<
  SoundConfigurationRow,
  any,
  string | number,
  any,
  any
>;

const defaultColumnWidthPx = 200;

const getColumnPosition = (props: SoundConfigurationRendererParams): number | 'first' | 'last' => {
  const index = props.columnApi
    .getAllDisplayedColumns()
    .findIndex(c => c.getColId() === props.colDef.colId);
  // eslint-disable-next-line no-nested-ternary
  return index === 0
    ? 'first'
    : index === props.columnApi.getAllDisplayedColumns().length - 1
    ? 'last'
    : index;
};

export function NotificationsStatusCellRenderer(props: SoundConfigurationRendererParams) {
  return props.data?.hasNotificationStatusError ? (
    <TableCellRenderer
      className="sound-configuration--notification-status-no-file-error"
      data-col-position={getColumnPosition(props)}
      value="!"
      tooltipMsg={userPreferences.configuredAudibleNotificationFileNotFound(
        props.data.sound.selectedSound
      )}
      shouldCenterText
    >
      {props.children}
    </TableCellRenderer>
  ) : null;
}

export function SoundConfigurationCellRenderer(props: SoundConfigurationRendererParams) {
  return (
    <TableCellRenderer
      data-col-position={getColumnPosition(props)}
      value={props.valueFormatted ?? props.value}
    />
  );
}

export function SoundConfigurationDropdownRenderer(props: SoundConfigurationRendererParams) {
  if (!props.data || !props.data.sound) return null;

  const availableSounds = cloneDeep(props.data.sound.availableSounds);

  // if the configured sound happens to no longer be listed in the available sounds; add it
  // this will allow the selection to be correct and for an error to be displayed
  if (
    props.data.sound.selectedSound !== 'None' &&
    !availableSounds[props.data.sound.selectedSound]
  ) {
    availableSounds[props.data.sound.selectedSound] = props.data.sound.selectedSound;
  }
  return (
    <DropDown
      className="sound-configuration__dropdown"
      dropDownItems={availableSounds}
      value={props.data.sound.selectedSound}
      onMaybeValue={v => props.data.sound.onSelect(v)}
      data-cy="table-cell__select"
      title="Select a sound"
    />
  );
}

const headerCellBlockClass = 'soh-header-cell';

export const defaultColumnDefinition: SoundConfigurationColumnDefinition = {
  headerClass: [`${headerCellBlockClass}`, `${headerCellBlockClass}--neutral`],
  width: defaultColumnWidthPx,
  sortable: true,
  filter: true,
  disableStaticMarkupForHeaderComponentFramework: true,
  disableStaticMarkupForCellRendererFramework: true,
  cellRendererFramework: SoundConfigurationCellRenderer
};
