import type { RouteHandler, RouteMatchCallback } from 'workbox-core';
import { CacheFirst } from 'workbox-strategies';

import type { IgnoreList } from '../ignore-list';
import { shouldIgnore } from '../ignore-list';

const defaultTransformIgnoreList: IgnoreList = [];

export const CACHE_JS_ASSETS = 'js-assets';

export const testJsAsset: RouteMatchCallback = ({ request }) => {
  return request.destination === 'script' && !shouldIgnore(request, defaultTransformIgnoreList);
};
export const handleJsAsset: RouteHandler = new CacheFirst({
  cacheName: CACHE_JS_ASSETS
});
