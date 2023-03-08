import type { RouteHandler, RouteMatchCallback } from 'workbox-core';
import { CacheFirst } from 'workbox-strategies';

import type { IgnoreList } from '../ignore-list';
import { shouldIgnore } from '../ignore-list';

const defaultTransformIgnoreList: IgnoreList = [];

export const CACHE_IMAGE_ASSETS = 'image-assets';

export const testImageAsset: RouteMatchCallback = ({ request }) => {
  return request.destination === 'image' && !shouldIgnore(request, defaultTransformIgnoreList);
};
export const handleImageAsset: RouteHandler = new CacheFirst({
  cacheName: CACHE_IMAGE_ASSETS
});
