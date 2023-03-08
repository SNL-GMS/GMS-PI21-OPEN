import { testImageAsset } from '../../src/ts/routes/image-assets';

describe('Image Assets worker route', () => {
  describe('testImageAsset', () => {
    it('returns true if given an image destination', () => {
      expect(
        testImageAsset({
          request: {
            destination: 'image'
          }
        } as any)
      ).toBe(true);
    });
    it('returns false if given a non image destination', () => {
      expect(
        testImageAsset({
          request: {
            destination: 'script'
          }
        } as any)
      ).toBe(false);
    });
  });
});
