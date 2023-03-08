import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * System message definition request config definition
 */
export interface SystemMessageDefinitionRequestConfig extends RequestConfig {
  readonly systemMessageDefinition: {
    readonly baseUrl: string;
    readonly services: {
      readonly getSystemMessageDefinitions: ServiceDefinition;
    };
  };
}

/**
 * The system message definition request config for all services.
 */
export const config: SystemMessageDefinitionRequestConfig = {
  systemMessageDefinition: {
    baseUrl: `${UI_URL}${Endpoints.SystemMessageUrls.baseUrl}`,
    services: {
      getSystemMessageDefinitions: {
        requestConfig: {
          method: 'post',
          url: Endpoints.SystemMessageUrls.getSystemMessageDefinitions,
          headers: {
            accept: 'text/plain',
            'content-type': 'text/plain'
          },
          proxy: false,
          timeout: 60000,
          data: `"PlaceHolder"`
        }
      }
    }
  }
};
