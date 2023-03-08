import type { CommonTypes } from '@gms/common-model';
import config from 'config';
import https from 'http';
import includes from 'lodash/includes';
import isArray from 'lodash/isArray';
import type { WebSocket } from 'ws';
import { WebSocketServer } from 'ws';

import { gatewayLogger as logger } from '../log/gateway-logger';

const clients: Map<WebSocket, string[]> = new Map();
let server: https.Server;

// Cache the last message for when subscriber subscribes soh-messages
let lastSohMessage: CommonTypes.SystemEvent;
/**
 * Checks to see if `object` is a valid SystemEventType and casts.
 *
 * @param object the object to validate; check if it is a valid SystemEventType
 * @returns boolean
 */
export function isSystemEventType(
  object: Record<'eventType', unknown>
): object is CommonTypes.SystemEventType {
  return object.eventType !== undefined;
}

/**
 * Parses and validates a system event type message.
 *
 * @param message the message
 * @returns an array of system event types; empty if invalid
 */
export const parseAndValidateSystemEventTypeMessage = (
  message: string
): CommonTypes.SystemEventType[] => {
  if (message !== undefined) {
    const parsed = JSON.parse(message);
    if (isArray(parsed)) {
      if (parsed.every(isSystemEventType)) {
        return parsed;
      }
    }
  }
  return [];
};

/**
 * If subscribing to 'soh-message' send back the last soh-message
 * on the websocket. Reason is so the subscriber is not waiting
 * up to 20 seconds for the next message.
 *
 * @param systemEvents list of System Event types to check against
 * @param ws Websocket to send the last message on.
 */
const checkToSendLastSohMessage = (
  systemEvents: CommonTypes.SystemEventType[],
  ws: WebSocket
): void => {
  // If system events contains 'soh-message send back lastSohMessage
  if (systemEvents.find(systemEvent => systemEvent.eventType === 'soh-message') && lastSohMessage) {
    ws.send(JSON.stringify(lastSohMessage));
  }
};

/**
 * Handles, parses and validates SystemEventType messages that were
 * received on the provided websocket.
 *
 * If valid, the system event types will be registered for the websocket.
 *
 * @param message the received message
 * @param ws the websocket connect
 */
export const handleSystemEventTypeMessage = (message: string, ws: WebSocket): void => {
  const systemEvents = parseAndValidateSystemEventTypeMessage(message);
  if (systemEvents && systemEvents.length > 0) {
    clients.set(
      ws,
      systemEvents.map(e => e.eventType)
    );
    checkToSendLastSohMessage(systemEvents, ws);
  }
};

const onSocketConnect = (ws: WebSocket) => {
  const systemEvent: CommonTypes.SystemEvent = {
    id: '1',
    specversion: '0.2',
    source: 'api-gateway',
    type: 'api-gateway-connected'
  };
  ws.send(JSON.stringify(systemEvent));

  ws.on('message', (message: string) => {
    handleSystemEventTypeMessage(message, ws);
  });

  ws.on('close', () => {
    clients.delete(ws);
  });
};

/**
 * Creates the WebSocket server
 */
export const createWebSocketServer = (): void => {
  logger.info(`Creating the web socket server...`);

  // Load configuration settings
  const serverConfig = config.get('server');

  // Websocket port
  const wsPort = serverConfig.ws.port;
  logger.info(`wsPort ${wsPort}`);

  const wss = new WebSocketServer({ noServer: true });

  server = https
    .createServer(req => {
      // here we only handle websocket connections
      // in real project we'd have some other code here to handle non-websocket requests
      wss.handleUpgrade(req, req.socket, Buffer.alloc(0), onSocketConnect);
    })
    .listen(wsPort);
};

/**
 * Route System Event wrapped messages to subscribers depending on System Event type
 *
 * @param systemEvent message to send back
 */
export const sendSubscriptionMessage = (systemEvent: CommonTypes.SystemEvent): void => {
  // check to see if we need to update the latest SOH message
  if (systemEvent.type === 'soh-message') {
    lastSohMessage = systemEvent;
  }
  // send the message on each websocket connection is registered for the event type
  clients.forEach((topics, ws) => {
    if (includes(topics, systemEvent.type)) {
      ws.send(JSON.stringify(systemEvent));
    }
  });
};

/**
 * Cleanup available on shutdown
 */
export const shutDownWebsocketServer = (): void => {
  if (server) {
    server.close();
  }
};
