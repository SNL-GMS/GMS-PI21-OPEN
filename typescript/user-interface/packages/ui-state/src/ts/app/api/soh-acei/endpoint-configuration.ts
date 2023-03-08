import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * SSAM control request config definition
 */
export interface GetHistoricalAceiData extends RequestConfig {
  readonly sohAcei: {
    readonly baseUrl: string;
    readonly services: {
      readonly getHistoricalAceiData: ServiceDefinition;
    };
  };
}

/**
 * The SSAM control request config for all services.
 */
export const config: GetHistoricalAceiData = {
  sohAcei: {
    baseUrl: `${UI_URL}/frameworks-osd-service`,
    services: {
      getHistoricalAceiData: {
        requestConfig: {
          method: 'post',
          url: `/osd/coi/acquired-channel-environment-issues/query/station-id-time-and-type`,
          responseType: 'json',
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          timeout: 120000
        }
      }
    }
  }
};
