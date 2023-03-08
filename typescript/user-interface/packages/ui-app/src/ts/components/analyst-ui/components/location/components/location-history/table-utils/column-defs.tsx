import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';

import type { LocationHistoryRow } from '../types';
import { LocationHistoryCheckBox, LocationSetSwitch } from './cell-renderer-frameworks';

/** The save fieled name */
export const SAVE_FIELD_NAME = 'save';

/** The preferred fieled name */
export const PREFERRED_FIELD_NAME = 'preferred';

/**
 * Column Definitions for Transfer Gaps list
 */
// TODO define the generic types for the column definition
export const columnDefs: ColumnDefinition<LocationHistoryRow, any, any, any, any>[] = [
  {
    headerName: 'ASBDUSA',
    field: 'locationSetId',
    cellRendererFramework: LocationSetSwitch,
    sortable: true,
    hide: true
  },
  {
    headerName: 'Type',
    field: 'locType',
    resizable: true,
    width: 90,
    cellStyle: { 'text-align': 'left' },
    headerTooltip: 'Event Location Type (Standard or Master)'
  },
  {
    headerName: 'Lat (\u00B0)',
    field: 'lat',
    resizable: true,
    cellStyle: { 'text-align': 'right' },
    width: 70,
    valueFormatter: e => setDecimalPrecision(e.data.lat),
    headerTooltip: 'Latitude of event location'
  },
  {
    headerName: 'Lon (\u00B0)',
    field: 'lon',
    resizable: true,
    cellStyle: { 'text-align': 'right' },
    width: 70,
    valueFormatter: e => setDecimalPrecision(e.data.lon),
    headerTooltip: 'Longitude of event location'
  },
  {
    headerName: 'Depth (km)',
    field: 'depth',
    resizable: true,
    cellStyle: { 'text-align': 'right' },
    width: 90,
    valueFormatter: e => setDecimalPrecision(e.data.depth),
    headerTooltip: 'Depth of event location'
  },
  {
    headerName: 'Time',
    field: 'time',
    resizable: true,
    cellStyle: { 'text-align': 'right' },
    width: 210,
    headerTooltip: 'Time of event'
  },
  {
    headerName: 'Restraint',
    field: 'restraint',
    resizable: true,
    width: 140,
    cellStyle: { 'text-align': 'left' },
    headerTooltip: 'Depth restraint of location calculation'
  },
  {
    headerName: 'Semi Major (km)',
    field: 'smajax',
    resizable: true,
    width: 122,
    cellStyle: { 'text-align': 'right' },
    valueFormatter: e => (e.data.smajax ? setDecimalPrecision(e.data.smajax) : '-'),
    headerTooltip: 'Semi-major axis length of error ellipse'
  },
  {
    headerName: 'Semi Minor (km)',
    field: 'sminax',
    resizable: true,
    width: 122,
    cellStyle: { 'text-align': 'right' },
    valueFormatter: e => (e.data.sminax ? setDecimalPrecision(e.data.sminax) : '-'),
    headerTooltip: 'Semi-minor axis length of error ellipse'
  },
  {
    headerName: 'Strike (\u00B0)',
    field: 'strike',
    resizable: true,
    width: 70,
    cellStyle: { 'text-align': 'right' },
    valueFormatter: e => (e.data.strike ? setDecimalPrecision(e.data.strike) : '-'),
    headerTooltip: 'Strike of major axis of error ellipse'
  },
  {
    headerName: 'Std Dev',
    field: 'stdev',
    resizable: true,
    width: 70,
    cellStyle: { 'text-align': 'right' },
    valueFormatter: e => (e.data.stdev ? setDecimalPrecision(e.data.stdev) : '-'),
    headerTooltip: 'Standard deviation of the observed'
  },
  {
    headerName: 'Count',
    field: 'count',
    resizable: true,
    hide: true,
    width: 95,
    cellStyle: { 'text-align': 'right' },
    sortable: true,
    sort: 'desc'
  },
  {
    headerName: 'Preferred',
    field: PREFERRED_FIELD_NAME,
    cellStyle: {
      display: 'flex',
      'justify-content': 'center'
    },
    cellRendererFramework: LocationHistoryCheckBox,
    width: 80,
    headerTooltip: 'Mark an event location as preferred'
  }
];
export const autoGroupColumnDef = {
  headerName: 'Save',
  field: SAVE_FIELD_NAME,
  width: 65,
  cellRendererFramework: LocationSetSwitch,
  comparator: (a, b) => {
    if (a === null || b === null) {
      return b - a;
    }
    return 0;
  },
  headerTooltip: 'Save this location set when the event is saved'
};
