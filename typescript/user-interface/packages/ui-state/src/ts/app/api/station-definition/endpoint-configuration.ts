import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * Station definition request config definition
 */
export interface StationDefinitionRequestConfig extends RequestConfig {
  readonly stationDefinition: {
    readonly baseUrl: string;
    readonly services: {
      readonly getStationGroupsByNames: ServiceDefinition;
      readonly getStations: ServiceDefinition;
      readonly getStationsEffectiveAtTimes: ServiceDefinition;
      readonly getChannelsByNames: ServiceDefinition;
    };
  };
}

/**
 * The station definition request config for all services.
 */
export const config: StationDefinitionRequestConfig = {
  stationDefinition: {
    baseUrl: `${UI_URL}${Endpoints.StationDefinitionUrls.baseUrl}`,
    services: {
      getStationGroupsByNames: {
        requestConfig: {
          method: 'post',
          url: Endpoints.StationDefinitionUrls.getStationGroupsByNames,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          proxy: false,
          timeout: 60000
        }
      },
      getStations: {
        requestConfig: {
          method: 'post',
          url: Endpoints.StationDefinitionUrls.getStations,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          proxy: false,
          timeout: 60000
        }
      },
      getStationsEffectiveAtTimes: {
        requestConfig: {
          method: 'post',
          url: Endpoints.StationDefinitionUrls.getStationsEffectiveAtTimes,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          proxy: false,
          timeout: 60000
        }
      },
      getChannelsByNames: {
        requestConfig: {
          method: 'post',
          url: Endpoints.StationDefinitionUrls.getChannelsByNames,
          headers: {
            accept: 'application/json',
            'content-type': 'application/json'
          },
          proxy: false,
          // TODO: reduce timeout once query is parallelized
          timeout: 180000
        }
      }
    }
  }
};
