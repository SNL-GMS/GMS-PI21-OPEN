import { dateToString, epochSecondsNow, sleep, toOSDTime } from '@gms/common-util';
import config from 'config';
import * as Immutable from 'immutable';
import type { Consumer, InstrumentationEvent, Kafka } from 'kafkajs';
import { logLevel as LogLevel } from 'kafkajs';
import includes from 'lodash/includes';
import join from 'lodash/join';

import { gatewayLogger as logger } from '../log/gateway-logger';
import { AbstractKafka, configureSnappyCompression, createConsumer, createKafka } from './kafka';
import type { ConsumerCallback, KafkaSettings } from './types';
import { KafkaStatus } from './types';

/**
 * Periodic time to wait before retrying to again
 */
const RETRY_PERIOD_MS = 2000;

// configure snappy compression
configureSnappyCompression();

/**
 * Returns InstrumentationEvent's payload if payload is defined else the whole event
 *
 * @param e InstrumentationEvent
 * @returns string of event
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const instrumentationEventString = (e: InstrumentationEvent<unknown>): string => {
  return e.payload ? JSON.stringify(e.payload) : JSON.stringify(e);
};

/**
 * KAFKA Consumer implementation using KAFKA JS
 */
export class KafkaConsumer extends AbstractKafka {
  /** The singleton instance */
  private static instance: KafkaConsumer;

  /**
   * Creates an KAFKA Consumer instance.
   *
   * @param clientId the unique client id
   * @param brokers the registered brokers
   * @param groupId the unique group id
   * @param topics the unique topics to subscribe too
   * @param connectionTimeout the connection timeout (default 1000)
   * @param logLevel the log level for KAFKA (default WARN)
   */
  public static readonly createKafkaConsumer = (
    kafkaSettings: KafkaSettings,
    topics: string[],
    logLevel: LogLevel = LogLevel.WARN
  ): KafkaConsumer => {
    const kafka = createKafka(
      kafkaSettings.clientId,
      kafkaSettings.brokers,
      kafkaSettings.connectionTimeout ? kafkaSettings.connectionTimeout : 1000,
      logLevel
    );
    const consumer = createConsumer(
      kafka,
      kafkaSettings.groupId,
      kafkaSettings.maxWaitTimeInMs,
      kafkaSettings.heartbeatInterval
    );
    return new KafkaConsumer(kafka, consumer, topics);
  };

  /**
   * Returns the singleton instance of the KAFKA consumer.
   *
   * @returns the instance of the KAFKA consumer
   */
  public static Instance(): KafkaConsumer {
    if (KafkaConsumer.instance === undefined) {
      logger.debug(`Instantiating the KAFKA consumer`);

      // Load configuration settings
      const kafkaSettings: KafkaSettings = config.get('kafka');
      logger.info(`Configured KAFKA consumer clientId ${kafkaSettings.clientId}`);
      logger.info(`Configured KAFKA consumer brokers ${join(kafkaSettings.brokers, ', ')}`);
      logger.info(`Configured KAFKA consumer groupId ${kafkaSettings.groupId}`);
      logger.info(`Configured KAFKA Consumer connectionTimeout ${kafkaSettings.connectionTimeout}`);
      logger.info(`Configured KAFKA Consumer maxWaitTimeInMs ${kafkaSettings.maxWaitTimeInMs}`);

      // create the consumer
      const topics: string[] = [kafkaSettings.consumerTopics.systemEvent];
      logger.info(`Configured KAFKA consumer topics ${join(topics, ', ')}`);

      KafkaConsumer.instance = KafkaConsumer.createKafkaConsumer(kafkaSettings, topics);
    }
    return KafkaConsumer.instance;
  }

  /** The KAFKA consumer instance */
  private readonly consumer: Consumer;

  /**
   * The registered consumer callbacks
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private kafkaConsumerCallbacks: Immutable.Map<string, ConsumerCallback<any>>;

  /**
   * Constructor.
   *
   * @param kafka the kafka instance
   * @param consumer the kafka consumer
   * @param topics the unique topics to subscribe too
   */
  private constructor(kafka: Kafka, consumer: Consumer, topics: string[]) {
    super(kafka, topics);
    this.consumer = consumer;
    this.kafkaConsumerCallbacks = Immutable.Map();
  }

  /**
   * Registers a consumer callback for a topic.
   *
   * @param topic the topic
   * @param callback the callback
   */
  public readonly registerKafkaConsumerCallbackForTopic = <T>(
    topic: string,
    callback: ConsumerCallback<T>
  ): void => {
    if (topic === undefined || topic === null) {
      logger.error(`Invalid topic, failed to register consumer callback for topic`);
      return;
    }

    if (callback === undefined || callback === null) {
      logger.error(`Invalid callback, failed to register consumer callback for topic`);
      return;
    }

    if (!includes(this.topics, topic)) {
      logger.error(
        `Registering KAFKA consumer callback for topic that has not been configured with the consumer: ${topic}`
      );
    }

    if (this.kafkaConsumerCallbacks.has(topic)) {
      logger.warn(`Overwriting an existing registered KAFKA consumer callback for topic: ${topic}`);
    }

    logger.info(`Registering KAFKA consumer callback for topic: ${topic}`);
    this.kafkaConsumerCallbacks = this.kafkaConsumerCallbacks.set(topic, callback);
  };

  /**
   * Registers a consumer callback for multiple topic.
   *
   * @param topic the topic
   * @param callback the callback
   */
  public readonly registerKafkaConsumerCallbackForTopics = <T>(
    topics: string[],
    callback: ConsumerCallback<T>
  ): void => {
    topics.forEach(t => this.registerKafkaConsumerCallbackForTopic(t, callback));
  };

  /**
   * Un-registers a consumer callback for a given topic.
   *
   * @param topic the topic
   */
  public readonly unregisterKafkaConsumerCallbackForTopic = (topic: string): void => {
    if (this.kafkaConsumerCallbacks.has(topic)) {
      logger.info(`Un-registering KAFKA consumer callback for topic: ${topic}`);
      this.kafkaConsumerCallbacks = this.kafkaConsumerCallbacks.remove(topic);
    }
  };

  /**
   * Start and initializes the KAFKA consumer.
   */
  public readonly start = async (): Promise<void> => {
    if (this.getStatus() === KafkaStatus.NOT_INITIALIZED) {
      logger.info('Starting and initializing KAFKA consumer');
      this.updateStatus(KafkaStatus.CONNECTING);
      this.updateStatusHistoryInformation('consumer.connecting', KafkaStatus.CONNECTING);
      await this.initializeAndConnectKafka('consumer', this.run);
    } else {
      logger.warn(`KAFKA consumer has already been started`);
    }
  };

  /**
   * Stop the KAFKA consumer.
   */
  public readonly stop = async (): Promise<void> => {
    if (this.consumer) {
      logger.info('Stopping KAFKA consumer');

      clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = undefined;

      this.updateStatus(KafkaStatus.STOPPED);
      this.updateStatusHistoryInformation('consumer.stopped', KafkaStatus.STOPPED);

      await this.consumer
        .disconnect()
        .catch(e => logger.error(`Failed to disconnect KAFKA consumer ${e}`));

      await this.consumer.stop().catch(e => logger.error(`Failed to stop KAFKA consumer ${e}`));
    }
  };

  /**
   * Configures the event listeners for the consumer.
   */
  private readonly configureEventListeners = () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.consumer.on(this.consumer.events.CONNECT, (e: InstrumentationEvent<any>) => {
      const info = `${this.convertTimestamp(
        e.timestamp
      )} Consumer connected: ${instrumentationEventString(e)}`;
      this.updateStatus(KafkaStatus.CONNECTED);
      this.updateStatusHistoryInformation(this.consumer.events.CONNECT, info);
      logger.info(info);
    });

    this.consumer.on(
      this.consumer.events.HEARTBEAT,
      (
        e: InstrumentationEvent<{ groupId: string; memberId: string; groupGenerationId: string }>
      ) => {
        const info = `${this.convertTimestamp(
          e.timestamp
        )} Consumer heartbeat: ${instrumentationEventString(e)}`;
        this.updateStatusHistoryInformation(this.consumer.events.HEARTBEAT, info);
      }
    );

    this.consumer.on(
      this.consumer.events.REQUEST_TIMEOUT,
      (
        e: InstrumentationEvent<{
          broker: string;
          clientId: string;
          correlationId: string;
          createdAt: number;
          sentAt: number;
          pendingDuration: number;
          apiName: string;
          apiKey: string;
          apiVersion: string;
        }>
      ) => {
        const info = `${this.convertTimestamp(
          e.timestamp
        )} Consumer request timeout: ${instrumentationEventString(e)}`;
        this.updateStatusHistoryInformation(this.consumer.events.REQUEST_TIMEOUT, info);
        logger.warn(info);
      }
    );

    this.consumer.on(
      this.consumer.events.REQUEST_QUEUE_SIZE,
      (e: InstrumentationEvent<{ broker: string; clientId: string; queueSize: number }>) => {
        const info = `${this.convertTimestamp(
          e.timestamp * 1000
        )} Consumer request queue size: ${instrumentationEventString(e)}`;
        this.updateStatusHistoryInformation(this.consumer.events.REQUEST_QUEUE_SIZE, info);
      }
    );

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.consumer.on(this.consumer.events.DISCONNECT, (e: InstrumentationEvent<any>) => {
      const info = `${this.convertTimestamp(
        e.timestamp
      )} Consumer disconnected: ${instrumentationEventString(e)}`;
      this.updateStatus(KafkaStatus.DISCONNECTED);
      this.updateStatusHistoryInformation(this.consumer.events.DISCONNECT, info);
      logger.warn(info);
    });

    this.consumer.on(
      this.consumer.events.CRASH,
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (e: InstrumentationEvent<{ error: any; groupId: string }>) => {
        const info = `${this.convertTimestamp(
          e.timestamp
        )} Consumer crashed: ${instrumentationEventString(e)}`;
        this.updateStatus(KafkaStatus.DISCONNECTED);
        this.updateStatusHistoryInformation(this.consumer.events.CRASH, info);
        logger.error(info);
      }
    );

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.consumer.on(this.consumer.events.STOP, (e: InstrumentationEvent<any>) => {
      const info = `${this.convertTimestamp(
        e.timestamp
      )} Consumer stopped: ${instrumentationEventString(e)}`;
      this.updateStatus(KafkaStatus.STOPPED);
      this.updateStatusHistoryInformation(this.consumer.events.STOP, info);
      logger.info(info);
    });
  };

  /**
   * Will connect to consumer and subscribe to all topics.
   *
   * @returns boolean if connected and subscribe to all topics
   */
  private readonly connectToConsumer = async (): Promise<boolean> => {
    // if already connected and failed to subscribe to topic need to disconnect before trying to reconnect
    await this.consumer.disconnect().catch(e => {
      logger.warn(`Unexpected error while attempting to disconnect to consumer`, e);
    });

    await this.consumer.connect().catch(e => {
      logger.error(`Unexpected error occurred while attempting to connect to consumer`, e);
      return false;
    });

    // eslint-disable-next-line no-restricted-syntax
    for (const topic of this.topics) {
      // eslint-disable-next-line no-await-in-loop
      const subscribed = await this.consumer
        .subscribe({ topic, fromBeginning: false })
        .then(() => true)
        .catch(e => {
          logger.error(`Failed to subscribe to topic ${topic}`, e);
          return false;
        });
      if (!subscribed) {
        return false;
      }
    }
    return true;
  };

  /**
   * Runs the KAFKA consumer
   */
  private readonly run = async (): Promise<void> => {
    // Keep trying till connected to Kafka consumer and subscribe to topics
    let success = false;
    /* eslint-disable no-await-in-loop */
    while (!success) {
      logger.debug(`Attempting to connect and subscribe to consumer.`);
      success = await this.connectToConsumer();
      if (!success) {
        logger.warn(`Failed to successfully connect and subscribe to all topics; will retry...`);
        await sleep(RETRY_PERIOD_MS);
      }
    }
    /* eslint-enable no-await-in-loop */
    logger.info(
      `Successfully connected and subscribed topics (${this.topics.join(',')}) to consumer.`
    );

    /**
     * Handled the consumed messages for a given topic
     *
     * @param topic the topic
     * @param messages the messages
     */
    const handleMessages = async (topic: string, messages: Immutable.List<string>): Promise<void> =>
      new Promise(resolve => {
        if (
          this.kafkaConsumerCallbacks.has(topic) &&
          this.kafkaConsumerCallbacks.get(topic) !== undefined
        ) {
          this.kafkaConsumerCallbacks.get(topic)(topic, messages);
        } else {
          logger.warn(`No consumer callback for KAFKA consumer configured for topic: ${topic}`);
        }
        resolve();
      });

    /* configure event listeners */
    this.configureEventListeners();

    await this.consumer.run({
      eachBatch: async ({ batch, resolveOffset }) => {
        // Check if there is no messages to process
        if (!batch || !batch.messages || batch.messages.length === 0) {
          return;
        }

        let messages: Immutable.List<string> = Immutable.List();
        try {
          // eslint-disable-next-line no-restricted-syntax
          for (const message of batch.messages) {
            resolveOffset(message.offset);
            messages = messages.push(JSON.parse(message.value.toString()));
          }
          const info = `Last consumed ${messages.size} message(s) at ${toOSDTime(
            epochSecondsNow()
          )}`;
          this.updateStatusHistoryInformation('consumer.received', info);
          await handleMessages(batch.topic, messages);
        } catch (e) {
          const info = `For topic ${batch.topic} failed to consume ${
            messages.size
          } message(s) at ${dateToString(new Date())} ${e}`;
          this.updateStatus(KafkaStatus.ERROR);
          this.updateStatusHistoryInformation('consumer.error', info);
          logger.error(`Failed to consume KAFKA message(s) ${e} info ${info}`);
        }
      }
    });
  };
}
