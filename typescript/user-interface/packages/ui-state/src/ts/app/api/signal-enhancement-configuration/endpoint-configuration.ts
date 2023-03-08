import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * Station definition request config definition
 */
export interface SignalEnhancementConfigurationRequestConfig extends RequestConfig {
  readonly signalEnhancementConfiguration: {
    readonly baseUrl: string;
    readonly services: {
      readonly getSignalEnhancementConfiguration: ServiceDefinition;
    };
  };
}

/**
 * The station definition request config for all services.
 */
export const config: SignalEnhancementConfigurationRequestConfig = {
  signalEnhancementConfiguration: {
    baseUrl: `${UI_URL}${Endpoints.SignalEnhancementConfigurationUrls.baseUrl}`,
    services: {
      getSignalEnhancementConfiguration: {
        requestConfig: {
          method: 'get',
          url: Endpoints.SignalEnhancementConfigurationUrls.getSignalEnhancementConfiguration,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          proxy: false,
          timeout: 60000
        }
      }
    }
  }
};
