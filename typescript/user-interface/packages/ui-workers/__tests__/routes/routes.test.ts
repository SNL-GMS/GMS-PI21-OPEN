import { Route } from 'workbox-routing';

import { handleCssAsset, testCssAsset } from '../../src/ts/routes/css-assets';
import { handleImageAsset, testImageAsset } from '../../src/ts/routes/image-assets';
import { handleJsAsset, testJsAsset } from '../../src/ts/routes/js-assets';
import {
  cssAssetRoute,
  imageAssetRoute,
  jsAssetRoute,
  typeTransformerRoute
} from '../../src/ts/routes/routes';
import { handleTypeTransformer, testTypeTransformer } from '../../src/ts/routes/type-transformer';

describe('Routes', () => {
  beforeEach(() => {
    jest.resetModules();
  });
  test('routes exist', () => {
    expect(imageAssetRoute).toBeDefined();
    expect(jsAssetRoute).toBeDefined();
    expect(cssAssetRoute).toBeDefined();
    expect(typeTransformerRoute).toBeDefined();
  });
  it('creates a route for image assets', () => {
    expect((Route as any).mock.calls[0][0]).toBe(testImageAsset);
    expect((Route as any).mock.calls[0][1]).toBe(handleImageAsset);
  });
  it('creates a route for js assets', () => {
    expect((Route as any).mock.calls[1][0]).toBe(testJsAsset);
    expect((Route as any).mock.calls[1][1]).toBe(handleJsAsset);
  });
  it('creates a route for css assets', () => {
    expect((Route as any).mock.calls[2][0]).toBe(testCssAsset);
    expect((Route as any).mock.calls[2][1]).toBe(handleCssAsset);
  });
  it('creates a route for deserialize type transformer endpoints', () => {
    expect((Route as any).mock.calls[3][0]).toBe(testTypeTransformer);
    expect((Route as any).mock.calls[3][1]).toBe(handleTypeTransformer);
  });
});
