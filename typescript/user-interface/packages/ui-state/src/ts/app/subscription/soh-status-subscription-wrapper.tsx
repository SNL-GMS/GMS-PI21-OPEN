/* eslint-disable react/destructuring-assignment */
/* eslint-disable max-classes-per-file */
import type { SohTypes } from '@gms/common-model';
import { CommonTypes } from '@gms/common-model';
import {
  compose,
  isTimeStale,
  MILLISECONDS_IN_SECOND,
  setDecimalPrecision,
  toOSDTime,
  uuid
} from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import type { BaseQueryFn, MutationDefinition } from '@reduxjs/toolkit/dist/query';
import type { MutationTrigger } from '@reduxjs/toolkit/dist/query/react/buildHooks';
import cloneDeep from 'lodash/cloneDeep';
import delay from 'lodash/delay';
import unionBy from 'lodash/unionBy';
import React from 'react';
import type { ConnectedProps } from 'react-redux';
import { connect } from 'react-redux';
import * as Redux from 'redux';

import { ssamControlApiSlice } from '../api/ssam-control';
import { useAppSelector } from '../hooks/react-redux-hooks';
import { useClientLog } from '../hooks/system-event-gateway-hooks';
import { dataAcquisitionActions } from '../state/data-acquisition';
import type { AppState } from '../store';
import { addLatestSohMessages, initializeSohStatusBuffering } from './soh-status-buffer';
import { addSubscriber, removeSubscriber } from './subscription';

const logger = UILogger.create(
  'GMS_LOG_SOH_STATUS_SUBSCRIPTION',
  process.env.GMS_LOG_SOH_STATUS_SUBSCRIPTION
);

let logIt;
let userName: string;

/**
 * Finds the latest time field from a group of station soh objects
 */
export const getLatestSohTime = (stationSohs: SohTypes.UiStationSoh[]): number =>
  stationSohs && stationSohs.length > 0
    ? stationSohs
        .map(soh => (soh.time ? soh.time : 0))
        .reduce((accum, val) => (val > accum ? val : accum), 0)
    : 0;

/**
 * Filters out `undefined` and empty messages
 *
 * @param messages the messages to filter
 * @returns the filtered messages
 */
const filterEmptyMessages = (...messages: string[]) =>
  // filter out undefined and empty messages
  messages ? messages.filter(msg => msg && msg.trim && msg.trim().length > 0) : [];

/**
 * Handles the mutation to the System Event Gateway
 *
 * @param logLevel the log level
 * @param messages messages to log
 */
const logToServer = async (
  logLevel: CommonTypes.LogLevel,
  messages: string[],
  log: MutationTrigger<
    MutationDefinition<
      CommonTypes.ClientLogInput[],
      BaseQueryFn<any, unknown, unknown, unknown, unknown>,
      never,
      void,
      'systemEventGatewayApi'
    >
  >
): Promise<void> => {
  await new Promise<void>(resolve => {
    // only log to the server if the configured log level is greater than or equal to the message to be logged
    const messagesToLog = filterEmptyMessages(...messages);
    if (messagesToLog && messagesToLog.length > 0) {
      const time = new Date().toISOString();
      const variables: CommonTypes.ClientLogInput[] = messagesToLog.map(message => ({
        logLevel,
        message,
        time,
        userName
      }));
      log(variables).catch(e => logger.error(`couldn't log timing ${e}`));
    }
    resolve();
  });
};

/**
 * Timing point log
 *
 * @param message type string message to be logged
 * @param messages
 */
const timing = (messages: string[]): void => {
  logToServer(CommonTypes.LogLevel.timing, messages, logIt).catch(e =>
    logger.error('Failed to log to server', e)
  );
};

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState) => ({
  selectedStationIds: state.app.common.selectedStationIds,
  sohStatus: state.app.dataAcquisition.data.sohStatus,
  sohConfigurationQuery: ssamControlApiSlice.endpoints.getSohConfiguration.select()(state)
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = {
  getSohConfiguration: ssamControlApiSlice.endpoints.getSohConfiguration.initiate,
  setSohStatus: dataAcquisitionActions.setSohStatus
};

const connector = connect(mapStateToProps, mapDispatchToProps);

type ConnectedReduxProps = ConnectedProps<typeof connector>;

export type SohStatusSubscriptionProps = ConnectedReduxProps;

const subscriberId = `soh-status-subscription-${uuid.asString()}`;
let subscriptionRequested = false;
export const registerSubscription = (): void => {
  if (subscriptionRequested) {
    return;
  }
  subscriptionRequested = true;

  // Callback from the subscription list of StageInterval
  const onMessage: (sohMessage: SohTypes.StationAndStationGroupSoh) => void = sohMessage => {
    if (!sohMessage) {
      return;
    }
    addLatestSohMessages([sohMessage]);
  };

  const onOpen = (e: Event, isReconnect: boolean): void => {
    logger.debug(`SOH Message subscription open (reconnected: ${isReconnect})`);
  };

  try {
    addSubscriber(subscriberId, 'soh-message', onMessage, onOpen);
    logger.info(`SOH Message subscription subscribed ${subscriberId}`);
  } catch (e) {
    logger.error(`Failed to establish websocket connection ${subscriberId}`, e);
  }
};

const unRegisterSubscription = (): void => {
  removeSubscriber(subscriberId, 'soh-message');
  logger.info(`SOH Message subscription unsubscribed ${subscriberId}`);
  subscriptionRequested = false;
};

/**
 * The SOH status message subscription component
 */
function SohStatusSubscriptionFC(): React.ReactElement {
  logIt = useClientLog();
  const userSessionState = useAppSelector(state => state.app.userSession);
  userName = userSessionState.authenticationStatus.userName;
  registerSubscription();
  // eslint-disable-next-line react/jsx-no-useless-fragment
  return <>{}</>;
}

/**
 * The system message subscription component.
 */
export class SohStatusSubscriptionComponent<
  T extends SohStatusSubscriptionProps
> extends React.Component<T> {
  // timer id for updating stale data
  private staleTimerId: number | undefined;

  private unsubscribe: null | (() => void) = null;

  public componentDidMount(): void {
    // Initialize the Soh Status message buffer
    initializeSohStatusBuffering(this.updateSohMessage);

    const { getSohConfiguration } = this.props;
    // eslint-disable-next-line @typescript-eslint/unbound-method
    const { unsubscribe } = getSohConfiguration();
    this.unsubscribe = unsubscribe;
  }

  public componentWillUnmount(): void {
    // clean up timer on unmount
    this.cancelStaleTimer();
    this.unsubscribe?.();
    unRegisterSubscription();
  }

  /**
   * Set stale timer.
   */
  private setStaleTimer(): void {
    this.cancelStaleTimer();
    this.staleTimerId = delay(
      () =>
        this.props.setSohStatus({
          ...this.props.sohStatus,
          isStale: true
        }),
      this.props.sohConfigurationQuery.data?.sohStationStaleMs
    );
  }

  /**
   * Send back timing point messages to log for each Station SOH message update received
   * The gateway will decide if the message should be logged based on the log level set.
   *
   * @param stationAndStationGroupSoh
   */
  // eslint-disable-next-line class-methods-use-this
  private readonly logTimingPoints = (
    stationAndStationGroupSoh: SohTypes.StationAndStationGroupSoh
  ): void => {
    const now = Date.now();

    // Do not log timing point C for any Ack/Quiet responses
    const { isUpdateResponse } = stationAndStationGroupSoh;
    if (!isUpdateResponse) {
      const timingPointMessages = stationAndStationGroupSoh.stationSoh.map(
        stationSoh =>
          `Timing point C: SOH object ${stationSoh.uuid} displayed in UI at ${toOSDTime(
            now / MILLISECONDS_IN_SECOND
          )} A->C ${setDecimalPrecision(now / MILLISECONDS_IN_SECOND - stationSoh.time, 3)} seconds`
      );

      // Call reporting timing points mutation to record in the UI Backend log
      timing(timingPointMessages);
    }
  };

  public readonly updateSohMessage = (
    stationAndStationGroupSoh: SohTypes.StationAndStationGroupSoh
  ): void => {
    this.updateSohMessagesInRedux(false, [stationAndStationGroupSoh]);
  };

  /**
   * Updates the Redux store for the SOH Status
   *
   * @param loading the loading status of the query for the SOH Station data
   * @param stationAndStationGroupSoh the station and station group SOH data - does not include the channel data
   */
  public readonly updateSohMessagesInRedux = (
    loading: boolean,
    stationAndStationGroupSoh: SohTypes.StationAndStationGroupSoh[]
  ): void => {
    // clone the data to make sure we do not mutate the Redux state
    const clonedStationAndStationGroupSohMessages = cloneDeep(stationAndStationGroupSoh);
    let mergedStationAndStationGroupSoh: SohTypes.StationAndStationGroupSoh;
    clonedStationAndStationGroupSohMessages.forEach(clonedStationAndStationGroupSoh => {
      // merge station soh data
      mergedStationAndStationGroupSoh = {
        stationGroups: unionBy(
          clonedStationAndStationGroupSoh.stationGroups,
          this.props.sohStatus.stationAndStationGroupSoh.stationGroups,
          'id'
        ),
        stationSoh: unionBy(
          clonedStationAndStationGroupSoh.stationSoh,
          this.props.sohStatus.stationAndStationGroupSoh.stationSoh,
          'id'
        ),
        isUpdateResponse: clonedStationAndStationGroupSoh.isUpdateResponse
      };

      // Send to gateway timing points.
      this.logTimingPoints(clonedStationAndStationGroupSoh);
    });

    // sort the station soh data by name
    mergedStationAndStationGroupSoh.stationSoh.sort(
      (a: SohTypes.UiStationSoh, b: SohTypes.UiStationSoh) =>
        a.stationName.localeCompare(b.stationName)
    );

    const lastUpdated = getLatestSohTime(mergedStationAndStationGroupSoh.stationSoh);
    const isStale = isTimeStale(
      lastUpdated,
      this.props.sohConfigurationQuery.data?.sohStationStaleMs
    );
    try {
      this.props.setSohStatus({
        lastUpdated,
        isStale,
        loading,
        stationAndStationGroupSoh: mergedStationAndStationGroupSoh
      });
      this.setStaleTimer();
    } catch (e) {
      logger.error(`Failed to update Redux state for SOH Status ${e}`);
    }
  };

  /**
   * Cancel stale timer.
   */
  private cancelStaleTimer(): void {
    clearTimeout(this.staleTimerId);
    this.staleTimerId = undefined;
  }

  /** React render lifecycle method  */
  public render(): JSX.Element {
    return <SohStatusSubscriptionFC />;
  }
}

/**
 *
 * Wrap the provided component with the system message subscription and context.
 *
 * @param Component the component to wrap
 * @param store the redux store
 */
const SohStatusSubscription = compose(
  // connect the redux props
  connect(mapStateToProps, mapDispatchToProps)
)(SohStatusSubscriptionComponent);

/**
 * Wrap the provided component with the SOH Status Subscription.
 *
 * @param Component the component to wrap
 * @param store the redux store
 */
export const wrapSohStatusSubscription = (Component: any, props: any) =>
  Redux.compose()(
    // eslint-disable-next-line react/display-name, react/prefer-stateless-function
    class<T> extends React.Component<T> {
      public render(): JSX.Element {
        return (
          <>
            <SohStatusSubscription />
            {/* eslint-disable-next-line react/jsx-props-no-spreading */}
            <Component {...props} />
          </>
        );
      }
    }
  );
