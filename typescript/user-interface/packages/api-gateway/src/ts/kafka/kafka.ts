import type { KeyValue } from '@gms/common-util';
import { MILLISECONDS_IN_SECOND, toOSDTime } from '@gms/common-util';
import * as Immutable from 'immutable';
import type { Consumer, LoggerEntryContent, Producer } from 'kafkajs';
import {
  CompressionCodecs,
  CompressionTypes,
  Kafka,
  KafkaJSConnectionError,
  logLevel as LogLevel
} from 'kafkajs';

import { gatewayLogger as logger } from '../log/gateway-logger';
import type { IKafka } from './types';
import { KafkaStatus } from './types';

/** Delay between retrying connections to the kafka broker if a connection attempt fails */
export const RETRY_BROKER_DELAY = 5000;

/**
 * Configure SNAPPY Compression for KAFKA JS.
 */
export const configureSnappyCompression = (): void => {
  // eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports, global-require
  const SnappyCodec = require('kafkajs-snappy');

  // Enable the Snappy Compression Codec
  CompressionCodecs[CompressionTypes.Snappy] = SnappyCodec;
};

/**
 * Convert KAFKA JS log level to Winston log level.
 *
 * @param level the KAFKA JS log level
 */
export const toWinstonLogLevel = (level: LogLevel): string => {
  switch (level) {
    case LogLevel.ERROR:
      return 'error';
    case LogLevel.NOTHING:
      return 'error';
    case LogLevel.WARN:
      return 'warn';
    case LogLevel.INFO:
      return 'info';
    default:
      return 'debug';
  }
};

/**
 * Creates a KAFKA JS Log Creator
 *
 * @param namespace the namespace
 * @param msgLogLevel the log level
 * @param label the label
 * @param log the log entry
 */
export const kafkaLogCreator = (
  namespace: string,
  msgLogLevel: LogLevel,
  label: string,
  log: LoggerEntryContent,
  kafkaLogLevel: LogLevel // Remove when KafkaJS issue 1384 is fixed
): void => {
  const { message, ...extra } = log;
  if (kafkaLogLevel >= msgLogLevel) {
    logger.log({
      level: toWinstonLogLevel(msgLogLevel),
      message,
      extra
    });
  }

  /**
   * TODO: Temporary workaround for KafkaJS issue 1384
   * should retest after each KafkaJS version upgrade
   */
  if (message.startsWith('Created 0 fetchers')) {
    throw new KafkaJSConnectionError(
      'No brokers available! Avoiding tight loop. See: https://github.com/tulios/kafkajs/issues/1384'
    );
  }
};

/**
 * Create the KAFKA instance.
 *
 * @param clientId the unique client id
 * @param brokers the registered brokers
 * @param connectionTimeout the connection timeout (default 1000)
 * @param logLevel the log level for KAFKA (default WARN)
 */
export const createKafka = (
  clientId: string,
  brokers: string[],
  connectionTimeout = 1000,
  logLevel: LogLevel = LogLevel.WARN
): Kafka =>
  // Create kafka with DEBUG set, will filter out log level in the kafkaLogCreator
  // TODO: remove this when KafkaJS issue https://github.com/tulios/kafkajs/issues/1384 is fixed (change logLevel from debug)
  new Kafka({
    clientId,
    brokers,
    connectionTimeout,
    logLevel: LogLevel.DEBUG,
    logCreator: () => ({ namespace, level, label, log }) => {
      kafkaLogCreator(namespace, level, label, log, logLevel);
    }
  });
/**
 * Create the KAFKA consumer.
 *
 * @param kafka the kafka instance
 * @param groupId the unique group id
 */
export const createConsumer = (
  kafka: Kafka,
  groupId: string,
  maxWaitTimeInMs: number,
  heartbeatInterval: number
): Consumer => kafka.consumer({ groupId, maxWaitTimeInMs, heartbeatInterval });

/**
 * Create the KAFKA producer.
 *
 * @param kafka the kafka instance
 */
export const createProducer = (kafka: Kafka): Producer => kafka.producer({});

/**
 * Abstract class that defines the common implementation
 * for a KAFKA Consumer or Producer.
 */
export abstract class AbstractKafka implements IKafka {
  /** The amount of history entries to keep */
  protected static readonly historySize = 20;

  /** The KAFKA instance */
  protected readonly kafka: Kafka;

  /** The KAFKA topics */
  protected readonly topics: string[];

  /** The date since the KAFKA instance last established a connection */
  private upTime: Date | undefined = undefined;

  /** The KAFKA status */
  private status: KafkaStatus;

  /** The KAFKA status history information */
  private statusHistoryInformation: Immutable.List<KeyValue<string, string | Error>>;

  /** The unique timeout id */
  protected reconnectTimeoutId: ReturnType<typeof setTimeout>;

  /**
   * Constructor.
   *
   * @param kafka the kafka instance
   * @param topics the unique topics to subscribe too
   */
  public constructor(kafka: Kafka, topics: string[]) {
    this.kafka = kafka;
    this.topics = topics;

    this.status = KafkaStatus.NOT_INITIALIZED;
    this.statusHistoryInformation = Immutable.List();
    this.updateStatusHistoryInformation('not.initialized', KafkaStatus.NOT_INITIALIZED);
    this.reconnectTimeoutId = undefined;
  }

  /**
   * Returns the KAFKA status.
   */
  public readonly getStatus = (): KafkaStatus => this.status;

  /**
   * Initialize and connect the KAFKA consumer/producer.
   *
   * @param id string
   * @para run method to start everything
   */
  public readonly initializeAndConnectKafka = async (
    id: 'consumer' | 'producer',
    run: () => Promise<void>
  ): Promise<void> => {
    if (this.getStatus() !== KafkaStatus.STARTED) {
      await run()
        .then(() => {
          clearTimeout(this.reconnectTimeoutId);
          this.reconnectTimeoutId = undefined;
          this.updateStatus(KafkaStatus.STARTED);
          this.updateStatusHistoryInformation(`${id}.started`, KafkaStatus.STARTED);
        })
        .then(() => {
          this.updateStatus(KafkaStatus.CONNECTED);
          this.updateStatusHistoryInformation(`${id}.connected`, KafkaStatus.CONNECTED);
        })
        .catch(e => {
          logger.warn(`Connection to kafka broker for ${id} failed, retrying...`);

          this.updateStatus(KafkaStatus.RECONNECTING);
          this.updateStatusHistoryInformation(`${id}.error`, e);

          clearTimeout(this.reconnectTimeoutId);
          this.reconnectTimeoutId = undefined;

          this.reconnectTimeoutId = setTimeout(async () => {
            await this.initializeAndConnectKafka(id, run);
          }, RETRY_BROKER_DELAY);
        });
    } else {
      logger.info(`KAFKA ${id} is already initialized and connected`);
    }
  };

  /**
   * Returns the KAFKA status history information.
   */
  public readonly getStatusHistoryInformation = (): Immutable.List<
    KeyValue<string, string | Error>
  > => this.statusHistoryInformation.reverse();

  /**
   * Returns the KAFKA status history information (information about the events that have been issued) as an object.
   * Only keeps the latest information.
   */
  // eslint-disable-next-line @typescript-eslint/ban-types
  public readonly getStatusHistoryInformationAsObject = (): Object => {
    // convert to an Object so it can be converted to JSON
    const mapToObj = <V>(list: Immutable.List<KeyValue<string, V>>) => {
      const obj = {};
      list.forEach((value, key) => {
        obj[key] = `${value.id} ${String(value.value)}`;
      });
      return obj;
    };
    return mapToObj<string | Error>(this.getStatusHistoryInformation());
  };

  /**
   * Returns the Date the KAFKA instance has been up and running (connected) since
   */
  public readonly getUpTime = (): Date => this.upTime;

  /**
   * Returns the number of seconds the KAFKA instance has been up and running (connected)
   */
  public readonly getUpTimeSeconds = (): number => {
    if (this.status === KafkaStatus.CONNECTED && this.upTime) {
      const now = new Date();
      // calculate the number of seconds
      return (now.getTime() - this.upTime.getTime()) / 1000;
    }
    return 0;
  };

  /**
   * Returns true if connected; false otherwise.
   */
  public readonly connected = (): boolean => this.status === KafkaStatus.CONNECTED;

  /**
   * Start the KAFKA consumer/producer.
   */
  public abstract start(): Promise<void>;

  /**
   * Stop the KAFKA consumer/producer.
   */
  public abstract stop(): Promise<void>;

  /**
   * Convert KAFKA timestamp to a human readable format
   */
  // eslint-disable-next-line class-methods-use-this
  protected readonly convertTimestamp = (timestampMs: number): string =>
    toOSDTime(timestampMs / MILLISECONDS_IN_SECOND);

  /**
   * Updates the status of the KAFKA instance.
   */
  protected readonly updateStatus = (status: KafkaStatus): void => {
    // if the status has changed from not CONNECTED to CONNECTED set upTime to now
    // if the status is not CONNECTED set to undefined in case we loss the connection
    if (status === KafkaStatus.CONNECTED && this.status !== KafkaStatus.CONNECTED) {
      this.upTime = new Date();
    } else if (status !== KafkaStatus.CONNECTED) {
      this.upTime = undefined;
    }
    this.status = status;
  };

  /**
   * Updates the status history information.
   */
  protected readonly updateStatusHistoryInformation = (id: string, value: string | Error): void => {
    this.statusHistoryInformation = this.statusHistoryInformation.push({ id, value });
    // only keep a limited amount of history
    while (this.statusHistoryInformation.size > AbstractKafka.historySize) {
      this.statusHistoryInformation = this.statusHistoryInformation.delete(0);
    }
  };
}
