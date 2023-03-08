import type { CommonTypes } from '@gms/common-model';
import { MILLISECONDS_IN_SECOND, sleep, SUBSCRIPTIONS_PROXY_URI } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import { deserializeTypeTransformer } from '@gms/ui-workers';
import Immutable from 'immutable';

import type { UserSessionState } from '../state';
import { isApiGatewayConnected } from './types';

const logger = UILogger.create('GMS_LOG_SUBSCRIPTION', process.env.GMS_LOG_SUBSCRIPTION);

/**
 * The disconnect grace period. This time must pass in order
 * to be considered truly disconnected. This is to help prevent
 * false or temporary disconnections/reconnects with the subscription.
 */
const DISCONNECT_GRACE_PERIOD_MS = 5000;

/**
 * Periodic time to wait before retrying to connect
 */
const CONNECT_RETRY_PERIOD_MS = 1000;

/**
 * The disconnected timeout.
 */
let disconnectedTimeout: ReturnType<typeof setTimeout>;
// Prevents prematurely sending the event type subscriptions
let connectionCompleted = false;

const endpoint = `interactive-analysis-api-gateway/subscriptions`;
let socketConnection: WebSocket;
/**
 * Retrieve the WebSocket connection associated with UI subscriptions
 */
export const getWsConnection = (): WebSocket => socketConnection;
let subscribers: Immutable.Map<string, Immutable.Map<string, Subscriber<any>>> = Immutable.Map();

let updateConnectionStatus: (connected: boolean) => void;
let userSessionState: UserSessionState;
export const registerConnectionStatusCallback = (
  callback: (connected: boolean) => void,
  userSession: UserSessionState
): void => {
  updateConnectionStatus = callback;
  userSessionState = userSession;
};

export interface Subscriber<T> {
  id: string;
  onMessage: (data: T) => void;
  onOpen?: (e: Event, isReconnect: boolean) => void;
  onClose?: (e: Event) => void;
  onError?: (e: Event) => void;
}
/**
 * Returns the map of subscription callbacks that have been registered.
 * !Exported for testing ONLY.
 */
export const getSubscriptionCallbacks = (): Immutable.Map<
  string,
  Immutable.Map<string, Subscriber<any>>
> => subscribers;

/**
 * Returns the subscription metadata of the event types that have been registered.
 */
export const getSubscriptionMetadata = (): Immutable.List<{ eventType: string }> =>
  Immutable.List([...subscribers.keys()].map(eventType => ({ eventType })));

/**
 * Sends list of event types to subscribe to.
 * !Exported for testing ONLY.
 */
export const sendSubscriptionEventTypes = (): void => {
  const typesToSubscribe = getSubscriptionMetadata();
  if (connectionCompleted && !typesToSubscribe.isEmpty()) {
    try {
      socketConnection.send(JSON.stringify(typesToSubscribe));
    } catch (error) {
      logger.error('Failed to send subscription types to System Event gateway', error);
    }
  }
};

/**
 * The subscription onMessage event handler.
 * !Exported for testing ONLY.
 */
export const onMessage: (e: MessageEvent) => void = e => {
  try {
    const systemEvent: CommonTypes.SystemEvent = JSON.parse(e.data);
    if (isApiGatewayConnected(systemEvent)) {
      connectionCompleted = true;
      // clear out the disconnect timeout; connection has been reestablished
      clearTimeout(disconnectedTimeout);
      disconnectedTimeout = undefined;
      logger.info(`Connection to System Event Gateway has been established.`);
      if (updateConnectionStatus) {
        updateConnectionStatus(true);
      }
      // Set user session connected status
      sendSubscriptionEventTypes();
    } else if (subscribers.has(systemEvent.type)) {
      const data = deserializeTypeTransformer(systemEvent.data);
      subscribers.get(systemEvent.type).forEach(subscriber => {
        subscriber.onMessage(data);
      });
    } else {
      logger.warn(
        `Subscription received unexpected system event type ${JSON.stringify(systemEvent)}`
      );
    }
  } catch (err) {
    logger.error(`Error processing subscription onMessage data ${err}`);
  }
};

/**
 * The subscription onOpen event handler.
 * !Exported for testing ONLY.
 */
export const onOpen: (e: Event) => void = e => {
  subscribers.forEach(subscriber => {
    subscriber.forEach(s => {
      if (s.onOpen) {
        s.onOpen(e, getWsConnection() !== undefined);
      }
    });
  });
};

/**
 * The subscription onError event handler.
 * !Exported for testing ONLY.
 */
export const onError: (e: Event) => void = e => {
  subscribers.forEach(subscriber => {
    subscriber.forEach(s => {
      if (s.onError) {
        s.onError(e);
      }
    });
  });
};

/**
 * The subscription onClose event handler. If the UserSession state is set to connected
 * create a timer to set the state to disconnected after the grace period as expired. If
 * connection has been re-established the timer will be canceled.
 * !Exported for testing ONLY.
 */
export const onClose = async (e: Event): Promise<void> => {
  if (connectionCompleted && userSessionState.connected) {
    disconnectedTimeout = setInterval(() => {
      if (updateConnectionStatus) {
        // set the grace period timeout; if this timeout fires then we are truly disconnected
        logger.warn(
          `Subscription failed to reconnect to server after ${
            DISCONNECT_GRACE_PERIOD_MS / MILLISECONDS_IN_SECOND
          } seconds setting state to disconnected.`
        );
        updateConnectionStatus(false);
      }
      // let subscribers know lost connection
      subscribers.forEach(subscriber => {
        subscriber.forEach(s => {
          if (s.onClose) {
            s.onClose(e);
          }
        });
      });
      // Updated status done our job
      clearTimeout(disconnectedTimeout);
    }, DISCONNECT_GRACE_PERIOD_MS);
  }
  connectionCompleted = false;
  await sleep(CONNECT_RETRY_PERIOD_MS);

  // eslint-disable-next-line @typescript-eslint/no-use-before-define
  establishWsConnection();
};

/**
 * Establishes a new WebSocket connection to be used for subscriptions to the System Event Gateway
 *
 * @returns the established WebSocket connection
 */
export const establishWsConnection = (): WebSocket => {
  if (socketConnection) {
    socketConnection.close();
    socketConnection = undefined;
  }
  socketConnection = new WebSocket(`${SUBSCRIPTIONS_PROXY_URI}/${endpoint}`);
  socketConnection.onopen = e => onOpen(e);
  socketConnection.onerror = e => onError(e);
  socketConnection.onmessage = e => onMessage(e);
  socketConnection.onclose = async e => onClose(e);
  return socketConnection;
};

/**
 * Add the provided callback to be invoked when events matching the provided eventType are received
 *
 * @param eventType Type of the event to trigger
 * @param onMessageCallback The callback to be triggered with event data
 * @param onOpenCallback (optional) The callback to be triggered on connection open
 * @param onCloseCallback (optional) The callback to be triggered on connection close
 * @param onErrorCallback (optional) The callback to be triggered on connection error
 * @returns A unique ID string to be optionally used to remove the subscriber callback in the future
 */
export const addSubscriber = <T>(
  subscriberId: string,
  eventType: string,
  onMessageCallback: (data: T) => void,
  onOpenCallback?: (e: Event, isReconnect: boolean) => void,
  onCloseCallback?: (e: Event) => void,
  onErrorCallback?: (e: Event) => void
): string => {
  const subscriber: Subscriber<T> = {
    id: subscriberId,
    onMessage: onMessageCallback,
    onOpen: onOpenCallback,
    onClose: onCloseCallback,
    onError: onErrorCallback
  };
  if (subscribers.has(eventType)) {
    subscribers = subscribers.set(
      eventType,
      subscribers.get(eventType).set(subscriber.id, subscriber)
    );
  } else {
    subscribers = subscribers.set(
      eventType,
      Immutable.Map<string, Subscriber<any>>().set(subscriber.id, subscriber)
    );

    if (getWsConnection()) {
      sendSubscriptionEventTypes();
    }
  }
  return subscriber.id;
};

/**
 * Removes the callback associated with the provided ID for the provided event type
 *
 * @param id Unique ID associated with a previously registered callback
 * @param eventType The event type the target callback was registered to
 */
export const removeSubscriber = (id: string, eventType: string): void => {
  if (subscribers.has(eventType)) {
    subscribers = subscribers.set(eventType, subscribers.get(eventType).delete(id));

    if (subscribers.get(eventType).size <= 0) {
      subscribers = subscribers.delete(eventType);
      sendSubscriptionEventTypes();
    }
  }
};
