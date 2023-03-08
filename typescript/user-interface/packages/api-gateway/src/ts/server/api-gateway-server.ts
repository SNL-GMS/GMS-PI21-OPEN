import type { SohTypes } from '@gms/common-model';
import { CommonTypes } from '@gms/common-model';
import config from 'config';
import type * as Immutable from 'immutable';
import msgpack from 'msgpack-lite';
import xssFilters from 'xss-filters';

import { KafkaConsumer } from '../kafka/kafka-consumer';
import { KafkaProducer } from '../kafka/kafka-producer';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { publishAcknowledgedChange, publishQuietedChange } from '../soh/ack-quiet-publishing';
import { getHistoricalAceiData, getHistoricalSohData } from '../soh/mock-historical-soh-acei';
import { createExpressServer, getProtocol } from './express-server';
import { createHttpServer } from './http-server';
import {
  configureRouteAlive,
  configureRouteCheckInitialized,
  configureRouteCheckKafka,
  configureRouteCheckWebsocket,
  configureRouteHealthCheck,
  configureRouteReady
} from './routes';
import { createWebSocketServer, sendSubscriptionMessage } from './websocket-server';

const app = createExpressServer();

/**
 * Gets the header value from request
 *
 * @param httpConfig http config
 * @param headerName header name
 * @returns a value from the header
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
export function getHeaderValue(httpConfig: any, headerName: string): string {
  let value;
  if (httpConfig && httpConfig.headers) {
    Object.keys(httpConfig.headers).forEach(key => {
      if (key.toLowerCase() === headerName.toLowerCase()) {
        value = httpConfig.headers[key];
      }
    });
  }
  return value;
}

/**
 * Consumes System Event wrapped messages from the Kafka Consumer
 * and sends them to the websocket subscription for distribution
 *
 * @param topic System Event Kafka is listening to
 * @param messages to be sent to the websocket subscription
 */
export const consumeSystemEventMessages = (
  messages: Immutable.List<CommonTypes.SystemEvent>
): void => {
  if (messages.size > 0) {
    messages.forEach(msg => {
      sendSubscriptionMessage(msg);
    });
  }
};

/**
 * Register the KAFKA consumer callbacks for topics.
 */
export const registerKafkaConsumerCallbacks = (): void => {
  const kafkaSettings = config.get('kafka');
  // register the callbacks for the topics
  KafkaConsumer.Instance().registerKafkaConsumerCallbackForTopics<CommonTypes.SystemEvent>(
    [kafkaSettings.consumerTopics.systemEvent],
    (topic, messages) => consumeSystemEventMessages(messages)
  );
};

export const initializeKafka = async (): Promise<void> => {
  // Initialize the KAFKA Consumers and Producers
  logger.info(`==> initialize KAFKA configurations`);

  // Initialize the system message Kafka consumer
  await KafkaConsumer.Instance().start();

  // Initialize the SOH Kafka producer
  await KafkaProducer.Instance().start();

  // register callbacks for kafka
  registerKafkaConsumerCallbacks();

  configureRouteCheckKafka(app);
};

export const initializeWebsocketServer = (): void => {
  logger.info(`==> initialize websocket server`);
  createWebSocketServer();
  configureRouteCheckWebsocket(app);
};

/**
 * sets up and configures route for client logging of timing points
 */
export const configureAdditionalRoutesClientLogs = (): void => {
  const responseSuccess = JSON.stringify({
    status: 'complete'
  });
  const handler = (req, res) => {
    const logs: CommonTypes.ClientLogInput[] = req.body;
    logs.forEach((log: CommonTypes.ClientLogInput) => {
      if (log.logLevel === CommonTypes.LogLevel.timing) {
        logger.timing(log.message, log.userName);
      } else {
        logger.client(log.message, log.logLevel, log.userName);
      }
    });
    res.send(JSON.stringify(responseSuccess));
  };

  const clientLogRoute = '/interactive-analysis-api-gateway/client-log';
  logger.info(`register ${clientLogRoute}`);
  app.post(clientLogRoute, handler);
};

/**
 * sets up and configures SOH Acknowledge route to send message on Kafka
 */
export const configureAdditionalRoutesAckSoh = (): void => {
  const ackHandler = (req, res) => {
    const ackStationStatuses: SohTypes.AcknowledgedSohStatusChange[] = req.body;
    ackStationStatuses.forEach(async ackStation => publishAcknowledgedChange(ackStation));
    const response = {
      status: 'complete'
    };
    res.send(JSON.stringify(response));
  };

  const clientLogRoute = '/interactive-analysis-api-gateway/acknowledge-soh-status';
  logger.info(`register ${clientLogRoute}`);
  app.post(clientLogRoute, ackHandler);
};

/**
 * sets up and configures SOH Quiet route to send message on Kafka
 */
export const configureAdditionalRoutesQuietSoh = (): void => {
  const ackHandler = (req, res) => {
    const quietReqs: SohTypes.QuietedSohStatusChange[] = req.body;
    // Send each quiet requests
    quietReqs.forEach(async quietReq => publishQuietedChange(quietReq));
    const response = {
      status: 'complete'
    };
    res.send(JSON.stringify(response));
  };

  const clientLogRoute = '/interactive-analysis-api-gateway/quiet-soh-status';
  logger.info(`register ${clientLogRoute}`);
  app.post(clientLogRoute, ackHandler);
};

/**
 * sets up and configures Historical ACEI route for when running locally
 */
export const configureAdditionalRoutesMockAcei = (): void => {
  const ackHandler = (req, res) => {
    const aceiData = getHistoricalAceiData(req.body);
    res.send(xssFilters.inHTMLData(JSON.stringify(aceiData ?? [])));
  };

  const aceiRoute =
    '/frameworks-osd-service/osd/coi/acquired-channel-environment-issues/query/station-id-time-and-type';
  logger.info(`register ${aceiRoute}`);
  app.post(aceiRoute, ackHandler);
};

/**
 * sets up and configures Historical SOH route for when running locally
 */
export const configureAdditionalRoutesMockHistoricalSoh = (): void => {
  const ackHandler = (req, res) => {
    const sohData: SohTypes.UiHistoricalSoh = getHistoricalSohData(req.body);

    // Encode data if msgpack header is set and response header content-type
    if (getHeaderValue(req, 'accept') === 'application/msgpack') {
      res.header('content-type', 'application/msgpack');
      res.send(msgpack.encode(sohData));
    } else {
      res.send(xssFilters.inHTMLData(JSON.stringify(sohData)));
    }
  };

  const aceiRoute = '/ssam-control/retrieve-decimated-historical-station-soh';
  logger.info(`register ${aceiRoute}`);
  app.post(aceiRoute, ackHandler);
};

export const startApiGateway = (): void => {
  logger.info('Starting API Gateway Server...');
  createHttpServer(app);
  initializeWebsocketServer();
  configureAdditionalRoutesQuietSoh();
  configureAdditionalRoutesAckSoh();
  configureAdditionalRoutesMockHistoricalSoh();
  configureAdditionalRoutesClientLogs();
  configureAdditionalRoutesMockAcei();
  configureRouteAlive(app);
  configureRouteReady(app, getProtocol());
  configureRouteHealthCheck(app, getProtocol());
  configureRouteCheckInitialized(app);
  initializeKafka().catch(e => logger.error(e));
};

startApiGateway();
