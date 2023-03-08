/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { prettifyAllCapsEnumType, toSentenceCase } from '@gms/common-util';
import type { CellRendererParams } from '@gms/ui-core-components';
import { TooltipWrapper } from '@gms/ui-core-components';
import * as React from 'react';

import { SohRollupCell } from '~components/data-acquisition-ui/shared/table/soh-cell-renderers';
import {
  getCellStatus,
  getDataReceivedStatusRollup
} from '~components/data-acquisition-ui/shared/table/utils';

import type { EnvironmentalSoh, EnvironmentTableContext, EnvironmentTableRow } from '../types';
import { EnvironmentTableDataContext } from '../types';

export type MonitorTypeCellRendererParams = CellRendererParams<
  { id: string },
  EnvironmentTableContext,
  string,
  any,
  any
>;

const convertMapToArray = (map: Map<string, EnvironmentalSoh>) => {
  const arr: EnvironmentalSoh[] = [];
  map.forEach(envSoh => {
    arr.push(envSoh);
  });
  return arr;
};

/**
 * Renders a basic SohCell for a monitor-type cell
 *
 * @param data an EnvironmentTableRow to use to render this monitor cell
 */
export const MonitorTypeCellRendererBase: React.FunctionComponent<
  Partial<CellRendererParams<{ id: string }, any, string, any, any>> & EnvironmentTableRow
  // eslint-disable-next-line react/function-component-definition
> = props => (
  <div data-cy="monitor-type-cell">
    <SohRollupCell
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...props}
      className={`soh-cell__title
        soh-cell--solid
        soh-cell__title--${props.monitorStatus?.toLowerCase()}
        soh-cell--${props.monitorStatus?.toLowerCase()}`}
      value={prettifyAllCapsEnumType(props.monitorType, true)}
      cellStatus={getCellStatus(props.monitorStatus)}
      dataReceivedStatus={getDataReceivedStatusRollup(
        convertMapToArray(props.valueAndStatusByChannelName)
      )}
    />
  </div>
);

/**
 * Renders a `div` around the channel cell which is used to style the cell to indicate that it is selected or not.
 *
 * @param isSelected true if the cell should be marked as selected; false otherwise
 */
const SelectedMonitorRenderer: React.FunctionComponent<React.PropsWithChildren<{
  isSelected: boolean;
  // eslint-disable-next-line react/display-name
}>> = React.memo(props => (
  <div className={props.isSelected ? 'is-selected' : ''}>{props.children}</div>
));

/**
 * Renders a monitor-type cell with a tooltip containing the monitor-type name and the status.
 */
export function MonitorTypeCellRenderer(props: MonitorTypeCellRendererParams): JSX.Element {
  return (
    <EnvironmentTableDataContext.Consumer>
      {context => {
        const data = context.data.find(d => d.id === props.data.id);
        return data ? (
          <TooltipWrapper
            content={`${prettifyAllCapsEnumType(data.monitorType, true)} - Status: ${toSentenceCase(
              data.monitorStatus
            )}`}
          >
            <SelectedMonitorRenderer isSelected={data?.monitorIsSelected}>
              {/* eslint-disable-next-line react/jsx-props-no-spreading */}
              <MonitorTypeCellRendererBase {...props} {...data} />
            </SelectedMonitorRenderer>
          </TooltipWrapper>
        ) : undefined;
      }}
    </EnvironmentTableDataContext.Consumer>
  );
}
