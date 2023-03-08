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
 * Creates a timeliness cell as a solid cell
 */
function TimelinessCellRenderer(props) {
  const { data, isStationCell } = props;
  return (
    <StationStatisticsTableDataContext.Consumer>
      {context => {
        const data2 = context.data.find(d => d.id === data.id);
        if (!data2) return null;

        const stationName = data2.stationData?.stationName;
        const dataReceivedStatus = getDataReceivedStatus(data2.stationTimeliness);

        // If it is station timeliness, set to that value; otherwise, use channel timeliness
        const titleToUse = isStationCell
          ? data2.stationTimeliness?.toString()
          : setTooltip(data2.channelTimeliness);

        // If it is station timeliness, it is non-contributing; otherwise, figure out channel rollup
        const cellStatusToUse = isStationCell
          ? CellStatus.NON_CONTRIBUTING
          : getCellStatus(data2.channelTimeliness?.status, data2.channelTimeliness?.isContributing);

        // If it is station timeliness, set to data received status above; otherwise, figure out channel status
        const dataReceivedStatusToUse = isStationCell
          ? dataReceivedStatus
          : getDataReceivedStatus(data2.channelTimeliness);

        // If it is station timeliness, format stationTimeliness; otherwise, format channel value
        const valueToUse = isStationCell
          ? formatSohValue(data2.stationTimeliness)
          : formatSohValue(data2.channelTimeliness?.value);

        return (
          <StationStatisticsDragCell stationId={stationName}>
            <div title={`${titleToUse}`} data-cy="timeliness-cell">
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

export function ChannelTimelinessCellRenderer(props: NumberCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <TimelinessCellRenderer {...props} isStationCell={false} />
  );
}

export function StationTimelinessCellRenderer(props: NumberCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <TimelinessCellRenderer {...props} isStationCell />
  );
}
