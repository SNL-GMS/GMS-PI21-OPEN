import { Endpoints } from '@gms/common-model';
import { UI_URL } from '@gms/common-util';
import type { RequestConfig, ServiceDefinition } from '@gms/ui-workers';

/**
 * User manager request config definition
 */
export interface UserManagerRequestConfig extends RequestConfig {
  readonly user: {
    readonly baseUrl: string;
    readonly services: {
      readonly getUserProfile: ServiceDefinition;
      readonly setUserProfile: ServiceDefinition;
    };
  };
}

/**
 * The user manager request config for all services.
 */
export const config: UserManagerRequestConfig = {
  user: {
    baseUrl: `${UI_URL}${Endpoints.UserManagerServiceUrls.baseUrl}`,
    services: {
      getUserProfile: {
        requestConfig: {
          method: 'post',
          url: Endpoints.UserManagerServiceUrls.getUserProfile,
          proxy: false,
          headers: {
            accept: 'application/json',
            'content-type': 'text/plain'
          },
          timeout: 60000,
          data: `"defaultUser"`
        }
      },
      setUserProfile: {
        requestConfig: {
          method: 'post',
          url: Endpoints.UserManagerServiceUrls.setUserProfile,
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
