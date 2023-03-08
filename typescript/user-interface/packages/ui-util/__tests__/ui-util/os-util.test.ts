import { getOS, OSTypes } from '../../src/ts/ui-util/os-util';

describe('os-util', () => {
  describe('os-util', () => {
    beforeEach(() => {
      Object.defineProperty(window.navigator, 'platform', {
        value: '',
        configurable: true
      });
      Object.defineProperty(window.navigator, 'userAgent', {
        value: '',
        configurable: true
      });
    });
    it('recognizes a mac', () => {
      Object.defineProperty(window.navigator, 'platform', {
        value: 'Macintosh',
        configurable: true
      });
      expect(getOS()).toBe(OSTypes.MAC);
    });
    it('recognizes Windows', () => {
      Object.defineProperty(window.navigator, 'platform', {
        value: 'Windows',
        configurable: true
      });
      expect(getOS()).toBe(OSTypes.WINDOWS);
    });
    it('recognizes Linux', () => {
      Object.defineProperty(window.navigator, 'platform', {
        value: 'Linux',
        configurable: true
      });
      expect(getOS()).toBe(OSTypes.LINUX);
    });
    it('recognizes Android', () => {
      Object.defineProperty(window.navigator, 'userAgent', {
        value: 'Android',
        configurable: true
      });
      expect(getOS()).toBe(OSTypes.ANDROID);
    });
    it('recognizes iOS', () => {
      Object.defineProperty(window.navigator, 'platform', {
        value: 'iPad',
        configurable: true
      });
      expect(getOS()).toBe(OSTypes.IOS);
    });
    it('returns null if unknown', () => {
      Object.defineProperty(window.navigator, 'platform', {
        value: 'A strange platform',
        configurable: true
      });
      Object.defineProperty(window.navigator, 'userAgent', {
        value: 'Some strange user agent',
        configurable: true
      });
      expect(getOS()).toBe(null);
    });
  });
});
