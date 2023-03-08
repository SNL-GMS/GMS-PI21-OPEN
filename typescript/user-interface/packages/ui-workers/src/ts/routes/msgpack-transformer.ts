import { decode } from 'msgpack-lite';
import type {
  RouteHandler,
  RouteHandlerCallbackOptions,
  RouteMatchCallback
} from 'workbox-core/types';

import type { IgnoreList } from '../ignore-list';
import { shouldIgnore } from '../ignore-list';
import { deserializeTypeTransformer, transformRequest } from '../query';
import { logger } from '../sw-logger';

const defaultTransformIgnoreList: IgnoreList = [];

export const unmsgpack = (val: Uint8Array) => {
  if (val?.length > 0) {
    return decode(val);
  }
  return undefined;
};

export const testMsgPackTransformer: RouteMatchCallback = ({ request }): boolean => {
  return (
    !shouldIgnore(request, defaultTransformIgnoreList) &&
    request.headers.get('accept') === 'application/msgpack'
  );
};

export const handleMsgPackTransformer: RouteHandler = async ({
  request
}: RouteHandlerCallbackOptions) => {
  try {
    const req = await transformRequest(request);
    const response = await fetch(req);

    if (response.ok) {
      const decoded = unmsgpack(new Uint8Array(await response.clone().arrayBuffer()));
      // Also run deserialize type transformer, since the route handler for that will not match message pack encoded results
      const typeTransformed = deserializeTypeTransformer(decoded);
      const newBody = JSON.stringify(typeTransformed);
      return new Response(newBody);
    }
    logger.error(response.statusText);
    logger.error(
      `Request to ${JSON.stringify(response.url)} failed. Response:`,
      JSON.stringify(response)
    );
    return Promise.reject(response.statusText);
  } catch (e) {
    logger.error(e);
    logger.error('The above error failed on request', request);
    return Promise.reject(e);
  }
};
