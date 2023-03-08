import type { CommonTypes, SohTypes } from '@gms/common-model';
import {
  convertSecondsToDuration,
  Logger,
  MILLISECONDS_IN_SECOND,
  toOSDTime,
  uuid4
} from '@gms/common-util';
import type { BaseQueryFn, MutationDefinition } from '@reduxjs/toolkit/dist/query';
import type { MutationTrigger } from '@reduxjs/toolkit/dist/query/react/buildHooks';

import {
  useAcknowledgeSohStatusMutation,
  useClientLogMutation,
  useQuietSohStatusMutation
} from '../api/system-event-gateway';
import { useAppSelector } from './react-redux-hooks';

const logger = Logger.create(
  'GMS_LOG_SYSTEM_EVENT_GATEWAY',
  process.env.GMS_LOG_SYSTEM_EVENT_GATEWAY
);
/**
 * A hook that sends the client log messages to the system event gateway
 */
export const useClientLog = (): MutationTrigger<
  MutationDefinition<
    CommonTypes.ClientLogInput[],
    BaseQueryFn<any, unknown, unknown, unknown, unknown>,
    never,
    void,
    'systemEventGatewayApi'
  >
> => {
  const [clientLogMutation] = useClientLogMutation();
  return clientLogMutation;
};

/**
 * A hook that SOH Status Acknowledgement to the system event gateway
 */
export const useGetAcknowledgeSohStatusMutation = (): MutationTrigger<
  MutationDefinition<
    SohTypes.AcknowledgedSohStatusChange[],
    BaseQueryFn<any, unknown, unknown, unknown, unknown>,
    never,
    void,
    'systemEventGatewayApi'
  >
> => {
  const [acknowledgeSohStatusMutation] = useAcknowledgeSohStatusMutation();
  return acknowledgeSohStatusMutation;
};

/**
 * Hook that takes the AcknowledgeSohStatus and iterates thru the status
 * to create multiple AcknowledgedSohStatusChange that are then sent to the System Event gateway
 */
export const useAcknowledgeSohStatus = (): ((
  stationToAcknowledge: SohTypes.AcknowledgeSohStatus
) => Promise<void>) => {
  const callMutation = useGetAcknowledgeSohStatusMutation();
  const sohStatus = useAppSelector(state => state.app.dataAcquisition.data.sohStatus);
  return async stationToAcknowledge => {
    logger.info(
      `Publishing soh acknowledgment for station(s): ${stationToAcknowledge.stationNames.join(
        ','
      )} by ${stationToAcknowledge.userName} ${stationToAcknowledge.comment}`
    );

    const stationsToAcknowledge: SohTypes.AcknowledgedSohStatusChange[] = stationToAcknowledge.stationNames
      .map(stationName => {
        const stationSoh =
          sohStatus?.stationAndStationGroupSoh?.stationSoh?.find(
            uiStation => uiStation.stationName === stationName
          ) ?? undefined;
        if (!stationSoh) {
          return undefined;
        }

        // Build the list of unacknowledged monitor/status pairs
        const uiStationSohs: SohTypes.SohStatusChange[] = [];
        stationSoh.channelSohs.forEach(channelSoh => {
          channelSoh.allSohMonitorValueAndStatuses.forEach(mvs => {
            if (mvs.hasUnacknowledgedChanges) {
              uiStationSohs.push({
                firstChangeTime: stationSoh.time,
                sohMonitorType: mvs.monitorType,
                changedChannel: channelSoh.channelName
              });
            }
          });
        });

        // Acknowledged all unacknowledged changes for the station
        if (uiStationSohs.length > 0) {
          return {
            acknowledgedAt: toOSDTime(Date.now() / MILLISECONDS_IN_SECOND),
            acknowledgedBy: stationToAcknowledge.userName,
            comment: stationToAcknowledge.comment,
            id: uuid4(),
            acknowledgedStation: stationName,
            acknowledgedChanges: uiStationSohs
          };
        }
        return undefined;
      })
      .filter(ack => ack !== undefined);
    await callMutation(stationsToAcknowledge);
  };
};

/**
 * A hook that SOH Status Quiet to the system event gateway
 */
export const useGetQuietSohStatusMutation = (): MutationTrigger<
  MutationDefinition<
    SohTypes.QuietedSohStatusChange[],
    BaseQueryFn<any, unknown, unknown, unknown, unknown>,
    never,
    void,
    'systemEventGatewayApi'
  >
> => {
  const [quietSohStatusMutation] = useQuietSohStatusMutation();
  return quietSohStatusMutation;
};

/**
 * Hook that takes the ChannelMonitorInput and iterates thru it
 * to create multiple QuietedSohStatusChange that are then sent to the System Event gateway
 */
export const useQuietSohStatus = (): ((
  channelToQuiet: SohTypes.ChannelMonitorInput
) => Promise<void>) => {
  const callMutation = useGetQuietSohStatusMutation();
  return async channelToQuiet => {
    const channelMonitorPairs = channelToQuiet.channelMonitorPairs
      .map(c => `${c.channelName}/${c.monitorType}`)
      .join(',');
    const comment = channelToQuiet.comment !== undefined ? ` : ${channelToQuiet.comment}` : '';
    logger.info(
      `Publishing soh quiet for channel(s): ${channelMonitorPairs}` +
        ` by ${channelToQuiet.userName}${comment}`
    );

    const channelMonitorsToQuiet: SohTypes.QuietedSohStatusChange[] = channelToQuiet.channelMonitorPairs.map(
      sohStatusChange => {
        // If there is not a quite timer already in place for the channel monitor pair, then add one
        // Using the default quiet interval. If a pair is quieted for a week, don't want it to be overwritten.
        const quietedUntilTime =
          (Date.now() + Number(channelToQuiet.quietDurationMs)) / MILLISECONDS_IN_SECOND;
        return {
          stationName: channelToQuiet.stationName,
          sohMonitorType: sohStatusChange.monitorType,
          channelName: sohStatusChange.channelName,
          comment: channelToQuiet.comment,
          quietUntil: toOSDTime(quietedUntilTime),
          quietDuration: convertSecondsToDuration(
            channelToQuiet.quietDurationMs / MILLISECONDS_IN_SECOND
          ),
          quietedBy: channelToQuiet.userName
        };
      }
    );
    await callMutation(channelMonitorsToQuiet);
  };
};
