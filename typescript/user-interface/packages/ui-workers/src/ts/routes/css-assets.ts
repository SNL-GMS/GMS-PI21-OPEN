import type { RouteHandler, RouteMatchCallback } from 'workbox-core';
import { CacheFirst } from 'workbox-strategies';

import type { IgnoreList } from '../ignore-list';
import { shouldIgnore } from '../ignore-list';

const defaultTransformIgnoreList: IgnoreList = [];

export const CACHE_CSS_ASSETS = 'css-assets';

export const testCssAsset: RouteMatchCallback = ({ request }) => {
  return request.destination === 'style' && !shouldIgnore(request, defaultTransformIgnoreList);
};
export const handleCssAsset: RouteHandler = new CacheFirst({
  cacheName: CACHE_CSS_ASSETS
});
