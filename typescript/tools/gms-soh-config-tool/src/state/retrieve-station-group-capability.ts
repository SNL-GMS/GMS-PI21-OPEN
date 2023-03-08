import { QueryReturnValue } from '@reduxjs/toolkit/dist/query/baseQueryTypes';
import {
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/dist/query/react';
import { MaybePromise } from '@reduxjs/toolkit/dist/query/tsHelpers';
import uniqueId from 'lodash/uniqueId';
import {
  OperatorType,
  RollupEntry,
  RollupType,
} from './station-controls-slice';

export interface RetrieveStationGroupCapabilityQueryProps {
  stationName: string;
  groupNames: string[];
  channelNames: string[];
}

/**
 * Type of data returned from query after being processed for station group capability
 */
export interface StationGroupCapability {
  stationName: string;
  groupName: string;
  defaultRollup: RollupEntry;
}

export type RollupOperatorOperands = {
  operatorType: OperatorType;
  channelOperands?: string[];
  sohMonitorTypeOperands?: string[];
  goodThreshold?: number;
  marginalThreshold?: number;
  rollupOperatorOperands?: RollupOperatorOperands[];
};

export type StationGroupCapabilityQueryResults = {
  channelsToStationRollupOperator: {
    operatorType: OperatorType;
    goodThreshold?: number;
    marginalThreshold?: number;
    rollupOperatorOperands?: RollupOperatorOperands[];
    channelOperands?: string[];
  };
};

const convertQueryEntryToRollup = (
  rollupOperatorOperands: RollupOperatorOperands
): RollupEntry => {
  return {
    id: uniqueId(),
    rollupType: rollupOperatorOperands.rollupOperatorOperands
      ? RollupType.ROLLUP_OF_ROLLUPS
      : RollupType.ROLLUP_OF_CHANNELS,
    operatorType: rollupOperatorOperands.operatorType,
    rollups: rollupOperatorOperands.rollupOperatorOperands
      ? rollupOperatorOperands.rollupOperatorOperands.map(
          (rollupOperatorOperand) =>
            convertQueryEntryToRollup(rollupOperatorOperand)
        )
      : undefined,
    threshold: {
      goodThreshold: rollupOperatorOperands.goodThreshold ?? 1,
      marginalThreshold: rollupOperatorOperands.marginalThreshold ?? 0,
    },
    channels: rollupOperatorOperands.channelOperands ?? [],
  };
};

const convertQueryDataToRollup = (
  queryData: StationGroupCapabilityQueryResults,
  channelNames: string[]
): RollupEntry => {
  let rollupEntry: RollupEntry = {
    id: `default ${uniqueId()}`,
    rollupType: queryData.channelsToStationRollupOperator.rollupOperatorOperands
      ? RollupType.ROLLUP_OF_ROLLUPS
      : RollupType.ROLLUP_OF_CHANNELS,
    operatorType: queryData.channelsToStationRollupOperator.operatorType,
    rollups: queryData.channelsToStationRollupOperator.rollupOperatorOperands
      ? queryData.channelsToStationRollupOperator.rollupOperatorOperands.map(
          (rollupOperatorOperand) =>
            convertQueryEntryToRollup(rollupOperatorOperand)
        )
      : undefined,
    threshold: {
      goodThreshold:
        queryData.channelsToStationRollupOperator.goodThreshold ?? 1,
      marginalThreshold:
        queryData.channelsToStationRollupOperator.marginalThreshold ?? 0,
    },
    channels: channelNames,
  };

  return rollupEntry;
};

/**
 * Function that returns an array of promises where each promise is a query to the config service
 * to get the station group capability info
 *
 * @param stationName name of the station
 * @param stationGroups list of groups to get data for
 * @param baseQuery base query function from rtk
 * @returns array of promises object for station group capability
 */
export const retrieveStationGroupsCapability = async (
  stationName: string,
  stationGroups: string[],
  channelNames: string[],
  baseQuery: (
    arg: string | FetchArgs
  ) => MaybePromise<
    QueryReturnValue<
      StationGroupCapabilityQueryResults,
      FetchBaseQueryError,
      {}
    >
  >
): Promise<StationGroupCapability[]> => {
  try {
    return await Promise.all<StationGroupCapability>(
      stationGroups.map(async (stationGroup) => {
        const result = await baseQuery({
          method: 'post',
          url: `/ui-processing-configuration-service/resolve`,
          headers: {
            accept: 'application/json',
            'Content-Type': 'application/json',
          },
          body: {
            configName: 'soh-control.station-capability-rollup',
            selectors: [
              {
                criterion: 'StationGroupName',
                value: stationGroup,
              },
              {
                criterion: 'StationName',
                value: stationName,
              },
            ],
          },
        });

        if (!result.data) {
          throw new Error(JSON.stringify(result.error));
        }

        return {
          stationName: stationName,
          groupName: stationGroup,
          defaultRollup: convertQueryDataToRollup(result.data, channelNames),
        };
      })
    );
  } catch (e) {
    console.error(e);
    throw e;
  }
};
