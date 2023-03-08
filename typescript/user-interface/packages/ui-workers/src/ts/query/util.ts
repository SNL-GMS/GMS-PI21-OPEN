import { serializeTypeTransformer } from './axios';
import type { RequestConfig } from './types';

/**
 * A type guard to test if an object is a {@link RequestConfig}
 *
 * @param obj an object to test to see if it is of type RequestConfig
 */
export const isRequestConfig = (obj: unknown): obj is RequestConfig => {
  if (typeof obj !== 'object') {
    return false;
  }
  return Object.keys(obj).reduce<boolean>((isOfType, k) => {
    return (
      isOfType &&
      !!Object.keys(obj[k]).find(j => j === 'baseUrl') &&
      !!Object.keys(obj[k]).find(j => j === 'services') &&
      Object.keys(obj[k]).length === 2
    );
  }, true);
};

/**
 * Takes a request and calls the custom Axios type transformers for serializing types.
 *
 * @param request a Request to transform
 */
export const transformRequest = async (request: Request): Promise<Request> => {
  let req = request;
  if (request.method === 'POST') {
    const requestBody = await request.json();
    const newBody = serializeTypeTransformer(requestBody);
    req = new Request(request.url, {
      headers: request.headers,
      body: JSON.stringify(newBody),
      method: 'POST'
    });
  }
  return req;
};
