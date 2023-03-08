import { Logger } from '@gms/common-util';

import { createKafka, createProducer } from '../src/ts/kafka/kafka';
import { KafkaProducer } from '../src/ts/kafka/kafka-producer';
import { KafkaStatus } from '../src/ts/kafka/types';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

describe('kafka producer', () => {
  test('can be imported', () => {
    const kafkaProducer: KafkaProducer = KafkaProducer.createKafkaProducer(
      'clientId',
      ['broker'],
      ['topic']
    );
    expect(kafkaProducer.getStatus()).toEqual(KafkaStatus.NOT_INITIALIZED);
  });

  test('initialize can handle errors', async () => {
    const kafka = createKafka('clientId', ['broker']);
    createProducer(kafka);

    const kafkaProducer: KafkaProducer = KafkaProducer.createKafkaProducer(
      'clientId',
      ['broker'],
      ['topic']
    );

    kafkaProducer.start().catch(() => logger.error);
    expect(kafkaProducer.getStatus()).not.toEqual(KafkaStatus.NOT_INITIALIZED);
    await kafkaProducer.stop();
  });
});
