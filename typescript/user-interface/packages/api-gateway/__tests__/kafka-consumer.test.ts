import { sleep } from '@gms/common-util';

import { createConsumer, createKafka } from '../src/ts/kafka/kafka';
import { KafkaConsumer } from '../src/ts/kafka/kafka-consumer';
import { KafkaStatus } from '../src/ts/kafka/types';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const kafkaSettings: any = {
  clientId: 'api-gateway',
  groupId: 'user-interface',
  brokers: ['kafka:9092'],
  connectionTimeout: 3000,
  maxWaitTimeInMs: 50,
  heartbeatInterval: 500, // ms
  consumerTopics: {
    systemMessagesTopic: 'system-message',
    uiStationSoh: 'soh-message'
  },
  producerTopics: {
    acknowledgedTopic: 'soh.ack-station-soh',
    quietedTopic: 'soh.quieted-list'
  }
};

describe('kafka consumer', () => {
  const kafka = createKafka('clientId', ['broker']);
  createConsumer(
    kafka,
    kafkaSettings.groupId,
    kafkaSettings.maxWaitTimeInMs,
    kafkaSettings.heartbeatInterval
  );

  const topicsToSubscribe = ['topic1', 'topic2', 'topic3', 'topic4'];
  const kafkaConsumer: KafkaConsumer = KafkaConsumer.createKafkaConsumer(
    kafkaSettings,
    topicsToSubscribe
  );

  const kafkaConsumerAny = kafkaConsumer as any;

  let subscribedCount: number;
  let throwOnTopic: string;
  let throwDisconnectError = false;
  let throwConnectError = false;

  kafkaConsumerAny.RETRY_PERIOD_MS = 100;

  const mockConsumer = {
    connect: jest.fn(async () => {
      kafkaConsumerAny.status = KafkaStatus.CONNECTED;
      if (throwConnectError) {
        throw new Error(`Test connect error`);
      }
      return Promise.resolve();
    }),
    disconnect: jest.fn(async () => {
      kafkaConsumerAny.status = KafkaStatus.DISCONNECTED;
      if (throwDisconnectError) {
        throw new Error(`Test disconnect error`);
      }
      return Promise.resolve();
    }),
    on: jest.fn(),
    events: {
      HEARTBEAT: 'consumer.heartbeat',
      COMMIT_OFFSETS: 'consumer.commit_offsets',
      GROUP_JOIN: 'consumer.group_join',
      FETCH: 'consumer.fetch',
      START_BATCH_PROCESS: 'consumer.start_batch_process',
      END_BATCH_PROCESS: 'consumer.end_batch_process',
      CONNECT: 'consumer.connect',
      DISCONNECT: 'consumer.disconnect',
      STOP: 'consumer.stop',
      CRASH: 'consumer.crash',
      REQUEST: 'consumer.network.request',
      REQUEST_TIMEOUT: 'consumer.network.request_timeout',
      REQUEST_QUEUE_SIZE: 'consumer.network.request_queue_size'
    },
    subscribe: async (topic: { topic: string; fromBeginning: boolean }) => {
      await sleep(1);
      if (throwOnTopic === topic.topic) {
        throw new Error(`Subscription topic error test catch and retry`);
      }
      subscribedCount += 1;
    },
    stop: jest.fn(async () => {
      kafkaConsumerAny.status = KafkaStatus.STOPPED;
      return Promise.resolve();
    }),
    run: jest.fn(async () => {
      return Promise.resolve();
    }),
    start: jest.fn(async () => {
      kafkaConsumerAny.status = KafkaStatus.STARTED;
      return Promise.resolve();
    })
  };

  kafkaConsumerAny.consumer = mockConsumer;
  kafkaConsumerAny.initializeAndConnectKafka = jest.fn(async () => {
    kafkaConsumerAny.status = KafkaStatus.CONNECTED;
    return Promise.resolve();
  });

  test('can be imported', () => {
    expect(kafkaConsumer.getStatus()).toEqual(KafkaStatus.NOT_INITIALIZED);
  });

  test('connect exception', async () => {
    throwConnectError = true;
    await kafkaConsumerAny.connectToConsumer();
    expect(kafkaConsumer.getStatus()).toEqual(KafkaStatus.CONNECTED);
    throwConnectError = false;
  });

  test('disconnect exception', async () => {
    throwDisconnectError = true;
    await kafkaConsumerAny.connectToConsumer();
    // should be connected since handles the disconnect exception and then calls connect
    expect(kafkaConsumer.getStatus()).toEqual(KafkaStatus.CONNECTED);
    throwDisconnectError = false;
  });

  test('initialize, connect, run and stop', async () => {
    await kafkaConsumer.start();
    expect(kafkaConsumer.getStatus()).toEqual(KafkaStatus.CONNECTED);

    subscribedCount = 0;
    throwOnTopic = undefined;
    await kafkaConsumerAny.connectToConsumer();
    expect(subscribedCount).toEqual(4); // disable throw should subscribe to all topics

    subscribedCount = 0;
    throwOnTopic = 'topic1';
    await kafkaConsumerAny.connectToConsumer();
    expect(subscribedCount).toEqual(0); // subscribe to only 1 topics

    subscribedCount = 0;
    throwOnTopic = 'topic2';
    await kafkaConsumerAny.connectToConsumer();
    expect(subscribedCount).toEqual(1); // subscribe to only 2 topics

    subscribedCount = 0;
    throwOnTopic = 'topic3';
    await kafkaConsumerAny.connectToConsumer();
    expect(subscribedCount).toEqual(2); // subscribe to only 3 topics

    subscribedCount = 0;
    throwOnTopic = 'topic4';
    await kafkaConsumerAny.connectToConsumer();
    expect(subscribedCount).toEqual(3); // subscribe to all topics

    subscribedCount = 0;
    throwOnTopic = 'topic5';
    await kafkaConsumerAny.connectToConsumer();
    expect(subscribedCount).toEqual(4); // only 4 topics exist, the max

    subscribedCount = 0;
    throwOnTopic = 'topic1';
    const runPromise: Promise<void> = kafkaConsumerAny.run();
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    await sleep(500);
    subscribedCount = 0;
    throwOnTopic = undefined;
    await runPromise.catch();
    expect(subscribedCount).toEqual(4); // subscribe to all topics

    expect(kafkaConsumer.getStatus()).toEqual(KafkaStatus.CONNECTED);
    await kafkaConsumer.stop();
    expect(kafkaConsumer.getStatus()).toEqual(KafkaStatus.STOPPED);
  });
});
