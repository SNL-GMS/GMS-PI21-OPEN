import { testCssAsset } from '../../src/ts/routes/css-assets';

describe('CSS Assets worker route', () => {
  describe('testCssAsset', () => {
    it('returns true if given a style destination', () => {
      expect(
        testCssAsset({
          request: {
            destination: 'style'
          }
        } as any)
      ).toBe(true);
    });
    it('returns false if given a non style destination', () => {
      expect(
        testCssAsset({
          request: {
            destination: 'script'
          }
        } as any)
      ).toBe(false);
    });
  });
});
