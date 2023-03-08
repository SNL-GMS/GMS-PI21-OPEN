import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * Processing station request config definition
 */
export interface ProcessingStationRequestConfig extends RequestConfig {
  readonly gateway: {
    readonly baseUrl: string;
    readonly services: {
      readonly getProcessingStationGroups: ServiceDefinition;
    };
  };
}

/**
 * The processing station request config for all services.
 */
export const config: ProcessingStationRequestConfig = {
  gateway: {
    baseUrl: `${UI_URL}${Endpoints.FrameworksOsdSUrls.baseUrl}`,
    services: {
      getProcessingStationGroups: {
        requestConfig: {
          method: 'post',
          url: Endpoints.FrameworksOsdSUrls.getProcessingStationGroups,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          timeout: 60000
        }
      }
    }
  }
};
