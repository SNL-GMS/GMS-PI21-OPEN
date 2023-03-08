import type { RouteMatchCallbackOptions } from 'workbox-core';

import { handleTypeTransformer, testTypeTransformer } from '../../src/ts/routes/type-transformer';
import { logger } from '../../src/ts/sw-logger';

// so we can mock fetch later on
const globalAny = global as any;
globalAny.fetch = jest
  .fn()
  .mockReturnValue(Promise.resolve(new Response(JSON.stringify({ duration: 'PT1M' }))));

describe('Type Transformer', () => {
  describe('testTypeTransformer', () => {
    it('exists', () => {
      expect(testTypeTransformer).toBeDefined();
    });
    it('returns true if request has header accept: application/json', () => {
      const headers = new Headers();
      headers.set('accept', 'application/json');
      expect(
        testTypeTransformer({
          request: {
            url: 'https://example.com',
            headers
          } as Request
        } as RouteMatchCallbackOptions)
      ).toBe(true);
    });
  });
  describe('handleTypeTransformer', () => {
    it('exists', () => {
      expect(handleTypeTransformer).toBeDefined();
    });
    it('transforms responses that contain durations', async () => {
      const mockRequest = new Request('https://example.com/deserialize', {
        method: 'POST',
        body: JSON.stringify({ foo: true })
      });
      const transformed: Response = await (handleTypeTransformer as any)({ request: mockRequest });
      expect(await transformed.json()).toMatchInlineSnapshot(`
      Object {
        "duration": 60,
      }
      `);
    });
    it('rejects if the response is not ok', async () => {
      const mockRequest = new Request('https://example.com/deserialize', {
        method: 'POST',
        body: JSON.stringify({ foo: true })
      });
      globalAny.fetch = jest
        .fn()
        .mockReturnValue(
          Promise.resolve(new Response(JSON.stringify({ duration: 'PT1M' }), { status: 500 }))
        );
      await expect(() => (handleTypeTransformer as any)({ request: mockRequest })).rejects.toMatch(
        'Internal Server Error'
      );
    });
    it('rejects and logs an error if the handler throws', async () => {
      const mockRequest = new Request('https://example.com/deserialize', {
        method: 'POST',
        body: JSON.stringify({ foo: true })
      });
      globalAny.fetch = jest.fn().mockImplementation(() => {
        throw new Error('Test that it throws');
      });
      const loggerSpy = jest.spyOn(logger, 'error');
      await expect(() =>
        (handleTypeTransformer as any)({ request: mockRequest })
      ).rejects.toMatchInlineSnapshot(`[Error: Test that it throws]`);
      expect(loggerSpy.mock.calls[0][0].toString()).toMatch('Error: Test that it throws');
    });
  });
});
