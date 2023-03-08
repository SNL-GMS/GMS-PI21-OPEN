import { encode } from 'msgpack-lite';
import type { RouteMatchCallbackOptions } from 'workbox-core';

import {
  handleMsgPackTransformer,
  testMsgPackTransformer,
  unmsgpack
} from '../../src/ts/routes/msgpack-transformer';
import { logger } from '../../src/ts/sw-logger';

// so we can mock fetch later on
const globalAny = global as any;
globalAny.fetch = jest
  .fn()
  .mockReturnValue(Promise.resolve(new Response(encode(JSON.stringify({ duration: 'PT1M' })))));

describe('Msgpack Transformer', () => {
  describe('unmsgpack', () => {
    it('returns the decoded value if given msgpacked data', () => {
      expect(unmsgpack(encode('foo'))).toBe('foo');
    });
    it('returns undefined if given undefined', () => {
      expect(unmsgpack(undefined)).toBe(undefined);
    });
  });
  describe('testMsgPackTransformer', () => {
    it('exists', () => {
      expect(testMsgPackTransformer).toBeDefined();
    });
    it('returns true if request has header accept: application/msgpack', () => {
      const headers = new Headers();
      headers.set('accept', 'application/msgpack');
      expect(
        testMsgPackTransformer({
          request: {
            url: 'https://example.com',
            headers
          } as Request
        } as RouteMatchCallbackOptions)
      ).toBe(true);
    });
  });
  describe('handleMsgPackTransformer', () => {
    it('exists', () => {
      expect(handleMsgPackTransformer).toBeDefined();
    });
    it('transforms responses that contain durations', async () => {
      const mockRequest = new Request('https://example.com/deserialize', {
        method: 'POST',
        body: JSON.stringify({ foo: true })
      });
      const transformed: Response = await (handleMsgPackTransformer as any)({
        request: mockRequest
      });
      expect(await transformed.json()).toMatchInlineSnapshot(`"{\\"duration\\":\\"PT1M\\"}"`);
    });
    it('rejects if the response is not ok', async () => {
      const mockRequest = new Request('https://example.com/deserialize', {
        method: 'POST',
        body: JSON.stringify({ foo: true })
      });
      globalAny.fetch = jest
        .fn()
        .mockReturnValue(
          Promise.resolve(
            new Response(encode(JSON.stringify({ duration: 'PT1M' })), { status: 500 })
          )
        );
      await expect(() =>
        (handleMsgPackTransformer as any)({ request: mockRequest })
      ).rejects.toMatch('Internal Server Error');
    });
    it('rejects and logs an error if the handler throws', async () => {
      const mockRequest = new Request('https://example.com/deserialize');
      globalAny.fetch = jest.fn().mockImplementation(() => {
        throw new Error('Test that it throws');
      });
      const loggerSpy = jest.spyOn(logger, 'error');
      await expect(() =>
        (handleMsgPackTransformer as any)({ request: mockRequest })
      ).rejects.toMatchInlineSnapshot(`[Error: Test that it throws]`);
      expect(loggerSpy.mock.calls[0][0].toString()).toMatch('Error: Test that it throws');
    });
  });
});
