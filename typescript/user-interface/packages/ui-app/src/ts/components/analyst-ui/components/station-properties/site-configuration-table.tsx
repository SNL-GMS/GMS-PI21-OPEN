import { Classes, H4 } from '@blueprintjs/core';
import type { ChannelTypes } from '@gms/common-model';
import { delayExecutionReturnClearTimeout, formatTimeForDisplay } from '@gms/common-util';
import { Table } from '@gms/ui-core-components';
import classNames from 'classnames';
import React from 'react';

import { getChannelGroupTypeForDisplay } from '~analyst-ui/components/station-properties/station-properties-utils';
import { messageConfig } from '~analyst-ui/config/message-config';
import { defaultColumnDefinition } from '~common-ui/common/table-types';
import {
  formatNumberForDisplayFixedThreeDecimalPlaces,
  getHeaderHeight,
  getRowHeightWithBorder,
  getTableCellStringValue
} from '~common-ui/common/table-utils';

import { siteConfigurationColumnDefs } from './column-definitions';
import type {
  SiteColumn,
  SiteConfigurationRow,
  SiteConfigurationRowClickedEvent,
  SiteConfigurationTableProps
} from './types';

function formatRows(sites: ChannelTypes.ChannelGroup[]): SiteConfigurationRow[] {
  return sites.map(
    (chan: ChannelTypes.ChannelGroup, idx: number): SiteConfigurationRow => {
      return {
        id: `${idx}`,
        name: getTableCellStringValue(chan.name),
        description: chan.description
          ? getTableCellStringValue(chan.description.replace(/_/g, ' '))
          : messageConfig.invalidCellText,
        effectiveAt: formatTimeForDisplay(chan.effectiveAt),
        effectiveUntil: chan.effectiveUntil
          ? formatTimeForDisplay(chan.effectiveUntil)
          : messageConfig.latestVersionCellText,
        elevationKm: formatNumberForDisplayFixedThreeDecimalPlaces(chan.location?.elevationKm),
        depthKm: formatNumberForDisplayFixedThreeDecimalPlaces(chan.location?.depthKm),
        latitudeDegrees: formatNumberForDisplayFixedThreeDecimalPlaces(
          chan.location?.latitudeDegrees
        ),
        longitudeDegrees: formatNumberForDisplayFixedThreeDecimalPlaces(
          chan.location?.longitudeDegrees
        ),
        type: getChannelGroupTypeForDisplay(chan.type)
      };
    }
  );
}

/**
 * Site configuration table component which takes in stations and uses column definitions to
 * create and ag grid table
 */
// eslint-disable-next-line react/function-component-definition
export const SiteConfigurationTable: React.FunctionComponent<SiteConfigurationTableProps> = (
  props: SiteConfigurationTableProps
) => {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { station, columnsToDisplay, selectedChannelGroup, onRowSelection } = props;
  const tableRef = React.useRef<Table<SiteConfigurationRow, unknown>>(null);

  const sites: ChannelTypes.ChannelGroup[] = station.channelGroups;
  const rowSites: SiteConfigurationRow[] = formatRows(sites);

  React.useEffect(() => {
    return delayExecutionReturnClearTimeout(() => {
      if (tableRef && tableRef.current) {
        tableRef.current.updateVisibleColumns<SiteColumn>(columnsToDisplay);
      }
    });
  }, [columnsToDisplay]);

  const defaultColDefRef = React.useRef(defaultColumnDefinition<SiteConfigurationRow>());

  const columnDefsRef = React.useRef(siteConfigurationColumnDefs);

  React.useEffect(() => {
    return delayExecutionReturnClearTimeout(() => {
      if (tableRef && tableRef.current && tableRef.current.getTableApi()) {
        tableRef.current
          .getTableApi()
          .forEachNode(node =>
            node.data.name === selectedChannelGroup
              ? node.setSelected(true)
              : node.setSelected(false)
          );
      }
    });
  }, [selectedChannelGroup]);

  return (
    <div
      className={classNames(
        'site-configuration-table',
        'ag-theme-dark',
        'station-properties-table',
        'with-separated-rows-color'
      )}
      data-cy="site-configuration-table"
    >
      <H4 className={classNames(`${Classes.HEADING} `)}>Channel Group Configuration</H4>
      <div className={classNames(['station-properties-table__wrapper'])}>
        <Table<SiteConfigurationRow, unknown>
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
          rowData={rowSites}
          rowHeight={getRowHeightWithBorder()}
          headerHeight={getHeaderHeight()}
          rowDeselection
          rowSelection="single"
          suppressCellFocus
          suppressDragLeaveHidesColumns
          onRowClicked={(event: SiteConfigurationRowClickedEvent) => onRowSelection(event)}
          overlayNoRowsTemplate="No Sites to display"
          suppressContextMenu
        />
      </div>
    </div>
  );
};
