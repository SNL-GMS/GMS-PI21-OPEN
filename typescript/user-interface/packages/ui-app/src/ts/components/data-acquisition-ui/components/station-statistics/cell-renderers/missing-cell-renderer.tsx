/* eslint-disable react/prop-types */
import type { NumberCellRendererParams } from '@gms/ui-core-components';
import { PercentBar } from '@gms/ui-core-components';
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
 * Creates a missing cell, including percent bar child
 */
function MissingCellRenderer(props) {
  const { data, isStationCell } = props;
  return (
    <StationStatisticsTableDataContext.Consumer>
      {context => {
        const data2 = context.data.find(d => d.id === data.id);
        if (!data2) return null;

        const stationName = data2.stationData?.stationName;
        const dataReceivedStatus = getDataReceivedStatus(data2.stationMissing);

        // If it is station missing, set to that value; otherwise, use channel missing
        const titleToUse = isStationCell
          ? data2.stationMissing?.toString()
          : setTooltip(data2.channelMissing);

        // If it is station missing, it is non-contributing; otherwise, figure out channel rollup
        const cellStatusToUse = isStationCell
          ? CellStatus.NON_CONTRIBUTING
          : getCellStatus(data2.channelMissing?.status, data2.channelMissing?.isContributing);

        // If it is station missing, set to data received status above; otherwise, figure out channel status
        const dataReceivedStatusToUse = isStationCell
          ? dataReceivedStatus
          : getDataReceivedStatus(data2.channelMissing);

        // If it is station missing, format stationMissing; otherwise, format channel value
        const valueToUse = isStationCell
          ? formatSohValue(data2.stationMissing)
          : formatSohValue(data2.channelMissing?.value);

        // If it is station missing, set stationMissing percentage; otherwise, use channel percentage
        const percentageToUse = isStationCell ? data2.stationMissing : data2.channelMissing?.value;

        return (
          <StationStatisticsDragCell stationId={stationName}>
            <div title={`${titleToUse}`} data-cy="missing-cell">
              <SohRollupCell
                // eslint-disable-next-line react/jsx-props-no-spreading
                {...props}
                className="table-cell--numeric"
                cellStatus={cellStatusToUse}
                dataReceivedStatus={dataReceivedStatusToUse}
                stationId={`${stationName}`}
                value={valueToUse}
              >
                <PercentBar percentage={percentageToUse} />
              </SohRollupCell>
            </div>
          </StationStatisticsDragCell>
        );
      }}
    </StationStatisticsTableDataContext.Consumer>
  );
}

export function ChannelMissingCellRenderer(props: NumberCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <MissingCellRenderer {...props} isStationCell={false} />
  );
}

export function StationMissingCellRenderer(props: NumberCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <MissingCellRenderer {...props} isStationCell />
  );
}
