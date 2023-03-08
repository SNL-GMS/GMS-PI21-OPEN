import type { WebSocket } from 'ws';

import { shutDownHttpServer } from '../src/ts/server/http-server';
import {
  createWebSocketServer,
  handleSystemEventTypeMessage,
  isSystemEventType,
  parseAndValidateSystemEventTypeMessage,
  sendSubscriptionMessage,
  shutDownWebsocketServer
} from '../src/ts/server/websocket-server';

const mockSendMessage = jest.fn(msg => {
  console.log(`sending ${msg}!`);
});
const websocket: Partial<WebSocket> = {
  send: mockSendMessage
};

describe('websocket server tests', () => {
  beforeAll(() => {
    shutDownWebsocketServer();
    shutDownHttpServer();
  });

  const systemEvent = {
    eventType: 'event'
  };

  const systemMessage = {
    data: 'Yoo',
    id: '1',
    source: 'rig',
    specversion: '0.2',
    type: 'soh-message'
  };

  it('test is system event type', () => {
    expect(isSystemEventType(systemEvent)).toBeTruthy();
  });

  it('can parse system event type messages', () => {
    expect(parseAndValidateSystemEventTypeMessage(undefined)).toHaveLength(0);
    expect(parseAndValidateSystemEventTypeMessage(JSON.stringify([systemEvent]))).toHaveLength(1);
  });

  it('subscribe to event type and receive subscription message', () => {
    const eventType = [{ eventType: 'event' }];
    systemMessage.type = 'event';
    handleSystemEventTypeMessage(JSON.stringify(eventType), websocket as WebSocket);
    expect(mockSendMessage).toBeCalledTimes(0);
    expect(sendSubscriptionMessage(systemMessage)).toBeUndefined();
    expect(mockSendMessage).toBeCalledTimes(1);
  });

  it('subscribe to soh-message type and receive subscription message', () => {
    mockSendMessage.mockClear();
    const eventType = [{ eventType: 'soh-message' }];
    systemMessage.type = 'soh-message';
    expect(() => sendSubscriptionMessage(systemMessage)).not.toThrow();

    handleSystemEventTypeMessage(JSON.stringify(eventType), websocket as WebSocket);
    expect(mockSendMessage).toBeCalledTimes(1);
    expect(sendSubscriptionMessage(systemMessage)).toBeUndefined();
    expect(mockSendMessage).toBeCalledTimes(2);
  });

  it('create websocket server', () => {
    expect(createWebSocketServer()).toBeUndefined();
    shutDownWebsocketServer();
    shutDownHttpServer();
  });
});
