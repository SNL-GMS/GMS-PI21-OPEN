import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * Processing configuration request config definition
 */
export interface ProcessingConfigurationRequestConfig extends RequestConfig {
  readonly processingConfiguration: {
    readonly baseUrl: string;
    readonly services: {
      readonly getProcessingConfiguration: ServiceDefinition;
    };
  };
}

/**
 * The processing configuration request config for all services.
 */
export const config: ProcessingConfigurationRequestConfig = {
  processingConfiguration: {
    baseUrl: `${UI_URL}${Endpoints.ProcessingConfigUrls.baseUrl}`,
    // Service endpoints for this component
    services: {
      getProcessingConfiguration: {
        requestConfig: {
          method: 'post',
          url: Endpoints.ProcessingConfigUrls.getProcessingConfiguration,
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
