import type { ColumnDefinition } from '@gms/ui-core-components';

import { largeCellWidthPx, medCellWidthPx } from '~common-ui/common/table-types';
import { numericStringComparator } from '~common-ui/common/table-utils';

import { channelColumnsToDisplay, siteColumnsToDisplay } from './station-properties-utils';
import type { ChannelConfigurationRow, SiteConfigurationRow } from './types';
import {
  ChannelColumn,
  channelColumnDisplayStrings,
  SiteColumn,
  siteColumnDisplayStrings
} from './types';

function sharedColumns<T>(): ColumnDefinition<T, unknown, unknown, unknown, unknown>[] {
  return [
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.name),
      field: SiteColumn.name,
      headerTooltip: 'Name',
      cellClass: 'monospace',
      width: medCellWidthPx
    },
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.effectiveAt),
      field: SiteColumn.effectiveAt,
      headerTooltip: 'Effective at',
      width: medCellWidthPx
    },
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.effectiveUntil),
      field: SiteColumn.effectiveUntil,
      headerTooltip: 'Effective until',
      width: medCellWidthPx
    },
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.latitudeDegrees),
      field: SiteColumn.latitudeDegrees,
      headerTooltip: 'Latitude in degrees',
      comparator: numericStringComparator
    },
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.longitudeDegrees),
      field: SiteColumn.longitudeDegrees,
      headerTooltip: 'Longitude in degrees',
      comparator: numericStringComparator
    },
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.depthKm),
      field: SiteColumn.depthKm,
      headerTooltip: 'Depth in kilometers',
      comparator: numericStringComparator
    },
    {
      headerName: siteColumnDisplayStrings.get(SiteColumn.elevationKm),
      field: SiteColumn.elevationKm,
      headerTooltip: 'Elevation in kilometers',
      comparator: numericStringComparator
    }
  ];
}

export const siteConfigurationColumnDefs: ColumnDefinition<
  SiteConfigurationRow,
  unknown,
  unknown,
  unknown,
  unknown
>[] = [
  ...sharedColumns<SiteConfigurationRow>(),
  {
    headerName: siteColumnDisplayStrings.get(SiteColumn.type),
    field: SiteColumn.type,
    headerTooltip: 'Type',
    hide: !siteColumnsToDisplay.get(SiteColumn.type),
    width: medCellWidthPx
  },
  {
    headerName: siteColumnDisplayStrings.get(SiteColumn.description),
    field: SiteColumn.description,
    headerTooltip: 'Description',
    hide: !siteColumnsToDisplay.get(SiteColumn.description),
    width: largeCellWidthPx
  }
];

export const ChannelColumnDefs: ColumnDefinition<
  ChannelConfigurationRow,
  unknown,
  unknown,
  unknown,
  unknown
>[] = [
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.name),
    field: ChannelColumn.name,
    headerTooltip: 'Name',
    hide: !channelColumnsToDisplay.get(ChannelColumn.name),
    width: medCellWidthPx,
    cellClass: 'monospace'
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.effectiveAt),
    field: ChannelColumn.effectiveAt,
    headerTooltip: 'Effective at',
    hide: !channelColumnsToDisplay.get(ChannelColumn.effectiveAt),
    width: medCellWidthPx
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.effectiveUntil),
    field: ChannelColumn.effectiveUntil,
    headerTooltip: 'Effective until',
    hide: !channelColumnsToDisplay.get(ChannelColumn.effectiveUntil),
    width: medCellWidthPx
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.latitudeDegrees),
    field: ChannelColumn.latitudeDegrees,
    headerTooltip: 'Latitude in degrees',
    hide: !channelColumnsToDisplay.get(ChannelColumn.latitudeDegrees),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.longitudeDegrees),
    field: ChannelColumn.longitudeDegrees,
    headerTooltip: 'Longitude in degrees',
    hide: !channelColumnsToDisplay.get(ChannelColumn.longitudeDegrees),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.depthKm),
    field: ChannelColumn.depthKm,
    headerTooltip: 'Depth in kilometers',
    hide: !channelColumnsToDisplay.get(ChannelColumn.depthKm),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.elevationKm),
    field: ChannelColumn.elevationKm,
    headerTooltip: 'Elevation in kilometers',
    hide: !channelColumnsToDisplay.get(ChannelColumn.elevationKm),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.nominalSampleRateHz),
    field: ChannelColumn.nominalSampleRateHz,
    headerTooltip: 'Nominal sample rate in hertz',
    hide: !channelColumnsToDisplay.get(ChannelColumn.nominalSampleRateHz),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.units),
    field: ChannelColumn.units,
    hide: !channelColumnsToDisplay.get(ChannelColumn.units),
    headerTooltip: 'Units'
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.orientationHorizontalDegrees),
    field: ChannelColumn.orientationHorizontalDegrees,
    headerTooltip: 'Orientation horizontal angle in degrees',
    hide: !channelColumnsToDisplay.get(ChannelColumn.orientationHorizontalDegrees),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.orientationVerticalDegrees),
    field: ChannelColumn.orientationVerticalDegrees,
    headerTooltip: 'Orientation vertical angle in degrees',
    hide: !channelColumnsToDisplay.get(ChannelColumn.orientationVerticalDegrees),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.calibrationFactor),
    field: ChannelColumn.calibrationFactor,
    headerTooltip: 'Calibration factor in seconds',
    width: medCellWidthPx,
    hide: !channelColumnsToDisplay.get(ChannelColumn.calibrationFactor),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.calibrationPeriod),
    field: ChannelColumn.calibrationPeriod,
    headerTooltip: 'Calibration period in seconds',
    width: medCellWidthPx,
    hide: !channelColumnsToDisplay.get(ChannelColumn.calibrationPeriod),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.calibrationEffectiveAt),
    field: ChannelColumn.calibrationEffectiveAt,
    headerTooltip: 'Calibration effective at',
    width: largeCellWidthPx,
    hide: !channelColumnsToDisplay.get(ChannelColumn.calibrationEffectiveAt)
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.calibrationTimeShift),
    field: ChannelColumn.calibrationTimeShift,
    headerTooltip: 'Calibration time shift in seconds',
    width: medCellWidthPx,
    hide: !channelColumnsToDisplay.get(ChannelColumn.calibrationTimeShift),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.calibrationStandardDeviation),
    field: ChannelColumn.calibrationStandardDeviation,
    headerTooltip: 'Calibration standard deviation',
    width: medCellWidthPx,
    hide: !channelColumnsToDisplay.get(ChannelColumn.calibrationStandardDeviation),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.northDisplacementKm),
    field: ChannelColumn.northDisplacementKm,
    headerTooltip: 'North displacement in kilometers',
    hide: !channelColumnsToDisplay.get(ChannelColumn.northDisplacementKm),
    comparator: numericStringComparator
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.eastDisplacementKm),
    field: ChannelColumn.eastDisplacementKm,
    headerTooltip: 'East displacement in kilometers',
    hide: !channelColumnsToDisplay.get(ChannelColumn.eastDisplacementKm)
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.verticalDisplacementKm),
    field: ChannelColumn.verticalDisplacementKm,
    headerTooltip: 'Vertical displacement in kilometers',
    hide: !channelColumnsToDisplay.get(ChannelColumn.verticalDisplacementKm)
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.description),
    field: ChannelColumn.description,
    headerTooltip: 'Description',
    hide: !channelColumnsToDisplay.get(ChannelColumn.description),
    width: largeCellWidthPx
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.channelDataType),
    field: ChannelColumn.channelDataType,
    headerTooltip: 'Channel data type',
    hide: !channelColumnsToDisplay.get(ChannelColumn.channelDataType),
    width: medCellWidthPx
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.channelBandType),
    field: ChannelColumn.channelBandType,
    headerTooltip: 'Channel band type',
    hide: !channelColumnsToDisplay.get(ChannelColumn.channelBandType)
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.channelInstrumentType),
    field: ChannelColumn.channelInstrumentType,
    headerTooltip: 'Channel instrument type',
    hide: !channelColumnsToDisplay.get(ChannelColumn.channelInstrumentType)
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.channelOrientationCode),
    field: ChannelColumn.channelOrientationCode,
    headerTooltip: 'Channel orientation code',
    hide: !channelColumnsToDisplay.get(ChannelColumn.channelOrientationCode),
    cellClass: 'force-left-justification'
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.channelOrientationType),
    field: ChannelColumn.channelOrientationType,
    headerTooltip: 'Channel orientation type',
    hide: !channelColumnsToDisplay.get(ChannelColumn.channelOrientationType),
    width: medCellWidthPx
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.calibrationResponseId),
    field: ChannelColumn.calibrationResponseId,
    headerTooltip: 'Calibration response ID',
    hide: !channelColumnsToDisplay.get(ChannelColumn.calibrationResponseId),
    width: medCellWidthPx
  },
  {
    headerName: channelColumnDisplayStrings.get(ChannelColumn.fapResponseId),
    field: ChannelColumn.fapResponseId,
    headerTooltip: 'Frequency amplitude response ID',
    hide: !channelColumnsToDisplay.get(ChannelColumn.fapResponseId),
    width: medCellWidthPx
  }
];
