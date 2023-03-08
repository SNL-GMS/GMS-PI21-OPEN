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
 * Creates an environment cell, including percent bar child
 */
function EnvironmentCellRenderer(props) {
  const { data, isStationCell } = props;
  return (
    <StationStatisticsTableDataContext.Consumer>
      {context => {
        const data2 = context.data.find(d => d.id === data.id);
        if (!data2) return null;

        const stationName = data2.stationData?.stationName;
        const dataReceivedStatus = getDataReceivedStatus(data2.stationEnvironment);

        // If it is station environment, set to that value; otherwise, use channel environment
        const titleToUse = isStationCell
          ? data2.stationEnvironment?.toString()
          : setTooltip(data2.channelEnvironment);

        // If it is station environment, it is non-contributing; otherwise, figure out channel rollup
        const cellStatusToUse = isStationCell
          ? CellStatus.NON_CONTRIBUTING
          : getCellStatus(
              data2.channelEnvironment?.status,
              data2.channelEnvironment?.isContributing
            );

        // If it is station environment, set to data received status above; otherwise, figure out channel status
        const dataReceivedStatusToUse = isStationCell
          ? dataReceivedStatus
          : getDataReceivedStatus(data2.channelEnvironment);

        // If it is station environment, format stationEnvironment; otherwise, format channel value
        const valueToUse = isStationCell
          ? formatSohValue(data2.stationEnvironment)
          : formatSohValue(data2.channelEnvironment?.value);

        // If it is station environment, set stationEnvironment percentage; otherwise, use channel percentage
        const percentageToUse = isStationCell
          ? data2.stationEnvironment
          : data2.channelEnvironment?.value;

        return (
          <StationStatisticsDragCell stationId={stationName}>
            <div title={`${titleToUse}`} data-cy="environment-cell">
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

export function ChannelEnvironmentCellRenderer(props: NumberCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <EnvironmentCellRenderer {...props} isStationCell={false} />
  );
}

export function StationEnvironmentCellRenderer(props: NumberCellRendererParams) {
  return (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <EnvironmentCellRenderer {...props} isStationCell />
  );
}
