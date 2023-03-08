import type { RequestConfig } from '../../src/ts/query';
import { isRequestConfig } from '../../src/ts/query';

describe('Query Util', () => {
  describe('isRequestConfig', () => {
    it('returns false for a string', () => {
      expect(isRequestConfig('Foo')).toBe(false);
    });
    it('returns true for a RequestConfig object', () => {
      const rq: RequestConfig = {
        test: {
          baseUrl: 'https://example.com',
          services: {
            foo: {
              requestConfig: {
                url: '/foo-endpoint'
              }
            }
          }
        }
      };
      expect(isRequestConfig(rq)).toBe(true);
    });
    it('returns false for a non RequestConfig object', () => {
      const other = {
        test: {
          notExpectedThing: 'this should return false because of this',
          baseUrl: 'https://example.com',
          services: {
            foo: {
              requestConfig: {
                url: '/foo-endpoint'
              }
            }
          }
        }
      };
      expect(isRequestConfig(other)).toBe(false);
    });
  });
});
