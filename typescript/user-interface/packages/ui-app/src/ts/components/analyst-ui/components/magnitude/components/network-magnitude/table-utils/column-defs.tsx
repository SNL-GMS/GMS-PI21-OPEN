import { LegacyEventTypes } from '@gms/common-model';
import { setDecimalPrecision, uuid } from '@gms/common-util';
import type { ColumnGroupDefinition } from '@gms/ui-core-components';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';

import { magnitudeColumnRepeatedArguments } from '~analyst-ui/components/magnitude/types';
import { systemConfig } from '~analyst-ui/config';

import { SetCircleTick } from './cell-renderer-frameworks';

/**
 * Gets data from the row parameters based off of magnitude type and access key
 *
 * @param params ag grid params
 * @param magnitudeType mag type to get data for
 * @param key key of the data in the object
 */
function getValueFromParams(params, magnitudeType: LegacyEventTypes.MagnitudeType, key: string) {
  return params.data.dataForMagnitude.get(magnitudeType)[key];
}

/**
 * Formats to two decimal places if it is a number. If not, return a '-'
 *
 * @param params the ag-grid params object
 */
function formatToTwoDecimals(params) {
  // eslint-disable-next-line no-restricted-globals
  return !params.value || isNaN(params.value) ? '-' : setDecimalPrecision(params.value, 2);
}

/**
 * If we get a number return it even if it's zero. Otherwise return '-'.
 * This handles null and undefined correctly (both will return '-').
 *
 * @param params the ag-grid params object
 */
function formatNumberOrDash(params) {
  // eslint-disable-next-line no-nested-ternary
  return params.value ? params.value : params.value === 0 ? params.value : '-';
}

/**
 * Generates the column definitions for each magnitude type
 *
 * @param magnitudeType Magnitude type to generate for
 * @param displayedMagnitudeTypes mapping of magnitude type to whether or not to hide that mag
 */
function generateColumnsPerMagnitude(
  magnitudeType: LegacyEventTypes.MagnitudeType,
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes
) {
  return {
    headerName: systemConfig.magnitudeTypeToDisplayName.get(magnitudeType),
    children: [
      {
        headerName: 'Mag',
        valueGetter: params =>
          getValueFromParams(params, magnitudeType, 'magnitude') !== undefined
            ? getValueFromParams(params, magnitudeType, 'magnitude')
            : '-',
        cellStyle: { 'text-align': 'right' },
        width: 60,
        id: uuid.asString(),
        hide: !displayedMagnitudeTypes[magnitudeType],
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: formatToTwoDecimals
      },
      {
        headerName: 'Std. Dev.',
        valueGetter: params =>
          getValueFromParams(params, magnitudeType, 'stdDeviation') !== undefined
            ? getValueFromParams(params, magnitudeType, 'stdDeviation')
            : '-',
        cellStyle: { 'text-align': 'right' },
        width: 80,
        id: uuid.asString(),
        hide: !displayedMagnitudeTypes[magnitudeType],
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: formatToTwoDecimals
      },
      {
        headerName: '# Def stations',
        valueGetter: params =>
          getValueFromParams(params, magnitudeType, 'numberOfDefiningStations'),
        cellStyle: { 'text-align': 'right' },
        width: 140,
        id: uuid.asString(),
        hide: !displayedMagnitudeTypes[magnitudeType],
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: params =>
          // eslint-disable-next-line no-nested-ternary
          params.value ? params.value : params.value === 0 ? params.value : '-'
      },
      {
        headerName: '# Non-def stations',
        valueGetter: params =>
          getValueFromParams(params, magnitudeType, 'numberOfNonDefiningStations'),
        cellStyle: { 'text-align': 'right' },
        width: 161,
        id: uuid.asString(),
        hide: !displayedMagnitudeTypes[magnitudeType],
        ...magnitudeColumnRepeatedArguments,
        valueFormatter: formatNumberOrDash
      }
    ]
  };
}

/**
 * Generates column defs
 *
 * @param displayedNetworkMagnitudeTypes a mapping of magnitude type to whether or not its visible
 */
export function generateNetworkMagnitudeColumnDefs(
  displayedNetworkMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes
): ColumnGroupDefinition[] {
  const columnDefs: ColumnGroupDefinition[] = [
    {
      headerName: 'Network magnitude',
      children: [
        {
          headerName: 'Location Restraint',
          field: 'location',
          cellStyle: () => ({
            display: 'flex',
            'justify-content': 'left',
            'align-items': 'left'
          }),
          width: 150,
          pinned: true,
          ...magnitudeColumnRepeatedArguments
        },
        {
          headerName: 'Preferred',
          field: 'isPreferred',
          cellStyle: () => ({
            display: 'flex',
            'justify-content': 'center',
            'padding-left': '9px',
            'padding-top': '3px'
          }),
          width: 119,
          pinned: true,
          ...magnitudeColumnRepeatedArguments,
          cellRendererFramework: SetCircleTick
        }
      ]
    }
  ];
  const magnitudeHeaderGroups: ColumnGroupDefinition[] = Object.keys(
    LegacyEventTypes.MagnitudeType
  ).map(key =>
    generateColumnsPerMagnitude(LegacyEventTypes.MagnitudeType[key], displayedNetworkMagnitudeTypes)
  );

  return [...columnDefs, ...magnitudeHeaderGroups];
}
