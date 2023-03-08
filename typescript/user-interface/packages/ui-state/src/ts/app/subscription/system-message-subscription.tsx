/* eslint-disable max-classes-per-file */
import type { SystemMessageTypes } from '@gms/common-model';
import { compose, MILLISECONDS_IN_SECOND, uuid } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import produce from 'immer';
import includes from 'lodash/includes';
import React from 'react';
import * as ReactRedux from 'react-redux';
import * as Redux from 'redux';

import { useGetProcessingCommonConfigurationQuery } from '../api/processing-configuration';
import type { SystemMessageState } from '../state/system-message';
import { addSystemMessages, clearAllSystemMessages } from '../state/system-message';
import type { AppState } from '../store';
import { addSubscriber, removeSubscriber } from './subscription';

const logger = UILogger.create('GMS_LOG_SYSTEM_MESSAGE', process.env.GMS_LOG_SYSTEM_MESSAGE);

/* Default if UI Analyst Configuration fails in Query */
const defaultSystemMessageLimit = 1000;

/* The timestamp to help figure out when to update in Redux */
let lastGatewayMessageTimestamp = 0;

/** Delay used in buffering before updating Redux with latest system messages */
const QUARTER_SECOND_MS = 250;
const HALF_SECOND_MS = 500;
let latestSystemMessages: SystemMessageTypes.SystemMessage[] = [];

let updateSystemMessages;
let systemMessageLimit: number;

/**
 * Check if need to update system messages into redux store
 */
export const checkToUpdateSystemMessages = (): void => {
  // Check to see if we haven't had any data for 1/2 second or the update interval has expired
  const now = Date.now();
  if (latestSystemMessages.length === 0 || now - lastGatewayMessageTimestamp < HALF_SECOND_MS) {
    return;
  }

  // Call method to update system messages in redux store
  updateSystemMessages(latestSystemMessages, systemMessageLimit);

  // Okay clear the latest list
  latestSystemMessages = [];

  // Reset timestamps since just published
  lastGatewayMessageTimestamp = now;
};

/**
 * Start timer that checks if new messages should be updated into redux store
 */
export const initializeSystemMessageBuffering = (
  updateSystemMessagesInRedux: (
    systemMessages: SystemMessageTypes.SystemMessage[],
    messageLimit: number
  ) => void,
  messageLimit: number
): void => {
  updateSystemMessages = updateSystemMessagesInRedux;
  systemMessageLimit = messageLimit;
  setTimeout(() => {
    checkToUpdateSystemMessages();
  }, QUARTER_SECOND_MS);

  setInterval(() => {
    checkToUpdateSystemMessages();
  }, QUARTER_SECOND_MS);
};

/**
 * Add system message to the local list to be periodically updated to redux store
 *
 * @param messages latest to add to list
 */
export const bufferSystemMessages = (messages: SystemMessageTypes.SystemMessage[]): void => {
  if (messages.length > 0) {
    latestSystemMessages = latestSystemMessages.concat(messages);
  }
};

/**
 * The system message component redux props
 */
export interface SystemMessageReduxProps {
  systemMessagesState: SystemMessageState;

  children?: React.ReactNode;

  /**
   *  Adds system messages to the redux store
   *
   * @param messages the system messages to add
   * @param limit (optional) limit the number of messages in the redux state
   * when adding new messages; if set when adding new messages the message list
   * result will not be larger than the size of the `limit` specified.
   * @param pageSizeBuffer (optional) the size of the page buffer; if specified and the
   * limit is reached then messages will be removed at increments of the page buffer size
   */
  addSystemMessages(
    messages: SystemMessageTypes.SystemMessage[],
    limit?: number,
    pageSizeBuffer?: number
  ): void;

  /** Clears (removes) all system messages from the redux store */
  clearAllSystemMessages(): void;
}

/**
 * Mapping redux state to the properties of the component
 *
 * @param state App state, root level redux store
 */
const mapStateToProps = (state: AppState): Partial<SystemMessageReduxProps> => ({
  systemMessagesState: state.app.systemMessage
});

/**
 * Mapping methods (actions and operations) to dispatch one or more updates to the redux store
 *
 * @param dispatch the redux dispatch event alerting the store has changed
 */
const mapDispatchToProps = (dispatch): Partial<SystemMessageReduxProps> =>
  Redux.bindActionCreators(
    {
      addSystemMessages,
      clearAllSystemMessages
    },
    dispatch
  );

const subscriberId = `system-message-subscription-${uuid.asString()}`;
let subscriptionRequested = false;
export const registerSubscription = (): void => {
  if (subscriptionRequested) {
    return;
  }
  subscriptionRequested = true;

  // Callback from the subscription list of StageInterval
  const onMessage: (systemMessage: SystemMessageTypes.SystemMessage) => void = systemMessage => {
    if (!systemMessage) {
      return;
    }
    bufferSystemMessages([systemMessage]);
  };

  const onOpen = (e: Event, isReconnect: boolean): void => {
    logger.debug(`System Message subscription open (reconnected: ${isReconnect})`);
  };

  try {
    addSubscriber(subscriberId, 'system-message', onMessage, onOpen);
    logger.info(`System Message subscription subscribed ${subscriberId}`);
  } catch (e) {
    logger.error(`Failed to establish websocket connection ${subscriberId}`, e);
  }
};

const unRegisterSubscription = (): void => {
  removeSubscriber(subscriberId, 'system-message');
  logger.info(`System Message subscription unsubscribed ${subscriberId}`);
};

/**
 * The system message subscription component
 */
const SystemMessageSubscriptionFC: React.FunctionComponent<React.PropsWithChildren<{
  updateSystemMessagesInRedux: (
    systemMessages: SystemMessageTypes.SystemMessage[],
    messageLimit: number
  ) => void;
  // eslint-disable-next-line react/function-component-definition
}>> = props => {
  // eslint-disable-next-line react/prop-types
  const { children, updateSystemMessagesInRedux } = props;
  const processingCommonQuery = useGetProcessingCommonConfigurationQuery();
  const messageLimit =
    processingCommonQuery && processingCommonQuery.data
      ? processingCommonQuery.data.systemMessageLimit
      : defaultSystemMessageLimit;

  registerSubscription();
  initializeSystemMessageBuffering(updateSystemMessagesInRedux, messageLimit);
  // eslint-disable-next-line react/jsx-no-useless-fragment
  return <>{children}</>;
};

/**
 * The system message subscription component.
 */
export class SystemMessageSubscriptionComponent<
  T extends SystemMessageReduxProps
> extends React.Component<T> {
  public componentWillUnmount(): void {
    unRegisterSubscription();
  }

  /**
   * Updates the Redux store.
   *
   * @param systemMessages the system messages to be added to the redux store
   */
  public readonly updateSystemMessagesInRedux = (
    systemMessages: SystemMessageTypes.SystemMessage[],
    messageLimit: number = defaultSystemMessageLimit
  ): void => {
    if (!systemMessages || systemMessages.length === 0) {
      return;
    }

    const { systemMessagesState } = this.props;
    // update the redux store
    try {
      // Fix system message time from epoch seconds to epoch milliseconds
      // !find where and why the messages need to be in milliseconds
      const timeFixedMessages = systemMessages.map(msg => {
        return produce(msg, draft => {
          draft.time *= MILLISECONDS_IN_SECOND;
        });
      });
      const systemMessagesToUpdate = timeFixedMessages.filter(sysMsg => {
        if (
          includes(
            systemMessagesState.systemMessages?.map(s => s.id),
            sysMsg.id
          )
        ) {
          logger.warn(`Duplicated system message received; dropping message ${sysMsg.id}`);
          return false;
        }
        return true;
      });

      const numberOfMessagesToDelete = messageLimit / 2;
      if (systemMessagesToUpdate && systemMessagesToUpdate.length > 0) {
        // eslint-disable-next-line react/destructuring-assignment
        this.props.addSystemMessages(
          systemMessagesToUpdate,
          messageLimit,
          numberOfMessagesToDelete
        );
      }
    } catch (e) {
      logger.error(`Failed to update Redux state for system messages ${e}`);
    }
  };

  /** React render lifecycle method  */
  public render(): JSX.Element {
    const { children } = this.props;
    return (
      <SystemMessageSubscriptionFC updateSystemMessagesInRedux={this.updateSystemMessagesInRedux}>
        {children}
      </SystemMessageSubscriptionFC>
    );
  }
}

/**
 *
 * Wrap the provided component with the system message subscription and context.
 *
 * @param Component the component to wrap
 * @param store the redux store
 */
const SystemMessageSubscription = compose(
  // connect the redux props
  ReactRedux.connect(mapStateToProps, mapDispatchToProps)
)(SystemMessageSubscriptionComponent);

/**
 * Wrap the provided component with the System Message Subscription.
 *
 * @param Component the component to wrap
 * @param store the redux store
 */
export const wrapSystemMessageSubscription = (Component: any, props: any) =>
  Redux.compose()(
    // eslint-disable-next-line react/display-name, react/prefer-stateless-function
    class<T> extends React.Component<T> {
      public render(): JSX.Element {
        return (
          <>
            <SystemMessageSubscription />
            {/* eslint-disable-next-line react/jsx-props-no-spreading */}
            <Component {...props} />
          </>
        );
      }
    }
  );
