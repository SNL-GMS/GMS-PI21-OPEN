import { testJsAsset } from '../../src/ts/routes/js-assets';

describe('JS Assets worker route', () => {
  describe('testJsAsset', () => {
    it('returns true if given a script destination', () => {
      expect(
        testJsAsset({
          request: {
            destination: 'script'
          }
        } as any)
      ).toBe(true);
    });
    it('returns false if given a non script destination', () => {
      expect(
        testJsAsset({
          request: {
            destination: 'image'
          }
        } as any)
      ).toBe(false);
    });
  });
});
