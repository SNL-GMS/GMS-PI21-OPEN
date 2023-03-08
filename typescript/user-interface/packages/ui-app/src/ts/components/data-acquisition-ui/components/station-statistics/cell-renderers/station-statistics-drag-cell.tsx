/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import * as React from 'react';

import { DragCell } from '~components/data-acquisition-ui/shared/table/drag-cell';

import { StationStatisticsContext } from '../station-statistics-context';

const rowClass = 'ag-row';

export interface StationStatisticsDragCellProps {
  stationId: string;
}

export const findClosestRow = (e: React.DragEvent): HTMLElement => {
  if (e.target instanceof Element) {
    return e.target.closest(`.${rowClass}`);
  }
  return undefined;
};

export const StationStatisticsDragCell: React.FunctionComponent<React.PropsWithChildren<
  StationStatisticsDragCellProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  const context = React.useContext(StationStatisticsContext);
  return (
    <DragCell
      getSelectedStationIds={() => context.selectedStationIds}
      setSelectedStationIds={ids => context.setSelectedStationIds(ids)}
      stationId={props?.stationId}
      getSingleDragImage={findClosestRow}
    >
      {props.children}
    </DragCell>
  );
};
