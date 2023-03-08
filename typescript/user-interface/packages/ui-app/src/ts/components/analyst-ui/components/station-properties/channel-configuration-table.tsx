import { Classes, H4 } from '@blueprintjs/core';
import type { ChannelTypes, ResponseTypes, StationTypes } from '@gms/common-model';
import { delayExecutionReturnClearTimeout, formatTimeForDisplay } from '@gms/common-util';
import { Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import React from 'react';

import {
  nonIdealStateLoadingStationDataQuery,
  nonIdealStateSelectChannelGroupRow
} from '~analyst-ui/components/station-properties/station-properties-non-ideal-states';
import {
  formatTimeShift,
  getChannelDataTypeForDisplay
} from '~analyst-ui/components/station-properties/station-properties-utils';
import { messageConfig } from '~analyst-ui/config/message-config';
import { defaultColumnDefinition } from '~common-ui/common/table-types';
import {
  formatNumberForDisplayFixedThreeDecimalPlaces,
  formatNumberForDisplayMaxThreeDecimalPlaces,
  getHeaderHeight,
  getRowHeightWithBorder,
  getTableCellStringValue
} from '~common-ui/common/table-utils';

import { ChannelColumnDefs } from './column-definitions';
import type {
  ChannelColumn,
  ChannelConfigurationRow,
  ChannelConfigurationTableProps
} from './types';

function parseChannelLocation(location: ChannelTypes.Location) {
  return {
    latitudeDegrees: location
      ? formatNumberForDisplayFixedThreeDecimalPlaces(location.latitudeDegrees)
      : messageConfig.invalidCellText,
    longitudeDegrees: location
      ? formatNumberForDisplayFixedThreeDecimalPlaces(location.longitudeDegrees)
      : messageConfig.invalidCellText,
    depthKm: location
      ? formatNumberForDisplayFixedThreeDecimalPlaces(location.depthKm)
      : messageConfig.invalidCellText,
    elevationKm: location
      ? formatNumberForDisplayFixedThreeDecimalPlaces(location.elevationKm)
      : messageConfig.invalidCellText
  };
}

function parseChannelResponse(response: ResponseTypes.Response) {
  return {
    calibrationFactor: response
      ? formatNumberForDisplayMaxThreeDecimalPlaces(response?.calibration?.calibrationFactor.value)
      : messageConfig.invalidCellText,
    calibrationPeriod: response
      ? formatNumberForDisplayMaxThreeDecimalPlaces(response?.calibration?.calibrationPeriodSec)
      : messageConfig.invalidCellText,
    calibrationStandardDeviation: response
      ? formatNumberForDisplayMaxThreeDecimalPlaces(
          response?.calibration?.calibrationFactor.standardDeviation
        )
      : messageConfig.invalidCellText,
    calibrationTimeShift: response
      ? formatTimeShift(response?.calibration?.calibrationTimeShift)
      : messageConfig.invalidCellText,
    calibrationEffectiveAt: response
      ? formatTimeForDisplay(response?.effectiveAt)
      : messageConfig.invalidCellText,
    calibrationResponseId: response
      ? getTableCellStringValue(response.id)
      : messageConfig.invalidCellText,
    fapResponseId: response
      ? getTableCellStringValue(response?.fapResponse?.id)
      : messageConfig.invalidCellText
  };
}

function formatChannelConfigurationTableRows(
  channels: ChannelTypes.Channel[],
  selectedStation: StationTypes.Station
): ChannelConfigurationRow[] {
  return channels.map(
    // eslint-disable-next-line complexity
    (chan: ChannelTypes.Channel, idx: number): ChannelConfigurationRow => {
      const relativePositions: Record<string, ChannelTypes.RelativePosition> =
        selectedStation.relativePositionsByChannel;
      const channelDisplacements = relativePositions[chan.name];
      const {
        calibrationEffectiveAt,
        calibrationFactor,
        calibrationPeriod,
        calibrationResponseId,
        calibrationStandardDeviation,
        calibrationTimeShift,
        fapResponseId
      } = parseChannelResponse(chan.response);
      const { latitudeDegrees, longitudeDegrees, depthKm, elevationKm } = parseChannelLocation(
        chan.location
      );
      return {
        id: `${idx}`,
        name: getTableCellStringValue(chan.name),
        channelBandType: getTableCellStringValue(chan.channelBandType),
        channelInstrumentType: getTableCellStringValue(chan.channelInstrumentType),
        channelOrientationType: getTableCellStringValue(chan.channelOrientationType),
        channelOrientationCode: getTableCellStringValue(chan.channelOrientationCode),
        channelDataType: getChannelDataTypeForDisplay(chan.channelDataType),
        nominalSampleRateHz: formatNumberForDisplayMaxThreeDecimalPlaces(chan.nominalSampleRateHz),
        description: chan.description
          ? getTableCellStringValue(chan.description.replace(/_/g, ' '))
          : messageConfig.invalidCellText,
        effectiveAt: formatTimeForDisplay(chan.effectiveAt),
        effectiveUntil: chan.effectiveUntil
          ? formatTimeForDisplay(chan.effectiveUntil)
          : messageConfig.latestVersionCellText,
        calibrationFactor,
        calibrationPeriod,
        calibrationStandardDeviation,
        calibrationTimeShift,
        calibrationEffectiveAt,
        calibrationResponseId,
        fapResponseId,
        orientationHorizontalDegrees: chan.orientationAngles
          ? formatNumberForDisplayMaxThreeDecimalPlaces(chan.orientationAngles.horizontalAngleDeg)
          : messageConfig.invalidCellText,
        orientationVerticalDegrees: chan.orientationAngles
          ? formatNumberForDisplayMaxThreeDecimalPlaces(chan.orientationAngles.verticalAngleDeg)
          : messageConfig.invalidCellText,
        latitudeDegrees,
        longitudeDegrees,
        depthKm,
        elevationKm,
        units: chan.units
          ? chan.units.slice(0, 1) + chan.units.slice(1).toLocaleLowerCase()
          : messageConfig.invalidCellText,
        northDisplacementKm: formatNumberForDisplayMaxThreeDecimalPlaces(
          channelDisplacements.northDisplacementKm
        ),
        verticalDisplacementKm: formatNumberForDisplayMaxThreeDecimalPlaces(
          channelDisplacements.verticalDisplacementKm
        ),
        eastDisplacementKm: formatNumberForDisplayMaxThreeDecimalPlaces(
          channelDisplacements.eastDisplacementKm
        )
      };
    }
  );
}

// eslint-disable-next-line react/function-component-definition
export const ChannelConfigurationTable: React.FunctionComponent<ChannelConfigurationTableProps> = (
  props: ChannelConfigurationTableProps
) => {
  const { channels, stationData, columnsToDisplay } = props;

  const tableRef = React.useRef<Table<ChannelConfigurationRow, unknown>>(null);

  const defaultColDefRef = React.useRef(defaultColumnDefinition<ChannelConfigurationRow>());

  const columnDefsRef = React.useRef(ChannelColumnDefs);
  React.useEffect(() => {
    return delayExecutionReturnClearTimeout(() => {
      if (tableRef && tableRef.current) {
        tableRef?.current?.updateVisibleColumns<ChannelColumn>(columnsToDisplay);
      }
    });
  }, [columnsToDisplay]);

  if (stationData && !channels) {
    return nonIdealStateSelectChannelGroupRow;
  }

  if (!stationData || !channels) {
    return nonIdealStateLoadingStationDataQuery;
  }
  const rowChannels: ChannelConfigurationRow[] = formatChannelConfigurationTableRows(
    channels,
    stationData
  );

  return (
    <div
      className={classNames(
        'channel-configuration-table',
        'ag-theme-dark',
        'station-properties-table',
        'with-separated-rows-color'
      )}
      data-cy="channel-configuration-table"
    >
      <H4 className={classNames(`${Classes.HEADING}`)}>Channel Configuration</H4>
      <div className={classNames(['station-properties-table__wrapper'])}>
        <Table<ChannelConfigurationRow, unknown>
          ref={ref => {
            tableRef.current = ref;
          }}
          context={{}}
          onGridReady={() => {
            if (tableRef && tableRef.current) {
              columnsToDisplay.forEach((shouldDisplay, columnName) => {
                tableRef.current?.setColumnVisible(columnName, shouldDisplay);
              });
            }
          }}
          defaultColDef={defaultColDefRef.current}
          columnDefs={columnDefsRef.current}
          rowData={rowChannels}
          rowHeight={getRowHeightWithBorder()}
          headerHeight={getHeaderHeight()}
          rowDeselection
          rowSelection="single"
          suppressCellFocus
          overlayNoRowsTemplate="No Channels to display"
          suppressContextMenu
          suppressDragLeaveHidesColumns
        />
      </div>
    </div>
  );
};
