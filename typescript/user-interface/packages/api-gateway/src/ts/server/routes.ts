import { toOSDTime } from '@gms/common-util';
import type { Express } from 'express';

import { KafkaConsumer } from '../kafka/kafka-consumer';
import { KafkaProducer } from '../kafka/kafka-producer';
import { gatewayLogger as logger } from '../log/gateway-logger';
import { healthChecks, HealthStatus } from './health-checks';

export const appName = process.env.APP_NAME || 'interactive-analysis-api-gateway';

/**
 * Defines the available routes.
 */
export interface Routes {
  /** route to check alive status */
  readonly alive: string;
  /** route to check the ready status */
  readonly ready: string;
  /** route to check all health status */
  readonly healthCheck: string;
  /** health check routes */
  readonly healthChecks: {
    /** initialization health check */
    readonly initialized: string;
    /** kafka health check */
    readonly kafka: string;
    /** websocket server health check */
    readonly websocket: string;
  };
}

/**
 * The routes
 */
const routes: Routes = {
  alive: `/${appName}/alive`,
  ready: `/${appName}/ready`,
  healthCheck: `/${appName}/health-check`,
  healthChecks: {
    initialized: `/${appName}/health-check/initialized`,
    kafka: `/${appName}/health-check/kafka`,
    websocket: `/${appName}/health-check/websocket`
  }
};

/**
 * Configures the `alive` route.
 * This route returns a timestamp indicating the the gateway server is alive.
 *
 * @param app the express server app
 */
export const configureRouteAlive = (app: Express): void => {
  const handler = (req, res) => res.send(Date.now().toString());
  logger.info(`register ${routes.alive}`);
  app.get(routes.alive, handler);
};

/**
 * Configures the `ready` route.
 * Performs simple health checks and verifies that the gateway is up and ready.
 *
 * @param app the express server app
 * @param protocol the http protocol
 */
export const configureRouteReady = (app: Express, protocol: string): void => {
  const handler = async (req, res) => {
    const checks = await healthChecks([
      { id: routes.alive, path: `${protocol}${req.headers.host}${routes.alive}` },
      {
        id: routes.healthChecks.initialized,
        path: `${protocol}${req.headers.host}${routes.healthChecks.initialized}`
      },
      {
        id: routes.healthChecks.websocket,
        path: `${protocol}${req.headers.host}${routes.healthChecks.websocket}`
      }
    ]);
    const status = checks.map<HealthStatus>(c => c.status).every(s => s === HealthStatus.OK)
      ? HealthStatus.OK
      : HealthStatus.FAILED;
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    return res.status(status === HealthStatus.OK ? 200 : 500).send(status);
  };

  logger.info(`register ${routes.ready}`);
  app.get(routes.ready, handler);
};

/**
 * Configures the `health-check` route.
 * Performs all of the health checks and returns each status.
 *
 * @param app the express server app
 * @param protocol the http protocol
 */
export const configureRouteHealthCheck = (app: Express, protocol: string): void => {
  const handler = async (req, res) => {
    const checks = await healthChecks([
      { id: routes.alive, path: `${protocol}${req.headers.host}${routes.alive}` },
      {
        id: routes.healthChecks.initialized,
        path: `${protocol}${req.headers.host}${routes.healthChecks.initialized}`
      },
      {
        id: routes.healthChecks.kafka,
        path: `${protocol}${req.headers.host}${routes.healthChecks.kafka}`
      },
      {
        id: routes.healthChecks.websocket,
        path: `${protocol}${req.headers.host}${routes.healthChecks.websocket}`
      }
    ]);
    return res.send(JSON.stringify(checks));
  };

  logger.info(`register ${routes.healthCheck}`);
  app.get(routes.healthCheck, handler);
};

/**
 * Configures the `health-check/initialized` route.
 * Performs a simple health check to see if the gateway is initialized.
 *
 * @param app the express server app
 */
export const configureRouteCheckInitialized = (app: Express): void => {
  const handler = (req, res) => res.send(HealthStatus.OK);
  logger.info(`register ${routes.healthChecks.initialized}`);
  app.get(routes.healthChecks.initialized, handler);
};

/**
 * Configures the `health-check/kafka` route.
 * Performs a simple health check to see if the KAFKA connections are ok.
 *
 * @param app the express server app
 */
export const configureRouteCheckKafka = (app: Express): void => {
  const handler = (req, res) =>
    res
      .status(
        KafkaConsumer.Instance().connected() && KafkaProducer.Instance().connected()
          ? // eslint-disable-next-line @typescript-eslint/no-magic-numbers
            200
          : // eslint-disable-next-line @typescript-eslint/no-magic-numbers
            500
      )
      .send({
        'KAFKA Consumer': {
          Status: KafkaConsumer.Instance().getStatus(),
          'Up Time': KafkaConsumer.Instance().getUpTime()
            ? `${String(
                toOSDTime(KafkaConsumer.Instance().getUpTime().getTime() / 1000)
              )} (${KafkaConsumer.Instance().getUpTimeSeconds()}s)`
            : 'N/A',
          'Status History': KafkaConsumer.Instance().getStatusHistoryInformationAsObject()
        },
        'KAFKA Producer': {
          Status: KafkaProducer.Instance().getStatus(),
          'Up Time': KafkaProducer.Instance().getUpTime()
            ? `${String(
                toOSDTime(KafkaProducer.Instance().getUpTime().getTime() / 1000)
              )} (${KafkaProducer.Instance().getUpTimeSeconds()}s)`
            : `N/A`,
          'Status History': KafkaProducer.Instance().getStatusHistoryInformationAsObject()
        }
      });
  logger.info(`register ${routes.healthChecks.kafka}`);
  app.get(routes.healthChecks.kafka, handler);
};

/**
 * Configures the `health-checks/websocket` route.
 * Performs simple health checks and verifies that the websocket server is ready
 *
 * @param app the express server app
 */
export const configureRouteCheckWebsocket = (app: Express): void => {
  const handler = (req, res) => res.send(HealthStatus.OK);
  logger.info(`register ${routes.healthChecks.websocket}`);
  app.get(routes.healthChecks.websocket, handler);
};
