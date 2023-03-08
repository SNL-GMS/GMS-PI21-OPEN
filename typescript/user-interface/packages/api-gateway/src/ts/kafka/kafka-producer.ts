import { epochSecondsNow, toOSDTime } from '@gms/common-util';
import config from 'config';
import type { InstrumentationEvent, Kafka, Message, Producer } from 'kafkajs';
import { CompressionTypes, logLevel as LogLevel } from 'kafkajs';
import includes from 'lodash/includes';
import join from 'lodash/join';

import { gatewayLogger as logger } from '../log/gateway-logger';
import { AbstractKafka, configureSnappyCompression, createKafka, createProducer } from './kafka';
import { KafkaStatus } from './types';

// configure snappy compression
configureSnappyCompression();

/**
 * KAFKA Producer implementation using KAFKA JS
 */
export class KafkaProducer extends AbstractKafka {
  /** The singleton instance */
  private static instance: KafkaProducer;

  /**
   * Creates an KAFKA Producer instance.
   *
   * @param clientId the unique client id
   * @param brokers the registered brokers
   * @param topics the unique topics to subscribe too
   * @param connectionTimeout the connection timeout (default 1000)
   * @param logLevel the log level for KAFKA (default WARN)
   */
  public static createKafkaProducer = (
    clientId: string,
    brokers: string[],
    topics: string[],
    connectionTimeout = 1000,
    logLevel: LogLevel = LogLevel.WARN
  ): KafkaProducer => {
    const kafka = createKafka(clientId, brokers, connectionTimeout, logLevel);
    const producer = createProducer(kafka);
    return new KafkaProducer(kafka, producer, topics);
  };

  /**
   * Returns the singleton instance of the KAFKA producer.
   *
   * @returns the instance of the KAFKA producer
   */
  public static Instance(): KafkaProducer {
    if (KafkaProducer.instance === undefined) {
      logger.debug(`Instantiating the KAFKA Producer')}`);

      // Load configuration settings
      const kafkaSettings = config.get('kafka');
      const { clientId } = kafkaSettings;
      const { brokers } = kafkaSettings;
      const { groupId } = kafkaSettings;
      const { connectionTimeout } = kafkaSettings;

      const topics: string[] = [
        kafkaSettings.producerTopics.acknowledgedTopic,
        kafkaSettings.producerTopics.quietedTopic
      ];

      logger.info(`Configured KAFKA Producer clientId ${clientId}`);
      logger.info(`Configured KAFKA Producer brokers ${join(brokers, ', ')}`);
      logger.info(`Configured KAFKA Producer groupId ${groupId}`);
      logger.info(`Configured KAFKA Producer topics ${join(topics, ', ')}`);
      logger.info(`Configured KAFKA Producer connectionTimeout ${connectionTimeout}`);

      // create the producer
      KafkaProducer.instance = KafkaProducer.createKafkaProducer(
        clientId,
        brokers,
        topics,
        connectionTimeout
      );
    }
    return KafkaProducer.instance;
  }

  /** The KAFKA producer instance */
  private readonly producer: Producer;

  /**
   * Constructor.
   *
   * @param kafka the kafka instance
   * @param producer the producer
   * @param topics the unique topics to subscribe too
   */
  private constructor(kafka: Kafka, producer: Producer, topics: string[]) {
    super(kafka, topics);
    this.producer = producer;
  }

  /**
   * Sends the provides messages to the topic.
   *
   * @param topic the topic to send/produce a message on
   * @param messages the messages to send
   * @param compression the compression type (default SNAPPY)
   */
  public readonly send = async (
    topic: string,
    messages: Message[],
    compression: CompressionTypes = CompressionTypes.Snappy
  ): Promise<void> => {
    if (messages === undefined || messages.length === 0) {
      logger.error(`KAFKA producer failed to send message(s), no message data provided`);
      return;
    }

    if (!includes(this.topics, topic)) {
      logger.error(
        `KAFKA producer failed to send message(s) for topic that has not been configured: ${topic}`
      );
      return;
    }

    await this.producer
      .send({
        topic,
        messages,
        compression
      })
      .then(() => {
        const info = `Last produced ${messages.length} message(s) at ${toOSDTime(
          epochSecondsNow()
        )}`;
        this.updateStatus(KafkaStatus.CONNECTED);
        this.updateStatusHistoryInformation('producer.sent', info);
      })
      .catch(e => {
        const info = `KAFKA producer failed to send message(s) for topic: ${topic} ${e}`;
        this.updateStatus(KafkaStatus.ERROR);
        this.updateStatusHistoryInformation('producer.error', info);
        logger.error(info);
      });
  };

  /**
   * Start and initializes the KAFKA producer.
   */
  public readonly start = async (): Promise<void> => {
    if (this.getStatus() === KafkaStatus.NOT_INITIALIZED) {
      logger.info('Starting and initializing KAFKA producer');
      this.updateStatus(KafkaStatus.CONNECTING);
      this.updateStatusHistoryInformation('producer.connecting', KafkaStatus.CONNECTING);
      await this.initializeAndConnectKafka('producer', this.run);
    } else {
      logger.warn(`KAFKA producer has already been started`);
    }
  };

  /**
   * Stop the KAFKA producer.
   */
  public readonly stop = async (): Promise<void> => {
    if (this.producer) {
      logger.info('Stopping KAFKA producer');

      clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = undefined;

      this.updateStatus(KafkaStatus.STOPPED);
      this.updateStatusHistoryInformation('producer.stopped', KafkaStatus.STOPPED);

      await this.producer
        .disconnect()
        .catch(e => logger.error(`Failed to disconnect KAFKA producer ${e}`));
    }
  };

  /**
   * Configures the event listeners for the producer.
   */
  private readonly configureEventListeners = () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.producer.on(this.producer.events.CONNECT, (e: InstrumentationEvent<any>) => {
      const info = `${this.convertTimestamp(e.timestamp)} Producer connected: ${JSON.stringify(
        e.payload
      )}`;
      this.updateStatus(KafkaStatus.CONNECTED);
      this.updateStatusHistoryInformation(this.producer.events.CONNECT, info);
      logger.info(info);
    });

    this.producer.on(
      this.producer.events.REQUEST_TIMEOUT,
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
        )} Producer request timeout: ${JSON.stringify(e.payload)}`;
        this.updateStatusHistoryInformation(this.producer.events.REQUEST_TIMEOUT, info);
        logger.warn(info);
      }
    );

    this.producer.on(
      this.producer.events.REQUEST_QUEUE_SIZE,
      (e: InstrumentationEvent<{ broker: string; clientId: string; queueSize: number }>) => {
        const info = `${this.convertTimestamp(
          e.timestamp
        )} Producer request queue size: ${JSON.stringify(e.payload)}`;
        this.updateStatusHistoryInformation(this.producer.events.REQUEST_QUEUE_SIZE, info);
      }
    );

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.producer.on(this.producer.events.DISCONNECT, (e: InstrumentationEvent<any>) => {
      const info = `${this.convertTimestamp(e.timestamp)} Producer disconnected: ${JSON.stringify(
        e.payload
      )}`;
      this.updateStatus(KafkaStatus.DISCONNECTED);
      this.updateStatusHistoryInformation(this.producer.events.DISCONNECT, info);
      logger.warn(info);
    });
  };

  /**
   * Runs the KAFKA producer
   */
  private readonly run = async (): Promise<void> => {
    // connect the producer
    await this.producer.connect();

    /* configure event listeners */
    this.configureEventListeners();
  };
}
