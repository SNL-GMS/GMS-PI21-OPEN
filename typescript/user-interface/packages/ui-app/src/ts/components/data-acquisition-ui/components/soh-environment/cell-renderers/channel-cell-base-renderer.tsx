/* eslint-disable react/prop-types */
import * as React from 'react';

import { dataAcquisitionUIConfig } from '~components/data-acquisition-ui/config';
import {
  getCellStatus,
  getDataReceivedStatus,
  setTooltip
} from '~components/data-acquisition-ui/shared/table/utils';

import type { EnvironmentalSoh } from '../types';

const cellHeightPx = dataAcquisitionUIConfig.dataAcquisitionUserPreferences.tableRowHeightPx;
/**
 * Creates a soh-cell with appropriate styles and wraps it around the children
 * of this component.
 *
 * @param environmentalSoh the soh object that is used to create this cell
 */
export const ChannelCellBaseRenderer: React.FunctionComponent<React.PropsWithChildren<{
  environmentSoh: EnvironmentalSoh;
  // eslint-disable-next-line react/function-component-definition
}>> = ({ environmentSoh, children }) => (
  <div
    title={`${setTooltip(environmentSoh)}`}
    style={{
      height: `${cellHeightPx}px`
    }}
    data-cy="soh-cell"
    className="table-cell soh-cell table-cell__value--numeric"
    data-cell-status={getCellStatus(environmentSoh?.status, environmentSoh?.isContributing)}
    data-received-status={getDataReceivedStatus(environmentSoh) ?? ''}
  >
    {children}
  </div>
);
