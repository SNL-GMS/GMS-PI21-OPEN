import type {
  RouteHandler,
  RouteHandlerCallbackOptions,
  RouteMatchCallback
} from 'workbox-core/types';

import type { IgnoreList } from '../ignore-list';
import { shouldIgnore } from '../ignore-list';
import { transformRequest } from '../query';
import { deserializeTypeTransformer } from '../query/axios/axios-transformers';
import { logger } from '../sw-logger';

const defaultTransformIgnoreList: IgnoreList = [];

export const testTypeTransformer: RouteMatchCallback = ({ request }): boolean => {
  return (
    !shouldIgnore(request, defaultTransformIgnoreList) &&
    request.headers.get('accept') !== 'application/msgpack' &&
    request.headers.get('accept') === 'application/json'
  );
};

export const handleTypeTransformer: RouteHandler = async ({
  request
}: RouteHandlerCallbackOptions) => {
  try {
    const req = await transformRequest(request);
    const response = await fetch(req);

    if (response.ok) {
      const data = await response.json();
      const transformed = deserializeTypeTransformer(data);
      return new Response(JSON.stringify(transformed));
    }
    logger.error(response.statusText);
    logger.error(JSON.stringify(response));
    return Promise.reject(response.statusText);
  } catch (e) {
    logger.error(e);
    return Promise.reject(e);
  }
};
