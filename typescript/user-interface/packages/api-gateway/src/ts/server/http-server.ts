import config from 'config';
import type { Express } from 'express';
import https from 'http';

import { gatewayLogger as logger } from '../log/gateway-logger';

let server: https.Server;
/**
 * Creates the HTTP server
 *
 * @param app the express server
 */
export const createHttpServer = (app: Express): https.Server => {
  logger.info(`Creating the http server...`);

  // Load configuration settings
  const serverConfig = config.get('server');

  // HTTP server port
  const httpPort = serverConfig.http.port;
  logger.info(`httpPort ${httpPort}`);

  server = https.createServer(app);
  server.listen(httpPort, () => {
    logger.info(`listening on port ${httpPort}`);
  });
  return server;
};

/**
 * shut down function to close http server connection
 */
export const shutDownHttpServer = (): void => {
  if (server) {
    server.close();
  }
};
