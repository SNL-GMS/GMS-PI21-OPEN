import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * System event gateway request config definition
 */
export interface SystemEventGatewayRequestConfig extends RequestConfig {
  readonly gateway: {
    readonly baseUrl: string;
    readonly services: {
      readonly sendClientLogs: ServiceDefinition;
      readonly acknowledgeSohStatus: ServiceDefinition;
      readonly quietSohStatus: ServiceDefinition;
    };
  };
}

/**
 * The system event gateway request config for all services.
 */
export const config: SystemEventGatewayRequestConfig = {
  gateway: {
    baseUrl: `${UI_URL}${Endpoints.SystemEventGatewayUrls.baseUrl}`,
    services: {
      sendClientLogs: {
        requestConfig: {
          method: 'post',
          url: Endpoints.SystemEventGatewayUrls.sendClientLogs,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          timeout: 60000
        }
      },
      acknowledgeSohStatus: {
        requestConfig: {
          method: 'post',
          url: Endpoints.SystemEventGatewayUrls.acknowledgeSohStatus,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          timeout: 60000
        }
      },
      quietSohStatus: {
        requestConfig: {
          method: 'post',
          url: Endpoints.SystemEventGatewayUrls.quietSohStatus,
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
