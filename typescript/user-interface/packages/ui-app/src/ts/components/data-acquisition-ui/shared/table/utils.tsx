import { SohTypes } from '@gms/common-model';
import { isTimeStale, setDecimalPrecision } from '@gms/common-util';
import uniq from 'lodash/uniq';
import React from 'react';

import { commonClassNames } from '~components/common-ui/config/common-class-names';
import { dataAcquisitionUserPreferences } from '~components/data-acquisition-ui/config';
import { messageConfig } from '~components/data-acquisition-ui/config/message-config';

import {
  DisabledStationSohContextMenu,
  StationSohContextMenu
} from '../context-menus/stations-cell-context-menu';
import type { CellData } from './types';

const nonContributingTooltipMessage =
  messageConfig.tooltipMessages.stationStatistics.nonContributingCell;
const nullTooltipMessage = messageConfig.tooltipMessages.stationStatistics.nullCell;
const notReceivedTooltipMessage = messageConfig.tooltipMessages.stationStatistics.notReceivedCell;

/**
 * Returns the height of a row, based on the user preferences, plus a border.
 * This helps get around a linter bug that doesn't see types for values in preferences
 */
export const getRowHeightWithBorder: () => number = () => {
  const defaultBorderSize = 4;
  const rowHeight: number = dataAcquisitionUserPreferences.tableRowHeightPx;
  return rowHeight + defaultBorderSize;
};

/**
 * Returns the height of a row, based on the user preferences, plus a border.
 * This helps get around a linter bug that doesn't see types for values in preferences
 */
export const getHeaderHeight: () => number = () => {
  const extraHeight = 4;
  const rowHeight: number = getRowHeightWithBorder();
  return rowHeight + extraHeight;
};

export const formatSohValue = (value: number): string => {
  // eslint-disable-next-line no-restricted-globals
  if (isNaN(value) || value === null || value === undefined) {
    return 'Unknown';
  }
  return setDecimalPrecision(value);
};

export const sharedSohTableClasses = commonClassNames.sharedTableClasses;

/**
 * Checks if data is null or undefined
 *
 * @param data data to check
 */
export const isNullData = (data: CellData): boolean =>
  data?.value === null || data?.value === undefined;

/**
 * Checks if status is null or undefined
 *
 * @param data CellData to check
 */
export const isNullStatus = (data: CellData): boolean =>
  data?.status === null || data?.status === undefined;

export enum CellStatus {
  GOOD = 'good',
  MARGINAL = 'marginal',
  BAD = 'bad',
  NON_CONTRIBUTING = 'non-contributing'
}

export enum DataReceivedStatus {
  RECEIVED = 'received',
  NOT_ENOUGH_DATA = 'not-enough-data',
  NOT_RECEIVED = 'not-received'
}

const sohStatusMatchesCellStatus = (statusSummary: SohTypes.SohStatusSummary) =>
  Object.values<string>(CellStatus).includes(statusSummary?.toLowerCase());

/**
 * Return a CellStatus that determines the cell status, or non-contributing
 *
 * @param data the cell data to check
 */
export const getCellStatus = (
  status: SohTypes.SohStatusSummary,
  isContributing = true
): CellStatus | CellStatus.NON_CONTRIBUTING => {
  if (isContributing) {
    if (sohStatusMatchesCellStatus(status)) {
      return status.toLowerCase() as CellStatus;
    }
  }
  return CellStatus.NON_CONTRIBUTING;
};

/**
 * Interprets the cell data to determine if there were any data problems, and of what type.
 * If data is a number in the case of station stats, check if number is defined.
 *
 * @param data the cell data/number to check
 */
export const getDataReceivedStatus = (data: CellData | number): DataReceivedStatus => {
  if (typeof data === 'number') {
    return data !== undefined ? DataReceivedStatus.RECEIVED : DataReceivedStatus.NOT_ENOUGH_DATA;
  }
  // eslint-disable-next-line no-nested-ternary
  return isNullStatus(data)
    ? DataReceivedStatus.NOT_RECEIVED
    : isNullData(data)
    ? DataReceivedStatus.NOT_ENOUGH_DATA
    : DataReceivedStatus.RECEIVED;
};

/**
 * RECEIVED if any data was received.
 * NOT_ENOUGH_DATA if none were received and at least one has NOT_ENOUGH_DATA.
 * NOT_RECEIVED if none were ever received.
 *
 * @param data a cell to check
 */
export const getDataReceivedStatusRollup = (data: CellData[]): DataReceivedStatus =>
  data.reduce<DataReceivedStatus>((worstStatus: DataReceivedStatus, cell) => {
    const cellStatus = getDataReceivedStatus(cell);
    if (cellStatus === DataReceivedStatus.RECEIVED || worstStatus === DataReceivedStatus.RECEIVED) {
      return DataReceivedStatus.RECEIVED;
    }
    if (
      cellStatus === DataReceivedStatus.NOT_ENOUGH_DATA ||
      worstStatus === DataReceivedStatus.NOT_ENOUGH_DATA
    ) {
      return DataReceivedStatus.NOT_ENOUGH_DATA;
    }
    return DataReceivedStatus.NOT_RECEIVED;
  }, DataReceivedStatus.NOT_RECEIVED);

/**
 * Sets the tooltip based on contributing, null status or null value
 *
 * @param data CellData to check
 */
export const setTooltip = (data: CellData): string =>
  // eslint-disable-next-line no-nested-ternary
  isNullStatus(data)
    ? notReceivedTooltipMessage
    : // eslint-disable-next-line no-nested-ternary
    !data.isContributing
    ? nonContributingTooltipMessage
    : isNullData(data)
    ? nullTooltipMessage
    : '';

/**
 * Helper function to determine if any of the selected Station SOH are stale or
 * are already acknowledged
 *
 * @param stationNames selected
 * @returns boolean
 */
export const isAcknowledgeEnabled = (
  stationNames: string[],
  stationSohs: SohTypes.UiStationSoh[],
  sohStationStaleTimeMS: number
): boolean => {
  let isDisabled = false;
  uniq(stationNames).forEach((name: string) => {
    const soh = stationSohs.find(entry => entry.stationName === name);
    if (soh && soh.time) {
      isDisabled = isTimeStale(soh.time, sohStationStaleTimeMS)
        ? true
        : !soh.needsAcknowledgement || isDisabled;
    } else {
      isDisabled = true;
    }
  });
  return !isDisabled;
};

/**
 * Method returns the appropriate Station SOH Context Menu
 *
 * @param stationNames the selected station names
 * @param stationSohs the station soh data
 * @param sohStationStaleTimeMS the station stale timeout in milliseconds
 * @param acknowledgeCallback the callback for acknowledging
 * @return StationSohContextMenu
 */
export const acknowledgeContextMenu = (
  stationNames: string[],
  stationSohs: SohTypes.UiStationSoh[],
  sohStationStaleTimeMS: number,
  acknowledgeCallback: (stationIds: string[], comment?: string) => void
): JSX.Element => {
  const sohContextMenuProps = {
    stationNames,
    // eslint-disable-next-line @typescript-eslint/unbound-method
    acknowledgeCallback
  };
  return isAcknowledgeEnabled(stationNames, stationSohs, sohStationStaleTimeMS) ? (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <StationSohContextMenu {...sohContextMenuProps} />
  ) : (
    // eslint-disable-next-line react/jsx-props-no-spreading
    <DisabledStationSohContextMenu {...sohContextMenuProps} />
  );
};

/**
 * Custom comparator for comparing cell values.
 * Handles undefined
 *
 * @param a the first value
 * @param b the second value
 * @returns number indicating sort order (standard comparator return)
 */
export const compareCellValues = (a: number, b: number): number => {
  if (a === undefined && b === undefined) {
    return 0;
  }
  if (a === undefined) {
    return -1;
  }
  if (b === undefined) {
    return 1;
  }
  return a - b;
};

/**
 * Compares statuses
 *
 * @param a first soh status
 * @param b second soh status
 * @returns the worst status
 */
export const getWorseStatus = (
  a: SohTypes.SohStatusSummary,
  b: SohTypes.SohStatusSummary
): SohTypes.SohStatusSummary => {
  if (a === SohTypes.SohStatusSummary.BAD || b === SohTypes.SohStatusSummary.BAD) {
    return SohTypes.SohStatusSummary.BAD;
  }
  if (a === SohTypes.SohStatusSummary.MARGINAL || b === SohTypes.SohStatusSummary.MARGINAL) {
    return SohTypes.SohStatusSummary.MARGINAL;
  }
  if (a === SohTypes.SohStatusSummary.GOOD || b === SohTypes.SohStatusSummary.GOOD) {
    return SohTypes.SohStatusSummary.GOOD;
  }
  return SohTypes.SohStatusSummary.NONE;
};

export const getWorstCapabilityRollup = (
  groups: SohTypes.StationSohCapabilityStatus[]
): SohTypes.SohStatusSummary =>
  groups &&
  groups.length &&
  groups.reduce(
    (
      worstFound: SohTypes.SohStatusSummary,
      capabilityStatus: SohTypes.StationSohCapabilityStatus
    ) => getWorseStatus(worstFound, capabilityStatus?.sohStationCapability),
    SohTypes.SohStatusSummary.NONE
  );
