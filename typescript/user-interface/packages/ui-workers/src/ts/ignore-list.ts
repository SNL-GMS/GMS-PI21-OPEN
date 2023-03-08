import flatMap from 'lodash/flatMap';

import type { RequestConfig, ServiceDefinition } from './query';
import { isRequestConfig } from './query/util';

export type IgnoreList = (RequestConfig | string | ServiceDefinition)[];

const testServiceDefShouldSkip = (request: Request, serviceDef: ServiceDefinition) => {
  return request.url.includes(serviceDef.requestConfig.url);
};

const getBaseUrls = (config: RequestConfig) =>
  Object.keys(config).map(configKey => config[configKey].baseUrl);

const testRequestConfigShouldSkip = (request: Request, config: RequestConfig) => {
  // test base urls for a match
  if (
    getBaseUrls(config).reduce<boolean>(
      (skip, baseUrl) => skip || request.url.includes(baseUrl),
      false
    )
  ) {
    return true;
  }

  // test service definitions for a match
  return flatMap(Object.keys(config), configKey => {
    return Object.keys(config[configKey].services).map(
      serviceKey => config[configKey].services[serviceKey]
    );
  }).reduce((skip, service) => {
    return skip || testServiceDefShouldSkip(request, service);
  }, false);
};

/**
 * Tests a request to see if it matches any entries in the provided {@link IgnoreList}
 *
 * @param request the request to check
 * @param ignoreList the ignore list to check against
 * @returns whether to ignore the provided request
 */
export const shouldIgnore = (request: Request, ignoreList: IgnoreList): boolean => {
  if (ignoreList == null || ignoreList.length === 0) {
    return false;
  }
  return ignoreList.reduce<boolean>((skip, ignoreOption) => {
    if (skip) {
      return skip;
    }
    if (typeof ignoreOption === 'string') {
      return request.url.includes(ignoreOption);
    }
    if (isRequestConfig(ignoreOption)) {
      return testRequestConfigShouldSkip(request, ignoreOption);
    }
    return testServiceDefShouldSkip(request, ignoreOption);
  }, false);
};
