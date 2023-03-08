/* eslint-disable react/prop-types */
import type { NumberCellRendererParams } from '@gms/ui-core-components';
import * as React from 'react';

import { SohRollupCell } from '~components/data-acquisition-ui/shared/table/soh-cell-renderers';
import {
  CellStatus,
  formatSohValue,
  getCellStatus,
  getDataReceivedStatus,
  setTooltip
} from '~components/data-acquisition-ui/shared/table/utils';

import { StationStatisticsTableDataContext } from '../station-statistics-table-context';
import { StationStatisticsDragCell } from './station-statistics-drag-cell';

/**
 * Creates a lag cell as a solid cell
 */
function LagCellRenderer(props) {
  return (
    <StationStatisticsTableDataContext.Consumer>
      {context => {
        const { data, isStationCell } = props;
        const data2 = context.data?.find(d => d.id === data?.id);
        if (!data2) return null;

        const stationName = data2.stationData?.stationName;
        const dataReceivedStatus = getDataReceivedStatus(data2.stationLag);

        // If it is station lag, set to that value; otherwise, use channel lag
        const titleToUse = isStationCell
          ? data2.stationLag?.toString()
          : setTooltip(data2.channelLag);

        // If it is station lag, it is non-contributing; otherwise, figure out channel rollup
        const cellStatusToUse = isStationCell
          ? CellStatus.NON_CONTRIBUTING
          : getCellStatus(data2.channelLag?.status, data2.channelLag?.isContributing);

        // If it is station lag, set to data received status above; otherwise, figure out channel status
        const dataReceivedStatusToUse = isStationCell
          ? dataReceivedStatus
          : getDataReceivedStatus(data2.channelLag);

        // If it is station lag, format stationLag; otherwise, format channel value
        const valueToUse = isStationCell
          ? formatSohValue(data2.stationLag)
          : formatSohValue(data2.channelLag?.value);

        return (
          <StationStatisticsDragCell stationId={stationName}>
            <div title={`${titleToUse}`} data-cy="lag-cell">
              <SohRollupCell
                // eslint-disable-next-line react/jsx-props-no-spreading
                {...props}
                className={`
                soh-cell--solid
                table-cell--numeric`}
                cellStatus={cellStatusToUse}
                dataReceivedStatus={dataReceivedStatusToUse}
                stationId={`${stationName}`}
                value={valueToUse}
              />
            </div>
          </StationStatisticsDragCell>
        );
      }}
    </StationStatisticsTableDataContext.Consumer>
  );
}

export function ChannelLagCellRenderer(props: NumberCellRendererParams) {
  // eslint-disable-next-line react/jsx-props-no-spreading
  return <LagCellRenderer {...props} isStationCell={false} />;
}

export function StationLagCellRenderer(props: NumberCellRendererParams) {
  // eslint-disable-next-line react/jsx-props-no-spreading
  return <LagCellRenderer {...props} isStationCell />;
}
