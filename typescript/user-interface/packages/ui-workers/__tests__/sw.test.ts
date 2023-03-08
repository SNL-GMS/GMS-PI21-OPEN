/* eslint-disable no-restricted-globals */
/* eslint-env serviceworker */
/// <reference no-default-lib="true"/>
/// <reference lib="webworker" />

import {
  clientConnectedMessage,
  listenersActiveMessage,
  skipWaitingMessage
} from '../src/ts/messages';
import { CACHE_CSS_ASSETS } from '../src/ts/routes/css-assets';
import { CACHE_IMAGE_ASSETS } from '../src/ts/routes/image-assets';
import { CACHE_JS_ASSETS } from '../src/ts/routes/js-assets';
import {
  handleActivate,
  handleFetch,
  handleMessage,
  messageClientsThatSwIsReady,
  registerSwEvents
} from '../src/ts/sw';

// so we can mock fetch later on
const globalAny = global as any;

// so we can use the correct type for self
// eslint-disable-next-line no-var
declare var self: ServiceWorkerGlobalScope;

jest.mock('../src/ts/sw-logger', () => {
  const actual = jest.requireActual('../src/ts/sw-logger');
  Object.defineProperty(self, 'clients', {
    writable: true,
    value: {
      claim: jest.fn(),
      matchAll: jest.fn(() => {
        throw new Error('called mock matchAll. Overwrite this with spyOn to inspect the mocker.');
      }),
      skipWaiting: jest.fn(() => {
        throw new Error(
          'called mock skipWaiting. Overwrite this with spyOn to inspect the mocker.'
        );
      })
    }
  });
  Object.defineProperty(self, 'skipWaiting', {
    writable: true,
    value: jest.fn()
  });
  Object.defineProperty(self, 'registration', {
    writable: true,
    value: {
      waiting: { postMessage: jest.fn() }
    }
  });
  return actual;
});

describe('Service Worker entrypoint', () => {
  beforeEach(() => {
    registerSwEvents();
    jest.resetModules();
  });
  describe('handleActivate', () => {
    it('clears caches', () => {
      handleActivate({
        waitUntil: async (promise: Promise<unknown>) => {
          try {
            await promise;
          } catch (e) {
            throw new Error('Test failed, promise rejected in handleActivate');
          }
        }
      } as any);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(caches.delete).toHaveBeenCalledWith(CACHE_IMAGE_ASSETS);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(caches.delete).toHaveBeenCalledWith(CACHE_CSS_ASSETS);
      // eslint-disable-next-line @typescript-eslint/unbound-method
      expect(caches.delete).toHaveBeenCalledWith(CACHE_JS_ASSETS);
    });
  });
  describe('handleMessage', () => {
    it(`sends a ${listenersActiveMessage} as a response when it receives a ${clientConnectedMessage} message`, async () => {
      const mockPostMessage = jest.fn();
      await handleMessage({
        data: clientConnectedMessage,
        source: {
          postMessage: mockPostMessage
        }
      } as any);
      expect(mockPostMessage).toHaveBeenCalledWith(listenersActiveMessage);
    });
    it('does nothing if the message is not a string', async () => {
      const mockPostMessage = jest.fn();
      await handleMessage({
        data: {},
        source: {
          postMessage: mockPostMessage
        }
      } as any);
      expect(mockPostMessage).not.toHaveBeenCalled();
    });
    it(`calls skipWaiting when sent a ${skipWaitingMessage} message`, async () => {
      const skipWaitingSpy = jest.spyOn(self, 'skipWaiting');
      await handleMessage({
        data: skipWaitingMessage
      } as any);

      expect(skipWaitingSpy).toHaveBeenCalled();
    });
  });
  describe('handleFetch', () => {
    it(`if refreshed while there is a worker waiting, sends the waiting worker a ${skipWaitingMessage} message`, () => {
      const mockRequest = {
        mode: 'navigate',
        method: 'GET'
      };
      // mock the response so it only has one "client"
      jest.spyOn(self.clients, 'matchAll').mockImplementation(() => [{}] as any);
      const postMessageSpy = jest.spyOn(self.registration.waiting, 'postMessage');
      handleFetch(({
        respondWith: async (response: Response | PromiseLike<Response>) => {
          await response;
          expect(postMessageSpy).toHaveBeenCalledWith(skipWaitingMessage);
        },
        request: mockRequest
      } as unknown) as FetchEvent);
    });
    it('responds with the request from the cache if there is a match', () => {
      const mockRequest = {
        mode: 'navigate',
        method: 'POST'
      };
      const mockResponse = new Response(JSON.stringify({ data: 'cache hit' }));
      const cacheMatchSpy = jest.spyOn(caches, 'match').mockImplementation(async () => {
        return Promise.resolve(mockResponse);
      });
      handleFetch(({
        respondWith: async (response: Response | PromiseLike<Response>) => {
          const result = await response;
          expect(result).toBe(mockResponse);
        },
        request: mockRequest
      } as unknown) as FetchEvent);
      expect(cacheMatchSpy.mock.calls[0][0]).toBe(mockRequest);
    });
    it('responds with the initial request if there is a cache miss', () => {
      const mockRequest = {
        mode: 'navigate',
        method: 'POST',
        url: 'http://example.com'
      };
      const mockResponse = 'cache miss';
      globalAny.fetch = jest.fn(() => mockResponse);
      jest.spyOn(caches, 'match').mockImplementation(async () => {
        return Promise.resolve(undefined);
      });
      handleFetch(({
        respondWith: async (response: Response | PromiseLike<Response>) => {
          const result = await response;
          expect(result).toBe(mockResponse);
          expect(globalAny.fetch).toHaveBeenCalledWith(mockRequest);
        },
        request: mockRequest
      } as unknown) as FetchEvent);
    });
  });
  describe('messageClientsThatSwIsReady', () => {
    beforeEach(() => {
      jest.resetModules();
    });
    it(`posts ${listenersActiveMessage} to each client`, async () => {
      const client1 = { postMessage: jest.fn };
      const client2 = { postMessage: jest.fn };
      const mockMatchAll = jest.fn(async () => {
        return Promise.resolve([client1, client2]);
      });
      jest.spyOn(self.clients, 'matchAll').mockImplementation(mockMatchAll as any);
      await messageClientsThatSwIsReady();
      expect(mockMatchAll).toHaveBeenCalled();
    });
  });
});
