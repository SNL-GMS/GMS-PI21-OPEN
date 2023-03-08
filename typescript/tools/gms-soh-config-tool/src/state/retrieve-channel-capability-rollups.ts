import { QueryReturnValue } from '@reduxjs/toolkit/dist/query/baseQueryTypes';
import {
  FetchArgs,
  FetchBaseQueryError,
} from '@reduxjs/toolkit/dist/query/react';
import { MaybePromise } from '@reduxjs/toolkit/dist/query/tsHelpers';
import flatMap from 'lodash/flatMap';
import uniqueId from 'lodash/uniqueId';
import { RollupOperatorOperands } from './retrieve-station-group-capability';
import {
  OperatorType,
  RollupEntry,
  RollupType,
} from './station-controls-slice';

export interface RetrieveChannelCapabilityRollupQueryProps {
  stationName: string;
  groupNames: string[];
  channelNames: string[];
  allMonitorNames: string[];
}

/**
 * Type of data returned from query after being processed for channel capability rollup
 */
export interface ChannelCapabilityRollup {
  stationName: string;
  groupName: string;
  channelName: string;
  defaultRollup: RollupEntry;
}

export type ChannelCapabilityRollupQueryResults = {
  sohMonitorsToChannelRollupOperator: {
    operatorType: OperatorType;
    goodThreshold?: number;
    marginalThreshold?: number;
    rollupOperatorOperands?: RollupOperatorOperands[];
    sohMonitorTypeOperands?: string[];
  };
};

const convertQueryEntryToRollup = (
  rollupOperatorOperands: RollupOperatorOperands
): RollupEntry => {
  return {
    id: uniqueId(),
    rollupType: rollupOperatorOperands.rollupOperatorOperands
      ? RollupType.ROLLUP_OF_ROLLUPS
      : RollupType.ROLLUP_OF_MONITORS,
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
    monitors: rollupOperatorOperands.sohMonitorTypeOperands ?? [],
  };
};

const convertQueryDataToRollup = (
  queryData: ChannelCapabilityRollupQueryResults,
  monitorNames: string[]
): RollupEntry => {
  let rollupEntry: RollupEntry = {
    id: `default ${uniqueId()}`,
    rollupType: queryData.sohMonitorsToChannelRollupOperator
      .rollupOperatorOperands
      ? RollupType.ROLLUP_OF_ROLLUPS
      : RollupType.ROLLUP_OF_MONITORS,
    operatorType: queryData.sohMonitorsToChannelRollupOperator.operatorType,
    rollups: queryData.sohMonitorsToChannelRollupOperator.rollupOperatorOperands
      ? queryData.sohMonitorsToChannelRollupOperator.rollupOperatorOperands.map(
          (rollupOperatorOperand) =>
            convertQueryEntryToRollup(rollupOperatorOperand)
        )
      : undefined,
    threshold: {
      goodThreshold:
        queryData.sohMonitorsToChannelRollupOperator.goodThreshold ?? 1,
      marginalThreshold:
        queryData.sohMonitorsToChannelRollupOperator.marginalThreshold ?? 0,
    },
    monitors: monitorNames,
  };

  return rollupEntry;
};

/**
 * Function that returns an array of promises where each promise is a query to the config service
 * to get the channel capability rollup info
 *
 * @param stationName name of the station
 * @param groupNames list of station groups
 * @param channelNames list of channel names to get data for
 * @param channelNames list of all monitor names
 * @param baseQuery base query function from rtk
 * @returns array of promises object for station group capability
 */
export const retrieveChannelCapabilityRollup = async (
  stationName: string,
  groupNames: string[],
  channelNames: string[],
  allMonitorNames: string[],
  baseQuery: (
    arg: string | FetchArgs
  ) => MaybePromise<
    QueryReturnValue<
      ChannelCapabilityRollupQueryResults,
      FetchBaseQueryError,
      {}
    >
  >
): Promise<ChannelCapabilityRollup[]> => {
  try {
    const groupNameChannelNames = flatMap(
      groupNames.map((groupName) =>
        channelNames.map((channelName) => ({ groupName, channelName }))
      )
    );
    return await Promise.all<ChannelCapabilityRollup>(
      groupNameChannelNames.map(async (groupNameChannelName) => {
        const result = await baseQuery({
          method: 'post',
          url: `/ui-processing-configuration-service/resolve`,
          headers: {
            accept: 'application/json',
            'Content-Type': 'application/json',
          },
          body: {
            configName: 'soh-control.channel-capability-rollup',
            selectors: [
              {
                criterion: 'StationGroupName',
                value: groupNameChannelName.groupName,
              },
              {
                criterion: 'StationName',
                value: stationName,
              },
              {
                criterion: 'ChannelName',
                value: groupNameChannelName.channelName,
              },
            ],
          },
        });

        if (!result.data) {
          throw new Error(JSON.stringify(result.error));
        }

        return {
          stationName: stationName,
          groupName: groupNameChannelName.groupName,
          channelName: groupNameChannelName.channelName,
          defaultRollup: convertQueryDataToRollup(result.data, allMonitorNames),
        };
      })
    );
  } catch (e) {
    console.error(e);
    throw e;
  }
};
